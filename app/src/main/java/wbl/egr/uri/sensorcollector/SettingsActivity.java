package wbl.egr.uri.sensorcollector;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import java.lang.ref.WeakReference;

import wbl.egr.uri.sensorcollector.fragments.SettingsFragment;
import wbl.egr.uri.sensorcollector.tasks.RequestHeartRateTask;

/**
 * Created by mconstant on 2/23/17.
 */

public class SettingsActivity extends AppCompatActivity {
    public static final String KEY_SENSOR_ENABLE = "pref_enable_sensors";
    public static final String KEY_SENSOR_PERIODIC = "pref_periodic_sensors";
    public static final String KEY_AUDIO_ENABLE = "pref_enable_audio";
    public static final String KEY_HR_CONSENT = "pref_hr_consent";
    public static final String KEY_AUDIO_DURATION = "pref_audio_duration";
    public static final String KEY_AUDIO_DELAY = "pref_audio_delay";
    public static final String KEY_IDENTIFIER = "pref_id";
    public static final String KEY_PATTERN = "pref_pattern";
    public static final String KEY_BLACKOUT_TOGGLE = "pref_blackout_toggle";

    public static void putString(Context context, String key, String value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key, String defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, defaultValue);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 034);
        }
        if (!SettingsActivity.getBoolean(this, SettingsActivity.KEY_HR_CONSENT, false)) {
            new RequestHeartRateTask().execute(new WeakReference<Activity>(this));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment.newInstance(), "settings_fragment")
                .commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
