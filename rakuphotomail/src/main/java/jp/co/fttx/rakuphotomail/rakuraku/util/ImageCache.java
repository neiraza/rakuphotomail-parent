package jp.co.fttx.rakuphotomail.rakuraku.util;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.util.Log;

public class ImageCache {
	private static HashMap<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>();

	public static Bitmap getImage(String key) {
		Log.d("steinsgate", "ImageCache#getImage:key"+ key);
		if (cache.containsKey(key)) {
			SoftReference<Bitmap> ref = cache.get(key);
			if (ref != null) {
				Log.d("steinsgate", "ImageCache#getImage:ref.get()" + ref.get());
				return ref.get();
			}
		}
		Log.d("steinsgate", "ImageCache#getImage:null");
		return null;
	}

	public static void setImage(String key, Bitmap image) {
		Log.d("steinsgate", "ImageCache#setImage key image:" + key + " " + image);
		cache.put(key, new SoftReference<Bitmap>(image));
	}

	public static boolean hasImage(String key) {
		return cache.containsKey(key);
	}

	public static void clear() {
		cache.clear();
	}
}
