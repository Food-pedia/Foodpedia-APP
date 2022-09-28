package kr.ac.cnu.computer.foodpedia_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class LogoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        // LogoActivity -> LoginActivity 화면 전환
        Handler hand = new Handler();

        hand.postDelayed(() -> {
            Intent i = new Intent(LogoActivity.this, LoginActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            finish();

        }, 2000);
    }
}