package com.hopebaytech.hcfsmgmt.info;

import android.content.ContentResolver;
import android.content.Context;
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
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.BitmapFactoryUtils;
import com.hopebaytech.hcfsmgmt.utils.HCFSConnStatus;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.LocationStatus;
import com.hopebaytech.hcfsmgmt.utils.Logs;

public class FileInfo extends ItemInfo implements Cloneable {

    private static final String CLASSNAME = FileInfo.class.getSimpleName();

    private static final String MIME_TYPE_IMAGE = "image";
    private static final String MIME_TYPE_VIDEO = "video";
    private static final String MIME_TYPE_AUDIO = "audio";
    private static final String MIME_TYPE_APPLICATION = "application";
    private static final String MIME_SUBTYPE_APK = "vnd.android.package-archive";
    private static final String MIME_SUBTYPE_OGG = "ogg";
    private static final String MIME_SUBTYPE_PNG = "png";
    private static final String MIME_SUBTYPE_SVG = "svg";

    private boolean mIsDirectory;
    private String mFilePath;
    private long mLastModified;
    private long mSize;
    private int mHashCode;

    /**
     * NOT real time data status of this file, only be updated when {@link #getFileDirStatus()} is called.
     */
    private int mFileDataStatus;

    public FileInfo(Context context) {
        super(context);
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    public void setDirectory(boolean isDirectory) {
        mIsDirectory = isDirectory;
    }

    public long getLastModified() {
        return mLastModified;
    }

    public void setLastModified(long lastModified) {
        this.mLastModified = lastModified;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        this.mSize = size;
    }

    @Nullable
    public Bitmap getIconImage() {
        Bitmap iconImage = null;
        if (mIsDirectory) {
            Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.icon_folder_default);
            iconImage = ((BitmapDrawable) drawable).getBitmap();
        } else {
            String filePath = mFilePath;
            String mimeType = getMimeType();
            Logs.d(CLASSNAME, "getIconImage", "filePath=" + filePath + ", mimeType=" + mimeType);
            if (mimeType != null) {
                int width, height;
                width = height = (int) mContext.getResources().getDimension(R.dimen.icon_image_width);
                try {
                    if (mimeType.startsWith(MIME_TYPE_IMAGE)) {
                        if (mimeType.contains(MIME_SUBTYPE_PNG)) {
                            // Show PNG file with alpha supported
                            Bitmap image = BitmapFactoryUtils.decodeFile(filePath, width, height);
                            iconImage = ThumbnailUtils.extractThumbnail(image, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                        } else if (mimeType.contains(MIME_SUBTYPE_SVG)) {
                            // TODO show svg file
                        } else {
                            Bitmap thumbImage = getImageThumbnail(filePath);
                            if (thumbImage == null) {
                                Bitmap image = BitmapFactoryUtils.decodeFile(filePath, width, height);
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
                    Logs.e(CLASSNAME, "getIconImage", Log.getStackTraceString(e));
                }
            }
        }

        // Unknown file type
        if (iconImage == null) {
            Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.icon_doc_default);
            iconImage = ((BitmapDrawable) drawable).getBitmap();
        }
        return iconImage;
    }

    @Override
    public Drawable getIconDrawable() {
        Drawable iconDrawable = null;
        if (mIsDirectory) {
            iconDrawable = ContextCompat.getDrawable(mContext, R.drawable.icon_folder_default);
        } else {
            String filePath = mFilePath;
            String mimeType = getMimeType();
            Logs.d(CLASSNAME, "getIconDrawable", "filePath=" + filePath + ", mimeType=" + mimeType);
            if (mimeType != null) {
                int width, height;
                width = height = (int) mContext.getResources().getDimension(R.dimen.icon_image_width);
                try {
                    if (mimeType.startsWith(MIME_TYPE_IMAGE)) {
                        if (mimeType.contains(MIME_SUBTYPE_PNG)) {
                            // Show PNG file with alpha supported
                            Bitmap image = BitmapFactoryUtils.decodeFile(filePath, width, height);
                            Bitmap thumbnail = ThumbnailUtils.extractThumbnail(image, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                            iconDrawable = new BitmapDrawable(mContext.getResources(), thumbnail);
                        } else if (mimeType.contains(MIME_SUBTYPE_SVG)) {
                            // TODO show svg file
                        } else {
                            Bitmap thumbnail = getImageThumbnail(filePath);
                            if (thumbnail == null) {
                                Bitmap image = BitmapFactoryUtils.decodeFile(filePath, width, height);
                                thumbnail = ThumbnailUtils.extractThumbnail(image, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                            }
                            iconDrawable = new BitmapDrawable(mContext.getResources(), thumbnail);
                        }
                    } else if (mimeType.startsWith(MIME_TYPE_VIDEO)) {
                        Bitmap thumbnail = getVideoThumbnail(filePath);
                        if (thumbnail == null) {
                            Bitmap image = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MICRO_KIND);
                            thumbnail = ThumbnailUtils.extractThumbnail(image, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                        }
                        iconDrawable = new BitmapDrawable(mContext.getResources(), thumbnail);
                    } else if (mimeType.startsWith(MIME_TYPE_APPLICATION)) {
                        if (mimeType.contains(MIME_SUBTYPE_APK)) {
                            String archiveFilePath = filePath;
                            PackageManager pm = mContext.getPackageManager();
                            PackageInfo packageInfo = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
                            ApplicationInfo appInfo = packageInfo.applicationInfo;
                            appInfo.sourceDir = archiveFilePath;
                            appInfo.publicSourceDir = archiveFilePath;
                            iconDrawable = appInfo.loadIcon(pm);
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
                    Logs.e(CLASSNAME, "getIconImage", Log.getStackTraceString(e));
                }
            }
        }

        // Unknown file type
        if (iconDrawable == null) {
            iconDrawable = ContextCompat.getDrawable(mContext, R.drawable.icon_doc_default);
        } else {
            if (((BitmapDrawable) iconDrawable).getBitmap() == null) {
                iconDrawable = ContextCompat.getDrawable(mContext, R.drawable.icon_doc_default);
            }
        }
        return iconDrawable;
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

    public String getFilePath() {
        return mFilePath;
    }

    public String getFileName() {
        return getName();
    }

    public String getMimeType() {
        return getMimeType(mFilePath);
    }

    @Nullable
    private String getMimeType(String path) {
        String extension = getFileExtension(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
    }

    private static String getFileExtension(String path) {
        if (!TextUtils.isEmpty(path)) {
            int filenamePos = path.lastIndexOf('/');
            String filename = filenamePos >=  0 ? path.substring(filenamePos + 1) : path;
            int dotPos = filename.lastIndexOf('.');
            if (dotPos >= 0) {
                return filename.substring(dotPos + 1);
            }
        }
        return "";
    }

    private int getFileDirStatus() {
        int fileDataStatus;
        int locationStatus = getFileDirLocationStatus();
        if (HCFSConnStatus.isAvailable(mContext, HCFSMgmtUtils.getHCFSStatInfo())) {
            fileDataStatus = DataStatus.AVAILABLE;
        } else {
            if (locationStatus == LocationStatus.LOCAL) {
                fileDataStatus = DataStatus.AVAILABLE;
            } else {
                fileDataStatus = DataStatus.UNAVAILABLE;
            }
        }
        mFileDataStatus = fileDataStatus;
        return fileDataStatus;
    }

    /**
     * @return The data status of this file, but the data status is not guaranteed to be correct.
     */
    public int getLayzyDataStatus() {
        return mFileDataStatus;
    }

    private int getFileDirLocationStatus() {
        int locationStatus;
        if (mIsDirectory) {
            locationStatus = HCFSMgmtUtils.getDirLocationStatus(getFilePath());
        } else {
            locationStatus = HCFSMgmtUtils.getFileLocationStatus(getFilePath());
        }
        return locationStatus;
    }

    @Override
    public int hashCode() {
        if (mHashCode == 0) {
            mHashCode = getFilePath().hashCode();
        }
        return mHashCode;
    }

    @Override
    public int getIconAlpha() {
        return getFileDirStatus() == DataStatus.AVAILABLE ? ICON_COLORFUL : ICON_TRANSPARENT;
    }

    public boolean isDirectory() {
        return mIsDirectory;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
