package wbl.egr.uri.sensorcollector.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandResultCallback;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.InvalidBandVersionException;
import com.microsoft.band.notifications.VibrationType;
import com.microsoft.band.sensors.BandSensorManager;
import com.microsoft.band.sensors.GsrSampleRate;
import com.microsoft.band.sensors.SampleRate;

import java.util.concurrent.TimeUnit;

import wbl.egr.uri.sensorcollector.SettingsActivity;
import wbl.egr.uri.sensorcollector.band_listeners.BandAccelerometerListener;
import wbl.egr.uri.sensorcollector.band_listeners.BandAmbientLightListener;
import wbl.egr.uri.sensorcollector.band_listeners.BandContactListener;
import wbl.egr.uri.sensorcollector.band_listeners.BandGsrListener;
import wbl.egr.uri.sensorcollector.band_listeners.BandHeartRateListener;
import wbl.egr.uri.sensorcollector.band_listeners.BandRRIntervalListener;
import wbl.egr.uri.sensorcollector.band_listeners.BandSkinTemperatureListener;
import wbl.egr.uri.sensorcollector.receivers.BandContactStateReceiver;
import wbl.egr.uri.sensorcollector.receivers.BandUpdateReceiver;
import wbl.egr.uri.sensorcollector.receivers.TestBandReceiver;

/**
 * Created by Matt Constant on 2/22/17.
 *
 * The BandCollectionService is the Service that is responsible for connecting to the Microsoft
 * Band, retrieving data about the Band, collecting Sensor data from the Band, and disconnecting
 * from the Band. This is the only component in this Application that has access to the Band.
 * The reason for this is to make interacting with the Band simpler, as well as insuring that
 * the Band is always connected to and disconnected from safely.
 *
 * Other components can receive information about the Band (such as when it is connected, the
 * connected Band's name, the connected Band's address, ect.) through this Service. For example, to
 * get the name of the connected Band, another component can call this Service's
 * requestBandInfo(Context) static method and register a BandUpdateReceiver. Once this Service
 * obtains the information, it will broadcast the information to all registered BandUpdateReceivers.
 *
 * This Service is meant to collect data from the Band indefinitely, therefore it is declared as
 * a foreground service when it is created to prevent it from being shutdown by the Android OS
 * unnecessarily. The process to properly begin sensor collection and then eventually stop is as
 * follows:
 *              BandCollectionService.connect(Context)
 *              BandCollectionService.startStreaming(Context)
 *              ...
 *              BandCollectionService.stopStreaming(Context)    [optional]
 *              BandCollectionService.disconnect(Context)
 *
 */

public class BandCollectionService extends Service {
    //Actions provided by this Service
    /**
     * When an Intent is received with this action, BandCollectionService attempts to open a
     * connection with a Microsoft Band.
     */
    public static final String ACTION_CONNECT = "uri.wbl.ear.action_connect";
    /**
     * When an Intent is received with this action, BandCollectionService attempts to close any
     * currently open connections to Microsoft Bands.
     */
    public static final String ACTION_DISCONNECT = "uri.wbl.ear.action_disconnect";
    /**
     * When an Intent is received with this action, BandCollectionService attempts to begin
     * collecting sensor data from the connected Microsoft Band.
     */
    public static final String ACTION_START_STREAMING = "uri.wbl.ear.action_start_streaming";
    /**
     * When an Intent is received with this action, BandCollectionService attempts to stop
     * collecting data from the connected Microsoft Band.
     */
    public static final String ACTION_STOP_STREAMING = "uri.wbl.ear.action_stop_streaming";
    /**
     * When an Intent is received with this action, BandCollectionService retrieves the connected
     * Band's name and address and broadcasts this information in a String array to all registered
     * BandUpdateReceivers.
     */
    public static final String ACTION_GET_INFO = "uri.wbl.ear.action_get_info";
    /**
     * When an Intent is received with this action, BandCollectionService restarts itself and sends
     * a broadcast to all registered TestBandReceivers to notify that the service is still working
     */
    public static final String ACTION_TEST_SERVICE = "uri.wbl.ear.action_test_service";

