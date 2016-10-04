package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.info.FileInfo;
import com.hopebaytech.hcfsmgmt.info.ItemInfo;
import com.hopebaytech.hcfsmgmt.main.MainActivity;
import com.hopebaytech.hcfsmgmt.utils.HCFSApiUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.UnitConverter;

import org.json.JSONObject;

import java.io.File;


/**
 * @author Aaron
 *         Created by Aaron on 2016/3/30.
 */
public class FileDialogFragment extends DialogFragment {

    public static final String TAG = FileDialogFragment.class.getSimpleName();
    private final String CLASSNAME = getClass().getSimpleName();

    private AppFileFragment.RecyclerViewHolder mViewHolder;
    private Thread mDisplayIconThread;
    private Thread mCalculateFileDirDataRatioThread;
    private Thread mCalculateFileDirSizeThread;

    private Context mContext;

    public static FileDialogFragment newInstance() {
        return new FileDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ItemInfo itemInfo = mViewHolder.getItemInfo();
        LayoutInflater inflater = ((MainActivity) mContext).getLayoutInflater();
        View view = inflater.inflate(R.layout.file_mgmt_dialog_fragment_file_dir_info, null);

        final ImageView fileDirIcon = (ImageView) view.findViewById(R.id.file_dir_icon);
        fileDirIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.icon_doc_default_gray));
        mDisplayIconThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Bitmap iconBitmap = itemInfo.getIconImage();
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fileDirIcon.setImageBitmap(iconBitmap);
                        }
                    });
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    Logs.w(CLASSNAME, "onCreateDialog", Log.getStackTraceString(e));
                }
            }
        });
        mDisplayIconThread.start();

        TextView fileDirName = (TextView) view.findViewById(R.id.file_dir_name);
        fileDirName.setText(itemInfo.getName());

        final ImageView fileDirPinIcon = (ImageView) view.findViewById(R.id.file_dir_pin_icon);
        if (itemInfo instanceof FileInfo && ((FileInfo) itemInfo).isDirectory()) {
            fileDirPinIcon.setVisibility(View.GONE);
        } else {
            fileDirPinIcon.setVisibility(View.VISIBLE);
            fileDirPinIcon.setImageDrawable(itemInfo.getPinUnpinImage(itemInfo.isPinned()));
            fileDirPinIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isPinned = !itemInfo.isPinned();
                    boolean allowPinUnpin = mViewHolder.pinUnpinItem(isPinned);
                    if (allowPinUnpin) {
                        fileDirPinIcon.setImageDrawable(itemInfo.getPinUnpinImage(isPinned));
                    }
                }
            });
        }

        final TextView fileDirSize = (TextView) view.findViewById(R.id.file_dir_size);
        String size = String.format(getString(R.string.app_file_dialog_data_size), getString(R.string.app_file_dialog_calculating));
        fileDirSize.setText(size);
        mCalculateFileDirSizeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileInfo fileInfo = ((FileInfo) itemInfo);
                    File file = new File(fileInfo.getFilePath());
                    long dirSize = getDirectorySize(file);
                    final String formatSize = UnitConverter.convertByteToProperUnit(dirSize);
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fileDirSize.setText(String.format(getString(R.string.app_file_dialog_data_size), formatSize));
                        }
                    });
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    Logs.w(CLASSNAME, "onCreateDialog", Log.getStackTraceString(e));
                }
            }
        });
        mCalculateFileDirSizeThread.start();

        final TextView fileDirDataRatio = (TextView) view.findViewById(R.id.file_dir_data_ratio);
        String ratio = String.format(getString(R.string.app_file_dialog_local_data_ratio), getString(R.string.app_file_dialog_calculating));
        fileDirDataRatio.setText(ratio);
        mCalculateFileDirDataRatioThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String dataRatio = getFileDirDataRatio((FileInfo) itemInfo);
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fileDirDataRatio.setText(dataRatio);
                        }
                    });
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    Logs.w(CLASSNAME, "onCreateDialog", Log.getStackTraceString(e));
                }
            }
        });
        mCalculateFileDirDataRatioThread.start();

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mDisplayIconThread != null) {
            mDisplayIconThread.interrupt();
        }

        if (mCalculateFileDirSizeThread != null) {
            mCalculateFileDirSizeThread.interrupt();
        }

        if (mCalculateFileDirDataRatioThread != null) {
            mCalculateFileDirDataRatioThread.interrupt();
        }

    }

    private String getFileDirDataRatio(FileInfo fileInfo) {
        FileDirStatusInfo fileDirStatusInfo;
        if (fileInfo.isDirectory()) {
            fileDirStatusInfo = getDirStatusInfo(fileInfo.getFilePath());
        } else {
            fileDirStatusInfo = getFileStatusInfo(fileInfo.getFilePath());
        }

        int numLocal = 0;
        int numHybrid = 0;
        int numCloud = 0;
        if (fileDirStatusInfo != null) {
            numLocal = fileDirStatusInfo.getNumLocal();
            numHybrid = fileDirStatusInfo.getNumHybrid();
            numCloud = fileDirStatusInfo.getNumCloud();
        }
        int numTotal = numLocal + numHybrid + numCloud;
        String ratio = numLocal + "/" + numTotal + " (local/total)";
        return String.format(getString(R.string.app_file_dialog_local_data_ratio), ratio);
    }

    @Nullable
    private FileDirStatusInfo getDirStatusInfo(String dirPath) {
        if (dirPath == null) {
            return null;
        }
        FileDirStatusInfo fileDirStatusInfo = null;
        try {
            String jsonResult = HCFSApiUtils.getDirStatus(dirPath);
            String logMsg = "dirPath=" + dirPath + ", jsonResult=" + jsonResult;
            JSONObject jObject = new JSONObject(jsonResult);
            boolean isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                int code = jObject.getInt("code");
                if (code == 0) {
                    JSONObject dataObj = jObject.getJSONObject("data");
                    int num_local = dataObj.getInt("num_local");
                    int num_hybrid = dataObj.getInt("num_hybrid");
                    int num_cloud = dataObj.getInt("num_cloud");
                    fileDirStatusInfo = new FileDirStatusInfo(num_local, num_hybrid, num_cloud);
                } else {
                    Logs.e(CLASSNAME, "getDirStatusInfo", logMsg);
                }
            } else {
                Logs.e(CLASSNAME, "getDirStatusInfo", logMsg);
            }
        } catch (Exception e) {
            Logs.e(CLASSNAME, "getDirStatusInfo", Log.getStackTraceString(e));
        }
        return fileDirStatusInfo;
    }

    @Nullable
    private FileDirStatusInfo getFileStatusInfo(String filePath) {
        if (filePath == null) {
            return null;
        }
        FileDirStatusInfo fileDirStatusInfo = null;
        try {
            String jsonResult = HCFSApiUtils.getFileStatus(filePath);
            String logMsg = "filePath=" + filePath + ", jsonResult=" + jsonResult;
            JSONObject jObject = new JSONObject(jsonResult);
            boolean isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                int num_local = 0;
                int num_hybrid = 0;
                int num_cloud = 0;
                int code = jObject.getInt("code");
                switch (code) {
                    case 0:
                        num_local = 1;
                        break;
                    case 1:
                        num_cloud = 1;
                        break;
                    case 2:
                        num_hybrid = 1;
                        break;
                }
                fileDirStatusInfo = new FileDirStatusInfo(num_local, num_hybrid, num_cloud);
                Logs.i(CLASSNAME, "getFileStatusInfo", logMsg);
            } else {
                Logs.e(CLASSNAME, "getFileStatusInfo", logMsg);
            }
        } catch (Exception e) {
            Logs.e(CLASSNAME, "getFileStatusInfo", Log.getStackTraceString(e));
        }
        return fileDirStatusInfo;
    }

    class FileDirStatusInfo {

        private int numLocal;
        private int numHybrid;
        private int numCloud;

        public FileDirStatusInfo(int numLocal, int numHybrid, int numCloud) {
            this.numLocal = numLocal;
            this.numHybrid = numHybrid;
            this.numCloud = numCloud;
        }

        public int getNumLocal() {
            return numLocal;
        }

        public int getNumHybrid() {
            return numHybrid;
        }

        public int getNumCloud() {
            return numCloud;
        }

    }

    /**
     * @return the size of a directory in bytes
     */
    private long getDirectorySize(File directory) {
        long dirSize = 0;
        if (directory.isDirectory()) {
            for (File dir : directory.listFiles()) {
                dirSize += getDirectorySize(dir);
            }
        } else {
            dirSize += directory.length();
        }
        return dirSize;
    }

    public void setViewHolder(AppFileFragment.RecyclerViewHolder viewHolder) {
        this.mViewHolder = viewHolder;
    }

}
