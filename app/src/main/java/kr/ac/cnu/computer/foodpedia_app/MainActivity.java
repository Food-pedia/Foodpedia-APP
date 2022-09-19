package kr.ac.cnu.computer.foodpedia_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.text.Layout;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static com.kakao.util.helper.Utility.getPackageInfo;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("키 해쉬 : ", getKeyHash(this));

//        // PieChart 메소드
//        PieChart pieChart = (PieChart) findViewById(R.id.chart1);
//        ArrayList<Entry> entries = new ArrayList<>();
//        for(int i=0; i < valList.size();i++){
//            entries.add(new Entry((Integer) valList.get(i), i));
//        }
//        PieDataSet depenses = new PieDataSet (entries, "월별 가입자수");
//        depenses.setAxisDependency(YAxis.AxisDependency.LEFT);
//        ArrayList<String> labels = new ArrayList<String>();
//        for(int i=0; i < labelList.size(); i++){
//            labels.add((String) labelList.get(i));
//        }
//        PieData data = new PieData(labels,depenses); // 라이브러리 v3.x 사용하면 에러 발생함
//        depenses.setColors(ColorTemplate.COLORFUL_COLORS);
//        pieChart.setData(data);
//        pieChart.animateXY(1000,1000);
//        pieChart.invalidate();

        /************* 하단바 *************/
        BottomNavigationView bottomNav = findViewById(R.id.navigationView);

        // item selection part
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
//                    case R.id.Today:
//                        final Intent intent = new Intent(StepCountChart.this, MainActivity.class);
//                        startActivity(intent);
//
//                        finish();
//                        overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
//                        return true;

                    case R.id.Camera:
                        LinearLayout camera_pop = findViewById(R.id.camera_pop);
                        camera_pop.setVisibility(View.VISIBLE);
                        camera_pop.bringToFront();
//                    case R.id.Records:
//                        final Intent intent3 = new Intent(StepCountChart.this, UserInfo.class);
//                        startActivity(intent3);
//                        finish();
//                        overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
//                        return true;
                }
                return false;
            }
        });
        /************* 하단바 *************/
    }


    public static String getKeyHash(final Context context) {
        PackageInfo packageInfo = getPackageInfo(context, PackageManager.GET_SIGNATURES);
        if (packageInfo == null)
            return null;

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                return Base64.encodeToString(md.digest(), Base64.NO_WRAP);
            } catch (NoSuchAlgorithmException e) {
                Log.w("TAG", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
        return null;
    }


}