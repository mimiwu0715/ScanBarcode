package com.example.scanbarcode;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

//Android遊戲開發中常用的三種視圖是:
//View：必須在UI主線程內更新畫面，速度較慢，提供圖形繪製函數、觸屏事件、按鍵事件函數等
//SurfaceView：適合2D遊戲的開發；是view的子類，類似使用雙緩機制，在新的線程中更新畫面所以刷新介面速度比view快。
//GLSurfaceView：基於SurfaceView視圖再次進行拓展的視圖類，專用於3D遊戲開發的視圖；是SurfaceView的子類，openGL專用。


public class ScannedBarcodeActivity extends AppCompatActivity {
    SurfaceView surfaceView;//擁有獨立的繪圖表面，而且獨立線程不會占用主線程資源
    TextView txtBarcodeValue;

    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    Button btnAction;
    String intentData = "";
    boolean isEmail = false;

    // Activity啟動後出現在手機螢幕上，之後再由使用者按下返回鍵結束Activity，各個Callback方法執行時機說明如下：
   // 當Activity準備要產生時，先呼叫onCreate方法。
   // Activity產生後（還未出現在手機螢幕上），呼叫onStart方法。
   //當Activity出現手機上後，呼叫onResume方法。
   //當使用者按下返回鍵結束Activity時， 先呼叫onPause方法。
   //當Activity從螢幕上消失時，呼叫onStop方法。
   // 最後完全結束Activity之前，呼叫onDestroy方法。
   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_barcode);
        initViews();
    }

    private void initViews() {
        txtBarcodeValue = findViewById(R.id.txtBarcodeValue);
        surfaceView = findViewById(R.id.surfaceView);
        btnAction = findViewById(R.id.btnAction);

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intentData.length() > 0) {
                    if(isEmail)
                        startActivity(new Intent(ScannedBarcodeActivity.this,EmailActivity.class).putExtra("email_address",intentData));
                    else {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(intentData)));
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {

        super.onResume();
        initialiseDectectorsAndSources();
    }

    @Override
    protected void onPause(){
       super.onPause();
       cameraSource.release();
    }

    @Override
    protected void onDestroy() {
       super.onDestroy();
       cameraSource.release();
    }

    //如何掃描 QRcode(https://ithelp.ithome.com.tw/articles/10242559)
    //第一步：在build.gradle添加依賴原件
    //第二步：在你的布局文件xml上增加一個TextView 和一個SurfaceView
    //第三步：定義需要的原件、相機、Google的Vision套件
    //第四步：使用套件
    //第五步：使用surfaceView 來顯示
    //第六步：開始讀寫條碼
    private void initialiseDectectorsAndSources(){
        Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();
        //條碼檢測器
         barcodeDetector = new BarcodeDetector.Builder( this)
         .setBarcodeFormats(Barcode.ALL_FORMATS)
         .build();

        cameraSource = new CameraSource.Builder(this,barcodeDetector)
        .setRequestedPreviewSize(1920,1080)
        .setAutoFocusEnabled(true)   //要加自動對焦
        .build() ;

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(ScannedBarcodeActivity.this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED){
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(ScannedBarcodeActivity.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) { cameraSource.stop(); }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if(barcodes.size() != 0) {

                 txtBarcodeValue.post(new Runnable() {
                     @Override
                     public void run(){
                         if (barcodes.valueAt(0).email != null){
                             txtBarcodeValue.removeCallbacks(null);
                             intentData = barcodes.valueAt(0).email.address;
                             txtBarcodeValue.setText(intentData);
                             isEmail = true;
                             btnAction.setText("ADD CONTENT TO THE MAIL");
                         } else {
                             isEmail = false;
                             btnAction.setText("LAUNCH URL");
                             intentData = barcodes.valueAt(0).displayValue;
                             txtBarcodeValue.setText(intentData);
                         }
                     }
                 });
                }

            }
        });

    }

}