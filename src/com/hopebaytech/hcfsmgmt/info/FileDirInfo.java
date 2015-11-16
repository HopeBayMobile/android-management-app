package com.hopebaytech.hcfsmgmt.info;

import java.io.File;
import java.util.regex.Pattern;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

public class FileDirInfo extends ItemInfo {

	public static final String MIME_TYPE_IMAGE = "image";
	public static final String MIME_TYPE_VIDEO = "video";
	public static final String MIME_TYPE_AUDIO = "audio";
	public static final String MIME_TYPE_APPLICATION_APK = "application/vnd.android.package-archive";
	private File currentFile;

	public FileDirInfo(Context context) {
		super(context);
	}

	public Object getIconImage() {
		if (currentFile.isDirectory()) {
			return ContextCompat.getDrawable(context, R.drawable.ic_folder_black);
		} else {
			String filePath = currentFile.getAbsolutePath();
			String mimeType = getMimeType();
			if (mimeType != null) {
				Log.d(HCFSMgmtUtils.TAG, "mimeType: " + mimeType);
				int width, height;
				width = height = (int) context.getResources().getDimension(R.dimen.item_image_height_width);
				if (mimeType.contains(MIME_TYPE_IMAGE)) {
					if (mimeType.contains("png")) {
						Bitmap image = BitmapFactory.decodeFile(filePath);
						Bitmap thumbImage = ThumbnailUtils.extractThumbnail(image, width, height);
//						return new BitmapDrawable(context.getResources(), thumbImage);
						return thumbImage;
					} else {
//						return new BitmapDrawable(context.getResources(), getThumbnail(absoluteFilePath));
						Bitmap thumbImage = getImageThumbnail(filePath);
						if (thumbImage == null) {
							Bitmap image = BitmapFactory.decodeFile(filePath);
							thumbImage = ThumbnailUtils.extractThumbnail(image, width, height);
						}
						return thumbImage;
					}
				} else if (mimeType.contains(MIME_TYPE_VIDEO)) {
					Bitmap thumbImage = getVideoThumbnail(filePath);
					if (thumbImage == null) {
						Bitmap image = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MICRO_KIND);
						thumbImage = ThumbnailUtils.extractThumbnail(image, width, height);
					}
					return thumbImage;
				} else if (mimeType.contains(MIME_TYPE_APPLICATION_APK)) {
					String archiveFilePath = filePath;
					PackageManager pm = context.getPackageManager();
					PackageInfo packageInfo = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
					ApplicationInfo appInfo = packageInfo.applicationInfo;
					appInfo.sourceDir = archiveFilePath;
					appInfo.publicSourceDir = archiveFilePath;
					return appInfo.loadIcon(pm);
				} else if (mimeType.contains(MIME_TYPE_AUDIO)) {
					return ContextCompat.getDrawable(context, R.drawable.ic_audio_white);
				} else {
					return ContextCompat.getDrawable(context, R.drawable.ic_file_black);
				}
			} else {
				return ContextCompat.getDrawable(context, R.drawable.ic_file_black);
			}
		}
	}
	
	private Bitmap getImageThumbnail(String path) {
		ContentResolver cr = context.getContentResolver();
		Cursor ca = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.MediaColumns._ID },
				MediaStore.MediaColumns.DATA + "=?", new String[] { path }, null);
		if (ca != null && ca.moveToFirst()) {
			int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
			ca.close();
			return MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
		}
		ca.close();
		return null;
	}
	
	private Bitmap getVideoThumbnail(String path) {
		ContentResolver cr = context.getContentResolver();
		Cursor ca = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.MediaColumns._ID },
				MediaStore.MediaColumns.DATA + "=?", new String[] { path }, null);
		if (ca != null && ca.moveToFirst()) {
			int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
			ca.close();
			return MediaStore.Video.Thumbnails.getThumbnail(cr, id, MediaStore.Video.Thumbnails.MICRO_KIND, null);
		}
		ca.close();
		return null;
	}

	public File getCurrentFile() {
		return currentFile;
	}

	public void setCurrentFile(File currentFile) {
		this.currentFile = currentFile;
	}

	public String getFilePath() {
		return currentFile.getAbsolutePath();
	}

	public String getFileName() {
		return currentFile.getName();
	}

	public String getMimeType() {
		return getMimeType(currentFile.getAbsolutePath());
	}

	public static String getMimeType(String url) {
		String type = null;
		String extension = getFileExtensionFromUrl(url);
		if (extension != null) {
			type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		}
		return type;
	}
	
	private static String getFileExtensionFromUrl(String url) {
		if (!TextUtils.isEmpty(url)) {
			int filenamePos = url.lastIndexOf('/');
			String filename = 0 <= filenamePos ? url.substring(filenamePos + 1) : url;

			// if the filename contains special characters, we don't consider it valid for our matching purposes:
			if (!filename.isEmpty() && Pattern.matches("^[^\\/:?\"<>|]+$", filename)) {
				int dotPos = filename.lastIndexOf('.');
				if (0 <= dotPos) {
					return filename.substring(dotPos + 1);
				}
			}
		}
		return "";
	}

}