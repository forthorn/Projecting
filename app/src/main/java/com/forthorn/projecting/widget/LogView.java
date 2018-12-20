package com.forthorn.projecting.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogView extends LinearLayout {

    private static final int MAX_LINES = 18;
    SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss:SSS");
    private List<TextView> mTextViewList = new ArrayList<>(MAX_LINES);
    private List<CharSequence> mLogStrings = new ArrayList<>(MAX_LINES);
    private Context mContext;

    public LogView(Context context) {
        this(context, null);
    }

    public LogView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        setBackgroundColor(Color.parseColor("#33000000"));
        setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < MAX_LINES; i++) {
            TextView textView = new TextView(mContext);
            textView.setMaxLines(1);
            textView.setSingleLine(true);
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(12);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            mTextViewList.add(textView);
            addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }


    public synchronized void append(CharSequence charSequence) {
        String log = sdf.format(new Date()) + " " + charSequence;
        Log.e("append", log);
        if (mLogStrings.size() >= MAX_LINES) {
            mLogStrings.remove(0);
        }
        mLogStrings.add(log);
        int size = mLogStrings.size();
        for (int i = 0; i < size; i++) {
            mTextViewList.get(i).setText((String) mLogStrings.get(i));
        }
    }

}
