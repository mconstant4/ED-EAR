package wbl.egr.uri.sensorcollector.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by mconstant on 2/23/17.
 */

public class AudioRecordModel {
    private Date mStartDate;
    private Date mEndDate;
    private boolean mTrigger = false;
    private String mFileName;
    private long mFileSize = -1L;

    public AudioRecordModel(Date startDate) {
        mStartDate = startDate;
    }

    public void setTriggered(boolean triggered) {
        mTrigger = triggered;
    }

    public void setEndDate(Date endDate) {
        mEndDate = endDate;
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public void setFileSize(long fileSize) {
        mFileSize = fileSize;
    }

    public String generateContents() {
        String contents = "";
        if (mStartDate != null) {
            contents += new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(mStartDate);
            contents += ",";
            contents += new SimpleDateFormat("kk:mm:ss.SSS", Locale.US).format(mStartDate);
            contents += ",";
        } else {
            contents += ",,";
        }
        if (mEndDate != null) {
            contents += new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(mEndDate);
            contents += ",";
            contents += new SimpleDateFormat("kk:mm:ss.SSS", Locale.US).format(mEndDate);
            contents += ",";
        } else {
            contents += ",,";
        }
        if (mFileName != null) {
            contents += mFileName;
            contents += ",";
        } else {
            contents += ",";
        }
        if (mFileSize != -1L) {
            contents += String.valueOf(mFileSize);
            contents += ",";
        } else {
            contents += ",";
        }
        contents += String.valueOf(mTrigger);

        return contents;
    }
}
