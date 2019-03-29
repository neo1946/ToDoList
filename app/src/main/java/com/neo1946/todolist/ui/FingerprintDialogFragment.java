package com.neo1946.todolist.ui;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.neo1946.todolist.R;
import com.neo1946.todolist.util.FingerPrintUtil;

import javax.crypto.Cipher;

import static android.content.Context.MODE_PRIVATE;
import static com.neo1946.todolist.MyApplication.SP_NAME;
import static com.neo1946.todolist.util.FingerPrintUtil.FINGER_AUTH_SP_NAME;

/**
 * 暂时和LoginAc强耦合 后面梳理
 */

@TargetApi(23)
public class FingerprintDialogFragment extends DialogFragment  implements DialogInterface.OnCancelListener,DialogInterface.OnDismissListener{

    private FingerprintManager fingerprintManager;

    private CancellationSignal mCancellationSignal;

    private Cipher mCipher;

    private FingerPrintUtil.FingerAuthCallBack callBack;

    private TextView errorMsg;

    private int time = 0;

    /**
     * 标识是否是用户主动取消的认证。
     */
    private boolean isSelfCancelled;

    public void setCipher(Cipher cipher) {
        mCipher = cipher;
    }

    public void setCallBack(FingerPrintUtil.FingerAuthCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fingerprintManager = getContext().getSystemService(FingerprintManager.class);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.finger_dialog, container, false);
        errorMsg = v.findViewById(R.id.error_msg);
        errorMsg.setVisibility(View.VISIBLE);
        TextView cancel = v.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callBack != null){
                    callBack.onFail();
                }
                dismiss();
                stopListening();
            }
        });
        cancel.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                time++;
                if(time > 4){
                    SharedPreferences sharedPreferences = getContext().getSharedPreferences(SP_NAME, MODE_PRIVATE);
                    sharedPreferences.edit().putBoolean(FINGER_AUTH_SP_NAME, false).apply();
                }
                return false;
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 开始指纹认证监听
        startListening(mCipher);
    }

    @Override
    public void onPause() {
        super.onPause();
        // 停止指纹认证监听
        stopListening();
    }

    private void startListening(Cipher cipher) {
        isSelfCancelled = false;
        mCancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(new FingerprintManager.CryptoObject(cipher), mCancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                if (!isSelfCancelled) {
                    errorMsg.setText(errString);
                    if (errorCode == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT) {
                        if(callBack != null){
                            callBack.onFail();
                        }
                        dismiss();
                    }
                }
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                if(!"".equals(helpString)){
                    errorMsg.setText(helpString);
                }
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                if(callBack != null){
                    callBack.onSuccess();
                }
                dismiss();
            }

            @Override
            public void onAuthenticationFailed() {
                errorMsg.setText("指纹认证失败，请再试一次");
            }
        }, null);
    }

    private void stopListening() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
            isSelfCancelled = true;
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
//        Toast.makeText(mContext,"cancel",Toast.LENGTH_SHORT).show();
        if(callBack != null){
            callBack.onFail();
        }
        dismiss();
        super.onCancel(dialog);
    }
}