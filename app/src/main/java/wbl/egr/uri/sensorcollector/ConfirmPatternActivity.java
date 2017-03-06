package wbl.egr.uri.sensorcollector;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.List;

import me.zhanghai.android.patternlock.PatternUtils;
import me.zhanghai.android.patternlock.PatternView;

/**
 * Created by mconstant on 2/24/17.
 */

public class ConfirmPatternActivity extends me.zhanghai.android.patternlock.ConfirmPatternActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRightButton.setVisibility(View.GONE);
    }

    @Override
    protected boolean isPatternCorrect(List<PatternView.Cell> pattern) {
        if (PatternUtils.patternToSha1String(pattern).equals(SettingsActivity.getString(this, SettingsActivity.KEY_PATTERN, null))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onConfirmed() {
        setResult(RESULT_OK);
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onWrongPattern() {
        if (numFailedAttempts == 5) {
            finish();
        } else {
            ++numFailedAttempts;
        }
    }
}
