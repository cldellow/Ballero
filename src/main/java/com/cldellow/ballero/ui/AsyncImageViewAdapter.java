package com.cldellow.ballero.ui;

import greendroid.image.ImageProcessor;
import greendroid.image.ImageRequest;
import greendroid.image.ImageRequest.ImageRequestCallback;
import greendroid.util.Config;
import greendroid.util.GDUtils;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Gallery;
import android.widget.BaseAdapter;
import android.view.View;
import android.view.ViewGroup;
import greendroid.widget.AsyncImageView;

//import com.cyrilmottier.android.greendroid.R;
import com.cldellow.ballero.R;

public class AsyncImageViewAdapter extends BaseAdapter {
    private Context mContext;
    private String[] mUrls;
    int mGalleryItemBackground;

    public AsyncImageViewAdapter(Context c, String[] urls) {
        mContext = c;
        mUrls = urls;
        TypedArray attr = mContext.obtainStyledAttributes(R.styleable.HelloGallery);
        mGalleryItemBackground = attr.getResourceId(
        R.styleable.HelloGallery_android_galleryItemBackground, 0);
        attr.recycle();
    }

    public int getCount() {
        return mUrls.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        AsyncImageView imageView = new AsyncImageView(mContext);
        imageView.setAdjustViewBounds(true);

        imageView.setUrl(mUrls[position]);
        imageView.setLayoutParams(new Gallery.LayoutParams(200, 150));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setBackgroundResource(mGalleryItemBackground);
        return imageView;
    }
}
