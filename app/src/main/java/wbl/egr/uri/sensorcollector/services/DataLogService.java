package wbl.egr.uri.sensorcollector.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by mconstant on 2/22/17.
 */

public class DataLogService extends IntentService {
    public static final String EXTRA_FILE_DESTINATION = "uri.wbl.ear.extra_file_destination";
    public static final String EXTRA_DATA = "uri.wbl.ear.extra_data";
    public static final String EXTRA_HEADER = "uri.wbl.ear.extra_header";

    public static void log(Context context, File file, String data, String header) {
        Intent intent = new Intent(context, DataLogService.class);
        intent.putExtra(EXTRA_FILE_DESTINATION, file.getAbsolutePath());
        intent.putExtra(EXTRA_DATA, data);
        intent.putExtra(EXTRA_HEADER, header);
        context.startService(intent);
    }

    public DataLogService() {
        super("DataLogServiceThread");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        if (intent == null || !intent.hasExtra(EXTRA_DATA)) {
            return;
        }

        boolean newFile = false;
        File file = new File(intent.getStringExtra(EXTRA_FILE_DESTINATION));
        if (!file.exists()) {
            newFile = true;
            file.getParentFile().mkdirs();
            try {
                if (!file.createNewFile()) {
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            if (newFile) {
                fileOutputStream.write(intent.getStringExtra(EXTRA_HEADER).getBytes());
                fileOutputStream.write("\n".getBytes());
            }
            fileOutputStream.write(intent.getStringExtra(EXTRA_DATA).getBytes());
            fileOutputStream.write("\n".getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
