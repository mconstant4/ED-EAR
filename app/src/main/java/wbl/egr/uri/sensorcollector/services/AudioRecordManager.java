package wbl.egr.uri.sensorcollector.services;

import android.Manifest;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import wbl.egr.uri.sensorcollector.MainActivity;
import wbl.egr.uri.sensorcollector.SettingsActivity;
import wbl.egr.uri.sensorcollector.receivers.AlarmReceiver;

/**
 * Created by mconstant on 2/23/17.
 */

public class AudioRecordManager extends IntentService {
    public static final String INTENT_ACTION = "intent_action";
    public static final int ACTION_AUDIO_START = 0;
    public static final int ACTION_AUDIO_TRIGGER = 1;
    public static final int ACTION_AUDIO_CREATE = 2;
    public static final int ACTION_AUDIO_CANCEL = 3;

    public static void start(Context context, int action) {
        Intent intent = new Intent(context, AudioRecordManager.class);
        intent.putExtra(INTENT_ACTION, action);
        context.startService(intent);
    }

    public AudioRecordManager() {
        super("AudioRecordManagerThread");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        if (intent == null || !intent.hasExtra(INTENT_ACTION)) {
            log("Service not Started Properly");
            return;
        }

        switch (intent.getIntExtra(INTENT_ACTION, -1)) {
            case ACTION_AUDIO_START:
                log("ACTION_AUDIO_START");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        log("anEAR must have Audio Record permission to record audio");
                        return;
                    }
                }
                //Start Recording
                startAudio(false);
                setAudioAlarm();
                break;
            case ACTION_AUDIO_TRIGGER:
                log("ACTION_AUDIO_TRIGGER");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        log("anEAR must have Audio Record permission to record audio");
                        return;
                    }
                }
                //Start Recording if Audio Recordings enabled
                if (SettingsActivity.getBoolean(this, SettingsActivity.KEY_AUDIO_ENABLE, false)) {
                    startAudio(true);
                }
                break;
            case ACTION_AUDIO_CREATE:
                log("ACTION_AUDIO_CREATE");
                setAudioAlarm();
                break;
            case ACTION_AUDIO_CANCEL:
                log("ACTION_AUDIO_CANCEL");
                cancelAlarm();
                break;
        }
    }

    private void setAudioAlarm() {
        int audioDuration = Integer.parseInt(SettingsActivity.getString(this, SettingsActivity.KEY_AUDIO_DURATION, "30"));
        int audioDelay = Integer.parseInt(SettingsActivity.getString(this, SettingsActivity.KEY_AUDIO_DELAY, "12")) * 60;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, audioDelay + audioDuration);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ((AlarmManager) getSystemService(ALARM_SERVICE)).setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), getPendingIntent(AlarmReceiver.AUDIO_ID, ACTION_AUDIO_START));
        } else {
            ((AlarmManager) getSystemService(ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), getPendingIntent(AlarmReceiver.AUDIO_ID, ACTION_AUDIO_START));
        }
    }

    private void cancelAlarm() {
        PendingIntent pendingIntent;
        pendingIntent = getPendingIntent(AlarmReceiver.AUDIO_ID, ACTION_AUDIO_START);
        stopService(new Intent(this, AudioRecorderService.class));
        ((AlarmManager) getSystemService(ALARM_SERVICE)).cancel(pendingIntent);
    }

    private PendingIntent getPendingIntent(int id, int action) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.KEY_ALARM_ID, id);
        intent.putExtra(AlarmReceiver.KEY_ACTION, action);

        return PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void startAudio(boolean trigger) {
        //Get time
        Calendar currentTime = Calendar.getInstance();
        //Check if between 1am and 5am
        Calendar calendar1am = Calendar.getInstance();
        calendar1am.set(Calendar.HOUR_OF_DAY, 1);
        Calendar calendar5am = Calendar.getInstance();
        calendar5am.set(Calendar.HOUR_OF_DAY, 5);
        if (currentTime.compareTo(calendar1am) > 0) {
            //After 1am
            if (currentTime.compareTo(calendar5am) < 0) {
                //Before 5am
                log("In Blackout Time");
                return;
            }
        }
        if (SettingsActivity.getBoolean(this, SettingsActivity.KEY_BLACKOUT_TOGGLE, false)) {
            Calendar calendar730am = Calendar.getInstance();
            calendar730am.set(Calendar.HOUR_OF_DAY, 7);
            calendar730am.set(Calendar.MINUTE, 30);
            Calendar calendar3pm = Calendar.getInstance();
            calendar3pm.set(Calendar.HOUR_OF_DAY, 15);
            if (currentTime.compareTo(calendar730am) > 0) {
                //After 7:30
                if (currentTime.compareTo(calendar3pm) < 0) {
                    //Before 3pm
                    log("In Blackout Time");
                    return;
                }
            }
        }


        File directory = MainActivity.getRootFile(this);
        String wavFileName = "";
        String p_id = SettingsActivity.getString(this, SettingsActivity.KEY_IDENTIFIER, null);
        if (p_id != null && p_id != "") {
            wavFileName += p_id + "_";
        }
        wavFileName += (new SimpleDateFormat("MM_dd_yyyy", Locale.US).format(new Date()) + "_");
        wavFileName += (new SimpleDateFormat("kk.mm.ss", Locale.US).format(new Date()) + "_");
        int count = 0;
        if (directory != null) {
            for (File file : directory.listFiles()) {
                if (file.getPath().endsWith(".wav")) {
                    count++;
                }
            }
        }

        wavFileName += count + ".wav";



        File file = new File(directory, wavFileName);
        File temp = new File(directory, "raw_audio.tmp");
        File audioRecordLog = new File(directory.getParentFile(), "AudioRecordLog.csv");

        Intent intent = new Intent(this, AudioRecorderService.class);
        intent.putExtra(AudioRecorderService.INTENT_WAV_FILE, file.getAbsolutePath());
        intent.putExtra(AudioRecorderService.INTENT_TEMP_FILE, temp.getAbsolutePath());
        intent.putExtra(AudioRecorderService.INTENT_LOG_FILE, audioRecordLog.getAbsolutePath());
        intent.putExtra(AudioRecorderService.INTENT_AUDIO_TRIGGER, trigger);
        startService(intent);
    }

    private void log(String message) {
        Log.d(this.getClass().getSimpleName(), message);
    }
}
