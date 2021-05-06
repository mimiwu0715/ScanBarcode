package com.example.scanbarcode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.File;
import java.io.FileNotFoundException;

public class PictureBarcodeActivity extends AppCompatActivity {
    Button btnOpenCamera;
    TextView txtResultBody;

    private Uri imageUri;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private BarcodeDetector detector;
    private static final int CAMERA_REQUEST = 101;
    private static final String SAVED_INSURE_URI = "uri";
    private static final String SAVED_INSTANCE_RESULT = "result";
    private static final String TAG = "API123";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_barcode);
        initViews();
        if (savedInstanceState != null) {
            if (imageUri != null) {
                imageUri = Uri.parse(savedInstanceState.getString(SAVED_INSURE_URI));
                txtResultBody.setText(savedInstanceState.getString(SAVED_INSTANCE_RESULT));
            }
        }

        detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();

        if (!detector.isOperational()) {
            txtResultBody.setText("Detector initialisation failed");
            return;
        }
    }

    private void initViews() {
        txtResultBody = findViewById(R.id.txtResultsBody);
        btnOpenCamera = findViewById(R.id.btnOpenCamera);
        //btnOpenCamera.setOnClickListener((View.OnClickListener) this);
        btnOpenCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ActivityCompat.requestPermissions需要權限
                ActivityCompat.requestPermissions(PictureBarcodeActivity.this, new
                        String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);

            }

        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    takeBarcodePicture();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void takeBarcodePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "pic.jpg");
        imageUri = FileProvider.getUriForFile(PictureBarcodeActivity.this,
                BuildConfig.APPLICATION_ID + ".provider", photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (imageUri != null) {
            outState.putString(SAVED_INSURE_URI, imageUri.toString());
            outState.putString(SAVED_INSTANCE_RESULT, txtResultBody.getText().toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //txtResultBody.setText("");



        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            launchMediaScanIntent();
            try {

                // 通過這個bitmap獲取圖片的寬和高
                Bitmap bitmap = decodeBitmapUri(this, imageUri);
                if (detector.isOperational() && bitmap != null) {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<Barcode> barcodes = detector.detect(frame);
                    for (int index = 0; index < barcodes.size(); index++){
                        Barcode code = barcodes.valueAt(index);
                        txtResultBody.setText(txtResultBody.getText()+"\n" + code.displayValue +"\n" );

                        int type = barcodes.valueAt(index).valueFormat;
                        switch (type){
                            case Barcode.CONTACT_INFO:
                                Log.i(TAG, code.displayValue);
                            break;
                            case Barcode.EMAIL:
                                Log.i(TAG,code.displayValue);
                                break;
                            case Barcode.PHONE:
                                Log.i(TAG, code.phone.number);
                                break;
                            case Barcode.PRODUCT:
                                Log.i(TAG, code.rawValue);
                                break;
                            case Barcode.SMS:
                                Log.i(TAG, code.sms.message);
                                break;
                            case Barcode.TEXT:
                                Log.i(TAG, code.displayValue);
                                break;
                            case Barcode.URL:
                                Log.i(TAG, "url: " + code.displayValue);
                                break;
                            case Barcode.WIFI:
                                Log.i(TAG, code.wifi.ssid);
                                break;
                            case Barcode.GEO:
                                Log.i(TAG, code.geoPoint.lat + ":" + code.geoPoint.lng);
                                break;
                            case Barcode.CALENDAR_EVENT:
                                Log.i(TAG, code.calendarEvent.description);
                                break;
                            case Barcode.DRIVER_LICENSE:
                                Log.i(TAG, code.driverLicense.licenseNumber);
                                break;
                            default:
                                Log.i(TAG, code.rawValue);
                                break;
                        }
                    }
                }


            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to load Image", Toast.LENGTH_SHORT)
                        .show();
                Log.e(TAG, e.toString());
            }
        }


    }

    private Bitmap decodeBitmapUri(Context ctx, Uri uri) throws FileNotFoundException {
        int targetW = 600;
        int targetH = 600;
        //API這樣說：如果該 值設為true那麼將不返回實際的bitmap，也不給其分配內存空間這樣就避免內存溢出了。但是允許我們查詢圖片的信息這其中就包括圖片大小信息
        //https://blog.csdn.net/haha_zhan/article/details/52524117
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
        int photoW = bmOptions.outWidth;//图片原始宽度
        int photoH = bmOptions.outHeight;//图片原始高度

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inSampleSize = scaleFactor;//圖片壓縮比例.
        bmOptions.inJustDecodeBounds = false;

        return BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
    }


    //保存文件成功後通过發送廣播来通知圖库刷新
    private void launchMediaScanIntent() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(imageUri);
        this.sendBroadcast(mediaScanIntent);
    }
}