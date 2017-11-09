package com.forthorn.projecting.widget;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.forthorn.projecting.R;

/**
 * Created by: Forthorn
 * Date: 9/7/2017.
 * Description:
 * 通用的提示窗口
 */

public class NoticeDialog implements View.OnClickListener {

    private Button mNegativeBtn;
    private Button mPositiveBtn;
    private TextView mTitleTv;
    private TextView mMessageTv;
    private Dialog mDialog;
    private View mDialogView;
    private Context mContext;
    private OnDialogListener mListener;

    public NoticeDialog(@NonNull Context context, @Nullable String title,
                        @NonNull String msg, @Nullable OnDialogListener listener) {
        mContext = context;
        mListener = listener;
        mDialog = new Dialog(mContext, R.style.normal_dialog_style);
        mDialogView = View.inflate(mContext, R.layout.widget_dialog_notice, null);

//        mTitleTv = (TextView) mDialogView.findViewById(R.id.dialog_title_tv);
        mMessageTv = (TextView) mDialogView.findViewById(R.id.dialog_message_tv);

        mNegativeBtn = (Button) mDialogView.findViewById(R.id.dialog_negative_btn);
        mPositiveBtn = (Button) mDialogView.findViewById(R.id.dialog_positive_btn);
//        if (title != null) {
//            mTitleTv.setText(title);
//        }
        mMessageTv.setText(msg);

        mDialog.setContentView(mDialogView);
        Window dialogWindow = mDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);
        mDialog.setCancelable(true);
        mDialog.setCanceledOnTouchOutside(false);
        mNegativeBtn.setOnClickListener(this);
        mPositiveBtn.setOnClickListener(this);
        mPositiveBtn.requestFocus();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_negative_btn:
                dismiss();
                break;
            case R.id.dialog_positive_btn:
                if (mListener != null) {
                    mListener.clickPositive();
                }
                dismiss();
                break;
            default:
                break;
        }
    }

    public void dismiss() {
        mDialog.dismiss();
    }

    public void show() {
        mDialog.show();
    }

    public boolean isShowing() {
        return mDialog.isShowing();
    }

    public interface OnDialogListener {
        void clickPositive();
    }

}
