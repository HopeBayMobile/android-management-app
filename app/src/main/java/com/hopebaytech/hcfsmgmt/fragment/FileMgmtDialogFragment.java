package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;
import com.hopebaytech.hcfsmgmt.info.FileDirInfo;
import com.hopebaytech.hcfsmgmt.info.ItemInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSApiUtils;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.UnitConverter;

import org.json.JSONObject;

import java.lang.reflect.Method;


/**
 * Created by Aaron on 2016/3/30.
 */
public class FileMgmtDialogFragment extends DialogFragment {

    public static final String TAG = FileMgmtDialogFragment.class.getSimpleName();
    private final String CLASSNAME = getClass().getSimpleName();
    public static final int UNINSTALL_REQUEST_CODE = 100;
    private ItemInfo mItemInfo;
    private Thread mCalculateAppDataLocalPercentageThread;

    public static FileMgmtDialogFragment newInstance() {
        return new FileMgmtDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mItemInfo instanceof AppInfo) {
            final AppInfo appInfo = (AppInfo) mItemInfo;

            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View view = inflater.inflate(R.layout.file_mgmt_dialog_fragment_app_info, null);

            ImageView appIcon = (ImageView) view.findViewById(R.id.app_icon);
            appIcon.setImageBitmap(appInfo.getIconImage());

            TextView appName = (TextView) view.findViewById(R.id.app_name);
            appName.setText(appInfo.getItemName());

            try {
                PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(appInfo.getPackageName(), 0);
                TextView appVersion = (TextView) view.findViewById(R.id.app_version);
                String version = String.format(getString(R.string.file_mgmt_dialog_app_version), packageInfo.versionName);
                appVersion.setText(version);
            } catch (PackageManager.NameNotFoundException e) {
                HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "onCreateDialog", Log.getStackTraceString(e));
            }

