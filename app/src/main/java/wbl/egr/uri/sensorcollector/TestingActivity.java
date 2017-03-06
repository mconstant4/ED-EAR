package wbl.egr.uri.sensorcollector;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import wbl.egr.uri.sensorcollector.fragments.TestingFragment;

/**
 * Created by mconstant on 2/23/17.
 */

public class TestingActivity extends AppCompatActivity {
    public static final String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private final int REQUEST_CODE = 3497;

    private CoordinatorLayout mMessageContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);

        mMessageContainer = (CoordinatorLayout) findViewById(R.id.message_container);

        //Request Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMISSIONS, REQUEST_CODE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, TestingFragment.getInstance(), "testing_fragment")
                .addToBackStack("testing_fragment")
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * The notify(String) method is called by its fragment to display a message to the User.
     * @param message Message to be displayed.
     */
    public void notify(String message) {
        Snackbar snackbar = Snackbar.make(mMessageContainer, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
    }
}
