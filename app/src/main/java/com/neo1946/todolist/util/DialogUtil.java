package com.neo1946.todolist.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import static com.neo1946.todolist.ui.NewActivity.REQUEST_CODE_CHOOSE;

/**
 * @author ouyangzhaoxian on 2019/03/11
 */
public class DialogUtil {
    /**
     * 获取授权弹框
     */
    public static void showAuthDialog(final String msg, final Context context,
                                      final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                REQUEST_CODE_CHOOSE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }
}
