package com.hopebaytech.hcfsmgmt.fragment;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.AppDAO;
import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.fragment.FileManagementFragment.GridRecyclerViewAdapter.GridRecyclerViewHolder;
import com.hopebaytech.hcfsmgmt.fragment.FileManagementFragment.LinearRecyclerViewAdapter.LinearRecyclerViewHolder;
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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
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
	private static File mCurrentFile = Environment.getExternalStorageDirectory(); // for file type display
	private RecyclerView mRecyclerView;
	private SectionedRecyclerViewAdapter mSectionedRecyclerViewAdapter;
	private RecyclerView.Adapter mRecyclerViewAdapter;
	private DividerItemDecoration mDividerItemDecoration;
	private ArrayAdapter<String> mSpinnerAdapter;
	private Handler mThreadHandler;
	private AppDAO mAppDAO;
	private DataTypeDAO mDataTypeDAO;
	private ProgressBar mProgressCircle;
	private Spinner mSpinner;
	private HorizontalScrollView mFilePathNavigationScrollView; // for file type display
	private LinearLayout mFilePathNavigationLayout; // for file type display

	private final int GRID_LAYOUT_SPAN_COUNT = 3;

	private enum DISPLAY_TYPE {
		GRID, LINEAR
	};

	private DISPLAY_TYPE mDisplayType = DISPLAY_TYPE.GRID;

	public static FileManagementFragment newInstance() {
		FileManagementFragment fragment = new FileManagementFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		switch (mDisplayType) {
		case LINEAR:
			mRecyclerViewAdapter = new LinearRecyclerViewAdapter();
			break;
		case GRID:
			mRecyclerViewAdapter = new GridRecyclerViewAdapter();
			break;
		}
		mDividerItemDecoration = new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL);
		// sectionedRecyclerViewAdapter = new SectionedRecyclerViewAdapter(recyclerViewAdapter); // TODO
		// sectionedRecyclerViewAdapter.setSections(new Section[] { new Section(0) }); // TODO

		mAppDAO = new AppDAO(getActivity());
		mDataTypeDAO = new DataTypeDAO(getActivity());

		String[] spinner_array = getActivity().getResources().getStringArray(R.array.file_management_spinner);
		mSpinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.file_management_spinner, spinner_array);
		mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

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
		mProgressCircle = (ProgressBar) view.findViewById(R.id.progress_circle);
		mFilePathNavigationLayout = (LinearLayout) view.findViewById(R.id.file_path_layout);
		mFilePathNavigationScrollView = (HorizontalScrollView) view.findViewById(R.id.file_path_navigation_scrollview);

		mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		// recyclerView.setHasFixedSize(true);
		mSectionedRecyclerViewAdapter = new SectionedRecyclerViewAdapter(mRecyclerViewAdapter); // TODO
		mSectionedRecyclerViewAdapter.setSections(new Section[] { new Section(0) }); // TODO
		switch (mDisplayType) {
		case LINEAR:
			mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
			break;
		case GRID:
			mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), GRID_LAYOUT_SPAN_COUNT));
			mSectionedRecyclerViewAdapter.setGridLayoutManagerSpanSize();
			break;
		}
		mRecyclerView.setAdapter(mSectionedRecyclerViewAdapter);

		mSpinner = (Spinner) view.findViewById(R.id.spinner);
		mSpinner.setAdapter(mSpinnerAdapter);
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String itemName = parent.getSelectedItem().toString();
				if (itemName.equals(getString(R.string.file_management_spinner_apps))) {
					mFilePathNavigationLayout.setVisibility(View.GONE);
					showTypeContent(R.string.file_management_spinner_apps);
				} else if (itemName.equals(getString(R.string.file_management_spinner_data_type))) {
					mFilePathNavigationLayout.setVisibility(View.GONE);
					showTypeContent(R.string.file_management_spinner_data_type);
				} else if (itemName.equals(getString(R.string.file_management_spinner_files))) {
					mFilePathNavigationLayout.removeAllViews();
					FilePathNavigationView currentPathView = new FilePathNavigationView(getActivity());
					currentPathView.setText("內部儲存空間/");
					currentPathView.setCurrentFilePath(mCurrentFile.getAbsolutePath());
					mFilePathNavigationLayout.addView(currentPathView);
					mFilePathNavigationLayout.setVisibility(View.VISIBLE);
					mCurrentFile = Environment.getExternalStorageDirectory();
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
				String itemName = mSpinner.getSelectedItem().toString();
				if (itemName.equals(getString(R.string.file_management_spinner_apps))) {
					showTypeContent(R.string.file_management_spinner_apps);
				} else if (itemName.equals(getString(R.string.file_management_spinner_data_type))) {
					showTypeContent(R.string.file_management_spinner_data_type);
				} else if (itemName.equals(getString(R.string.file_management_spinner_files))) {
					showTypeContent(R.string.file_management_spinner_files);
				}
				Snackbar.make(getView(), getString(R.string.file_management_snackbar_refresh), Snackbar.LENGTH_SHORT).show();
			}
		});

		final ImageView displayType = (ImageView) view.findViewById(R.id.display_type);
		displayType.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PopupMenu popupMenu = new PopupMenu(getActivity(), displayType);
				popupMenu.getMenuInflater().inflate(R.menu.file_management_top_popup_menu, popupMenu.getMenu());
				popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						if (item.getItemId() == R.id.menu_list) {
							if (mDisplayType == DISPLAY_TYPE.LINEAR) {
								return false;
							}
							mDisplayType = DISPLAY_TYPE.LINEAR;
							((GridRecyclerViewAdapter) mRecyclerViewAdapter).shutdownExcecutor();
							ArrayList<ItemInfo> itemInfos = ((GridRecyclerViewAdapter) mRecyclerViewAdapter).getItems();
							mRecyclerViewAdapter = new LinearRecyclerViewAdapter(itemInfos);
							mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//							mRecyclerView.addItemDecoration(mDividerItemDecoration); // TODO
						} else if (item.getItemId() == R.id.menu_grid) {
							if (mDisplayType == DISPLAY_TYPE.GRID) {
								return false;
							}
							mDisplayType = DISPLAY_TYPE.GRID;
							((LinearRecyclerViewAdapter) mRecyclerViewAdapter).shutdownExcecutor();
							ArrayList<ItemInfo> itemInfos = ((LinearRecyclerViewAdapter) mRecyclerViewAdapter).getItems();
							mRecyclerViewAdapter = new GridRecyclerViewAdapter(itemInfos);
							mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), GRID_LAYOUT_SPAN_COUNT));
