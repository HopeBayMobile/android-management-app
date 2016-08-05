package com.hopebaytech.hcfsmgmt.info;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;

import java.io.File;
import java.util.regex.Pattern;

public class FileDirInfo extends ItemInfo implements Cloneable {

    private final String CLASS_NAME = this.getClass().getSimpleName();

    public static final String MIME_TYPE_IMAGE = "image";
    public static final String MIME_TYPE_VIDEO = "video";
    public static final String MIME_TYPE_AUDIO = "audio";
    public static final String MIME_TYPE_APPLICATION = "application";
    public static final String MIME_SUBTYPE_APK = "vnd.android.package-archive";
    public static final String MIME_SUBTYPE_OGG = "ogg";
    public static final String MIME_SUBTYPE_PNG = "png";
    public static final String MIME_SUBTYPE_SVG = "svg";
    private File currentFile;

    public FileDirInfo(Context context) {
        super(context);
    }

    @Nullable
    public Bitmap getIconImage() {
        Bitmap iconImage = null;
        if (currentFile.isDirectory()) {
            Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.icon_folder_default);
            iconImage = ((BitmapDrawable) drawable).getBitmap();
        } else {
            String filePath = currentFile.getAbsolutePath();
            String mimeType = getMimeType();
            String logMsg = "filePath=" + filePath + ", mimeType=" + mimeType;
            Logs.d(CLASS_NAME, "getIconImage", logMsg);
            if (mimeType != null) {
                int width, height;
                width = height = (int) mContext.getResources().getDimension(R.dimen.icon_image_width);
                try {
                    if (mimeType.startsWith(MIME_TYPE_IMAGE)) {
                        if (mimeType.contains(MIME_SUBTYPE_PNG)) {
                            /** Show PNG file with alpha supported */
                            Bitmap image = BitmapFactory.decodeFile(filePath);
                            iconImage = ThumbnailUtils.extractThumbnail(image, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                        } else if (mimeType.contains(MIME_SUBTYPE_SVG)) {
                            // TODO show svg file
                        } else {
                            Bitmap thumbImage = getImageThumbnail(filePath);
                            if (thumbImage == null) {
                                Bitmap image = BitmapFactory.decodeFile(filePath);
                                thumbImage = ThumbnailUtils.extractThumbnail(image, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                            }
                            iconImage = thumbImage;
                        }
                    } else if (mimeType.startsWith(MIME_TYPE_VIDEO)) {
                        Bitmap thumbImage = getVideoThumbnail(filePath);
                        if (thumbImage == null) {
                            Bitmap image = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MICRO_KIND);
                            thumbImage = ThumbnailUtils.extractThumbnail(image, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                        }
                        iconImage = thumbImage;
                    } else if (mimeType.startsWith(MIME_TYPE_APPLICATION)) {
                        if (mimeType.contains(MIME_SUBTYPE_APK)) {
                            String archiveFilePath = filePath;
                            PackageManager pm = mContext.getPackageManager();
                            PackageInfo packageInfo = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
                            ApplicationInfo appInfo = packageInfo.applicationInfo;
                            appInfo.sourceDir = archiveFilePath;
                            appInfo.publicSourceDir = archiveFilePath;
                            iconImage = ((BitmapDrawable) appInfo.loadIcon(pm)).getBitmap();
                        }
//						else if (mimeType.contains(MIME_SUBTYPE_OGG)) {
//							Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_audio_white);
//							return ((BitmapDrawable) drawable).getBitmap();
//							// return ContextCompat.getDrawable(mContext, R.drawable.ic_audio_white);
//						}
                    }
//					else if (mimeType.contains(MIME_TYPE_AUDIO)) {
//						Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_audio_white);
//						return ((BitmapDrawable) drawable).getBitmap();
//						// return ContextCompat.getDrawable(mContext, R.drawable.ic_audio_white);
//					}
//					Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.icon_doc_default);
//                    iconImage = ((BitmapDrawable) drawable).getBitmap();
                    // return ContextCompat.getDrawable(mContext, R.drawable.ic_file_black);
                } catch (Exception e) {
                    Logs.e(CLASS_NAME, "getIconImage", Log.getStackTraceString(e));
                }
            }
        }

        /** Unknown file type */
        if (iconImage == null) {
            Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.icon_doc_default);
            iconImage = ((BitmapDrawable) drawable).getBitmap();
        }
        return iconImage;
    }

    @Nullable
    private Bitmap getImageThumbnail(String path) {
        Bitmap thumbnail = null;
        ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.MediaColumns._ID},
                MediaStore.MediaColumns.DATA + "=?", new String[]{path}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                thumbnail = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
            }
            cursor.close();
        }
        return thumbnail;
    }

    @Nullable
    private Bitmap getVideoThumbnail(String path) {
        Bitmap thumbnail = null;
        ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.MediaColumns._ID},
                MediaStore.MediaColumns.DATA + "=?", new String[]{path}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                thumbnail = MediaStore.Video.Thumbnails.getThumbnail(cr, id, MediaStore.Video.Thumbnails.MICRO_KIND, null);
            }
            cursor.close();
        }
        return thumbnail;
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

    @Nullable
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

            /** if the filename contains special characters, we don't consider it valid for our matching purposes */
            if (!filename.isEmpty() && Pattern.matches("^[^\\/:?\"<>|]+$", filename)) {
                int dotPos = filename.lastIndexOf('.');
                if (0 <= dotPos) {
                    return filename.substring(dotPos + 1);
                }
            }
        }
        return "";
    }

    public int getFileDirStatus() {
        int locationStatus = getFileDirLocationStatus();
//        Logs.d(CLASS_NAME, "getFileDirStatus", "itemName=" + getName() + ", locationStatus=" + locationStatus);
        if (NetworkUtils.isNetworkConnected(mContext)) {
            return ItemStatus.STATUS_AVAILABLE;
        } else {
            if (locationStatus == LocationStatus.LOCAL) {
                return ItemStatus.STATUS_AVAILABLE;
            } else {
                return ItemStatus.STATUS_UNAVAILABLE;
            }
        }
    }

    private int getFileDirLocationStatus() {
        int locationStatus;
        if (currentFile.isDirectory()) {
            locationStatus = HCFSMgmtUtils.getDirLocationStatus(getFilePath());
        } else {
            locationStatus = HCFSMgmtUtils.getFileLocationStatus(getFilePath());
        }
        return locationStatus;
    }

    @Override
    public Drawable getPinUnpinImage(boolean isPinned) {
        return HCFSMgmtUtils.getPinUnpinImage(mContext, isPinned);
    }

    @Override
    public int hashCode() {
        return getFilePath().hashCode();
    }

    @Override
    public int getIconAlpha() {
        return getFileDirStatus() == ItemStatus.STATUS_AVAILABLE ? ICON_COLORFUL : ICON_TRANSPARENT;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}