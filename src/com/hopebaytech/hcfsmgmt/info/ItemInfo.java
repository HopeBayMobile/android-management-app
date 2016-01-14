package com.hopebaytech.hcfsmgmt.info;

import com.hopebaytech.hcfsmgmt.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

public class ItemInfo {

	public static final int DATA_STATUS_CLOUD = 0;
	public static final int DATA_STATUS_HYBRID = 1;
	public static final int DATA_STATUS_LOCAL = 2;

	protected Context context;
//	protected BaseFileMgmtFragment baseFileMgmtFragment;
	private boolean isPinned;
	private int dataStatus;
	private String infoName;
	
	public ItemInfo(Context context) {
		this.context = context;
	}

//	public ItemInfo(Context context, BaseFileMgmtFragment baseFileMgmtFragment) {
//		this.context = context;
//		this.baseFileMgmtFragment = baseFileMgmtFragment;		
//	}

	public String getItemName() {
		return infoName;
	}

	public void setItemName(String infoName) {
		this.infoName = infoName;
	}

	public Drawable getPinImage(int status) {
		Drawable pinDrawable = null;
//		if (isPinned) {
//			pinDrawable = ContextCompat.getDrawable(context, R.drawable.pinned);
//		} else {
//			pinDrawable = ContextCompat.getDrawable(context, R.drawable.unpinned);
//		}
		if (isPinned) {
			if (status == FileStatus.LOCAL) {
				pinDrawable = ContextCompat.getDrawable(context, R.drawable.pinned);
			} else if (status == FileStatus.HYBRID || status == FileStatus.CLOUD) {
				pinDrawable = ContextCompat.getDrawable(context, R.drawable.pinning);
			} else {
				// pinDrawable = ContextCompat.getDrawable(context, R.drawable.default);
			}
		} else {
			switch (status) {
			case FileStatus.LOCAL:
				// TODO local image
				break;
			case FileStatus.HYBRID:
				// TODO hybrid image
				break;
			case FileStatus.CLOUD:
				// TODO cloud image
				break;
			default:
				// pinDrawable = ContextCompat.getDrawable(context, R.drawable.default);
				break;
			}
			pinDrawable = ContextCompat.getDrawable(context, R.drawable.unpinned);
		}
		return pinDrawable;
	}

	public boolean isPinned() {
		return isPinned;
	}

