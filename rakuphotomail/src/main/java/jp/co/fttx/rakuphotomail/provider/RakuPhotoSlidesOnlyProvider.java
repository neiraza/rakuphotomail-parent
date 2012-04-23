package jp.co.fttx.rakuphotomail.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.io.File;

public class RakuPhotoSlidesOnlyProvider extends ContentProvider {
	public static final Uri CONTENT_URI = Uri.parse("content://jp.co.fttx.rakuphotomail.rakuphotoslidesonlyprovider");

	@Override
	public boolean onCreate() {
		final File cacheDir = getContext().getCacheDir();
		if (cacheDir == null) {
			Log.d("tadahiro", "cacheDir is nullllllllllll");
			return true;
		}
		File[] files = cacheDir.listFiles();
		if (files == null) {
			Log.d("tadahiro", "files is nullllllllllll");
			return true;
		}
		for (File file : files) {
			if (file.getName().endsWith(".tmp")) {
				file.delete();
			}
		}

		return true;
	}


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}


	@Override
	public String getType(Uri uri) {
		return null;
	}


	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}


	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		return null;
	}


	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}

}
