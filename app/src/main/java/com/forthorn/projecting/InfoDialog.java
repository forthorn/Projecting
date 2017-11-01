package com.forthorn.projecting;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.forthorn.projecting.entity.Users;

import java.util.List;


public class InfoDialog {

    private Dialog mDialog;
    private View mDialogView;
    private Context mContext;

    private TextView mTitle;
    private ListView mListView;
    private List<Users> mDatas;

    private BaseAdapter mAdapter;

    private OnDialogListener mListener;

    public InfoDialog(Context context, String title, List<Users> datas, OnDialogListener listener) {
        mContext = context;
        mListener = listener;
        mDatas = datas;
        mDialog = new Dialog(mContext, R.style.MyDialogStyle);
        mDialogView = View.inflate(mContext, R.layout.widget_info_dialog, null);
        mTitle = (TextView) mDialogView.findViewById(R.id.dialog_title);
        mTitle.setText(title);
        mListView = (ListView) mDialogView.findViewById(R.id.listview);

        mAdapter = new UsersAdapter(mContext, mDatas);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mListener.selectInfo(mDatas.get(i));
                dismiss();
            }
        });
        mDialog.setContentView(mDialogView);
        Window dialogWindow = mDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);
        initDialog();
    }

    private void initDialog() {
        mDialog.setCancelable(true);
        mDialog.setCanceledOnTouchOutside(true);
    }

    public void show() {
        mDialog.show();
    }

    public void dismiss() {
        mDialog.dismiss();
    }

    public interface OnDialogListener {
        void selectInfo(Users users);
    }


    class UsersAdapter extends BaseAdapter {

        private List<Users> mList;
        private Context mContext;
        private LayoutInflater mInflater;

        public UsersAdapter(Context context, List<Users> list) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            mList = list;
        }

        @Override
        public int getCount() {
            return mList == null ? 0 : mList.size();
        }

        @Override
        public Users getItem(int i) {
            return mList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = mInflater.inflate(R.layout.item_user, null);
            TextView textView = (TextView) v.findViewById(R.id.user_tv);
            textView.setText(mList.get(i).getUsername());

            return v;
        }
    }
}