	public void setPinned(boolean isPinned) {
		this.isPinned = isPinned;
	}

//	public int getDataStatus() {
//		return dataStatus;
//	}
//
//	public void setDataStatus(int dataStatus) {
//		this.dataStatus = dataStatus;
//	}

//	protected void onFileDirPinUnpinClick(final FileDirInfo fileInfo, PinUnpinIconChanger iconChanger) {
//		iconChanger.setPinUnpinIcon();
//
//		Intent intent = new Intent(context, HCFSMgmtService.class);
//		intent.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_PIN_FILE_DIRECTORY);
//		intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_FILE_DIR_FILEAPTH, fileInfo.getFilePath());
//		intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_FILE_DIR_PIN_STATUS, fileInfo.isPinned());
//		context.startService(intent);
//	}

//	public void onLinearPinViewClick(Handler mThreadHandler, final ImageView pinView) {
//		ItemInfo itemInfo = this;
//		if (itemInfo instanceof AppInfo) {
//			final AppInfo appInfo = (AppInfo) itemInfo;
//			onAppPinUnpinClick(appInfo, new PinUnpinIconChanger() {
//				@Override
//				public void setPinUnpinIcon() {
//					// Below code should be executed inside setPinUnpinIcon()
//					final boolean isPinned = !appInfo.isPinned();
//					appInfo.setPinned(isPinned);
//					setLinearItemPinUnpinIcon(isPinned, pinView);
//				}
//			}, mThreadHandler);
//		} else if (itemInfo instanceof DataTypeInfo) {
//			final DataTypeInfo dataTypeInfo = (DataTypeInfo) itemInfo;
//			onDataTypePinUnpinClick(dataTypeInfo, new PinUnpinIconChanger() {
//
//				@Override
//				public void setPinUnpinIcon() {
//					// Below code should be executed inside setPinUnpinIcon()
//					final boolean isPinned = !dataTypeInfo.isPinned();
//					dataTypeInfo.setPinned(isPinned);
//					setLinearItemPinUnpinIcon(isPinned, pinView);
//				}
//			}, mThreadHandler);
//		} else if (itemInfo instanceof FileDirInfo) {
//
//			final FileDirInfo fileInfo = (FileDirInfo) itemInfo;
//
//			onFileDirPinUnpinClick(fileInfo, new PinUnpinIconChanger() {
//				@Override
//				public void setPinUnpinIcon() {
//					// Below code should be executed inside setPinUnpinIcon()
//					final boolean isPinned = !fileInfo.isPinned();
//					fileInfo.setPinned(isPinned);
//					setLinearItemPinUnpinIcon(isPinned, pinView);
//				}
//			});
//		}
//
//	}
	
//	private void setLinearItemPinUnpinIcon(boolean isPinned, ImageView pinView) {
//		if (isPinned) {
//			pinView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pinned));
//		} else {
//			pinView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.unpinned));
//		}
//	}

//	public void onLinearLayoutViewClick(RecyclerView mRecyclerView, LinearLayout mFilePathNavigationLayout,
//			final HorizontalScrollView mFilePathNavigationScrollView, File mCurrentFile) {
//		ItemInfo itemInfo = this;
//		if (itemInfo instanceof AppInfo) {
//
//		} else if (itemInfo instanceof DataTypeInfo) {
//
//		} else if (itemInfo instanceof FileDirInfo) {
//			final FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
//			if (fileDirInfo.getCurrentFile().isDirectory()) {
//				// Set the first visible item position to previous FilePathNavigationView when navigating to next directory level
//				LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
//				int firstVisibleItemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
//				int childCount = mFilePathNavigationLayout.getChildCount();
//				FilePathNavigationView filePathNavigationViewPrev = (FilePathNavigationView) mFilePathNavigationLayout.getChildAt(childCount - 1);
//				filePathNavigationViewPrev.setFirstVisibleItemPosition(firstVisibleItemPosition);
//
//				// Add the current FilePathNavigationView to navigation layout
//				mCurrentFile = fileDirInfo.getCurrentFile();
//				FilePathNavigationView filePathNavigationView = new FilePathNavigationView(context, mRecyclerView, mFilePathNavigationLayout);
//				String currentPath = mCurrentFile.getAbsolutePath();
//				String navigationText = currentPath.substring(currentPath.lastIndexOf("/") + 1) + "/";
//				filePathNavigationView.setText(navigationText);
//				filePathNavigationView.setCurrentFilePath(currentPath);
//				mFilePathNavigationLayout.addView(filePathNavigationView);
//				mFilePathNavigationScrollView.post(new Runnable() {
//					public void run() {
//						mFilePathNavigationScrollView.fullScroll(View.FOCUS_RIGHT);
//					}
//				});
//
//				// Show the file list of the entered directory
//				BaseFileMgmtFragment.showTypeContent(context, new FileManager() {
//					@Override
//					public ArrayList<ItemInfo> getListofItems() {
//						return BaseFileMgmtFragment.getListOfFileDirs(context);
//					}
//				});
//			} else {
//				// Build the intent
//				String mimeType = fileDirInfo.getMimeType();
//				Intent intent = new Intent(Intent.ACTION_VIEW);
//				if (mimeType != null) {
//					intent.setDataAndType(Uri.fromFile(fileDirInfo.getCurrentFile()), mimeType);
//				} else {
//					intent.setData(Uri.fromFile(fileDirInfo.getCurrentFile()));
//				}
//
//				// Verify it resolves
//				PackageManager packageManager = context.getPackageManager();
//				List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//				boolean isIntentSafe = activities.size() > 0;
//
//				// Start an activity if it's safe
//				if (isIntentSafe) {
//					context.startActivity(intent);
//				} else {
//					Snackbar.make(baseFileMgmtFragment.getView(), context.getString(R.string.file_management_snackbar_unkown_type_file), Snackbar.LENGTH_SHORT).show();
//				}
//			}
//		}
//	}

//	private void onAppPinUnpinClick(final AppInfo appInfo, PinUnpinIconChanger iconChanger, Handler mThreadHandler) {
//		iconChanger.setPinUnpinIcon();
//		mThreadHandler.post(new Runnable() {
//			@Override
//			public void run() {
//				AppDAO mAppDAO = new AppDAO(context);
//				mAppDAO.update(appInfo);
//				mAppDAO.close();
//
//				Intent intent = new Intent(context, HCFSMgmtService.class);
//				intent.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_PIN_APP);
//				intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_NAME, appInfo.getItemName());
//				intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_PACKAGE_NAME, appInfo.getPackageName());
//				intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_DATA_DIR, appInfo.getDataDir());
//				intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_SOURCE_DIR, appInfo.getSourceDir());
//				intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_EXTERNAL_DIR, appInfo.getExternalDir());
//				intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_PIN_STATUS, appInfo.isPinned());
//				context.startService(intent);
//			}
//		});
//	}

//	private void onDataTypePinUnpinClick(final DataTypeInfo dataTypeInfo, PinUnpinIconChanger iconChanger, Handler mThreadHandler) {
//		iconChanger.setPinUnpinIcon();
//		mThreadHandler.post(new Runnable() {
//			@Override
//			public void run() {
//				DataTypeDAO mDataTypeDAO = new DataTypeDAO(context);
//				mDataTypeDAO.update(dataTypeInfo);
//				mDataTypeDAO.close();
//
//				if (dataTypeInfo.isPinned()) {
//					HCFSMgmtUtils.startPinDataTypeFileAlarm(context);
//				} else {
//					HCFSMgmtUtils.stopPinDataTypeFileAlarm(context);
//				}
//			}
//		});
//	}
	
//	public Object getIconImage() {
//		Object icon = null;
//		ItemInfo itemInfo = this;
//		if (itemInfo instanceof AppInfo) {
//			AppInfo appInfo = (AppInfo) itemInfo;
//			icon = appInfo.getIconImage();
//		} else if (itemInfo instanceof DataTypeInfo) {
//			DataTypeInfo dataTypeInfo = (DataTypeInfo) itemInfo;
//			icon = dataTypeInfo.getIconImage();
//		} else if (itemInfo instanceof FileDirInfo) {
//			FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
//			icon = fileDirInfo.getIconImage();
//		}
//		return icon;
//	}
	
//	public void onMenuItemClick(Handler mThreadHandler, final Boolean isPinned, final ImageView pinImageView) {
//		final ItemInfo itemInfo = this;
//		if (itemInfo instanceof AppInfo) {
//			onAppPinUnpinClick((AppInfo) itemInfo, new PinUnpinIconChanger() {
//				@Override
//				public void setPinUnpinIcon() {
//					// Below code should be executed inside setPinUnpinIcon()
//					itemInfo.setPinned(isPinned);
//					setGridItemPinUnpinIcon(isPinned, pinImageView);
//				}
//			}, mThreadHandler);
//		} else if (itemInfo instanceof DataTypeInfo) {
//			onDataTypePinUnpinClick((DataTypeInfo) itemInfo, new PinUnpinIconChanger() {
//				@Override
//				public void setPinUnpinIcon() {
//					// Below code should be executed inside setPinUnpinIcon()
//					itemInfo.setPinned(isPinned);
//					setGridItemPinUnpinIcon(isPinned, pinImageView);
//				}
//			}, mThreadHandler);
//		} else if (itemInfo instanceof FileDirInfo) {
//			onFileDirPinUnpinClick((FileDirInfo) itemInfo, new PinUnpinIconChanger() {
//				@Override
//				public void setPinUnpinIcon() {
//					// Below code should be executed inside setPinUnpinIcon()
//					itemInfo.setPinned(isPinned);
//					setGridItemPinUnpinIcon(isPinned, pinImageView);
//				}
//			});
//		}
//	}
	
//	private void setGridItemPinUnpinIcon(boolean isPinned, ImageView pinImageView) {
//		if (isPinned) {
//			pinImageView.setVisibility(View.VISIBLE);
//		} else {
//			pinImageView.setVisibility(View.GONE);
//		}
//	}
	
//	public void onGridViewHolderClick(RecyclerView mRecyclerView, LinearLayout mFilePathNavigationLayout,
//			final HorizontalScrollView mFilePathNavigationScrollView, File mCurrentFile) {
//		ItemInfo itemInfo = this;
//		if (itemInfo instanceof AppInfo) {
//
//		} else if (itemInfo instanceof DataTypeInfo) {
//
//		} else if (itemInfo instanceof FileDirInfo) {
//			final FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
//			if (fileDirInfo.getCurrentFile().isDirectory()) {
//				// Set the first visible item position to previous FilePathNavigationView when navigating to next directory level
//				GridLayoutManager gridLayoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
//				int firstVisibleItemPosition = gridLayoutManager.findFirstCompletelyVisibleItemPosition();
//				int childCount = mFilePathNavigationLayout.getChildCount();
//				FilePathNavigationView filePathNavigationViewPrev = (FilePathNavigationView) mFilePathNavigationLayout
//						.getChildAt(childCount - 1);
//				filePathNavigationViewPrev.setFirstVisibleItemPosition(firstVisibleItemPosition);
//
//				// Add the current FilePathNavigationView to navigation layout
//				mCurrentFile = fileDirInfo.getCurrentFile();
//				FilePathNavigationView filePathNavigationView = new FilePathNavigationView(context, mRecyclerView, mFilePathNavigationLayout);
//				String currentPath = mCurrentFile.getAbsolutePath();
//				String navigationText = currentPath.substring(currentPath.lastIndexOf("/") + 1) + "/";
//				filePathNavigationView.setText(navigationText);
//				filePathNavigationView.setCurrentFilePath(currentPath);
//				mFilePathNavigationLayout.addView(filePathNavigationView);
//				mFilePathNavigationScrollView.post(new Runnable() {
//					public void run() {
//						mFilePathNavigationScrollView.fullScroll(View.FOCUS_RIGHT);
//					}
//				});
//
//				// Show the file list of the entered directory
//				BaseFileMgmtFragment.showTypeContent(context, new FileManager() {
//					@Override
//					public ArrayList<ItemInfo> getListofItems() {
//						return BaseFileMgmtFragment.getListOfFileDirs(context);
//					}
//				});
//			} else {
//				// Build the intent
//				String mimeType = fileDirInfo.getMimeType();
//				Intent intent = new Intent(Intent.ACTION_VIEW);
//				if (mimeType != null) {
//					intent.setDataAndType(Uri.fromFile(fileDirInfo.getCurrentFile()), mimeType);
//				} else {
//					intent.setData(Uri.fromFile(fileDirInfo.getCurrentFile()));
//				}
//
//				// Verify it resolves
//				PackageManager packageManager = context.getPackageManager();
//				List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//				boolean isIntentSafe = activities.size() > 0;
//
//				// Start an activity if it's safe
//				if (isIntentSafe) {
//					context.startActivity(intent);
//				} else {
//					Snackbar.make(baseFileMgmtFragment.getView(), context.getString(R.string.file_management_snackbar_unkown_type_file), Snackbar.LENGTH_SHORT).show();
//				}
//			}
//		}
//	}
	
//	public void setItemPinnedStatus() {
//		ItemInfo itemInfo = this;
//		if (itemInfo instanceof AppInfo) {
//			// AppInfo appInfo = (AppInfo) item;
//		} else if (itemInfo instanceof DataTypeInfo) {
//			// DataTypeInfo dataTypeInfo = (DataTypeInfo) item;
//		} else if (itemInfo instanceof FileDirInfo) {
//			FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
//			boolean isPinned = HCFSMgmtUtils.isPathPinned(fileDirInfo.getFilePath());
//			itemInfo.setPinned(isPinned);
//		}
//	}

}