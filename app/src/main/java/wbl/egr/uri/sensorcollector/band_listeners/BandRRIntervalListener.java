package wbl.egr.uri.sensorcollector.band_listeners;

import android.content.Context;

import com.microsoft.band.sensors.BandAmbientLightEvent;
import com.microsoft.band.sensors.BandRRIntervalEvent;
import com.microsoft.band.sensors.BandRRIntervalEventListener;

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

public class BandRRIntervalListener implements BandRRIntervalEventListener {
    private static final String HEADER = "Date,Time,Interval (Seconds)";

    private Context mContext;

    public BandRRIntervalListener(Context context) {
        mContext = context;
    }

    @Override
    public void onBandRRIntervalChanged(BandRRIntervalEvent bandRRIntervalEvent) {
        Date date = Calendar.getInstance().getTime();
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        String timeString = new SimpleDateFormat("kk:mm:ss.SSS", Locale.US).format(date);
        String data = dateString + "," + timeString + "," +
                bandRRIntervalEvent.getInterval();
        DataLogService.log(mContext, new File(MainActivity.getRootFile(mContext), "rr.csv"), data, HEADER);
    }
}
