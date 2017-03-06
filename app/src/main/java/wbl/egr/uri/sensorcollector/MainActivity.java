package wbl.egr.uri.sensorcollector;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import wbl.egr.uri.sensorcollector.services.BandCollectionService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = this;
        Button connectButton = (Button) findViewById(R.id.connect_btn);
        Button streamButton = (Button) findViewById(R.id.stream_btn);
        Button stopStreamButton = (Button) findViewById(R.id.stop_stream_btn);
        Button disconnectButton = (Button) findViewById(R.id.disconnect_btn);

        /*connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BandCollectionService.class);
                intent.setAction(BandCollectionService.ACTION_CONNECT);
                context.startService(intent);
            }
        });
        streamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BandCollectionService.class);
                intent.setAction(BandCollectionService.ACTION_START_STREAMING);
                context.startService(intent);
            }
        });
        stopStreamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BandCollectionService.class);
                intent.setAction(BandCollectionService.ACTION_STOP_STREAMING);
                context.startService(intent);
            }
        });
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BandCollectionService.class);
                intent.setAction(BandCollectionService.ACTION_DISCONNECT);
                context.startService(intent);
            }
        });*/

        startActivity(new Intent(this, SettingsActivity.class));
    }

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
            rootDir.mkdirs();
        }
        return rootDir;
    }
}
