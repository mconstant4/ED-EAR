package wbl.egr.uri.sensorcollector.band_listeners;

import android.content.Context;
import android.util.Log;

import com.microsoft.band.sensors.BandGsrEvent;
import com.microsoft.band.sensors.BandGsrEventListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import wbl.egr.uri.sensorcollector.MainActivity;
import wbl.egr.uri.sensorcollector.services.DataLogService;

/**
 * Created by mconstant on 2/22/17.
 */

public class BandGsrListener implements BandGsrEventListener {
    private static final String HEADER = "Date,Time,Resistance (kOhm)";

    private Context mContext;

    public BandGsrListener(Context context) {
        mContext = context;
    }
    @Override
    public void onBandGsrChanged(BandGsrEvent bandGsrEvent) {
        Date date = Calendar.getInstance().getTime();
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        String timeString = new SimpleDateFormat("kk:mm:ss.SSS", Locale.US).format(date);
        String data = dateString + "," + timeString + "," +
                bandGsrEvent.getResistance();
        DataLogService.log(mContext, new File(MainActivity.getRootFile(mContext), "/gsr.csv"), data, HEADER);
    }
}
