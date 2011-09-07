package jp.co.fttx.rakuphotomail.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.mail.internet.MimeUtility;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore.AttachmentInfo;
import jp.co.fttx.rakuphotomail.mail.store.StorageManager;

import java.io.*;
import java.util.List;

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
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
