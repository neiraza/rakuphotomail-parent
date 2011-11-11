/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.fttx.rakuphotomail.activity;

import java.util.ArrayList;
import java.util.List;

import jp.co.fttx.rakuphotomail.R;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

public class Gallery2 extends Activity implements OnItemClickListener {

	private ImageView image;
	private List<Bitmap> imageItems;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery_2);

		// Reference the Gallery view
		Gallery g = (Gallery) findViewById(R.id.gallery2);
		// Set the adapter to our custom adapter (below)
		g.setAdapter(new ImageAdapter(this));

		// Set a item click listener, and just Toast the clicked position
		g.setOnItemClickListener(this);
		
		image = (ImageView) findViewById(R.id.image2);
		image.setImageBitmap(imageItems.get(0));
	}
	
	@Override
	public void onItemClick(AdapterView parent, View v, int position,
			long id) {
		Toast.makeText(Gallery2.this, "" + position, Toast.LENGTH_SHORT)
				.show();
		image.setImageBitmap(imageItems.get(position));
	}

	// アダプターは外だしにすべき
	public class ImageAdapter extends BaseAdapter {
		int mGalleryItemBackground;

		public ImageAdapter(Context c) {
			mContext = c;
			TypedArray a = obtainStyledAttributes(R.styleable.Gallery1);
			mGalleryItemBackground = a.getResourceId(
					R.styleable.Gallery1_android_galleryItemBackground, 0);
			a.recycle();

			imageItems = setDroidList();
		}

		public int getCount() {
			return imageItems.size();
		}

		// 悪い見本。本来なら画像イメージを返すべき。
		public Object getItem(int position) {
			return imageItems.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i = new ImageView(mContext);

			Bitmap bitmap = (Bitmap) getItem(position);
			i.setImageBitmap(bitmap);

			i.setBackgroundResource(mGalleryItemBackground);

			return i;
		}

		private Context mContext;

		// XXX まさにゴミ
		private ArrayList<Bitmap> setDroidList() {
			ArrayList<Bitmap> list = new ArrayList<Bitmap>();
			Resources r = getResources();

			Log.d("vmware", "start!");

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeResource(r, R.drawable.droid, options);
			int displayW = getWindowManager().getDefaultDisplay().getWidth();
			int displayH = getWindowManager().getDefaultDisplay().getHeight();
			int scaleW = options.outWidth / displayW + 1;
			int scaleH = options.outHeight / displayH + 1;
			options.inJustDecodeBounds = false;
			options.inSampleSize = Math.max(scaleW, scaleH);
			list.add(BitmapFactory.decodeResource(r, R.drawable.droid, options));

			Log.d("vmware", "second!");

			BitmapFactory.Options options2 = new BitmapFactory.Options();
			options2.inJustDecodeBounds = true;
			BitmapFactory.decodeResource(r, R.drawable.droid2, options2);
			int displayW2 = getWindowManager().getDefaultDisplay().getWidth();
			int displayH2 = getWindowManager().getDefaultDisplay().getHeight();
			int scaleW2 = options2.outWidth / displayW2 + 1;
			int scaleH2 = options2.outHeight / displayH2 + 1;
			options2.inJustDecodeBounds = false;
			options2.inSampleSize = Math.max(scaleW2, scaleH2);
			list.add(BitmapFactory.decodeResource(r, R.drawable.droid2,
					options2));

			BitmapFactory.Options options3 = new BitmapFactory.Options();
			options3.inJustDecodeBounds = true;
			BitmapFactory.decodeResource(r, R.drawable.droid3, options3);
			int displayW3 = getWindowManager().getDefaultDisplay().getWidth();
			int displayH3 = getWindowManager().getDefaultDisplay().getHeight();
			int scaleW3 = options3.outWidth / displayW3 + 1;
			int scaleH3 = options3.outHeight / displayH3 + 1;
			options3.inJustDecodeBounds = false;
			options3.inSampleSize = Math.max(scaleW3, scaleH3);
			list.add(BitmapFactory.decodeResource(r, R.drawable.droid3,
					options3));

			BitmapFactory.Options options4 = new BitmapFactory.Options();
			options4.inJustDecodeBounds = true;
			BitmapFactory.decodeResource(r, R.drawable.droid4, options4);
			int displayW4 = getWindowManager().getDefaultDisplay().getWidth();
			int displayH4 = getWindowManager().getDefaultDisplay().getHeight();
			int scaleW4 = options4.outWidth / displayW4 + 1;
			int scaleH4 = options4.outHeight / displayH4 + 1;
			options4.inJustDecodeBounds = false;
			options4.inSampleSize = Math.max(scaleW4, scaleH4);
			list.add(BitmapFactory.decodeResource(r, R.drawable.droid4,
					options4));

			Log.d("vmware", "goal!");
			return list;
		}
	}


}
