package com.example.scanbarcode;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EmailActivity extends AppCompatActivity implements View.OnClickListener{
    EditText inSubject, inBody;
    TextView txtEmailAddress;
    Button btnSendEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);
        initView1();
        initView2();
    }

    private void initView1(){
        inSubject = findViewById(R.id.inSubject);
        inBody = findViewById(R.id.inBody);
        txtEmailAddress = findViewById(R.id.txtEmailAddress);
    }
    private void initView2(){
        btnSendEmail = findViewById(R.id.btnSendEmail);
        btnSendEmail.setOnClickListener(this);
        if (getIntent().getStringExtra("email_adress") != null) {
            txtEmailAddress.setText("Recipient : " + getIntent().getStringExtra("email_adress"));
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSendEmail:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{txtEmailAddress.getText().toString()});
                intent.putExtra(Intent.EXTRA_SUBJECT, inSubject.getText().toString().trim());
                intent.putExtra(Intent.EXTRA_TEXT, inBody.getText().toString().trim());

                startActivity(Intent.createChooser(intent, "Send Email"));
                break;
        }
    }
}


