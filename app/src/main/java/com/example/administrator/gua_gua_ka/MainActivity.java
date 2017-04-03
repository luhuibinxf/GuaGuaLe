package com.example.administrator.gua_gua_ka;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.administrator.gua_gua_ka.view.GuaGuaKa;

public class MainActivity extends AppCompatActivity {
    GuaGuaKa mGGk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGGk= (GuaGuaKa) findViewById(R.id.main_ggk);
        mGGk.setOnGuaGuaKaCompleteListener(new GuaGuaKa.OnGuaGuaKaCompleteListener() {
            @Override
            public void complete() {
                Toast.makeText(MainActivity.this, "挂了多少啊", Toast.LENGTH_SHORT).show();
            }
        });
        mGGk.setText("我是咯阿达");
    }
}
