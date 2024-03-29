package com.example.openseesawme;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Fingerprint extends AppCompatActivity {
    final String TAG = "Fingerprint";
    private ImageView iv_fingerprint;
    private TextView tv_message;
    private LinearLayout linearLayout;
    Button btn_fpclose;
    TextView tv_pin;
    String scanDeviceAddress;

    private static final String KEY_NAME = "example_key";
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private Cipher cipher;
    private FingerprintManager.CryptoObject cryptoObject;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint_bottom);

        iv_fingerprint = (ImageView) findViewById(R.id.iv_fingerprint);
        tv_message = (TextView) findViewById(R.id.tv_message);
        tv_message.setText("앱이 시작되었습니다.");
        linearLayout = (LinearLayout) findViewById(R.id.ll_secure);
        btn_fpclose = findViewById(R.id.btn_fpclose);
        linearLayout.setVisibility(LinearLayout.GONE);
        tv_pin = findViewById(R.id.tv_pin);

        //없어도 되나?
        Intent inIntent = getIntent();
//        final boolean done=inIntent.getBooleanExtra("done",true);
        scanDeviceAddress = inIntent.getStringExtra("bea_id");

        //pin번호 입력 눌렀을 때
        tv_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //SharedPreference값읽어오기
                SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
                String pin_key = pref.getString("pin_key","fail");//pin 번호
                if(pin_key.equals("fail")){
                    Intent intent = new Intent(getApplicationContext(), pin_register1.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    Intent intent = new Intent(getApplicationContext(), pin_enter.class);
                    startActivity(intent);
                    finish();
                }
            }
        });


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

            if(!fingerprintManager.isHardwareDetected()){//Manifest에 Fingerprint 퍼미션을 추가해 줘야 사용가능
                tv_message.setText("지문을 사용할 수 없는 디바이스 입니다.");
            } else if(ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED){
                tv_message.setText("지문사용을 허용해 주세요.");
                /*잠금화면 상태를 체크한다.*/
            } else if(!keyguardManager.isKeyguardSecure()){
                tv_message.setText("잠금화면을 설정해 주세요.");
            } else if(!fingerprintManager.hasEnrolledFingerprints()){
                tv_message.setText("등록된 지문이 없습니다.");
            } else {//모든 관문을 성공적으로 통과(지문인식을 지원하고 지문 사용이 허용되어 있고 잠금화면이 설정되었고 지문이 등록되어 있을때)
                tv_message.setText("손가락을 홈버튼에 대 주세요.");

                generateKey();
                if(cipherInit()){
                    cryptoObject = new FingerprintManager.CryptoObject(cipher);
                    //핸들러실행
                    FingerprintHandler fingerprintHandler = new FingerprintHandler(this);
                    fingerprintHandler.startAutho(fingerprintManager, cryptoObject);

                }
            }
        }


        //화면 닫기
        btn_fpclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //jsp로 보내는 코드★★★★★가있었던 자리 -=========================
                try{
                    String result2 = new FingerActivity().execute(scanDeviceAddress).get();
                }catch (Exception e){}

                Intent outIntent = new Intent(getApplicationContext(), TrueMainActivity.class);
                //outIntent.putExtra("done",true);
                setResult(RESULT_OK, outIntent);

                //============================================
                finish();
                //startActivity(outIntent);

            }


        });

    }


    //Cipher Init()
    public boolean cipherInit(){
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }
        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    //Key Generator
    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get KeyGenerator instance", e);
        }

        try {
            keyStore.load(null);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | CertificateException | IOException e){
            throw new RuntimeException(e);
        }
    }

}