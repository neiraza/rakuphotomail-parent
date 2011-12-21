/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.rakuraku.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import jp.co.fttx.rakuphotomail.R;

import java.util.ArrayList;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class ThumbnailImageAdapter extends BaseAdapter {
    private int mGalleryItemBackground;
    private Context mContext;
    private ArrayList<Bitmap> mImageItems;

    public ThumbnailImageAdapter(Context c) {
        Log.d("maguro", "ThumbnailImageAdapter Construct start");
        mContext = c;
        TypedArray a = mContext.obtainStyledAttributes(R.styleable.GalleryThumbnail);
        mGalleryItemBackground = a
                .getResourceId(R.styleable.GalleryThumbnail_android_galleryItemBackground, 0);
        a.recycle();
        mImageItems = new ArrayList<Bitmap>();
        Log.d("maguro", "ThumbnailImageAdapter Construct end");
    }

    @Override
    public int getCount() {
        return mImageItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mImageItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView i = new ImageView(mContext);
        Bitmap bitmap = (Bitmap) getItem(position);
        i.setImageBitmap(bitmap);
        i.setBackgroundResource(mGalleryItemBackground);

        return i;
    }

    public void setImageItems(ArrayList<Bitmap> imageItems) {
        this.mImageItems = imageItems;
    }
}
