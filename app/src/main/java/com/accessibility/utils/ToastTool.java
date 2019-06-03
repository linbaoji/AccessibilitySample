package com.accessibility.utils;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/5/31.
 */

public class ToastTool {
    private static Toast mToast;
    public static void show(final Context context, final String msg) {
        if (mToast == null) {
            View v = Toast.makeText(context, "", Toast.LENGTH_LONG).getView();
            mToast = new Toast(context);
            mToast.setView(v);
        }
        mToast.setText(msg);
        mToast.setDuration(Toast.LENGTH_LONG);
        mToast.show();


    }
}
