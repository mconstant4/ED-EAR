package wbl.egr.uri.sensorcollector.fragments;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import wbl.egr.uri.sensorcollector.MainActivity;
import wbl.egr.uri.sensorcollector.R;
import wbl.egr.uri.sensorcollector.SettingsActivity;
import wbl.egr.uri.sensorcollector.receivers.TestBandReceiver;
import wbl.egr.uri.sensorcollector.services.AudioRecordManager;
import wbl.egr.uri.sensorcollector.services.BandCollectionService;

/**
 * Created by mconstant on 2/23/17.
 */

public class TestingFragment extends Fragment {
    protected Button mTestButton;

    private int mAttempts;
    private boolean mSensorWorking;

    private TestBandReceiver mTestBandReceiver = new TestBandReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("TEST", "Sensor returned");
            mSensorWorking = true;
        }
    };

    public static Fragment getInstance() {
        return new TestingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().registerReceiver(mTestBandReceiver, TestBandReceiver.INTENT_FILTER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_testing, container, false);

        mAttempts = 0;
        mTestButton = (Button) view.findViewById(R.id.test_button);
        mTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });


        return view;
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mTestBandReceiver);
        super.onDestroy();
    }

    private void test() {
        if (!isBandCollectionServiceRunning()) {
            BandCollectionService.test(getActivity());
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int audioResult = checkAudio();
                        int sensorResult = checkSensors();

                        if (audioResult == 0 && sensorResult == 0) {
                            //Test Passed
                            final MaterialDialog resultDialog = new MaterialDialog.Builder(getActivity())
                                    .title("Success!")
                                    .customView(R.layout.view_test_success, true)
                                    .positiveText("OK")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            remindToChargeBand();
                                        }
                                    })
                                    .autoDismiss(true)
                                    .canceledOnTouchOutside(false)
                                    .show();
                        } else if (audioResult != 0 && sensorResult == 0) {
                            //Only Audio Failed
                            if (mAttempts < 1) {
                                new MaterialDialog.Builder(getActivity())
                                        .title("Test Failed")
                                        .customView(R.layout.view_test_audio_fail, true)
                                        .negativeText("Restart")
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                AudioRecordManager.start(getActivity(), AudioRecordManager.ACTION_AUDIO_CANCEL);
                                                AudioRecordManager.start(getActivity(), AudioRecordManager.ACTION_AUDIO_START);
                                            }
                                        })
                                        .canceledOnTouchOutside(false)
                                        .show();
                                mAttempts++;
                            } else {
                                callRA(audioResult);
                            }
                        } else if (audioResult == 0 && sensorResult != 0) {
                            //Only Sensors Failed
                            if (mAttempts < 1) {
                                new MaterialDialog.Builder(getActivity())
                                        .title("Test Failed")
                                        .customView(R.layout.view_test_sensors_fail, true)
                                        .negativeText("Restart")
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                AudioRecordManager.start(getActivity(), AudioRecordManager.ACTION_AUDIO_CANCEL);
                                                AudioRecordManager.start(getActivity(), AudioRecordManager.ACTION_AUDIO_START);

                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            try {
                                                                BandCollectionService.disconnect(getActivity());
                                                                Thread.sleep(250);
                                                                BandCollectionService.connect(getActivity());
                                                                Thread.sleep(250);
                                                                BandCollectionService.startStream(getActivity());
                                                                getActivity().runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        SettingsActivity.putBoolean(getActivity(), SettingsActivity.KEY_SENSOR_ENABLE, true);
                                                                        SettingsActivity.putBoolean(getActivity(), SettingsActivity.KEY_AUDIO_ENABLE, true);
                                                                        test();
                                                                    }
                                                                });
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    }).start();
                                            }
                                        })
                                        .canceledOnTouchOutside(false)
                                        .show();
                                mAttempts++;
                            } else {
                                callRA(sensorResult);
                            }
                        } else {
                            //Both Failed
                            if (mAttempts < 1) {
                                new MaterialDialog.Builder(getActivity())
                                        .title("Test Failed")
                                        .customView(R.layout.view_test_fail, true)
                                        .negativeText("Restart")
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                AudioRecordManager.start(getActivity(), AudioRecordManager.ACTION_AUDIO_CANCEL);
                                                AudioRecordManager.start(getActivity(), AudioRecordManager.ACTION_AUDIO_START);

                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            try {
                                                                BandCollectionService.disconnect(getActivity());
                                                                Thread.sleep(250);
                                                                BandCollectionService.connect(getActivity());
                                                                Thread.sleep(250);
                                                                BandCollectionService.startStream(getActivity());
                                                                getActivity().runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        SettingsActivity.putBoolean(getActivity(), SettingsActivity.KEY_SENSOR_ENABLE, true);
                                                                        SettingsActivity.putBoolean(getActivity(), SettingsActivity.KEY_AUDIO_ENABLE, true);
                                                                        test();
                                                                    }
                                                                });
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    }).start();
                                            }
                                        })
                                        .canceledOnTouchOutside(false)
                                        .show();
                                mAttempts++;
                            } else {
                                callRA(4);
                            }
                        }
                    }
                });
            }
        }, 500);
    }

    private int checkAudio() {
        File root = MainActivity.getRootFile(getActivity());
        if (!root.exists()) {
            return 1;
        }

        ArrayList<File> audioFiles = new ArrayList<>();
        for (File file : root.listFiles()) {
            if (file.getName().endsWith(".wav")) {
                audioFiles.add(file);
            }
        }
        if (audioFiles.size() == 0) {
            return 2;
        }
        long lastModified = 0;
        for (File file : audioFiles) {
            if (file.lastModified() > lastModified) {
                lastModified = file.lastModified();
            }
        }
        Calendar currentTimeThreshold = Calendar.getInstance();

        if (SettingsActivity.getBoolean(getActivity(), SettingsActivity.KEY_BLACKOUT_TOGGLE, false)) {
            //Check if in nightly blackout
            Calendar currentTime = Calendar.getInstance();
            Calendar calendar1am = Calendar.getInstance();
            calendar1am.set(Calendar.HOUR_OF_DAY, 1);
            Calendar calendar5am = Calendar.getInstance();
            calendar5am.set(Calendar.HOUR_OF_DAY, 5);
            Calendar calendar730am = Calendar.getInstance();
            calendar730am.set(Calendar.HOUR_OF_DAY, 7);
            calendar730am.set(Calendar.MINUTE, 30);
            Calendar calendar3pm = Calendar.getInstance();
            calendar3pm.set(Calendar.HOUR_OF_DAY, 15);

            if (currentTime.compareTo(calendar1am) > 0 && currentTime.compareTo(calendar5am) < 0) {
                //In nightly blackout time
                //Check from initial blackout time
                currentTimeThreshold.setTimeInMillis(calendar1am.getTimeInMillis());
                currentTimeThreshold.set(Calendar.MINUTE, currentTimeThreshold.get(Calendar.MINUTE)-15);
            } else if (currentTime.compareTo(calendar730am) > 0 && currentTime.compareTo(calendar3pm) < 0) {
                //In school blackout
                //Check from initial blackout time
                currentTimeThreshold.setTimeInMillis(calendar730am.getTimeInMillis());
                currentTimeThreshold.set(Calendar.MINUTE, currentTimeThreshold.get(Calendar.MINUTE)-15);
            } else {
                //Not in blackout time
                //Check from current time
                currentTimeThreshold.set(Calendar.MINUTE, currentTimeThreshold.get(Calendar.MINUTE)-15);
            }
        } else {
            currentTimeThreshold.set(Calendar.MINUTE, currentTimeThreshold.get(Calendar.MINUTE)-15);
        }
        Calendar lastModifiedTime = Calendar.getInstance();
        lastModifiedTime.setTimeInMillis(lastModified);
        Log.d("TEST", "Threshold Time: " + currentTimeThreshold.getTime() + "\nLast Modified: " + lastModifiedTime.getTime());
        if (lastModifiedTime.compareTo(currentTimeThreshold) < 0) {
            return 3;
        } else {
            return 0;
        }
    }

    private int checkSensors() {
        if (mSensorWorking || isBandCollectionServiceRunning()) {
            return 0;
        } else {
            return 1;
        }
    }

    private void callRA(int error) {
        //Call RA
        mAttempts = 0;
        new MaterialDialog.Builder(getActivity())
                .title("Error - " + error)
                .content("Try testing again. If this error continues to occur, please call the RA at 401-354-2803")
                .positiveText("OK")
                .canceledOnTouchOutside(false)
                .show();
    }

    private void remindToChargeBand() {
        new MaterialDialog.Builder(getActivity())
                .title("Please charge your Band and keep charging your device!")
                .positiveText("OK")
                .show();
    }

    private boolean isBandCollectionServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (BandCollectionService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
