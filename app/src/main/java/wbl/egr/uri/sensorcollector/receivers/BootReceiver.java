package wbl.egr.uri.sensorcollector.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import wbl.egr.uri.sensorcollector.SettingsActivity;
import wbl.egr.uri.sensorcollector.services.AudioRecordManager;
import wbl.egr.uri.sensorcollector.services.BandCollectionService;

/**
 * Created by mconstant on 2/23/17.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        if (SettingsActivity.getBoolean(context, SettingsActivity.KEY_SENSOR_ENABLE, false)) {
            BandCollectionService.connect(context, true);
        }

        if (SettingsActivity.getBoolean(context, SettingsActivity.KEY_AUDIO_ENABLE, false)) {
            AudioRecordManager.start(context, AudioRecordManager.ACTION_AUDIO_CANCEL);
            AudioRecordManager.start(context, AudioRecordManager.ACTION_AUDIO_CREATE);
        }
    }
}