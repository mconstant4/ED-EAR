package wbl.egr.uri.sensorcollector;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Application {
    public static File getRootFile(Context context) {
        File root;
        root = new File("/storage/sdcard1");
        if (!root.exists() || !root.canWrite()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                root = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOCUMENTS);
            } else {
                root = new File(Environment.getExternalStorageDirectory(), "Documents");
            }
        }
        File directory;
        String id = SettingsActivity.getString(context, SettingsActivity.KEY_IDENTIFIER, null);
        if (id == null || id.equals("")) {
            directory = new File(root, ".anear");
        } else {
            directory = new File(root, ".anear/" + id);
        }
        String date = new SimpleDateFormat("MM_dd_yyyy", Locale.US).format(new Date());
        File rootDir = new File(directory, date);
        if (!rootDir.exists()) {
            if (rootDir.mkdirs()) {
                Log.d("MAIN", "Made parent directories");
            }
        }
        return rootDir;
    }
}
