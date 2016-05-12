package com.ggface.achivetricks.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ggface.achivetricks.R;

public class WarningToast extends Toast {

    private Context mContext;
    private LayoutInflater mInflater;
    private TextView textView;

    public WarningToast(Context context) {
        super(context);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        init();
    }

    protected Context getContext() {
        return mContext;
    }

    protected LayoutInflater getLayoutInflater() {
        return mInflater;
    }

    private void init() {
        View mView = getLayoutInflater().inflate(R.layout.toast_warning, null, false);

        setDuration(Toast.LENGTH_SHORT);
        setView(mView);

        textView = (TextView) mView.findViewById(R.id.text);
    }

    @Override
    public void setText(int resId) {
        textView.setText(getContext().getText(resId));
    }

    @Override
    public void setText(CharSequence s) {
        textView.setText(s);
    }

    public interface OnToastListener {
        void showWarning(String message);
        void hide();
    }
}
