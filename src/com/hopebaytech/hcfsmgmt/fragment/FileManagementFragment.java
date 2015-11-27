package com.hopebaytech.hcfsmgmt.fragment;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.AppDAO;
import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;
import com.hopebaytech.hcfsmgmt.info.FileDirInfo;
import com.hopebaytech.hcfsmgmt.info.ItemInfo;
import com.hopebaytech.hcfsmgmt.services.HCFSMgmtService;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class FileManagementFragment extends Fragment {

	public static String TAG = FileManagementFragment.class.getSimpleName();
	private Handler mUI_handler;
	private RecyclerView recyclerView;
	private RecyclerViewAdapter recyclerViewAdapter;
	private ArrayAdapter<String> spinnerAdapter;
	private Handler mThreadHandler;
	private AppDAO appDAO;
	private DataTypeDAO dataTypeDAO;
	private ProgressBar progressCircle;
	private Spinner spinner;
	private HorizontalScrollView filePathNavigationScrollView; // for file type display
	private LinearLayout filePathNavigationLayout; // for file type display
	private static File currentFile = Environment.getExternalStorageDirectory(); // for file type display
	// private int overallYScroll;

	// private int overallYScroll = 0;

	public static FileManagementFragment newInstance() {
		FileManagementFragment fragment = new FileManagementFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUI_handler = new Handler();
		recyclerViewAdapter = new RecyclerViewAdapter();
		appDAO = new AppDAO(getActivity());
		dataTypeDAO = new DataTypeDAO(getActivity());

		String[] spinner_array = getActivity().getResources().getStringArray(R.array.file_management_spinner);
		spinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.file_management_spinner, spinner_array);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		HandlerThread mThread = new HandlerThread(FileManagementFragment.class.getSimpleName());
		mThread.start();
		mThreadHandler = new Handler(mThread.getLooper());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.file_management_fragment, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
		toolbar.setTitle(getString(R.string.nav_default_mountpoint));

		View view = getView();

		progressCircle = (ProgressBar) view.findViewById(R.id.progress_circle);
		filePathNavigationLayout = (LinearLayout) view.findViewById(R.id.file_path_layout);
		filePathNavigationScrollView = (HorizontalScrollView) view.findViewById(R.id.file_path_navigation_scrollview);
		filePathNavigationScrollView.post(new Runnable() {
			@Override
			public void run() {
				filePathNavigationScrollView.fullScroll(View.FOCUS_RIGHT);
			}
		});

		// final RelativeLayout title_info_bar = (RelativeLayout) view.findViewById(R.id.title_info_bar);
		recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
		recyclerView.setAdapter(recyclerViewAdapter);
		// recyclerView.setOnScrollListener(new OnScrollListener() {
		//
		// private boolean isTitleInfoBarHide = false;
		//
		// @Override
		// public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
		// super.onScrolled(recyclerView, dx, dy);
		// overallYScroll += dy;
		// LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
		// ViewGroup.LayoutParams layoutParams = recyclerView.getLayoutParams();
		// Log.d(HCFSMgmtUtils.TAG, "recyclerView.getHeight(): " + recyclerView.getHeight());
		// recyclerView.setLayoutParams(layoutParams);
		//
		// if (overallYScroll > 0) {
		//
		// Log.d(HCFSMgmtUtils.TAG, "isHide: " + isTitleInfoBarHide);

		// if (!isTitleInfoBarHide) {
		// TranslateAnimation animate = new TranslateAnimation(0, 0, 0, -title_info_bar.getHeight());
		// animate.setAnimationListener(new Animation.AnimationListener() {
		//
		// @Override
		// public void onAnimationStart(Animation animation) {
		//
		// }
		//
		// @Override
		// public void onAnimationEnd(Animation animation) {
		// }
		//
		// @Override
		// public void onAnimationRepeat(Animation animation) {
		//
		// }
		//
		// });
		// animate.setDuration(500);
		// animate.setFillAfter(true);
		// title_info_bar.startAnimation(animate);
		// recyclerView.startAnimation(animate);
		// isTitleInfoBarHide = true;
		// }
		//
		// } else {
		// title_info_bar.setVisibility(View.VISIBLE);
		// }
		// Log.d(HCFSMgmtUtils.TAG, "dy: " + dy);
		// Log.d(HCFSMgmtUtils.TAG, "overallYScroll: " + overallYScroll);
		// }
		//
		// });

		spinner = (Spinner) view.findViewById(R.id.spinner);
		spinner.setAdapter(spinnerAdapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String itemName = parent.getSelectedItem().toString();
				if (itemName.equals(getString(R.string.file_management_spinner_apps))) {
					filePathNavigationLayout.setVisibility(View.GONE);
					showTypeContent(R.string.file_management_spinner_apps);
				} else if (itemName.equals(getString(R.string.file_management_spinner_data_type))) {
					filePathNavigationLayout.setVisibility(View.GONE);
					showTypeContent(R.string.file_management_spinner_data_type);
				} else if (itemName.equals(getString(R.string.file_management_spinner_files))) {
					filePathNavigationLayout.removeAllViews();
					FilePathNavigationView currentPathView = new FilePathNavigationView(getActivity());
					currentPathView.setText("內部儲存空間/");
					currentPathView.setCurrentFilePath(currentFile.getAbsolutePath());
					filePathNavigationLayout.addView(currentPathView);
					filePathNavigationLayout.setVisibility(View.VISIBLE);
					currentFile = Environment.getExternalStorageDirectory();
					showTypeContent(R.string.file_management_spinner_files);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		ImageView refresh = (ImageView) view.findViewById(R.id.refresh);
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String itemName = spinner.getSelectedItem().toString();
				if (itemName.equals(getString(R.string.file_management_spinner_apps))) {
					showTypeContent(R.string.file_management_spinner_apps);
				} else if (itemName.equals(getString(R.string.file_management_spinner_data_type))) {
					showTypeContent(R.string.file_management_spinner_data_type);
				} else if (itemName.equals(getString(R.string.file_management_spinner_files))) {
					showTypeContent(R.string.file_management_spinner_files);
				}

				// Toast.makeText(getActivity(), getString(R.string.file_management_snackbar_refresh), Toast.LENGTH_SHORT).show();
				Snackbar.make(getView(), getString(R.string.file_management_snackbar_refresh), Snackbar.LENGTH_SHORT).show();
			}
		});

		final ImageView display_type = (ImageView) view.findViewById(R.id.display_type);
		display_type.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PopupMenu popupMenu = new PopupMenu(getActivity(), display_type);
				popupMenu.getMenuInflater().inflate(R.menu.file_management_top_popup_menu, popupMenu.getMenu());
				popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						Log.d(HCFSMgmtUtils.TAG, item.getTitle().toString());
						return false;
					}
				});
				popupMenu.show();
			}
		});
	}

	private ArrayList<ItemInfo> getListOfDataType() {
		ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
		String[] dataTypeArray = getResources().getStringArray(R.array.file_management_list_data_types);
		for (int i = 0; i < dataTypeArray.length; i++) {
			DataTypeInfo dbDataTypeInfo = null;
			DataTypeInfo dataTypeInfo = new DataTypeInfo(getActivity());
			dataTypeInfo.setItemName(dataTypeArray[i]);
			if (dataTypeArray[i].equals(getString(R.string.file_management_list_data_type_image))) {
				dataTypeInfo.setDataType(HCFSMgmtUtils.DATA_TYPE_IMAGE);
				dataTypeInfo.setIconImage(R.drawable.ic_photo_black);
				dbDataTypeInfo = dataTypeDAO.get(HCFSMgmtUtils.DATA_TYPE_IMAGE);
			} else if (dataTypeArray[i].equals(getString(R.string.file_management_list_data_type_video))) {
				dataTypeInfo.setDataType(HCFSMgmtUtils.DATA_TYPE_VIDEO);
				dataTypeInfo.setIconImage(R.drawable.ic_video_black);
				dbDataTypeInfo = dataTypeDAO.get(HCFSMgmtUtils.DATA_TYPE_VIDEO);
			} else if (dataTypeArray[i].equals(getString(R.string.file_management_list_data_type_audio))) {
				dataTypeInfo.setDataType(HCFSMgmtUtils.DATA_TYPE_AUDIO);
				dataTypeInfo.setIconImage(R.drawable.ic_music_black);
				dbDataTypeInfo = dataTypeDAO.get(HCFSMgmtUtils.DATA_TYPE_AUDIO);
			}

			boolean isDataTypePinned = HCFSMgmtUtils.deafultPinnedStatus;
			if (dbDataTypeInfo != null) {
				isDataTypePinned = dbDataTypeInfo.isPinned();
				dataTypeInfo.setPinned(isDataTypePinned);
			} else {
				dataTypeInfo.setPinned(isDataTypePinned);
				dataTypeDAO.insert(dataTypeInfo);
			}
			items.add(dataTypeInfo);
		}
		return items;
	}

	private ArrayList<ItemInfo> getListOfFileDirs() {
		ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
		if (isExternalStorageReadable()) {
			File[] fileList = currentFile.listFiles();
			Arrays.sort(fileList);
			for (int i = 0; i < fileList.length; i++) {
				FileDirInfo fileDirInfo = new FileDirInfo(getActivity());
				File file = fileList[i];
				fileDirInfo.setItemName(file.getName());
				fileDirInfo.setCurrentFile(file);

				// String filePath = file.getAbsolutePath().replace(HCFSMgmtUtils.REPLACE_FILE_PATH_OLD, HCFSMgmtUtils.REPLACE_FILE_PATH_NEW);
				// boolean isPinned = HCFSMgmtUtils.isPathPinned(filePath);
				// fileDirInfo.setPinned(isPinned);
				items.add(fileDirInfo);
			}
		}
		return items;
	}

	public void showTypeContent(final int resource_string_id) {
		recyclerViewAdapter.clear();
		recyclerViewAdapter.notifyDataSetChanged();
		progressCircle.setVisibility(View.VISIBLE);
		mThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				ArrayList<ItemInfo> itemDatas = null;
				switch (resource_string_id) {
				case R.string.file_management_spinner_apps:
					itemDatas = getListOfInstalledApps();
					break;
				case R.string.file_management_spinner_data_type:
					itemDatas = getListOfDataType();
					break;
				case R.string.file_management_spinner_files:
					itemDatas = getListOfFileDirs();
					break;
				default:
					break;
				}
				final ArrayList<ItemInfo> items = itemDatas;
				mUI_handler.post(new Runnable() {
					@Override
					public void run() {
						recyclerViewAdapter.setItemData(items);
						recyclerViewAdapter.notifyDataSetChanged();
						if (items.size() != 0) {
							getView().findViewById(R.id.no_file_layout).setVisibility(View.GONE);
						} else {
							getView().findViewById(R.id.no_file_layout).setVisibility(View.VISIBLE);
						}
						progressCircle.setVisibility(View.GONE);
					}
				});
			}
		});
	}

	private ArrayList<ItemInfo> getListOfInstalledApps() {
		ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
		final PackageManager pm = getActivity().getPackageManager();
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		for (ApplicationInfo packageInfo : packages) {
			if (!isSystemPackage(packageInfo)) {
				AppInfo appInfo = new AppInfo(getActivity());
				appInfo.setUid(packageInfo.uid);
				appInfo.setApplicationInfo(packageInfo);
				appInfo.setItemName(packageInfo.loadLabel(pm).toString());

				AppInfo dbAppInfo = appDAO.get(packageInfo.packageName);
				if (dbAppInfo != null) {
					boolean isAppPinned = dbAppInfo.isPinned();
					appInfo.setPinned(isAppPinned);
				} else {
					appInfo.setPinned(HCFSMgmtUtils.deafultPinnedStatus);
					appDAO.insert(appInfo);
				}
				items.add(appInfo);
				// getPackageSize(pm, packageInfo);
			}
		}
		return items;
	}

	private void getAppPackageSize(PackageManager pm, ApplicationInfo packageInfo) {
		try {
			Method getPackageSizeInfo = pm.getClass().getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
			getPackageSizeInfo.invoke(pm, packageInfo.packageName, new IPackageStatsObserver.Stub() {
				@Override
				public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
					Log.d(HCFSMgmtUtils.TAG, "codeSize: " + pStats.codeSize);
				}
			});
		} catch (Exception e) {
			Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
		}
	}

	private boolean isSystemPackage(ApplicationInfo packageInfo) {
		return ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true : false;
	}

	private boolean isInstalledOnExternalStorage(ApplicationInfo packageInfo) {
		return ((packageInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) ? true : false;
	}

	public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
		private ArrayList<ItemInfo> items;
		private ThreadPoolExecutor executor;

		public RecyclerViewAdapter() {
			items = new ArrayList<ItemInfo>();

			int N = Runtime.getRuntime().availableProcessors();
			executor = new ThreadPoolExecutor(N, N * 2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
			executor.allowCoreThreadTimeOut(true);
			executor.prestartCoreThread();
		}

		public RecyclerViewAdapter(@Nullable ArrayList<ItemInfo> items) {
			this.items = (items == null) ? new ArrayList<ItemInfo>() : items;
		}

		@Override
		public int getItemCount() {
			return items.size();
		}

		@Override
		public void onBindViewHolder(final RecyclerViewHolder holder, final int position) {
			final ItemInfo item = items.get(position);
			holder.setItemInfo(item);
			holder.itemName.setText(item.getItemName());
			holder.itemName.setSelected(true);
			holder.imageView.setImageDrawable(null);
			holder.pinView.setImageDrawable(null);
			executor.execute(new Runnable() {
				@Override
				public void run() {
					Object icon = null;
					if (item instanceof AppInfo) {
						AppInfo appInfo = (AppInfo) item;
						icon = appInfo.getIconImage();
					} else if (item instanceof DataTypeInfo) {
						DataTypeInfo dataTypeInfo = (DataTypeInfo) item;
						icon = dataTypeInfo.getIconImage();
					} else if (item instanceof FileDirInfo) {
						FileDirInfo fileDirInfo = (FileDirInfo) item;
						icon = fileDirInfo.getIconImage();
					}
					final Object imageDrawable = icon;
					mUI_handler.post(new Runnable() {
						@Override
						public void run() {
							if (imageDrawable instanceof Drawable) {
								holder.imageView.setImageDrawable((Drawable) imageDrawable);
							} else if (imageDrawable instanceof Bitmap) {
								holder.imageView.setImageBitmap((Bitmap) imageDrawable);
							}
						}
					});
				}
			});
			executor.execute(new Runnable() {
				@Override
				public void run() {
					if (item instanceof AppInfo) {
						// AppInfo appInfo = (AppInfo) item;
					} else if (item instanceof DataTypeInfo) {
						// DataTypeInfo dataTypeInfo = (DataTypeInfo) item;
					} else if (item instanceof FileDirInfo) {
						FileDirInfo fileDirInfo = (FileDirInfo) item;
						boolean isPinned = HCFSMgmtUtils.isPathPinned(fileDirInfo.getFilePath());
						item.setPinned(isPinned);
					}
					mUI_handler.post(new Runnable() {
						@Override
						public void run() {
							holder.pinView.setImageDrawable(item.getPinImage());
						}
					});
				}
			});
		}

		@Override
		public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_management_single_item, parent, false);
			return new RecyclerViewHolder(view);
		}

		private void setItemData(@Nullable ArrayList<ItemInfo> items) {
			this.items = (items == null) ? new ArrayList<ItemInfo>() : items;
		}

		private void clear() {
			items.clear();
		}

	}

	public class RecyclerViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

		protected TextView itemName;
		protected ImageView imageView;
		protected ImageView pinView;
		protected ItemInfo itemInfo;

		public RecyclerViewHolder(View itemView) {
			super(itemView);
			itemName = (TextView) itemView.findViewById(R.id.itemName);
			imageView = (ImageView) itemView.findViewById(R.id.iconView);
			pinView = (ImageView) itemView.findViewById(R.id.pinView);
			pinView.setOnClickListener(this);
			itemView.setOnClickListener(this);
		}

		public void setItemInfo(ItemInfo itemInfo) {
			this.itemInfo = itemInfo;
		}

		private void setPinViewDrawable(boolean isPinned) {
			if (isPinned) {
				pinView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.pinned));
			} else {
				pinView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.unpinned));
			}
		}

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.pinView) {
				if (itemInfo instanceof AppInfo) {
					final AppInfo appInfo = (AppInfo) itemInfo;
					final boolean isPinned = !appInfo.isPinned();
					appInfo.setPinned(isPinned);
					setPinViewDrawable(isPinned);

					mThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							appDAO.update(appInfo);

							Intent intent = new Intent(getActivity(), HCFSMgmtService.class);
							intent.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_PIN_APP);
							intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_NAME, appInfo.getItemName());
							intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_PACKAGE_NAME, appInfo.getPackageName());
							intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_DATA_DIR, appInfo.getDataDir());
							intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_SOURCE_DIR, appInfo.getSourceDir());
							intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_EXTERNAL_DIR, appInfo.getExternalDir());
							intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_PIN_STATUS, appInfo.isPinned());
							getActivity().startService(intent);
						}
					});
				} else if (itemInfo instanceof DataTypeInfo) {
					final DataTypeInfo dataTypeInfo = (DataTypeInfo) itemInfo;
					final boolean isPinned = !dataTypeInfo.isPinned();
					dataTypeInfo.setPinned(isPinned);
					setPinViewDrawable(isPinned);

					mThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							dataTypeDAO.update(dataTypeInfo);
							if (isPinned) {
								HCFSMgmtUtils.startPinDataTypeFileAlarm(getActivity());
							} else {
								HCFSMgmtUtils.stopPinDataTypeFileAlarm(getActivity());
							}
						}
					});
				} else if (itemInfo instanceof FileDirInfo) {
					final FileDirInfo fileInfo = (FileDirInfo) itemInfo;
					if (fileInfo.getMimeType() != null) {
						if (fileInfo.getMimeType().contains(HCFSMgmtUtils.DATA_TYPE_IMAGE)) {
							if (HCFSMgmtUtils.isDataTypePinned(dataTypeDAO, HCFSMgmtUtils.DATA_TYPE_IMAGE)) {
								Snackbar.make(getView(), getString(R.string.file_management_not_allowed_to_pin_image), Snackbar.LENGTH_LONG).show();
								return;
							}
						} else if (fileInfo.getMimeType().contains(HCFSMgmtUtils.DATA_TYPE_VIDEO)) {
							if (HCFSMgmtUtils.isDataTypePinned(dataTypeDAO, HCFSMgmtUtils.DATA_TYPE_VIDEO)) {
								Snackbar.make(getView(), getString(R.string.file_management_not_allowed_to_pin_video), Snackbar.LENGTH_LONG).show();
								return;
							}
						} else if (fileInfo.getMimeType().contains(HCFSMgmtUtils.DATA_TYPE_AUDIO)) {
							if (HCFSMgmtUtils.isDataTypePinned(dataTypeDAO, HCFSMgmtUtils.DATA_TYPE_AUDIO)) {
								Snackbar.make(getView(), getString(R.string.file_management_not_allowed_to_pin_audio), Snackbar.LENGTH_LONG).show();
								return;
							}
						}
					}
					final boolean isPinned = !fileInfo.isPinned();
					fileInfo.setPinned(isPinned);
					setPinViewDrawable(isPinned);

					Intent intent = new Intent(getActivity(), HCFSMgmtService.class);
					intent.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_PIN_FILE_DIRECTORY);
					intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_FILE_DIR_FILEAPTH, fileInfo.getFilePath());
					intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_FILE_DIR_PIN_STATUS, fileInfo.isPinned());
					getActivity().startService(intent);
				}
			} else if (v.getId() == R.id.itemLayout) {
				if (itemInfo instanceof AppInfo) {

				} else if (itemInfo instanceof DataTypeInfo) {

				} else if (itemInfo instanceof FileDirInfo) {
					final FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
					if (fileDirInfo.getCurrentFile().isDirectory()) {
						// Set the first visible item position to previous FilePathNavigationView when navigating to next directory level
						LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
						int firstVisibleItemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
						int childCount = filePathNavigationLayout.getChildCount();
						FilePathNavigationView filePathNavigationViewPrev = (FilePathNavigationView) filePathNavigationLayout
								.getChildAt(childCount - 1);
						filePathNavigationViewPrev.setFirstVisibleItemPosition(firstVisibleItemPosition);

						// Add the current FilePathNavigationView to navigation layout
						currentFile = fileDirInfo.getCurrentFile();
						FilePathNavigationView filePathNavigationView = new FilePathNavigationView(getActivity());
						String currentPath = currentFile.getAbsolutePath();
						String navigationText = currentPath.substring(currentPath.lastIndexOf("/") + 1) + "/";
						filePathNavigationView.setText(navigationText);
						filePathNavigationView.setCurrentFilePath(currentPath);
						filePathNavigationLayout.addView(filePathNavigationView);

						// Show the file list of the entered directory
						showTypeContent(R.string.file_management_spinner_files);
					} else {
						// Build the intent
						String mimeType = fileDirInfo.getMimeType();
						Intent intent = new Intent(Intent.ACTION_VIEW);
						if (mimeType != null) {
							intent.setDataAndType(Uri.fromFile(fileDirInfo.getCurrentFile()), mimeType);
						} else {
							intent.setData(Uri.fromFile(fileDirInfo.getCurrentFile()));
						}

						// Verify it resolves
						PackageManager packageManager = getActivity().getPackageManager();
						List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
						boolean isIntentSafe = activities.size() > 0;

						// Start an activity if it's safe
						if (isIntentSafe) {
							startActivity(intent);
						} else {
							// Toast.makeText(getActivity(), getString(R.string.file_management_snackbar_unkown_type_file),
							// Toast.LENGTH_SHORT).show();
							Snackbar.make(getView(), getString(R.string.file_management_snackbar_unkown_type_file), Snackbar.LENGTH_SHORT).show();
						}
					}
				}
			}
		}

	}

	public class DividerItemDecoration extends RecyclerView.ItemDecoration {

		private final int[] ATTRS = new int[] { android.R.attr.listDivider };
		public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;
		public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;
		private Drawable mDivider;
		private int mOrientation;

		public DividerItemDecoration(Context context, int orientation) {
			final TypedArray typedArray = context.obtainStyledAttributes(ATTRS);
			mDivider = typedArray.getDrawable(0);
			typedArray.recycle();
			setOrientation(orientation);
		}

		public void setOrientation(int orientation) {
			if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
				throw new IllegalArgumentException("Invalid orientation");
			}
			mOrientation = orientation;
		}

		@Override
		public void onDraw(Canvas c, RecyclerView parent) {
			if (mOrientation == VERTICAL_LIST) {
				drawVertical(c, parent);
			} else {
				drawHorizontal(c, parent);
			}
		}

		public void drawVertical(Canvas c, RecyclerView parent) {
			final int left = parent.getPaddingLeft();
			final int right = parent.getWidth() - parent.getPaddingRight();

			final int childCount = parent.getChildCount();
			for (int i = 0; i < childCount; i++) {
				final View child = parent.getChildAt(i);
				final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
				final int top = child.getBottom() + params.bottomMargin + Math.round(ViewCompat.getTranslationY(child));
				final int bottom = top + mDivider.getIntrinsicHeight();
				mDivider.setBounds(left, top, right, bottom);
				mDivider.draw(c);
			}
		}

		public void drawHorizontal(Canvas c, RecyclerView parent) {
			final int top = parent.getPaddingTop();
			final int bottom = parent.getHeight() - parent.getPaddingBottom();

			final int childCount = parent.getChildCount();
			for (int i = 0; i < childCount; i++) {
				final View child = parent.getChildAt(i);
				final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
				final int left = child.getRight() + params.rightMargin + Math.round(ViewCompat.getTranslationX(child));
				final int right = left + mDivider.getIntrinsicHeight();
				mDivider.setBounds(left, top, right, bottom);
				mDivider.draw(c);
			}
		}

		@Override
		public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
			if (mOrientation == VERTICAL_LIST) {
				outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
			} else {
				outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
			}
		}
	}

	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (appDAO != null) {
			appDAO.close();
		}

		if (dataTypeDAO != null) {
			dataTypeDAO.close();
		}
	}

	public class FilePathNavigationView extends TextView implements OnClickListener {

		private String currentFilePath;
		private int firstVisibleItemPosition;

		public FilePathNavigationView(Context context) {
			super(context);

			int[] attrs = new int[] { android.R.attr.selectableItemBackground };
			TypedArray typedArray = context.obtainStyledAttributes(attrs);
			Drawable drawableFromTheme = typedArray.getDrawable(0);
			typedArray.recycle();
			setBackground(drawableFromTheme);

			int padding = (int) getResources().getDimension(R.dimen.fragment_vertical_padding);
			setPadding(0, padding, 0, padding);

			setTextColor(Color.WHITE);
			setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			int startIndex = filePathNavigationLayout.indexOfChild(this) + 1;
			int childCount = filePathNavigationLayout.getChildCount();
			filePathNavigationLayout.removeViews(startIndex, childCount - startIndex);

			FileManagementFragment.currentFile = new File(currentFilePath);
			showTypeContent(R.string.file_management_spinner_files);

			mUI_handler.post(new Runnable() {
				@Override
				public void run() {
					((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPosition(firstVisibleItemPosition);
				}
			});
		}

		public String getCurrentFilePath() {
			return currentFilePath;
		}

		public void setCurrentFilePath(String currentFilePath) {
			this.currentFilePath = currentFilePath;
		}

		public int getFirstVisibleItemPosition() {
			return firstVisibleItemPosition;
		}

		public void setFirstVisibleItemPosition(int firstVisibleItemPosition) {
			this.firstVisibleItemPosition = firstVisibleItemPosition;
		}

	}

	public boolean onBackPressed() {
		String selctedItemName = spinner.getSelectedItem().toString();
		if (selctedItemName.equals(getString(R.string.file_management_spinner_files))) {
			if (!currentFile.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
				getView().findViewById(R.id.no_file_layout).setVisibility(View.GONE);
				currentFile = currentFile.getParentFile();
				int childCount = filePathNavigationLayout.getChildCount();
				FilePathNavigationView filePathNavigationView = (FilePathNavigationView) filePathNavigationLayout.getChildAt(childCount - 2);
				final int firstVisibleItemPosition = filePathNavigationView.firstVisibleItemPosition;
				filePathNavigationLayout.removeViewAt(childCount - 1);
				showTypeContent(R.string.file_management_spinner_files);

				mUI_handler.post(new Runnable() {
					@Override
					public void run() {
						((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPosition(firstVisibleItemPosition);
					}
				});
				return true;
			}
		}
		return false;
	}

}