    //States
    private static final int STATE_CONNECTED = 0;
    private static final int STATE_STREAMING = 1;
    private static final int STATE_DISCONNECTED = 2;
    private static final int STATE_OTHER = 3;

    public static void connect(Context context) {
        Intent intent = new Intent(context, BandCollectionService.class);
        intent.setAction(ACTION_CONNECT);
        context.startService(intent);
    }

    public static void startStream(Context context) {
        Intent intent = new Intent(context, BandCollectionService.class);
        intent.setAction(ACTION_START_STREAMING);
        context.startService(intent);
    }

    /*public static void stopStream(Context context) {
        Intent intent = new Intent(context, BandCollectionService.class);
        intent.setAction(ACTION_STOP_STREAMING);
        context.startService(intent);
    }*/

    public static void requestBandInfo(Context context) {
        Intent intent = new Intent(context, BandCollectionService.class);
        intent.setAction(ACTION_GET_INFO);
        context.startService(intent);
    }

    public static void disconnect(Context context) {
        Intent intent = new Intent(context, BandCollectionService.class);
        intent.setAction(ACTION_DISCONNECT);
        context.startService(intent);
    }

    public static void test(Context context) {
        Intent intent = new Intent(context, BandCollectionService.class);
        intent.setAction(ACTION_TEST_SERVICE);
        context.startService(intent);
    }

    private final int NOTIFICATION_ID = 43;

    private BandClientManager mBandClientManager;
    private BandClient mBandClient;
    private BandAccelerometerListener mBandAccelerometerListener;
    private BandAmbientLightListener mBandAmbientLightListener;
    private BandContactListener mBandContactListener;
    private BandGsrListener mBandGsrListener;
    private BandHeartRateListener mBandHeartRateListener;
    private BandRRIntervalListener mBandRRIntervalListener;
    private BandSkinTemperatureListener mBandSkinTemperatureListener;

    private Context mContext;
    private String mBandName;
    private String mBandAddress;

    private int mState;

