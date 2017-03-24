package wbl.egr.uri.sensorcollector.receivers;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

/**
 * Created by mconstant on 2/23/17.
 */

public abstract class TestBandReceiver extends BroadcastReceiver {
    public static final String EXTRA_STATE = "uri.wbl.ear.extra_state";
    public static final IntentFilter INTENT_FILTER = new IntentFilter("uri.wbl.ear.test_band_receiver");
}
