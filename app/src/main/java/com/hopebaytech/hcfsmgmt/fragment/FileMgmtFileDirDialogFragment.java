package com.hopebaytech.hcfsmgmt.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.info.FileDirInfo;
import com.hopebaytech.hcfsmgmt.info.ItemInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSApiUtils;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.UnitConverter;

import org.json.JSONObject;

import java.io.File;


/**
 * Created by Aaron on 2016/3/30.
 */
public class FileMgmtFileDirDialogFragment extends DialogFragment {

    public static final String TAG = FileMgmtFileDirDialogFragment.class.getSimpleName();
    private final String CLASSNAME = getClass().getSimpleName();
    private ItemInfo mItemInfo;
    private Thread mCalculateFileDirDataRatioThread;
    private Thread mCalculateFileDirSizeThread;

    public static FileMgmtFileDirDialogFragment newInstance() {
        return new FileMgmtFileDirDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mItemInfo instanceof FileDirInfo) {
            final FileDirInfo fileDirInfo = (FileDirInfo) mItemInfo;

            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View view = inflater.inflate(R.layout.file_mgmt_dialog_fragment_file_dir_info, null);

            ImageView fileDirIcon = (ImageView) view.findViewById(R.id.file_dir_icon);
            fileDirIcon.setImageBitmap(fileDirInfo.getIconImage());

            TextView fileDirName = (TextView) view.findViewById(R.id.file_dir_name);
            fileDirName.setText(fileDirInfo.getItemName());

            final TextView fileDirSize = (TextView) view.findViewById(R.id.file_dir_size);
            String size = String.format(getString(R.string.file_mgmt_dialog_data_size), getString(R.string.file_mgmt_dialog_calculating));
            fileDirSize.setText(size);
            mCalculateFileDirSizeThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    FileDirInfo fileDirInfo = ((FileDirInfo) mItemInfo);
                    long dirSize = getDirectorySize(fileDirInfo.getCurrentFile());
                    final String formatSize = UnitConverter.convertByteToProperUnit(dirSize);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fileDirSize.setText(String.format(getString(R.string.file_mgmt_dialog_data_size), formatSize));
                        }
                    });
                }
            });
            mCalculateFileDirSizeThread.start();

            final TextView fileDirDataRatio = (TextView) view.findViewById(R.id.file_dir_data_ratio);
            String ratio = String.format(getString(R.string.file_mgmt_dialog_local_data_ratio), getString(R.string.file_mgmt_dialog_calculating));
            fileDirDataRatio.setText(ratio);
            mCalculateFileDirDataRatioThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    final String dataRatio = getFileDirDataRatio(fileDirInfo);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fileDirDataRatio.setText(dataRatio);
                        }
                    });
                }
            });
            mCalculateFileDirDataRatioThread.start();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(view).setPositiveButton(R.string.alert_dialog_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            return builder.create();
        }
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mCalculateFileDirSizeThread != null) {
            mCalculateFileDirSizeThread.interrupt();
        }

        if (mCalculateFileDirDataRatioThread != null) {
            mCalculateFileDirDataRatioThread.interrupt();
        }

    }

    public void setItemInfo(ItemInfo mItemInfo) {
        this.mItemInfo = mItemInfo;
    }

    private String getFileDirDataRatio(FileDirInfo fileDirInfo) {
        FileDirStatusInfo fileDirStatusInfo;
        if (fileDirInfo.getCurrentFile().isDirectory()) {
            fileDirStatusInfo = getDirStatusInfo(fileDirInfo.getFilePath());
        } else {
            fileDirStatusInfo = getFileStatusInfo(fileDirInfo.getFilePath());
        }
        int numLocal = fileDirStatusInfo.getNumLocal();
        int numHybrid = fileDirStatusInfo.getNumHybrid();
        int numCloud = fileDirStatusInfo.getNumCloud();
        int numTotal = numLocal + numHybrid + numCloud;
        String ratio = numLocal + "/" + numTotal + " (local/total)";
        return String.format(getString(R.string.file_mgmt_dialog_local_data_ratio), ratio);
    }

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
                    HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getDirStatusInfo", logMsg);
                }
            } else {
                HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getDirStatusInfo", logMsg);
            }
        } catch (Exception e) {
            HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getDirStatusInfo", Log.getStackTraceString(e));
        }
        return fileDirStatusInfo;
    }

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
                HCFSMgmtUtils.log(Log.INFO, CLASSNAME, "getFileStatusInfo", logMsg);
            } else {
                HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getFileStatusInfo", logMsg);
            }
        } catch (Exception e) {
            HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getFileStatusInfo", Log.getStackTraceString(e));
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
     * Return the size of a directory in bytes
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

}