            final TextView appSize = (TextView) view.findViewById(R.id.app_size);
            String size = String.format(getString(R.string.file_mgmt_dialog_app_size), getString(R.string.file_mgmt_dialog_calculating));
            appSize.setText(size);
            try {
                PackageManager pm = getActivity().getPackageManager();
                Method getPackageSizeInfo = pm.getClass().getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                getPackageSizeInfo.invoke(pm, appInfo.getPackageName(), new IPackageStatsObserver.Stub() {
                    @Override
                    public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                        final long totalSize = pStats.cacheSize
                                + pStats.codeSize
                                + pStats.dataSize
                                + pStats.externalCacheSize
                                + pStats.externalCodeSize
                                + pStats.externalDataSize
                                + pStats.externalMediaSize
                                + pStats.externalObbSize;

                        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onCreateDialog", "cacheSize=" + UnitConverter.convertByteToProperUnit(pStats.cacheSize));
                        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onCreateDialog", "codeSize=" + UnitConverter.convertByteToProperUnit(pStats.codeSize));
                        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onCreateDialog", "dataSize=" + UnitConverter.convertByteToProperUnit(pStats.dataSize));
                        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onCreateDialog", "externalCacheSize=" + UnitConverter.convertByteToProperUnit(pStats.externalCacheSize));
                        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onCreateDialog", "externalCodeSize=" + UnitConverter.convertByteToProperUnit(pStats.externalCodeSize));
                        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onCreateDialog", "externalDataSize=" + UnitConverter.convertByteToProperUnit(pStats.externalDataSize));
                        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onCreateDialog", "externalMediaSize=" + UnitConverter.convertByteToProperUnit(pStats.externalMediaSize));
                        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onCreateDialog", "externalObbSize=" + UnitConverter.convertByteToProperUnit(pStats.externalObbSize));

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String formatSize = UnitConverter.convertByteToProperUnit(totalSize);
                                appSize.setText(String.format(getString(R.string.file_mgmt_dialog_app_size), formatSize));
                            }
                        });

                    }
                });
            } catch (Exception e) {
                HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "onCreateDialog", Log.getStackTraceString(e));
            }

            final TextView appPkgName = (TextView) view.findViewById(R.id.app_pkg_name);
            String pkgName = String.format(getString(R.string.file_mgmt_dialog_app_package_name), appInfo.getPackageName());
            appPkgName.setText(pkgName);

            final TextView appLocalPercentage = (TextView) view.findViewById(R.id.app_local_percentage);
            String percentage = String.format(getString(R.string.file_mgmt_dialog_app_percentage), getString(R.string.file_mgmt_dialog_calculating));
            appLocalPercentage.setText(percentage);
            mCalculateAppDataLocalPercentageThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String percentage = String.format(getString(R.string.file_mgmt_dialog_app_percentage), getAppDataInLocalPercentage(appInfo));
                            appLocalPercentage.setText(percentage);
                        }
                    });
                }
            });
            mCalculateAppDataLocalPercentageThread.start();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(view)
                    .setPositiveButton(R.string.file_mgmt_dialog_app_open, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage(appInfo.getPackageName());
                            startActivity(launchIntent);
                        }
                    })
                    .setNegativeButton(R.string.file_mgmt_dialog_app_remove, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_DELETE);
                            intent.setData(Uri.parse("package:" + appInfo.getPackageName()));
                            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                            startActivityForResult(intent, UNINSTALL_REQUEST_CODE);
                        }
                    })
                    .setNeutralButton(R.string.file_mgmt_dialog_app_cancel, null);
            return builder.create();
        } else if (mItemInfo instanceof DataTypeInfo) {
            return super.onCreateDialog(savedInstanceState);
        } else if (mItemInfo instanceof FileDirInfo) {
            return super.onCreateDialog(savedInstanceState);
        } else {
            return super.onCreateDialog(savedInstanceState);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(HCFSMgmtUtils.TAG, CLASSNAME + ": onActivityResult");
        if (requestCode == UNINSTALL_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("TAG", "onActivityResult: user accepted the (un)install");
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("TAG", "onActivityResult: user canceled the (un)install");
            } else {
                Log.d("TAG", "onActivityResult: failed to (un)install");
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mCalculateAppDataLocalPercentageThread != null) {
            mCalculateAppDataLocalPercentageThread.interrupt();
        }

    }

    public void setItemInfo(ItemInfo mItemInfo) {
        this.mItemInfo = mItemInfo;
    }

    private String getAppDataInLocalPercentage(AppInfo appInfo) {
        DirStatusInfo sourceDirInfo = getDirStatusInfo(appInfo.getSourceDir());
        DirStatusInfo dataDirInfo = getDirStatusInfo(appInfo.getDataDir());
        DirStatusInfo externalDirInfo = getDirStatusInfo(appInfo.getExternalDir());

        int numLocal = 0;
        int numHybrid = 0;
        int numCloud = 0;
        if (sourceDirInfo != null) {
            numLocal += sourceDirInfo.getNumLocal();
            numHybrid += sourceDirInfo.getNumHybrid();
            numCloud += sourceDirInfo.getNumCloud();
        }
        if (dataDirInfo != null) {
            numLocal += dataDirInfo.getNumLocal();
            numHybrid += dataDirInfo.getNumHybrid();
            numCloud += dataDirInfo.getNumCloud();
        }
        if (externalDirInfo != null) {
            numLocal += externalDirInfo.getNumLocal();
            numHybrid += externalDirInfo.getNumHybrid();
            numCloud += externalDirInfo.getNumCloud();
        }

        float percentage = (numLocal / (numLocal + numHybrid + numCloud)) * 100;
        return UnitConverter.formatPercentage(percentage) + "%";
    }

    private DirStatusInfo getDirStatusInfo(String dirPath) {
        if (dirPath == null) {
            return null;
        }
        DirStatusInfo dirStatusInfo = null;
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
                    dirStatusInfo = new DirStatusInfo(num_local, num_hybrid, num_cloud);
                } else {
                    HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getDirStatusInfo", logMsg);
                }
            } else {
                HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getDirStatusInfo", logMsg);
            }
        } catch (Exception e) {
            HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getDirStatusInfo", Log.getStackTraceString(e));
        }
        return dirStatusInfo;
    }

    class DirStatusInfo {

        private int numLocal;
        private int numHybrid;
        private int numCloud;

        public DirStatusInfo(int numLocal, int numHybrid, int numCloud) {
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

}