package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.DataStatus;
import com.hopebaytech.hcfsmgmt.info.ItemInfo;
import com.hopebaytech.hcfsmgmt.main.MainActivity;
import com.hopebaytech.hcfsmgmt.utils.HCFSApiUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.UnitConverter;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.List;


/**
 * @author Aaron
 *         Created by Aaron on 2016/3/30.
 */
public class AppDialogFragment extends DialogFragment {

    public static final String TAG = AppDialogFragment.class.getSimpleName();
    private final String CLASSNAME = TAG;

    private Context mContext;
    private AppFileFragment.RecyclerViewHolder mViewHolder;

    public static AppDialogFragment newInstance() {
        return new AppDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ItemInfo itemInfo = mViewHolder.getItemInfo();
        if (!(itemInfo instanceof AppInfo)) {
            return super.onCreateDialog(savedInstanceState);
        }

        final AppInfo appInfo = (AppInfo) itemInfo;

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        final View view = inflater.inflate(R.layout.app_dialog_fragment_app_info, null);

        ImageView appIcon = (ImageView) view.findViewById(R.id.app_icon);
        appIcon.setImageBitmap(appInfo.getIconImage());

        TextView appName = (TextView) view.findViewById(R.id.app_name);
        appName.setText(appInfo.getName());

        final ImageView appPinIcon = (ImageView) view.findViewById(R.id.app_pin_icon);
        appPinIcon.setImageDrawable(appInfo.getPinUnpinImage(appInfo.isPinned()));
        appPinIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isPinned = !appInfo.isPinned();
                boolean allowPinUnpin = mViewHolder.pinUnpinItem(isPinned);
                if (allowPinUnpin) {
                    appPinIcon.setImageDrawable(appInfo.getPinUnpinImage(isPinned));
                }
            }
        });

        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(appInfo.getPackageName(), 0);
            TextView appVersion = (TextView) view.findViewById(R.id.app_version);
            String version = String.format(getString(R.string.app_file_dialog_app_version), packageInfo.versionName);
            appVersion.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            Logs.e(CLASSNAME, "onCreateDialog", Log.getStackTraceString(e));
        }

        final TextView appSize = (TextView) view.findViewById(R.id.app_size);
        String size = String.format(getString(R.string.app_file_dialog_data_size), getString(R.string.app_file_dialog_calculating));
        appSize.setText(size);
        try {
            PackageManager pm = mContext.getPackageManager();
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

                    Logs.d(CLASSNAME, "onCreateDialog", "cacheSize=" + UnitConverter.convertByteToProperUnit(pStats.cacheSize));
                    Logs.d(CLASSNAME, "onCreateDialog", "codeSize=" + UnitConverter.convertByteToProperUnit(pStats.codeSize));
                    Logs.d(CLASSNAME, "onCreateDialog", "dataSize=" + UnitConverter.convertByteToProperUnit(pStats.dataSize));
                    Logs.d(CLASSNAME, "onCreateDialog", "externalCacheSize=" + UnitConverter.convertByteToProperUnit(pStats.externalCacheSize));
                    Logs.d(CLASSNAME, "onCreateDialog", "externalCodeSize=" + UnitConverter.convertByteToProperUnit(pStats.externalCodeSize));
                    Logs.d(CLASSNAME, "onCreateDialog", "externalDataSize=" + UnitConverter.convertByteToProperUnit(pStats.externalDataSize));
                    Logs.d(CLASSNAME, "onCreateDialog", "externalMediaSize=" + UnitConverter.convertByteToProperUnit(pStats.externalMediaSize));
                    Logs.d(CLASSNAME, "onCreateDialog", "externalObbSize=" + UnitConverter.convertByteToProperUnit(pStats.externalObbSize));

                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String formatSize = UnitConverter.convertByteToProperUnit(totalSize);
                            appSize.setText(String.format(getString(R.string.app_file_dialog_data_size), formatSize));
                        }
                    });

                }
            });
        } catch (Exception e) {
            Logs.e(CLASSNAME, "onCreateDialog", Log.getStackTraceString(e));
        }

        final TextView appPkgName = (TextView) view.findViewById(R.id.app_pkg_name);
        String pkgName = String.format(getString(R.string.app_file_dialog_app_package_name), appInfo.getPackageName());
        appPkgName.setText(pkgName);

        final TextView appDataRatio = (TextView) view.findViewById(R.id.app_local_percentage);
        String ratio = String.format(getString(R.string.app_file_dialog_local_data_ratio), getString(R.string.app_file_dialog_calculating));
        appDataRatio.setText(ratio);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String ratio = getAppDataRatio(appInfo);
                ((MainActivity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        appDataRatio.setText(ratio);
                    }
                });
            }
        }).start();

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view)
                .setPositiveButton(R.string.app_file_dialog_app_open, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (appInfo.getLazyAppStatus() == DataStatus.UNAVAILABLE) {
                            Toast.makeText(mContext,
                                    R.string.app_dialog_unavailable,
                                    Toast.LENGTH_LONG)
                                    .show();
                            return;
                        }

                        Intent launchIntent = mContext.getPackageManager()
                                .getLaunchIntentForPackage(appInfo.getPackageName());
                        mContext.startActivity(launchIntent);
                    }
                })
                .setNegativeButton(R.string.app_file_dialog_app_remove, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_DELETE);
                        intent.setData(Uri.parse("package:" + appInfo.getPackageName()));
                        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                        startActivity(intent);
                    }
                });
        return builder.create();
    }

    private String getAppDataRatio(AppInfo appInfo) {

        int numLocal = 0;
        int numHybrid = 0;
        int numCloud = 0;

        DirStatusInfo sourceDirInfo = getDirStatusInfo(appInfo.getSourceDir());
        if (sourceDirInfo != null) {
            numLocal += sourceDirInfo.getNumLocal();
            numHybrid += sourceDirInfo.getNumHybrid();
            numCloud += sourceDirInfo.getNumCloud();
        }

        DirStatusInfo dataDirInfo = getDirStatusInfo(appInfo.getDataDir());
        if (dataDirInfo != null) {
            numLocal += dataDirInfo.getNumLocal();
            numHybrid += dataDirInfo.getNumHybrid();
            numCloud += dataDirInfo.getNumCloud();
        }

        List<String> externalDirList = appInfo.getExternalDirList();
        if (externalDirList != null) {
            for (String dirPath : externalDirList) {
                DirStatusInfo externalDirInfo = getDirStatusInfo(dirPath);
                numLocal += externalDirInfo.getNumLocal();
                numHybrid += externalDirInfo.getNumHybrid();
                numCloud += externalDirInfo.getNumCloud();
            }
        }

        int numTotal = numLocal + numHybrid + numCloud;
        String ratio = numLocal + "/" + numTotal + " (local/total)";
        return String.format(mContext.getString(R.string.app_file_dialog_local_data_ratio), ratio);
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
                    Logs.e(CLASSNAME, "getDirStatusInfo", logMsg);
                }
            } else {
                Logs.e(CLASSNAME, "getDirStatusInfo", logMsg);
            }
        } catch (Exception e) {
            Logs.e(CLASSNAME, "getDirStatusInfo", Log.getStackTraceString(e));
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

    public void setViewHolder(AppFileFragment.RecyclerViewHolder viewHolder) {
        this.mViewHolder = viewHolder;
    }

}
