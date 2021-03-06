package com.forthorn.projecting.baserx;

import android.content.Context;

import com.forthorn.projecting.app.AppApplication;
import com.forthorn.projecting.util.NetWorkUtils;

import rx.Subscriber;

/**
 * des:订阅封装
 * Created by xsf
 * on 2016.09.10:16
 */

/********************
 * 使用例子
 ********************/
/*_apiService.login(mobile, verifyCode)
        .//省略
        .subscribe(new RxSubscriber<User user>(mContext,false) {
@Override
public void _onNext(User user) {
        // 处理user
        }

@Override
public void _onError(String msg) {
        ToastUtil.showShort(mActivity, msg);
        });*/
public abstract class RxSubscriber<T> extends Subscriber<T> {

    private Context mContext;
    private String msg;
    private boolean showDialog = true;

    /**
     * 是否显示浮动dialog
     */
    public void showDialog() {
        this.showDialog = true;
    }

    public void hideDialog() {
        this.showDialog = true;
    }

    public RxSubscriber(Context context, String msg, boolean showDialog) {
        this.mContext = context;
        this.msg = msg;
        this.showDialog = showDialog;
    }

    public RxSubscriber(Context context) {
        this(context, "请稍后...", true);
    }

    public RxSubscriber(Context context, boolean showDialog) {
        this(context, "请稍后...", showDialog);
    }

    @Override
    public void onCompleted() {
        if (showDialog) {
//            LoadingDialog.cancelDialogForLoading();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (showDialog) {
            try {
//                LoadingDialog.showDialogForLoading((Activity) mContext, msg, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onNext(T t) {
        _onNext(t);
    }

    @Override
    public void onError(Throwable e) {
        if (showDialog) {

        }
//            LoadingDialog.cancelDialogForLoading();
        e.printStackTrace();
        //网络
        if (!NetWorkUtils.isNetConnected(AppApplication.getContext())) {
            _onError("网络不可用,请检查你的网络");
        }
        //服务器
        else if (e instanceof ServerException) {
            _onError(e.getMessage());
        }
        //其它
        else {
            _onError("网络访问错误，请稍后再试");
        }
    }

    protected abstract void _onNext(T t);

    protected abstract void _onError(String message);

}