//							mRecyclerView.removeItemDecoration(mDividerItemDecoration); // TODO
							mSectionedRecyclerViewAdapter.setGridLayoutManagerSpanSize();
						}
						mSectionedRecyclerViewAdapter.setBaseAdapter(mRecyclerViewAdapter);
						mRecyclerView.setAdapter(mSectionedRecyclerViewAdapter);
						mRecyclerViewAdapter.notifyDataSetChanged();
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
				dbDataTypeInfo = mDataTypeDAO.get(HCFSMgmtUtils.DATA_TYPE_IMAGE);
			} else if (dataTypeArray[i].equals(getString(R.string.file_management_list_data_type_video))) {
				dataTypeInfo.setDataType(HCFSMgmtUtils.DATA_TYPE_VIDEO);
				dataTypeInfo.setIconImage(R.drawable.ic_video_black);
				dbDataTypeInfo = mDataTypeDAO.get(HCFSMgmtUtils.DATA_TYPE_VIDEO);
			} else if (dataTypeArray[i].equals(getString(R.string.file_management_list_data_type_audio))) {
				dataTypeInfo.setDataType(HCFSMgmtUtils.DATA_TYPE_AUDIO);
				dataTypeInfo.setIconImage(R.drawable.ic_music_black);
				dbDataTypeInfo = mDataTypeDAO.get(HCFSMgmtUtils.DATA_TYPE_AUDIO);
			}

			boolean isDataTypePinned = HCFSMgmtUtils.deafultPinnedStatus;
			if (dbDataTypeInfo != null) {
				isDataTypePinned = dbDataTypeInfo.isPinned();
				dataTypeInfo.setPinned(isDataTypePinned);
			} else {
				dataTypeInfo.setPinned(isDataTypePinned);
				mDataTypeDAO.insert(dataTypeInfo);
			}
			items.add(dataTypeInfo);
		}
		return items;
	}

	private ArrayList<ItemInfo> getListOfFileDirs() {
		ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
		if (isExternalStorageReadable()) {
			File[] fileList = mCurrentFile.listFiles();
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

		switch (mDisplayType) {
		case LINEAR:
			((LinearRecyclerViewAdapter) mRecyclerViewAdapter).clear();
			break;
		case GRID:
			((GridRecyclerViewAdapter) mRecyclerViewAdapter).clear();
			break;
		}
		mRecyclerViewAdapter.notifyDataSetChanged();
		mProgressCircle.setVisibility(View.VISIBLE);
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
				}
				final ArrayList<ItemInfo> itemInfos = itemDatas;
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						switch (mDisplayType) {
						case LINEAR:
							((LinearRecyclerViewAdapter) mRecyclerViewAdapter).setItemData(itemInfos);
							break;
						case GRID:
							((GridRecyclerViewAdapter) mRecyclerViewAdapter).setItemData(itemInfos);
							break;
						}
						mSectionedRecyclerViewAdapter.setBaseAdapter(mRecyclerViewAdapter);
						mRecyclerView.setAdapter(mSectionedRecyclerViewAdapter);
						mRecyclerViewAdapter.notifyDataSetChanged();
						if (itemInfos.size() != 0) {
							getView().findViewById(R.id.no_file_layout).setVisibility(View.GONE);
						} else {
							getView().findViewById(R.id.no_file_layout).setVisibility(View.VISIBLE);
						}
						mProgressCircle.setVisibility(View.GONE);
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

				AppInfo dbAppInfo = mAppDAO.get(packageInfo.packageName);
				if (dbAppInfo != null) {
					boolean isAppPinned = dbAppInfo.isPinned();
					appInfo.setPinned(isAppPinned);
				} else {
					appInfo.setPinned(HCFSMgmtUtils.deafultPinnedStatus);
					mAppDAO.insert(appInfo);
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

	public class GridRecyclerViewAdapter extends RecyclerView.Adapter<GridRecyclerViewHolder> {

		private ArrayList<ItemInfo> items;
		private ThreadPoolExecutor executor;

		public GridRecyclerViewAdapter() {
			items = new ArrayList<ItemInfo>();
			initialize();
		}

		public GridRecyclerViewAdapter(@Nullable ArrayList<ItemInfo> items) {
			this.items = (items == null) ? new ArrayList<ItemInfo>() : items;
			initialize();
		}

		public void initialize() {
			int N = Runtime.getRuntime().availableProcessors();
			executor = new ThreadPoolExecutor(N, N * 2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
			executor.allowCoreThreadTimeOut(true);
			executor.prestartCoreThread();
		}

		public void shutdownExcecutor() {
			executor.shutdownNow();
		}

		public ArrayList<ItemInfo> getItems() {
			return items;
		}

		private void clear() {
			if (items != null)
				items.clear();
		}

		public void setItemData(@Nullable ArrayList<ItemInfo> items) {
			this.items = (items == null) ? new ArrayList<ItemInfo>() : items;
		}

		@Override
		public int getItemCount() {
			return items.size();
		}

		@Override
		public void onBindViewHolder(final GridRecyclerViewHolder holder, int position) {
			final ItemInfo item = items.get(position);
			holder.setItemInfo(item);
			holder.gridTextView.setText(item.getItemName());
			holder.gridImageView.setImageDrawable(null);

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
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (imageDrawable instanceof Drawable) {
								holder.gridImageView.setImageDrawable((Drawable) imageDrawable);
							} else if (imageDrawable instanceof Bitmap) {
								holder.gridImageView.setImageBitmap((Bitmap) imageDrawable);
							}
						}
					});
				}
			});
		}

		@Override
		public GridRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_management_grid_item, parent, false);
			return new GridRecyclerViewHolder(view);
		}

		public class GridRecyclerViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

			protected View rootView;
			protected ImageView gridImageView;
			protected TextView gridTextView;
			protected ItemInfo itemInfo;

			public GridRecyclerViewHolder(View itemView) {
				super(itemView);
				rootView = itemView;
				gridImageView = (ImageView) itemView.findViewById(R.id.gridImage);
				gridTextView = (TextView) itemView.findViewById(R.id.gridText);
				itemView.setOnClickListener(this);
			}

			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.gridItemLayout) {
					if (itemInfo instanceof AppInfo) {

					} else if (itemInfo instanceof DataTypeInfo) {

					} else if (itemInfo instanceof FileDirInfo) {
						final FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
						if (fileDirInfo.getCurrentFile().isDirectory()) {
							// Set the first visible item position to previous FilePathNavigationView when navigating to next directory level
							GridLayoutManager gridLayoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
							int firstVisibleItemPosition = gridLayoutManager.findFirstCompletelyVisibleItemPosition();
							int childCount = mFilePathNavigationLayout.getChildCount();
							FilePathNavigationView filePathNavigationViewPrev = (FilePathNavigationView) mFilePathNavigationLayout
									.getChildAt(childCount - 1);
							filePathNavigationViewPrev.setFirstVisibleItemPosition(firstVisibleItemPosition);

							// Add the current FilePathNavigationView to navigation layout
							mCurrentFile = fileDirInfo.getCurrentFile();
							FilePathNavigationView filePathNavigationView = new FilePathNavigationView(getActivity());
							String currentPath = mCurrentFile.getAbsolutePath();
							String navigationText = currentPath.substring(currentPath.lastIndexOf("/") + 1) + "/";
							filePathNavigationView.setText(navigationText);
							filePathNavigationView.setCurrentFilePath(currentPath);
							mFilePathNavigationLayout.addView(filePathNavigationView);
							mFilePathNavigationScrollView.post(new Runnable() {
							    public void run() {
							    	mFilePathNavigationScrollView.fullScroll(View.FOCUS_RIGHT);
							    }
							});

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
								Snackbar.make(getView(), getString(R.string.file_management_snackbar_unkown_type_file), Snackbar.LENGTH_SHORT).show();
							}
						}
					}
				}
			}

			public void setItemInfo(ItemInfo itemInfo) {
				this.itemInfo = itemInfo;
			}

		}

	}

	public class LinearRecyclerViewAdapter extends RecyclerView.Adapter<LinearRecyclerViewHolder> {
		private ArrayList<ItemInfo> items;
		private ThreadPoolExecutor executor;

		public LinearRecyclerViewAdapter() {
			items = new ArrayList<ItemInfo>();
			initialize();
		}

		public LinearRecyclerViewAdapter(@Nullable ArrayList<ItemInfo> items) {
			this.items = (items == null) ? new ArrayList<ItemInfo>() : items;
			initialize();
		}

		public void initialize() {
			int N = Runtime.getRuntime().availableProcessors();
			executor = new ThreadPoolExecutor(N, N * 2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
			executor.allowCoreThreadTimeOut(true);
			executor.prestartCoreThread();
		}

		public void shutdownExcecutor() {
			executor.shutdownNow();
		}

		@Override
		public int getItemCount() {
			return items.size();
		}

		public ArrayList<ItemInfo> getItems() {
			return items;
		}

		@Override
		public void onBindViewHolder(final LinearRecyclerViewHolder holder, final int position) {
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
					getActivity().runOnUiThread(new Runnable() {
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
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (holder instanceof LinearRecyclerViewHolder) {
								LinearRecyclerViewHolder linearHolder = (LinearRecyclerViewHolder) holder;
								linearHolder.pinView.setImageDrawable(item.getPinImage());
							}
						}
					});
				}
			});
		}

		@Nullable
		@Override
		public LinearRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_management_linear_item, parent, false);
			return new LinearRecyclerViewHolder(view);
		}

		private void setItemData(@Nullable ArrayList<ItemInfo> items) {
			this.items = (items == null) ? new ArrayList<ItemInfo>() : items;
		}

		private void clear() {
			if (items != null)
				items.clear();
		}

		public class LinearRecyclerViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

			protected View rootView;
			protected TextView itemName;
			protected ImageView imageView;
			protected ImageView pinView;
			protected ItemInfo itemInfo;

			public LinearRecyclerViewHolder(View itemView) {
				super(itemView);
				rootView = itemView;
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
								mAppDAO.update(appInfo);

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
								mDataTypeDAO.update(dataTypeInfo);
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
								if (HCFSMgmtUtils.isDataTypePinned(mDataTypeDAO, HCFSMgmtUtils.DATA_TYPE_IMAGE)) {
									Snackbar.make(getView(), getString(R.string.file_management_not_allowed_to_pin_image), Snackbar.LENGTH_LONG)
											.show();
									return;
								}
							} else if (fileInfo.getMimeType().contains(HCFSMgmtUtils.DATA_TYPE_VIDEO)) {
								if (HCFSMgmtUtils.isDataTypePinned(mDataTypeDAO, HCFSMgmtUtils.DATA_TYPE_VIDEO)) {
									Snackbar.make(getView(), getString(R.string.file_management_not_allowed_to_pin_video), Snackbar.LENGTH_LONG)
											.show();
									return;
								}
							} else if (fileInfo.getMimeType().contains(HCFSMgmtUtils.DATA_TYPE_AUDIO)) {
								if (HCFSMgmtUtils.isDataTypePinned(mDataTypeDAO, HCFSMgmtUtils.DATA_TYPE_AUDIO)) {
									Snackbar.make(getView(), getString(R.string.file_management_not_allowed_to_pin_audio), Snackbar.LENGTH_LONG)
											.show();
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
				} else if (v.getId() == R.id.linearItemLayout || v.getId() == R.id.gridItemLayout) {
					if (itemInfo instanceof AppInfo) {

					} else if (itemInfo instanceof DataTypeInfo) {

					} else if (itemInfo instanceof FileDirInfo) {
						final FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
						if (fileDirInfo.getCurrentFile().isDirectory()) {
							// Set the first visible item position to previous FilePathNavigationView when navigating to next directory level
							LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
							int firstVisibleItemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
							int childCount = mFilePathNavigationLayout.getChildCount();
							FilePathNavigationView filePathNavigationViewPrev = (FilePathNavigationView) mFilePathNavigationLayout
									.getChildAt(childCount - 1);
							filePathNavigationViewPrev.setFirstVisibleItemPosition(firstVisibleItemPosition);

							// Add the current FilePathNavigationView to navigation layout
							mCurrentFile = fileDirInfo.getCurrentFile();
							FilePathNavigationView filePathNavigationView = new FilePathNavigationView(getActivity());
							String currentPath = mCurrentFile.getAbsolutePath();
							String navigationText = currentPath.substring(currentPath.lastIndexOf("/") + 1) + "/";
							filePathNavigationView.setText(navigationText);
							filePathNavigationView.setCurrentFilePath(currentPath);
							mFilePathNavigationLayout.addView(filePathNavigationView);
							mFilePathNavigationScrollView.post(new Runnable() {
							    public void run() {
							    	mFilePathNavigationScrollView.fullScroll(View.FOCUS_RIGHT);
							    }
							});

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

	}

	public class SectionedRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

		private boolean mValid = true;
		private static final int SECTION_TYPE = 0;
		private RecyclerView.Adapter mBaseAdapter;
		private SparseArray<Section> mSections = new SparseArray<Section>();

		public SectionedRecyclerViewAdapter(RecyclerView.Adapter baseAdapter) {
			this.mBaseAdapter = baseAdapter;

			mBaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

				@Override
				public void onChanged() {
					mValid = mBaseAdapter.getItemCount() > 0;
					Log.d(HCFSMgmtUtils.TAG, "onChanged");
					notifyDataSetChanged();
				}

				@Override
				public void onItemRangeChanged(int positionStart, int itemCount) {
					mValid = mBaseAdapter.getItemCount() > 0;
					Log.d(HCFSMgmtUtils.TAG, "onItemRangeChanged");
					notifyItemRangeChanged(positionStart, itemCount);
				}

				@Override
				public void onItemRangeInserted(int positionStart, int itemCount) {
					mValid = mBaseAdapter.getItemCount() > 0;
					Log.d(HCFSMgmtUtils.TAG, "onItemRangeInserted");
					notifyItemRangeInserted(positionStart, itemCount);
				}

				@Override
				public void onItemRangeRemoved(int positionStart, int itemCount) {
					mValid = mBaseAdapter.getItemCount() > 0;
					Log.d(HCFSMgmtUtils.TAG, "onItemRangeRemoved");
					notifyItemRangeRemoved(positionStart, itemCount);
				}
			});

		}

		public void setGridLayoutManagerSpanSize() {
			final GridLayoutManager gridLayoutManager = (GridLayoutManager) (mRecyclerView.getLayoutManager());
			gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
				@Override
				public int getSpanSize(int position) {
					return (isSectionHeaderPosition(position)) ? gridLayoutManager.getSpanCount() : 1;
				}
			});
		}

		@Override
		public int getItemCount() {
			return (mValid ? mSections.size() + mBaseAdapter.getItemCount() : 0);
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder sectionViewHolder, int position) {
			if (isSectionHeaderPosition(position)) {

			} else {
				mBaseAdapter.onBindViewHolder(sectionViewHolder, sectionedPositionToPosition(position));
			}
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int typeView) {
			if (typeView == SECTION_TYPE) {
				View view = LayoutInflater.from(getActivity()).inflate(R.layout.file_management_sction_item, parent, false);
				return new SectionedViewHolder(view);
			} else {
				return mBaseAdapter.onCreateViewHolder(parent, typeView - 1);
			}
		}

		@Override
		public int getItemViewType(int position) {
			return isSectionHeaderPosition(position) ? SECTION_TYPE : mBaseAdapter.getItemViewType(sectionedPositionToPosition(position)) + 1;
		}

		@Override
		public long getItemId(int position) {
			return isSectionHeaderPosition(position) ? Integer.MAX_VALUE - mSections.indexOfKey(position)
					: mBaseAdapter.getItemId(sectionedPositionToPosition(position));
		}

		public void setBaseAdapter(RecyclerView.Adapter mBaseAdapter) {
			this.mBaseAdapter = mBaseAdapter;
		}

		public void setSections(Section[] sections) {
			mSections.clear();

			Arrays.sort(sections, new Comparator<Section>() {
				@Override
				public int compare(Section o, Section o1) {
					return (o.firstPosition == o1.firstPosition) ? 0 : ((o.firstPosition < o1.firstPosition) ? -1 : 1);
				}
			});

			int offset = 0; // offset positions for the headers we're adding
			for (Section section : sections) {
				section.sectionedPosition = section.firstPosition + offset;
				mSections.append(section.sectionedPosition, section);
				++offset;
			}

			// notifyDataSetChanged();
		}

		public boolean isSectionHeaderPosition(int position) {
			return mSections.get(position) != null;
		}

		public int sectionedPositionToPosition(int sectionedPosition) {
			if (isSectionHeaderPosition(sectionedPosition)) {
				return RecyclerView.NO_POSITION;
			}

			int offset = 0;
			for (int i = 0; i < mSections.size(); i++) {
				if (mSections.valueAt(i).sectionedPosition > sectionedPosition) {
					break;
				}
				--offset;
			}

			return sectionedPosition + offset;
		}

		public class SectionedViewHolder extends RecyclerView.ViewHolder {

			public View rootView;

			public SectionedViewHolder(View itemView) {
				super(itemView);
				rootView = itemView;
			}

		}

	}

	public class Section {
		int firstPosition;
		int sectionedPosition;

		public Section(int firstPosition) {
			this.firstPosition = firstPosition;
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
		if (mAppDAO != null) {
			mAppDAO.close();
		}

		if (mDataTypeDAO != null) {
			mDataTypeDAO.close();
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
			int startIndex = mFilePathNavigationLayout.indexOfChild(this) + 1;
			int childCount = mFilePathNavigationLayout.getChildCount();
			mFilePathNavigationLayout.removeViews(startIndex, childCount - startIndex);

			FileManagementFragment.mCurrentFile = new File(currentFilePath);
			showTypeContent(R.string.file_management_spinner_files);

			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					switch (mDisplayType) {
					case LINEAR:
						((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPosition(firstVisibleItemPosition);
						break;
					case GRID:
						((GridLayoutManager) mRecyclerView.getLayoutManager()).scrollToPosition(firstVisibleItemPosition);
						break;
					}
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
		String selctedItemName = mSpinner.getSelectedItem().toString();
		if (selctedItemName.equals(getString(R.string.file_management_spinner_files))) {
			if (!mCurrentFile.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
				getView().findViewById(R.id.no_file_layout).setVisibility(View.GONE);
				mCurrentFile = mCurrentFile.getParentFile();
				int childCount = mFilePathNavigationLayout.getChildCount();
				FilePathNavigationView filePathNavigationView = (FilePathNavigationView) mFilePathNavigationLayout.getChildAt(childCount - 2);
				final int firstVisibleItemPosition = filePathNavigationView.firstVisibleItemPosition;
				mFilePathNavigationLayout.removeViewAt(childCount - 1);
				showTypeContent(R.string.file_management_spinner_files);

				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPosition(firstVisibleItemPosition);
					}
				});
				return true;
			}
		}
		return false;
	}

}
