package com.neo1946.todolist.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.widget.Toast;

import com.neo1946.todolist.MyApplication;
import com.neo1946.todolist.ui.FingerprintDialogFragment;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * @author ouyangzhaoxian on 2019/03/11
 */
public class FingerPrintUtil {
    private final static String TAG = "FingerPrintUtil";
    public final static String FINGER_AUTH_SP_NAME = "FingerPrintAuth";
    private Activity activity;
    private FingerAuthCallBack callBack;
    private static final String DEFAULT_KEY_NAME = "default_key";
    KeyStore keyStore;

    public FingerPrintUtil(Activity activity,FingerAuthCallBack callBack) {
        this.activity = activity;
        this.callBack = callBack;
    }

    public void start(){
        if (supportFingerprint(activity)) {
            initKey();
            initCipher();
        }
    }

    public static boolean supportFingerprint(Activity activity) {
        if (Build.VERSION.SDK_INT < 23) {
            if(MyApplication.isTest) {
                Toast.makeText(activity,"您的系统版本过低，不支持指纹功能",Toast.LENGTH_SHORT).show();
            }
            return false;
        } else {
            KeyguardManager keyguardManager = activity.getSystemService(KeyguardManager.class);
            FingerprintManager fingerprintManager = activity.getSystemService(FingerprintManager.class);
            if (!fingerprintManager.isHardwareDetected()) {
                if(MyApplication.isTest){
                    Toast.makeText(activity,"您的手机不支持指纹功能",Toast.LENGTH_SHORT).show();
                }
                return false;
            } else if (!keyguardManager.isKeyguardSecure()) {
                if(MyApplication.isTest) {
                    Toast.makeText(activity,"您还未设置锁屏，请先设置锁屏并添加一个指纹",Toast.LENGTH_SHORT).show();
                }
                return false;
            } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                if(MyApplication.isTest) {
                    Toast.makeText(activity,"您至少需要在系统设置中添加一个指纹",Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        }
        return true;
    }

    @TargetApi(23)
    private void initKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(DEFAULT_KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);
            keyGenerator.init(builder.build());
            keyGenerator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @TargetApi(23)
    private void initCipher() {
        try {
            SecretKey key = (SecretKey) keyStore.getKey(DEFAULT_KEY_NAME, null);
            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            showFingerPrintDialog(cipher);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void showFingerPrintDialog(Cipher cipher) {
        FingerprintDialogFragment fragment = new FingerprintDialogFragment();
        fragment.setCallBack(callBack);
        fragment.setCipher(cipher);
        fragment.show(activity.getFragmentManager(), "fingerprint");
    }

    public interface FingerAuthCallBack{
        public void onSuccess();
        public void onFail();
    }
}
