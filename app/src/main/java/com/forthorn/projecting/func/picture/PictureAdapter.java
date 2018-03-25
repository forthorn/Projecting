package com.forthorn.projecting.func.picture;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

/**
 * Created by: Forthorn
 * Date: 10/28/2017.
 * Description:
 */

public class PictureAdapter extends BaseViewPagerAdapter<String> {

    private Context mContext;
    private RequestOptions options = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

    public PictureAdapter(Context context, List data) {
        super(context, data);
        this.mContext = context;
    }

    public PictureAdapter(Context context, List data, OnAutoViewPagerItemClickListener listener) {
        super(context, data, listener);
        this.mContext = context;
    }

    @Override
    public void loadImage(ImageView view, int position, String url) {
        Glide.with(mContext).load(url).apply(options).into(view);
    }
}
