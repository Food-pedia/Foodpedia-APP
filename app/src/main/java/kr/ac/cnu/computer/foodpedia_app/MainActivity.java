package kr.ac.cnu.computer.foodpedia_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
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

    TextView userName;
    EditText date;
    ImageView profile;
    Button bloodBtn, btn_camera, btn_gallery;
    final static int TAKE_PICTURE = 1;
    final static int GET_FROM_GALLERY = 2;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profile = findViewById(R.id.user_profile);
        userName = findViewById(R.id.user_text);
        bloodBtn = findViewById(R.id.bloodBtn);
        btn_camera = findViewById(R.id.btn_camera);
        btn_gallery = findViewById(R.id.btn_gallery);

        // ***** 카카오 프로필 이미지 *****
        String url = ((GlobalApplication) getApplication()).getKakaoProfile();
        if (url != null) {
            Glide.with(this).load(url).circleCrop().into(profile);
        }

        // ***** 카카오 연동 사용자 이름 *****
        Log.e("카카오 이름 : "," "+((GlobalApplication)getApplication()).getKakaoName()+"");
        userName.append(((GlobalApplication)getApplication()).getKakaoName()+"님");
        Log.e("키 해쉬 : ", getKeyHash(this));

        // ***** 카메라, 갤러리 *****
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d("LOG", "권한 설정 완료");
            } else {
                Log.d("LOG", "권한 설정 요청");
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_camera:
                        intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, TAKE_PICTURE);
                        break;
                }
            }
        });

        btn_gallery.setOnClickListener(v -> {
            switch (v.getId()) {
                case R.id.btn_gallery:
                    intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    intent.setType("image/*");
                    startActivityForResult(intent, GET_FROM_GALLERY);
                    break;
            }
        });


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
                    case R.id.Today:
                        final Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(intent);

                        finish();
                        overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                        return true;

                    case R.id.Camera:
                        View camera_pop = findViewById(R.id.camera_pop);
                        camera_pop.setVisibility(View.VISIBLE);
//                        bloodBtn.setVisibility(View.GONE);
                    case R.id.Records:
                        final Intent intent3 = new Intent(MainActivity.this, FoodRecordsActivity.class);
                        startActivity(intent3);
                        finish();
                        overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
                        return true;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case TAKE_PICTURE: // 카메라로 촬영하는 경우
                if (resultCode == RESULT_OK && intent.hasExtra("data")) {
                    Bitmap bitmap = (Bitmap) intent.getExtras().get("data");
//                    bitmap = bitmap.createScaledBitmap(bitmap,640,640,true);
//                    if (bitmap != null) {
//                       ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                       float scale = (float) (1024/(float)bitmap.getWidth());
//                       int image_w = (int) (bitmap.getWidth() * scale);
//                       int image_h = (int) (bitmap.getHeight() * scale);
//                       Bitmap resize = Bitmap.createScaledBitmap(bitmap, image_w, image_h, true);
//                       resize.compress(Bitmap.CompressFormat.JPEG, 100, stream);
////                       byte[] byteArray = stream.toByteArray();

                    Intent intent2 = new Intent(this, FoodRecognitionActivity.class);
                    intent2.putExtra("image", bitmap);
                    startActivity(intent2);
                }
                break;
            case GET_FROM_GALLERY:
                Log.e("DEBUG : result code is ", resultCode + "");
                if (resultCode == RESULT_OK) {
                    Uri uri = intent.getData();
                    Log.e("DEBUG :  ", uri + "");
                    GlideApp.with(getApplicationContext()).asBitmap().load(uri).override(300, 300).into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Bitmap bitmap = resource;
                            Log.e("DEBUG : ", bitmap + "");
                            Intent intent2 = new Intent(MainActivity.this, FoodRecognitionActivity.class);
                            intent2.putExtra("image", bitmap);
                            startActivity(intent2);
                        }
                    });
                }
                break;
        }
    }
}