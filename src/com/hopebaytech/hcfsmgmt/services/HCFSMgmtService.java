package com.hopebaytech.hcfsmgmt.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.db.ServiceAppDAO;
import com.hopebaytech.hcfsmgmt.db.ServiceFileDirDAO;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.info.ServiceAppInfo;
import com.hopebaytech.hcfsmgmt.info.ServiceFileDirInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class HCFSMgmtService extends Service {

	private ExecutorService cacheExecutor;
	private ServiceFileDirDAO serviceFileDirDAO;
	private ServiceAppDAO serviceAppDAO;

	@Override
	public void onCreate() {
		super.onCreate();

		cacheExecutor = Executors.newCachedThreadPool();
		serviceFileDirDAO = new ServiceFileDirDAO(this);
		serviceAppDAO = new ServiceAppDAO(this);
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
						notifyUploadCompleted(HCFSMgmtService.this);
						break;
					case HCFSMgmtUtils.INTENT_VALUE_PIN_DATA_TYPE_FILE:
						pinOrUnpinDataTypeFile(HCFSMgmtService.this);
						break;
					case HCFSMgmtUtils.INTENT_VALUE_PIN_APP:
						ServiceAppInfo serviceAppInfo = new ServiceAppInfo();
						serviceAppInfo
								.setPinned(intent.getBooleanExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_PIN_STATUS, HCFSMgmtUtils.deafultPinnedStatus));
						serviceAppInfo.setAppName(intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_NAME));
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
								intent.getBooleanExtra(HCFSMgmtUtils.INTENT_KEY_PIN_FILE_DIR_PIN_STATUS, HCFSMgmtUtils.deafultPinnedStatus));
						serviceFileDirDAO.insert(serviceFileDirInfo);
						pinOrUnpinFileOrDrectory(serviceFileDirInfo);
						serviceFileDirDAO.delete(serviceFileDirInfo.getFilePath());
						break;
					default:
						break;
					}
				}
			});
		} else {
			cacheExecutor.execute(new Runnable() {
				@Override
				public void run() {
					if (serviceFileDirDAO.getCount() > 0) {
						List<ServiceFileDirInfo> infoList = serviceFileDirDAO.getAll();
						for (final ServiceFileDirInfo info : infoList) {
							pinOrUnpinFileOrDrectory(info);
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

	private void notifyUploadCompleted(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean notifyUploadCompletedPref = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIFY_UPLAOD_COMPLETED, true);
		if (notifyUploadCompletedPref) {
			boolean isUploadCompleted = true; // need to call HCFS API for checking upload status
			if (isUploadCompleted) {
				int id_notify = HCFSMgmtUtils.NOTIFY_ID_UPLOAD_COMPLETED;
				String notify_title = getString(R.string.app_name);
				String notify_content = getString(R.string.notify_upload_completed);
				HCFSMgmtUtils.notifyEvent(context, id_notify, notify_title, notify_content);
			}
		}
	}

	private void pinOrUnpinApp(ServiceAppInfo info) {
		Log.d(HCFSMgmtUtils.TAG, "pinOrUnpinApp");
		boolean isPinned = info.isPinned();
		if (isPinned) {
			if (!HCFSMgmtUtils.pinApp(info)) {
				int notify_id = (int) (Math.random() * Integer.MAX_VALUE);
				String notify_title = getString(R.string.app_name);
				String notify_message = getString(R.string.notify_pin_app_failure) + ": " + info.getAppName();
				HCFSMgmtUtils.notifyEvent(this, notify_id, notify_title, notify_message);
			}
		} else {
			if (!HCFSMgmtUtils.unpinApp(info)) {
				int notify_id = (int) (Math.random() * Integer.MAX_VALUE);
				String notify_title = getString(R.string.app_name);
				String notify_message = getString(R.string.notify_unpin_app_failure) + ": " + info.getAppName();
				HCFSMgmtUtils.notifyEvent(this, notify_id, notify_title, notify_message);
			}
		}
	}

	private void pinOrUnpinDataTypeFile(Context context) {
		Log.d(HCFSMgmtUtils.TAG, "pinOrUnpinDataTypeFile");
		String notify_title = getString(R.string.app_name);
		DataTypeDAO dataTypeDAO = new DataTypeDAO(context);
		boolean isImagePinned = HCFSMgmtUtils.isDataTypePinned(dataTypeDAO, HCFSMgmtUtils.DATA_TYPE_IMAGE);
		ArrayList<String> imagePaths = HCFSMgmtUtils.getAvailableImagePaths(context);
		if (isImagePinned) {
			int imgFailedToPinCount = 0;
			for (String path : imagePaths) {
				if (!HCFSMgmtUtils.pinFileOrDirectory(path)) {
					imgFailedToPinCount++;
				}
			}
			int notify_id = HCFSMgmtUtils.NOTIFY_ID_IMAGE_PIN_UNPIN_FAILURE;
			String notify_message = getString(R.string.hcfs_management_service_image_failed_to_pin) + ": " + imgFailedToPinCount;
			HCFSMgmtUtils.notifyEvent(context, notify_id, notify_title, notify_message);
		} else {
			int imgFailedToUnpinCount = 0;
			for (String path : imagePaths) {
				if (!HCFSMgmtUtils.unpinFileOrDirectory(path)) {
					imgFailedToUnpinCount++;
				}
			}
			int notify_id = HCFSMgmtUtils.NOTIFY_ID_IMAGE_PIN_UNPIN_FAILURE;
			String notify_message = getString(R.string.hcfs_management_service_image_failed_to_unpin) + ": " + imgFailedToUnpinCount;
			HCFSMgmtUtils.notifyEvent(context, notify_id, notify_title, notify_message);
		}

		boolean isVideoPinned = HCFSMgmtUtils.isDataTypePinned(dataTypeDAO, HCFSMgmtUtils.DATA_TYPE_VIDEO);
		ArrayList<String> videoPaths = HCFSMgmtUtils.getAvailableVideoPaths(context);
		if (isVideoPinned) {
			int videoFailedToPinCount = 0;
			for (String path : videoPaths) {
				if (!HCFSMgmtUtils.pinFileOrDirectory(path)) {
					videoFailedToPinCount++;
				}
			}
			int notify_id = HCFSMgmtUtils.NOTIFY_ID_VIDEO_PIN_UNPIN_FAILURE;
			String notify_message = getString(R.string.hcfs_management_service_video_failed_to_pin) + ": " + videoFailedToPinCount;
			HCFSMgmtUtils.notifyEvent(context, notify_id, notify_title, notify_message);
		} else {
			int videoFailedToUnpinCount = 0;
			for (String path : videoPaths) {
				if (!HCFSMgmtUtils.unpinFileOrDirectory(path)) {
					videoFailedToUnpinCount++;
				}
			}
			int notify_id = HCFSMgmtUtils.NOTIFY_ID_VIDEO_PIN_UNPIN_FAILURE;
			String notify_message = getString(R.string.hcfs_management_service_video_failed_to_unpin) + ": " + videoFailedToUnpinCount;
			HCFSMgmtUtils.notifyEvent(context, notify_id, notify_title, notify_message);
		}

		boolean isAudioPinned = HCFSMgmtUtils.isDataTypePinned(dataTypeDAO, HCFSMgmtUtils.DATA_TYPE_AUDIO);
		ArrayList<String> audioPaths = HCFSMgmtUtils.getAvailableAudioPaths(context);
		if (isAudioPinned) {
			int audioFailedToPinCount = 0;
			for (String path : audioPaths) {
				if (!HCFSMgmtUtils.pinFileOrDirectory(path)) {
					audioFailedToPinCount++;
				}
			}
			int notify_id = HCFSMgmtUtils.NOTIFY_ID_AUDIO_PIN_UNPIN_FAILURE;
			String notify_message = getString(R.string.hcfs_management_service_audio_failed_to_pin) + ": " + audioFailedToPinCount;
			HCFSMgmtUtils.notifyEvent(context, notify_id, notify_title, notify_message);
		} else {
			int audioFailedToUnpinCount = 0;
			for (String path : audioPaths) {
				if (!HCFSMgmtUtils.unpinFileOrDirectory(path)) {
					audioFailedToUnpinCount++;
				}
			}
			int notify_id = HCFSMgmtUtils.NOTIFY_ID_AUDIO_PIN_UNPIN_FAILURE;
			String notify_message = getString(R.string.hcfs_management_service_audio_failed_to_unpin) + ": " + audioFailedToUnpinCount;
			HCFSMgmtUtils.notifyEvent(context, notify_id, notify_title, notify_message);
		}
	}

	private void pinOrUnpinFileOrDrectory(ServiceFileDirInfo info) {
		Log.d(HCFSMgmtUtils.TAG, "pinOrUnpinFileOrDrectory");
		String filePath = info.getFilePath();
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
