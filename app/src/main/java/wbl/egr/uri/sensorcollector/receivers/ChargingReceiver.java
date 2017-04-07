package wbl.egr.uri.sensorcollector.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import wbl.egr.uri.sensorcollector.MainActivity;
import wbl.egr.uri.sensorcollector.services.DataLogService;

/**
 * Created by mconstant on 2/23/17.
 */

public class ChargingReceiver extends BroadcastReceiver {
    public static final String HEADER = "date,time,charging";

    @Override
    public void onReceive(Context context, Intent intent) {
        File directory = MainActivity.getRootFile(context).getParentFile();
        File chargeFile = new File(directory, "charge_log.csv");
        String contents;

        String action = intent.getAction();
        boolean charging = action.equals(Intent.ACTION_POWER_CONNECTED);

        Calendar calendar = Calendar.getInstance();
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(calendar.getTime());
        String timeString = new SimpleDateFormat("kk:mm:ss.SSS", Locale.US).format(calendar.getTime());
        contents = dateString + "," + timeString + "," + String.valueOf(charging);

        DataLogService.log(context, chargeFile, contents, HEADER);
    }
}
