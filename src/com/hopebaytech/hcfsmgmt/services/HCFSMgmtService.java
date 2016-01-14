package com.hopebaytech.hcfsmgmt.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.db.ServiceAppDAO;
import com.hopebaytech.hcfsmgmt.db.ServiceFileDirDAO;
import com.hopebaytech.hcfsmgmt.db.UidDAO;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.info.ServiceAppInfo;
import com.hopebaytech.hcfsmgmt.info.ServiceFileDirInfo;
import com.hopebaytech.hcfsmgmt.info.UidInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class HCFSMgmtService extends Service {

	private ExecutorService cacheExecutor;
	private ServiceFileDirDAO serviceFileDirDAO;
	private ServiceAppDAO serviceAppDAO;
	private UidDAO uidDAO;

	@Override
	public void onCreate() {
		super.onCreate();
		cacheExecutor = Executors.newCachedThreadPool();
		serviceFileDirDAO = new ServiceFileDirDAO(this);
		serviceAppDAO = new ServiceAppDAO(this);
		uidDAO = new UidDAO(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		if (intent != null) {
			final int operation = intent.getIntExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_NOTIFY_UPLAOD_COMPLETED);
			cacheExecutor.execute(new Runnable() {
				public void run() {
					switch (operation) {
					case HCFSMgmtUtils.INTENT_VALUE_NOTIFY_UPLAOD_COMPLETED:
						notifyUploadCompleted();
						break;
					case HCFSMgmtUtils.INTENT_VALUE_PIN_DATA_TYPE_FILE:
						pinOrUnpinDataTypeFile();
						break;
					case HCFSMgmtUtils.INTENT_VALUE_PIN_APP:
						ServiceAppInfo serviceAppInfo = new ServiceAppInfo();
						serviceAppInfo
								.setPinned(intent.getBooleanExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_PIN_STATUS, HCFSMgmtUtils.DEFAULT_PINNED_STATUS));
						serviceAppInfo.setAppName(intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_NAME));
						serviceAppInfo.setPackageName(intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PIN_PACKAGE_NAME));
						serviceAppInfo.setDataDir(intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_DATA_DIR));
						serviceAppInfo.setSourceDir(intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_SOURCE_DIR));
						serviceAppInfo.setExternalDir(intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_EXTERNAL_DIR));
						serviceAppDAO.insert(serviceAppInfo);
						pinOrUnpinApp(serviceAppInfo);
						serviceAppDAO.delete(serviceAppInfo);
						break;
					case HCFSMgmtUtils.INTENT_VALUE_PIN_FILE_DIRECTORY:
						ServiceFileDirInfo serviceFileDirInfo = new ServiceFileDirInfo();
						serviceFileDirInfo.setFilePath(intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PIN_FILE_DIR_FILEAPTH));
						serviceFileDirInfo.setPinned(
								intent.getBooleanExtra(HCFSMgmtUtils.INTENT_KEY_PIN_FILE_DIR_PIN_STATUS, HCFSMgmtUtils.DEFAULT_PINNED_STATUS));
						serviceFileDirDAO.insert(serviceFileDirInfo);
						pinOrUnpinFileOrDirectory(serviceFileDirInfo);
						serviceFileDirDAO.delete(serviceFileDirInfo.getFilePath());
						break;
					case HCFSMgmtUtils.INTENT_VALUE_LAUNCH_UID_DATABASE:
						Log.d(HCFSMgmtUtils.TAG, "Launch UID database");
						uidDAO.deleteAll();
						PackageManager pm = getPackageManager();
						List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
						for (ApplicationInfo packageInfo : packages) {
							if (!HCFSMgmtUtils.isSystemPackage(packageInfo)) {
								int uid = packageInfo.uid;
								String packageName = packageInfo.packageName;
								uidDAO.insert(new UidInfo(uid, packageName));
							}
							;
						}
						break;
					case HCFSMgmtUtils.INTENT_VALUE_ADD_UID_TO_DATABASE:
						int uid = intent.getIntExtra(HCFSMgmtUtils.INTENT_KEY_UID, -1);
						String packageName = intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PACKAGE_NAME);
						Log.d(HCFSMgmtUtils.TAG, "PACKAGE_ADDED (uid): " + uid);
						Log.d(HCFSMgmtUtils.TAG, "PACKAGE_ADDED (packageName): " + packageName);
						if (uidDAO.get(packageName) == null) {
							uidDAO.insert(new UidInfo(uid, packageName));
						}
						break;
					case HCFSMgmtUtils.INTENT_VALUE_REMOVE_UID_FROM_DATABASE:
						uid = intent.getIntExtra(HCFSMgmtUtils.INTENT_KEY_UID, -1);
						packageName = intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PACKAGE_NAME);
						Log.d(HCFSMgmtUtils.TAG, "PACKAGE_REMOVED (uid): " + uid);
						Log.d(HCFSMgmtUtils.TAG, "PACKAGE_REMOVED (packageName): " + packageName);
						if (uidDAO.get(packageName) != null) {
							uidDAO.delete(packageName);
						}
						break;
					case HCFSMgmtUtils.INTENT_VALUE_RESET_XFER:
						HCFSMgmtUtils.resetXfer();
						break;
					default:
						break;
					}
				}
			});
		} else {
			/**
			 * Service is restarted and then execute the uncompleted pin/unpin operation 
			 * when user manually close app and removes it from background.
			 */
			cacheExecutor.execute(new Runnable() {
				@Override
				public void run() {
					if (serviceFileDirDAO.getCount() > 0) {
						List<ServiceFileDirInfo> infoList = serviceFileDirDAO.getAll();
						for (final ServiceFileDirInfo info : infoList) {
							pinOrUnpinFileOrDirectory(info);
							serviceFileDirDAO.delete(info.getFilePath());
						}
					}
				}
			});
			cacheExecutor.execute(new Runnable() {
				@Override
				public void run() {
					if (serviceAppDAO.getCount() > 0) {
						List<ServiceAppInfo> infoList = serviceAppDAO.getAll();
						for (final ServiceAppInfo info : infoList) {
							pinOrUnpinApp(info);
							serviceAppDAO.delete(info);
						}
					}
				}
			});
		}
		return super.onStartCommand(intent, flags, startId);
		// return START_REDELIVER_INTENT;
	}

	private void notifyUploadCompleted() {
		Log.d(HCFSMgmtUtils.TAG, "notifyUploadCompleted");
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean notifyUploadCompletedPref = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIFY_UPLAOD_COMPLETED, true);
		if (notifyUploadCompletedPref) {
			boolean isUploadCompleted = HCFSMgmtUtils.isDataUploadCompleted();
			if (isUploadCompleted) {
				int id_notify = HCFSMgmtUtils.NOTIFY_ID_UPLOAD_COMPLETED;
				String notify_title = getString(R.string.app_name);
				String notify_content = getString(R.string.notify_upload_completed);
				HCFSMgmtUtils.notifyEvent(this, id_notify, notify_title, notify_content);
			}
		}
	}

	private void pinOrUnpinApp(ServiceAppInfo info) {
		Log.d(HCFSMgmtUtils.TAG, "pinOrUnpinApp: " + info.getAppName());
		boolean isPinned = info.isPinned();
		if (isPinned) {
			if (!HCFSMgmtUtils.pinApp(info)) {
				handleAppFailureOfPinOrUnpin(isPinned, info, getString(R.string.notify_pin_app_failure));
			}
		} else {
			if (!HCFSMgmtUtils.unpinApp(info)) {
				handleAppFailureOfPinOrUnpin(isPinned, info, getString(R.string.notify_unpin_app_failure));
			}
		}
	}

	private void handleAppFailureOfPinOrUnpin(boolean isPinned, ServiceAppInfo info, String notifyMsg) {
		Log.d(HCFSMgmtUtils.TAG, "pinOrUnpinFailure: " + info.getAppName());
		int notify_id = (int) (Math.random() * Integer.MAX_VALUE);
		String notify_title = getString(R.string.app_name);
		String notify_message = notifyMsg + ": " + info.getAppName();
		HCFSMgmtUtils.notifyEvent(this, notify_id, notify_title, notify_message);
	}

	private void pinOrUnpinDataTypeFile() {
		Log.d(HCFSMgmtUtils.TAG, "pinOrUnpinDataTypeFile");

		String notify_title = getString(R.string.app_name);
		ArrayList<String> notifyMessageList = new ArrayList<String>();
		DataTypeDAO dataTypeDAO = new DataTypeDAO(this);
		boolean isImagePinned = HCFSMgmtUtils.isDataTypePinned(dataTypeDAO, HCFSMgmtUtils.DATA_TYPE_IMAGE);
		ArrayList<String> imagePaths = HCFSMgmtUtils.getAvailableImagePaths(this);
		if (isImagePinned) {
			int imgFailedToPinCount = 0;
			for (String path : imagePaths) {
				if (!HCFSMgmtUtils.pinFileOrDirectory(path)) {
					imgFailedToPinCount++;
				}
			}
			if (imgFailedToPinCount != 0) {
				notifyMessageList.add(getString(R.string.hcfs_management_service_image_failed_to_pin) + ": " + imgFailedToPinCount);
			}
		} else {
			int imgFailedToUnpinCount = 0;
			for (String path : imagePaths) {
				if (!HCFSMgmtUtils.unpinFileOrDirectory(path)) {
					imgFailedToUnpinCount++;
				}
			}
			if (imgFailedToUnpinCount != 0) {
				notifyMessageList.add(getString(R.string.hcfs_management_service_image_failed_to_unpin) + ": " + imgFailedToUnpinCount);
			}
		}

		boolean isVideoPinned = HCFSMgmtUtils.isDataTypePinned(dataTypeDAO, HCFSMgmtUtils.DATA_TYPE_VIDEO);
		ArrayList<String> videoPaths = HCFSMgmtUtils.getAvailableVideoPaths(this);
		if (isVideoPinned) {
			int videoFailedToPinCount = 0;
			for (String path : videoPaths) {
				if (!HCFSMgmtUtils.pinFileOrDirectory(path)) {
					videoFailedToPinCount++;
				}
			}
			if (videoFailedToPinCount != 0) {
				notifyMessageList.add(getString(R.string.hcfs_management_service_video_failed_to_pin) + ": " + videoFailedToPinCount);
			}
		} else {
			int videoFailedToUnpinCount = 0;
			for (String path : videoPaths) {
				if (!HCFSMgmtUtils.unpinFileOrDirectory(path)) {
					videoFailedToUnpinCount++;
				}
			}
			if (videoFailedToUnpinCount != 0) {
				notifyMessageList.add(getString(R.string.hcfs_management_service_video_failed_to_unpin) + ": " + videoFailedToUnpinCount);
			}
		}

		boolean isAudioPinned = HCFSMgmtUtils.isDataTypePinned(dataTypeDAO, HCFSMgmtUtils.DATA_TYPE_AUDIO);
		ArrayList<String> audioPaths = HCFSMgmtUtils.getAvailableAudioPaths(this);
		if (isAudioPinned) {
			int audioFailedToPinCount = 0;
			for (String path : audioPaths) {
				if (!HCFSMgmtUtils.pinFileOrDirectory(path)) {
					audioFailedToPinCount++;
				}
			}
			if (audioFailedToPinCount != 0) {
				notifyMessageList.add(getString(R.string.hcfs_management_service_audio_failed_to_pin) + ": " + audioFailedToPinCount);
			}
		} else {
			int audioFailedToUnpinCount = 0;
			for (String path : audioPaths) {
				if (!HCFSMgmtUtils.unpinFileOrDirectory(path)) {
					audioFailedToUnpinCount++;
				}
			}
			if (audioFailedToUnpinCount != 0) {
				notifyMessageList.add(getString(R.string.hcfs_management_service_audio_failed_to_unpin) + ": " + audioFailedToUnpinCount);
			}
		}

		if (notifyMessageList.size() != 0) {
			StringBuilder notify_message = new StringBuilder();
			for (int i = 0; i < notifyMessageList.size(); i++) {
				notify_message.append(notifyMessageList.get(i));
				if (i < notifyMessageList.size() - 1) {
					notify_message.append("\n");
				}
			}
			int notify_id = HCFSMgmtUtils.NOTIFY_ID_PIN_UNPIN_FAILURE;
			HCFSMgmtUtils.notifyEvent(this, notify_id, notify_title, notify_message.toString());
		}
	}

	private void pinOrUnpinFileOrDirectory(ServiceFileDirInfo info) {
		String filePath = info.getFilePath();
		Log.d(HCFSMgmtUtils.TAG, "pinOrUnpinFileOrDrectory: " + filePath);
		boolean isPinned = info.isPinned();
		if (isPinned) {
			boolean isSuccess = HCFSMgmtUtils.pinFileOrDirectory(filePath);
			if (!isSuccess) {
				int notify_id = (int) (Math.random() * Integer.MAX_VALUE);
				String notify_title = getString(R.string.app_name);
				String notify_message = getString(R.string.notify_pin_file_dir_failure) + "： " + filePath;
				HCFSMgmtUtils.notifyEvent(this, notify_id, notify_title, notify_message);
			}
		} else {
			boolean isSuccess = HCFSMgmtUtils.unpinFileOrDirectory(filePath);
			if (!isSuccess) {
				int notify_id = (int) (Math.random() * Integer.MAX_VALUE);
				String notify_title = getString(R.string.app_name);
				String notify_message = getString(R.string.notify_unpin_file_dir_failure) + "： " + filePath;
				HCFSMgmtUtils.notifyEvent(this, notify_id, notify_title, notify_message);
			}
		}
	}

}
