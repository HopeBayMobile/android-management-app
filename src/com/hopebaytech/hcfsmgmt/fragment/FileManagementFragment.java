package com.hopebaytech.hcfsmgmt.fragment;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class FileManagementFragment extends Fragment {

	private Handler mUI_handler;
	private RecyclerViewAdapter recyclerViewAdapter;
	private ArrayAdapter<String> spinnerAdapter;
	private Handler mThreadHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUI_handler = new Handler();
		recyclerViewAdapter = new RecyclerViewAdapter();

		String[] spinner_array = getActivity().getResources().getStringArray(R.array.file_management_spinner);
		spinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.file_management_spinner, spinner_array);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		HandlerThread mThread = new HandlerThread("mThread");
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
		View view = getView();
		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
		recyclerView.setAdapter(recyclerViewAdapter);

		final Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
		spinner.setAdapter(spinnerAdapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String itemName = parent.getSelectedItem().toString();
				if (itemName.equals(getString(R.string.file_management_spinner_apps))) {
					showTypeContent(R.string.file_management_spinner_apps);
				} else if (itemName.equals(getString(R.string.file_management_spinner_data_type))) {
					showTypeContent(R.string.file_management_spinner_data_type);
				} else if (itemName.equals(getString(R.string.file_management_spinner_files))) {
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
//				Snackbar.make(getView(), "重新整理", Snackbar.LENGTH_SHORT).setActionTextColor(Color.RED).show();
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

	@Override
	public void onStart() {
		super.onStart();

		// getInstalledApps();
		// getAvailableFilesPaths();
		// getAvailableImagePaths();
		// getAvailableAudioPaths();
		// getAvailableVideoPaths();
	}

	private ArrayList<ItemData> getListOfDataType() {
		ArrayList<ItemData> items = new ArrayList<ItemData>();
		String[] data_type_array = getResources().getStringArray(R.array.file_management_list_data_types);
		for (int i = 0; i < data_type_array.length; i++) {
			ItemData itemData = new ItemData();
			itemData.setName(data_type_array[i]);
			if (data_type_array[i].equals(getString(R.string.file_management_list_data_type_image))) {
				itemData.setImage(getResources().getDrawable(R.drawable.ic_photo_black));
			} else if (data_type_array[i].equals(getString(R.string.file_management_list_data_type_video))) {
				itemData.setImage(getResources().getDrawable(R.drawable.ic_video_black));
			} else if (data_type_array[i].equals(getString(R.string.file_management_list_data_type_music))) {
				itemData.setImage(getResources().getDrawable(R.drawable.ic_music_black));
			}
			items.add(itemData);
		}
		return items;
	}

	private ArrayList<ItemData> getListOfFiles() {
		return null;
	}

	private void showTypeContent(final int resource_string_id) {
		mThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				ArrayList<ItemData> itemDatas = null;
				switch (resource_string_id) {
				case R.string.file_management_spinner_apps:
					itemDatas = getListOfInstalledApps();
					Log.d(HCFSMgmtUtils.TAG, "app");
					break;
				case R.string.file_management_spinner_data_type:
					itemDatas = getListOfDataType();
					Log.d(HCFSMgmtUtils.TAG, "data_type");
					break;
				case R.string.file_management_spinner_files:
					itemDatas = getListOfFiles();
					Log.d(HCFSMgmtUtils.TAG, "files");
					break;
				default:
					break;
				}
				final ArrayList<ItemData> items = itemDatas;
				mUI_handler.post(new Runnable() {
					@Override
					public void run() {
						recyclerViewAdapter.clear();
						recyclerViewAdapter.setItemData(items);
						recyclerViewAdapter.notifyDataSetChanged();
					}
				});
			}
		});
	}

	@SuppressLint("NewApi")
	private ArrayList<ItemData> getListOfInstalledApps() {
		ArrayList<ItemData> items = new ArrayList<ItemData>();
		final PackageManager pm = getActivity().getPackageManager();
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		for (ApplicationInfo packageInfo : packages) {
			if (!isSystemPackage(packageInfo)) {
				ItemData itemData = new ItemData();
				itemData.setImage(pm.getApplicationIcon(packageInfo));
				itemData.setName(packageInfo.loadLabel(pm).toString());
				items.add(itemData);
				// Log.d(HCFSMgmtUtils.TAG, "uid:" + packageInfo.uid);
				// Log.d(HCFSMgmtUtils.TAG, "packageName: " +
				// packageInfo.packageName);
				// Log.d(HCFSMgmtUtils.TAG, "sourceDir: " +
				// packageInfo.sourceDir);
				// Log.d(HCFSMgmtUtils.TAG, "publicSourceDir: " +
				// packageInfo.publicSourceDir);
				// Log.d(HCFSMgmtUtils.TAG, "dataDir: " + packageInfo.dataDir);
				// Log.d(HCFSMgmtUtils.TAG, "nativeLibraryDir: " +
				// packageInfo.nativeLibraryDir);
				// Log.d(HCFSMgmtUtils.TAG, "sharedLibraryFiles: " +
				// packageInfo.sharedLibraryFiles);
				// getPackageSize(pm, packageInfo);
				// Log.d(HCFSMgmtUtils.TAG,
				// "--------------------------------------------------");
				// Log.d(HCFSMgmtUtils.TAG, "splitPublicSourceDirs: " +
				// packageInfo.splitPublicSourceDirs);
				// Log.d(HCFSMgmtUtils.TAG, "splitSourceDirs: " +
				// packageInfo.splitSourceDirs);
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

	@Nullable
	private ArrayList<String> getAvailableFilesPaths() {
		ArrayList<String> nonMediaFilePaths = null;
		ContentResolver resolver = getActivity().getContentResolver();
		String[] projection = null;
		String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
		Cursor cursor = resolver.query(MediaStore.Files.getContentUri("external"), projection, selection, null, null);
		if (cursor != null) {
			nonMediaFilePaths = new ArrayList<String>();
			cursor.moveToFirst();
			final int index = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
			for (int i = 0; i < cursor.getCount(); i++) {
				String path = cursor.getString(index);
				File file = new File(path);
				if (file.isDirectory()) {
					Log.d(HCFSMgmtUtils.TAG, "directory path: " + path);
				} else {
					Log.d(HCFSMgmtUtils.TAG, "file path: " + path);
				}
				nonMediaFilePaths.add(path);
				cursor.moveToNext();
			}
			cursor.close();
		}
		return nonMediaFilePaths;
	}

	@Nullable
	private ArrayList<String> getAvailableVideoPaths() {
		ArrayList<String> vedioPaths = null;
		ContentResolver resolver = getActivity().getContentResolver();
		String[] projection = { MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA };
		// External storage
		Cursor cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Video.Media._ID);
		if (cursor != null) {
			vedioPaths = new ArrayList<String>();
			cursor.moveToFirst();
			final int index = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
			for (int i = 0; i < cursor.getCount(); i++) {
				String path = cursor.getString(index);
				Log.d(HCFSMgmtUtils.TAG, "vedio path: " + path);
				vedioPaths.add(path);
				cursor.moveToNext();
			}
			cursor.close();
		}
		// Internal storage
		// cursor = resolver.query(MediaStore.Video.Media.INTERNAL_CONTENT_URI,
		// projection, null, null, MediaStore.Video.Media._ID);
		// cursor.moveToFirst();
		// for (int i = 0; i < cursor.getCount(); i++) {
		// String path = cursor.getString(index);
		// Log.d(HCFSMgmtUtils.TAG, "vedio path: " + path);
		// vedioPaths.add(path);
		// cursor.moveToNext();
		// }
		// cursor.close();
		return vedioPaths;
	}

	@Nullable
	private ArrayList<String> getAvailableAudioPaths() {
		ArrayList<String> audioPaths = null;
		ContentResolver resolver = getActivity().getContentResolver();
		String[] projection = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA };
		// External storage
		Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Audio.Media._ID);
		if (cursor != null) {
			audioPaths = new ArrayList<String>();
			cursor.moveToFirst();
			int index = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
			for (int i = 0; i < cursor.getCount(); i++) {
				String path = cursor.getString(index);
				Log.d(HCFSMgmtUtils.TAG, "audio path: " + path);
				audioPaths.add(path);
				cursor.moveToNext();
			}
			cursor.close();
		}
		// Internal storage
		// cursor = resolver.query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
		// projection, null, null, MediaStore.Audio.Media._ID);
		// cursor.moveToFirst();
		// for (int i = 0; i < cursor.getCount(); i++) {
		// String path = cursor.getString(index);
		// Log.d(HCFSMgmtUtils.TAG, "audio path: " + path);
		// audioPaths.add(path);
		// cursor.moveToNext();
		// }
		// cursor.close();
		return audioPaths;
	}

	@Nullable
	private ArrayList<String> getAvailableImagePaths() {
		ArrayList<String> imagePaths = null;
		ContentResolver resolver = getActivity().getContentResolver();
		String[] projection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
		// External storage
		Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media._ID);
		if (cursor != null) {
			imagePaths = new ArrayList<String>();
			cursor.moveToFirst();
			final int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
			for (int i = 0; i < cursor.getCount(); i++) {
				String path = cursor.getString(index);
				Log.d(HCFSMgmtUtils.TAG, "image path: " + path);
				imagePaths.add(path);
				cursor.moveToNext();
			}
			cursor.close();
		}
		// Internal storage
		// cursor = resolver.query(MediaStore.Images.Media.INTERNAL_CONTENT_URI,
		// projection, null, null, MediaStore.Images.Media._ID);
		// cursor.moveToFirst();
		// for (int i = 0; i < cursor.getCount(); i++) {
		// String path = cursor.getString(index);
		// Log.d(HCFSMgmtUtils.TAG, "image path: " + path);
		// imagePaths.add(path);
		// cursor.moveToNext();
		// }
		// cursor.close();
		return imagePaths;
	}

	public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

		private ArrayList<ItemData> items;

		public RecyclerViewAdapter() {
			items = new ArrayList<ItemData>();
		}

		public RecyclerViewAdapter(@Nullable ArrayList<ItemData> items) {
			if (items == null) {
				items = new ArrayList<ItemData>();
			} else {
				this.items = items;
			}
		}

		@Override
		public int getItemCount() {
			return items.size();
		}

		@Override
		public void onBindViewHolder(RecyclerViewHolder holder, int position) {
			ItemData item = items.get(position);
			holder.imageView.setImageDrawable(item.getImage());
			holder.name.setText(item.getName());
		}

		@Override
		public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_management_single_item, parent, false);
			RecyclerViewHolder recyclerViewHolder = new RecyclerViewHolder(view);
			return recyclerViewHolder;
		}

		private void setItemData(@Nullable ArrayList<ItemData> items) {
			if (items == null) {
				items = new ArrayList<ItemData>();
			} else {
				this.items = items;
			}
		}

		private void clear() {
			items.clear();
		}

	}

	public class RecyclerViewHolder extends RecyclerView.ViewHolder {

		protected View rootView;
		protected TextView name;
		protected ImageView imageView;

		public RecyclerViewHolder(View itemView) {
			super(itemView);
			name = (TextView) itemView.findViewById(R.id.name);
			imageView = (ImageView) itemView.findViewById(R.id.iconView);
			rootView = itemView;
		}

	}

	public class ItemData {

		private String name;
		private Drawable image;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Drawable getImage() {
			return image;
		}

		public void setImage(Drawable image) {
			this.image = image;
		}

	}

	public class DividerItemDecoration extends RecyclerView.ItemDecoration {

		private final int[] ATTRS = new int[] { android.R.attr.listDivider };

		public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

		public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

		private Drawable mDivider;

		private int mOrientation;

		public DividerItemDecoration(Context context, int orientation) {
			final TypedArray a = context.obtainStyledAttributes(ATTRS);
			mDivider = a.getDrawable(0);
			a.recycle();
			setOrientation(orientation);
		}

		public void setOrientation(int orientation) {
			if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
				throw new IllegalArgumentException("invalid orientation");
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

}
