package com.example.scanbarcode;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    Button btnTakePicture, btnScanBarcode;
TextView txta;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        //View btnTakePicture = findViewById(R.id.btnTakePicture);
        //View btnScanBarcode = findViewById(R.id.btnScanBarcode);
        //btnTakePicture.setOnClickListener((View.OnClickListener) this);
        //btnScanBarcode.setOnClickListener(this);
        btnTakePicture = (Button)findViewById(R.id.btnTakePicture);
        //txta = (TextView)findViewById(R.id.textView);
        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PictureBarcodeActivity.class));

            //   int c = 5;
            //   int d = 6;
            //   int e = c+d;
            //    Log.d("math", "c="+c+"; d="+d+"; e="+e);
            //    txta.setText(String.valueOf(e));
            }
        });
        btnScanBarcode = (Button)findViewById(R.id.btnScanBarcode);
        btnScanBarcode.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this, ScannedBarcodeActivity.class));

        }
});

    }
}