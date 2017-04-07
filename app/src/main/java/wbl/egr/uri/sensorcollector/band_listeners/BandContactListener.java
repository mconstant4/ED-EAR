package wbl.egr.uri.sensorcollector.band_listeners;

import android.content.Context;
import android.content.Intent;

import com.microsoft.band.sensors.BandAmbientLightEvent;
import com.microsoft.band.sensors.BandContactEvent;
import com.microsoft.band.sensors.BandContactEventListener;
import com.microsoft.band.sensors.BandContactState;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import wbl.egr.uri.sensorcollector.MainActivity;
import wbl.egr.uri.sensorcollector.receivers.BandContactStateReceiver;
import wbl.egr.uri.sensorcollector.services.DataLogService;

/**
 * Created by mconstant on 2/22/17.
 */

public class BandContactListener implements BandContactEventListener {
    private static final String HEADER = "Date,Time,Contact State";

    private Context mContext;

    public BandContactListener(Context context) {
        mContext = context;
    }

    @Override
    public void onBandContactChanged(BandContactEvent bandContactEvent) {
        Date date = Calendar.getInstance().getTime();
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        String timeString = new SimpleDateFormat("kk:mm:ss.SSS", Locale.US).format(date);
        String data = dateString + "," + timeString + "," +
                bandContactEvent.getContactState();
        DataLogService.log(mContext, new File(MainActivity.getRootFile(mContext), "contact.csv"), data, HEADER);

        //Broadcast Update
        Intent intent = new Intent(BandContactStateReceiver.INTENT_FILTER.getAction(0));
        intent.putExtra(BandContactStateReceiver.BAND_STATE, bandContactEvent.getContactState().name().equals(BandContactState.WORN.name()));
        mContext.sendBroadcast(intent);
    }
}