    BandContactStateReceiver mBandContactStateReceiver = new BandContactStateReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(BAND_STATE, false)) {
                resumeFromDynamicBlackout();
            } else {
                enterDynamicBlackout();
            }
        }
    };

    private BandResultCallback<ConnectionState> mBandConnectResultCallback = new BandResultCallback<ConnectionState>() {
        @Override
        public void onResult(ConnectionState connectionState, Throwable throwable) {
            switch (connectionState) {
                case CONNECTED:
                    log("Connected");
                    mState = STATE_CONNECTED;
                    updateNotification("CONNECTED");
                    try {
                        mBandClient.getNotificationManager().vibrate(VibrationType.RAMP_UP);
                    } catch (BandException e) {
                        e.printStackTrace();
                    }
                    //Broadcast Update
                    log("Broadcasting Update");
                    Intent intent = new Intent(BandUpdateReceiver.INTENT_FILTER.getAction(0));
                    intent.putExtra(BandUpdateReceiver.UPDATE_BAND_CONNECTED, true);
                    sendBroadcast(intent);
                    break;
                case BOUND:
                    log("Bound");
                    mState = STATE_DISCONNECTED;
                    updateNotification("DISCONNECTED");
                    Toast.makeText(mContext, "Could not connect to Band", Toast.LENGTH_LONG).show();
                    disconnect();
                    break;
                case BINDING:
                    log("Binding");
                    break;
                case UNBOUND:
                    log("Unbound");
                    mState = STATE_OTHER;
                    updateNotification("UNBOUND");
                    Toast.makeText(mContext, "Could not connect to Band", Toast.LENGTH_LONG).show();
                    break;
                case UNBINDING:
                    log("Unbinding");
                    break;
                default:
                    log("Unknown State");
                    updateNotification("ERROR");
                    break;
            }
        }
    };

    private BandResultCallback<Void> mBandDisconnectResultCallback = new BandResultCallback<Void>() {
        @Override
        public void onResult(Void aVoid, Throwable throwable) {
            log("Disconnected");
            updateNotification("DISCONNECTED");
            stopSelf();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        log("Service Created");
        mContext = this;
        mState = STATE_OTHER;
        mBandName = null;
        mBandAddress = null;

        //Declare as Foreground Service
        Notification notification = new Notification.Builder(this)
                .setContentTitle("ED EAR Active")
                .setSmallIcon(android.R.drawable.ic_secure)
                .setContentText("EAR is Starting")
                .build();
        startForeground(NOTIFICATION_ID, notification);

        mBandClientManager = BandClientManager.getInstance();
        mBandAccelerometerListener = new BandAccelerometerListener(this);
        mBandAmbientLightListener = new BandAmbientLightListener(this);
        mBandContactListener = new BandContactListener(this);
        mBandGsrListener = new BandGsrListener(this);
        mBandHeartRateListener = new BandHeartRateListener(this);
        mBandRRIntervalListener = new BandRRIntervalListener(this);
        mBandSkinTemperatureListener = new BandSkinTemperatureListener(this);

        registerReceiver(mBandContactStateReceiver, BandContactStateReceiver.INTENT_FILTER);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        if (flags == START_FLAG_REDELIVERY) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        connect();
                        Thread.sleep(250);
                        startStreaming();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {

            if (intent == null || intent.getAction() == null) {
                return START_NOT_STICKY;
            }

            switch (intent.getAction()) {
                case ACTION_CONNECT:
                    connect();
                    break;
                case ACTION_DISCONNECT:
                    disconnect();
                    break;
                case ACTION_START_STREAMING:
                    startStreaming();
                    break;
                case ACTION_STOP_STREAMING:
                    stopStreaming();
                    break;
                case ACTION_GET_INFO:
                    getInfo();
                    break;
                case ACTION_TEST_SERVICE:
                    Intent testIntent = new Intent(TestBandReceiver.INTENT_FILTER.getAction(0));
                    testIntent.putExtra(TestBandReceiver.EXTRA_CHECK, true);
                    sendBroadcast(testIntent);
                    final Context context = this;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                stopStreaming();
                                Thread.sleep(250);
                                disconnect(context);
                                Thread.sleep(250);
                                connect(context);
                                Thread.sleep(250);
                                startStream(context);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                default:
                    break;
            }
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mBandContactStateReceiver);
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(500);
        log("Service Destroyed");
        super.onDestroy();
    }

    private void connect() {
        if (mBandClientManager == null) {
            log("Connect Failed (Band Client Manager not Initialized)");
            return;
        }

        BandInfo[] pairedBands = mBandClientManager.getPairedBands();
        if (pairedBands == null || pairedBands.length == 0) {
            log("Connect Failed (No Bands are Paired with this Device)");
        } else if (pairedBands.length > 1) {
            /*
             * TODO
             * Implement UI to allow User to choose Band to pair to.
             * For now, always choose pairedBands[0]
             */
            connect(pairedBands[0]);
        } else {
            connect(pairedBands[0]);
        }
    }

    private void connect(BandInfo bandInfo) {
        log("Attempting to Connect to " + bandInfo.getMacAddress() + "...");
        mBandName = bandInfo.getName();
        mBandAddress = bandInfo.getMacAddress();
        mBandClient = mBandClientManager.create(this, bandInfo);
        mBandClient.connect().registerResultCallback(mBandConnectResultCallback);
    }

    private void getInfo() {
        if (mBandClient == null || !mBandClient.isConnected()) {
            return;
        }

        String[] bandInfo = new String[2];
        bandInfo[0] = mBandName;
        bandInfo[1] = mBandAddress;

        //Broadcast Update
        Intent intent = new Intent(BandUpdateReceiver.INTENT_FILTER.getAction(0));
        intent.putExtra(BandUpdateReceiver.UPDATE_BAND_INFO, true);
        intent.putExtra(BandUpdateReceiver.EXTRA_BAND_INFO, bandInfo);
        this.sendBroadcast(intent);
    }

    private void startStreaming() {
        log("Starting Stream");
        if (mBandClient == null || !mBandClient.isConnected()) {
            log("Band is not Connected");
            return;
        }

        if (mState != STATE_STREAMING) {
            log(mBandClient.getSensorManager().getCurrentHeartRateConsent().name());
            BandSensorManager bandSensorManager = mBandClient.getSensorManager();
            try {
                bandSensorManager.registerAccelerometerEventListener(mBandAccelerometerListener, SampleRate.MS128);
                bandSensorManager.registerAmbientLightEventListener(mBandAmbientLightListener);
                bandSensorManager.registerContactEventListener(mBandContactListener);
                bandSensorManager.registerGsrEventListener(mBandGsrListener, GsrSampleRate.MS200);
                bandSensorManager.registerHeartRateEventListener(mBandHeartRateListener);
                bandSensorManager.registerRRIntervalEventListener(mBandRRIntervalListener);
                bandSensorManager.registerSkinTemperatureEventListener(mBandSkinTemperatureListener);
                mState = STATE_STREAMING;
                updateNotification("STREAMING");
            } catch (BandException | InvalidBandVersionException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopStreaming() {
        if (mBandClient == null) {
            return;
        }

        if (mState == STATE_STREAMING) {
            BandSensorManager bandSensorManager = mBandClient.getSensorManager();
            try {
                bandSensorManager.unregisterAccelerometerEventListener(mBandAccelerometerListener);
                bandSensorManager.unregisterAmbientLightEventListener(mBandAmbientLightListener);
                bandSensorManager.unregisterContactEventListener(mBandContactListener);
                bandSensorManager.unregisterGsrEventListener(mBandGsrListener);
                bandSensorManager.unregisterHeartRateEventListener(mBandHeartRateListener);
                bandSensorManager.unregisterRRIntervalEventListener(mBandRRIntervalListener);
                bandSensorManager.unregisterSkinTemperatureEventListener(mBandSkinTemperatureListener);
                mState = STATE_CONNECTED;
                updateNotification("CONNECTED");
            } catch (BandIOException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private void enterDynamicBlackout() {
        if (mBandClient.isConnected()) {
            updateNotification("Band is not being worn");
        }
        BandSensorManager bandSensorManager = mBandClient.getSensorManager();
        try {
            bandSensorManager.unregisterAccelerometerEventListener(mBandAccelerometerListener);
            bandSensorManager.unregisterAmbientLightEventListener(mBandAmbientLightListener);
            bandSensorManager.unregisterGsrEventListener(mBandGsrListener);
            bandSensorManager.unregisterHeartRateEventListener(mBandHeartRateListener);
            bandSensorManager.unregisterRRIntervalEventListener(mBandRRIntervalListener);
            bandSensorManager.unregisterSkinTemperatureEventListener(mBandSkinTemperatureListener);
        } catch (BandIOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void resumeFromDynamicBlackout() {
        if (mBandClient.isConnected()) {
            updateNotification("STREAMING");
        }
        BandSensorManager bandSensorManager = mBandClient.getSensorManager();
        try {
            bandSensorManager.registerAccelerometerEventListener(mBandAccelerometerListener, SampleRate.MS128);
            bandSensorManager.registerAmbientLightEventListener(mBandAmbientLightListener);
            bandSensorManager.registerGsrEventListener(mBandGsrListener, GsrSampleRate.MS200);
            bandSensorManager.registerHeartRateEventListener(mBandHeartRateListener);
            bandSensorManager.registerRRIntervalEventListener(mBandRRIntervalListener);
            bandSensorManager.registerSkinTemperatureEventListener(mBandSkinTemperatureListener);
        } catch (BandException | InvalidBandVersionException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        if (mBandClientManager == null) {
            log("Disconnect Failed (Band Client Manager not Initialized)");
            return;
        }

        if (mBandClient == null || !mBandClient.isConnected()) {
            log("Disconnect Failed (Band is not Connected)");
            SettingsActivity.putBoolean(this, SettingsActivity.KEY_SENSOR_ENABLE, false);
            stopSelf();
            return;
        }

        try {
            mBandClient.getNotificationManager().vibrate(VibrationType.RAMP_DOWN);
        } catch (BandException e) {
            e.printStackTrace();
        }

        if (mState == STATE_STREAMING) {
            stopStreaming();
        }

        mBandClient.disconnect().registerResultCallback(mBandDisconnectResultCallback, 10, TimeUnit.SECONDS);
    }

    private void updateNotification(String status) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setContentTitle("EAR is Active")
                .setContentText("Band Status: " + status)
                .setSmallIcon(android.R.drawable.ic_secure);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void log(String message) {
        Log.d("BandCollectionService", message);
    }


}
