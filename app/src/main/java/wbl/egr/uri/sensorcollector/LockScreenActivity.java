package wbl.egr.uri.sensorcollector;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by mconstant on 2/23/17.
 */

public class LockScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent;
        if (SettingsActivity.getString(this, SettingsActivity.KEY_PATTERN, null) == null) {
            intent = new Intent(this, SetPatternActivity.class);
        } else {
            intent = new Intent(this, ConfirmPatternActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
