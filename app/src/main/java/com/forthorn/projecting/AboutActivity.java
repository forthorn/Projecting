package com.forthorn.projecting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.forthorn.projecting.app.BundleKey;
import com.forthorn.projecting.util.SPUtils;

public class AboutActivity extends Activity implements View.OnClickListener {

    private ImageView mAboutLogoIv;
    private TextView mAboutNameTv;
    private TextView mAboutCodeTv;
    private TextView mAboutAreaTv;
    private TextView mAboutAddressTv;
    private TextView mAboutTypeTv;
    private TextView mAboutIdTv;

    private String mName, mCode, mArea, mAddress, mType, mId;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        mContext = AboutActivity.this;
        initView();
        initData();
        initEvent();
    }

    private void initEvent() {
        mAboutLogoIv.setOnClickListener(this);
    }


    private void initData() {
        mName = SPUtils.getSharedStringData(mContext, BundleKey.DEVICE_NAME);
        mCode = SPUtils.getSharedStringData(mContext, BundleKey.DEVICE_CODE);
        mArea = SPUtils.getSharedStringData(mContext, BundleKey.DEVICE_AREA);
        mAddress = SPUtils.getSharedStringData(mContext, BundleKey.DEVICE_ADDRESS);
        mType = SPUtils.getSharedStringData(mContext, BundleKey.DEVICE_TYPE);
        mId = SPUtils.getSharedStringData(mContext, BundleKey.DEVICE_ID);
        mAboutNameTv.setText(mName);
        mAboutCodeTv.setText(mCode);
        mAboutAreaTv.setText(mArea);
        mAboutAddressTv.setText(mAddress);
        mAboutTypeTv.setText(mType);
        mAboutIdTv.setText("标识码:" + mCode);
        mAboutLogoIv.setImageResource(TextUtils.isEmpty(mCode) ? R.drawable.ic_info_offline : R.drawable.ic_info_online);
        if (TextUtils.isEmpty(mId)) {
            new AlertDialog.Builder(mContext).setMessage(Html.fromHtml("您的广告机后台还未注册\n请先在后台注册后再打开"))
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }
    }

    private void initView() {
        mAboutLogoIv = (ImageView) findViewById(R.id.about_logo_iv);
        mAboutNameTv = (TextView) findViewById(R.id.about_name_tv);
        mAboutCodeTv = (TextView) findViewById(R.id.about_code_tv);
        mAboutAreaTv = (TextView) findViewById(R.id.about_area_tv);
        mAboutAddressTv = (TextView) findViewById(R.id.about_address_tv);
        mAboutTypeTv = (TextView) findViewById(R.id.about_type_tv);
        mAboutIdTv = (TextView) findViewById(R.id.about_id_tv);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.about_logo_iv:
                finish();
                break;
            default:
                break;
        }
    }
}
