package kr.ac.cnu.computer.foodpedia_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kakao.util.helper.Utility.getPackageInfo;

public class MainActivity extends AppCompatActivity {

    TextView userName, calories_num, blood_num, weight_num;
    EditText weight_date_text, weight_text, blood_date_text, blood_text;
    ImageView profile, calorieIcon;
    Button bloodBtn, btn_camera, btn_gallery, input_weightBtn, input_bloodBtn, weight_saveBtn, weight_cancleBtn, blood_cancleBtn, blood_saveBtn;
    final static int TAKE_PICTURE = 1;
    final static int GET_FROM_GALLERY = 2;
    Intent intent;
    View camera_pop;
    PieChart pieChart;
    LineChart lineChart;
    BarChart barChart;
    Double calories = 0.0, fat = 0.0, protein = 0.0, carbohydrate = 0.0;
    DatePickerDialog datePickerDialog;

    long now = System.currentTimeMillis();
    Date now_date = new Date(now);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String getTime = sdf.format(now_date);

    List foodName = new ArrayList<String>();
    List<String> intake = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profile = findViewById(R.id.user_profile);
        userName = findViewById(R.id.user_text);
        input_bloodBtn = findViewById(R.id.input_bloodBtn);
        input_weightBtn = findViewById(R.id.input_weightBtn);
        btn_camera = findViewById(R.id.btn_camera);
        btn_gallery = findViewById(R.id.btn_gallery);
        camera_pop = findViewById(R.id.camera_pop);
        camera_pop.setVisibility(View.GONE);
        pieChart = findViewById(R.id.chart1);
        calories_num = findViewById(R.id.calories_num);
        lineChart = findViewById(R.id.linechart);
        barChart = findViewById(R.id.barchart);
        calorieIcon = findViewById(R.id.calorieIcon);
        blood_num = findViewById(R.id.blood_num);
        weight_num = findViewById(R.id.weight_num);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Glide.with(this).load(R.raw.fire).into(calorieIcon);

        // ***** 카카오 프로필 이미지 *****
        String url = ((GlobalApplication) getApplication()).getKakaoProfile();
        if (url != null) {
            Glide.with(this).load(url).circleCrop().into(profile);
        }

        // ***** 카카오 연동 사용자 이름 *****
        Log.e("카카오 이름 : ", " " + ((GlobalApplication) getApplication()).getKakaoName() + "");
        userName.append(((GlobalApplication) getApplication()).getKakaoName() + "님");
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

        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_gallery:
                        Log.e("갤러리 : ", "들어옴");
                        intent = new Intent(Intent.ACTION_PICK);
                        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                        intent.setType("image/*");
                        startActivityForResult(intent, GET_FROM_GALLERY);
                        break;
                }
            }
        });

        Handler handler = new Handler();

        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                Looper.prepare();
                // UI 작업 수행 불가능
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                List<String> foods = new ArrayList<>();
                List<Double> intake = new ArrayList<>();
                List<Float> weight = new ArrayList<>();
                List<Float> blood = new ArrayList<>();
                List<String> blood_date = new ArrayList<>();

                db.collection("foodRecord").whereEqualTo("member", ((GlobalApplication) getApplication()).getKakaoID())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    String key = document.getId(); // -> document id?
                                    if (key.contains(((GlobalApplication) getApplication()).getKakaoID() + "-" + getTime)) {
                                        HashMap record = (HashMap) document.getData();
                                        for (Object foodName : (ArrayList) record.get("foods")) {
                                            foods.add((String) foodName);
                                        }
                                        for (Object amount : (ArrayList) record.get("intake")) {
                                            intake.add((Double) amount);
                                        }

                                    }
                                }
                            }
                        });

                db.collection("weight").whereEqualTo("user", ((GlobalApplication) getApplication()).getKakaoID())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    HashMap record = (HashMap) document.getData();
                                    Log.e("record : ", record.get("weight").toString());
                                    weight.add(Float.parseFloat(record.get("weight").toString()));
                                }
                            }
                        });

                db.collection("blood").whereEqualTo("user", ((GlobalApplication) getApplication()).getKakaoID())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    HashMap record = (HashMap) document.getData();
                                    Log.e("record : ", record.get("blood").toString());
                                    blood.add(Float.parseFloat(record.get("blood").toString()));
                                    blood_date.add( record.get("date").toString());
                                }
                            }
                        });

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.e("foods ", foods + "");
                Log.e("intake ", intake + "");

                for (int index = 0; index < foods.size(); index++) {

                    int finalIndex = index;
                    db.collection("food").document(foods.get(index)).get().addOnCompleteListener(task -> {
                        //작업이 성공적으로 마쳤을때
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            HashMap foodMap = (HashMap) document.getData();

                            calories += Double.parseDouble(String.valueOf(foodMap.get("energy"))) * Double.parseDouble(String.valueOf(intake.get(finalIndex)));
                            fat += (Double.parseDouble(String.valueOf(foodMap.get("fat"))) * Double.parseDouble(String.valueOf(intake.get(finalIndex))));
                            protein += (Double.parseDouble(String.valueOf(foodMap.get("protein"))) * Double.parseDouble(String.valueOf(intake.get(finalIndex))));
                            carbohydrate += (Double.parseDouble(String.valueOf(foodMap.get("carbohydrate"))) * Double.parseDouble(String.valueOf(intake.get(finalIndex))));
                            Log.e("칼로리 : ", calories + "");
                            Log.e("지방 : ", fat + "");
                            Log.e("단백질 : ", protein + "");
                            Log.e("탄수화물 : ", carbohydrate + "");

                        }
                        // 데이터를 가져오는 작업이 에러났을 때
                        else {
                            Log.w("", "Error => ", task.getException());
                        }

                    });
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // label font
                        Typeface tf = Typeface.createFromAsset(getAssets(), "welcome.ttf");

                        // pie chart 세팅하는 부분
                        ArrayList<PieEntry> numOfIntake1 = new ArrayList<>();

                        numOfIntake1.add(new PieEntry(Math.round(fat), "지방"));
                        numOfIntake1.add(new PieEntry(Math.round(protein), "단백질"));
                        numOfIntake1.add(new PieEntry(Math.round(carbohydrate), "탄수화물"));

                        Description description = new Description();
                        description.setText("섭취 비율"); // label
                        description.setTextSize(12);
                        pieChart.setDescription(description);

                        PieDataSet dataSet1 = new PieDataSet(numOfIntake1, "");
                        dataSet1.setValueTextSize(15);

                        dataSet1.setColors(ColorTemplate.COLORFUL_COLORS);

                        PieData data1 = new PieData(dataSet1);

                        pieChart.setData(data1);
                        pieChart.invalidate();
                        pieChart.getDescription().setEnabled(false);
                        pieChart.animate();
                        pieChart.setNoDataText("No data");
                        Legend leg = pieChart.getLegend();
                        leg.setEnabled(false);

                        calories_num.setText(Integer.parseInt(String.valueOf(Math.round(calories))) + "kcal");

                        /*** 체중 linechart ***/
                        float weight_mean = 0;
                        ArrayList<Entry> values = new ArrayList<>();

                        for (int i = 0; i < weight.size(); i++) {
                            Log.e("확인 : ", weight.get(i) + "");
                            weight_mean += weight.get(i);
                            values.add(new Entry(i, weight.get(i)));
                        }
                        LineDataSet set1;
                        set1 = new LineDataSet(values, "DataSet 1");

                        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                        dataSets.add(set1); // add the data sets

                        // create a data object with the data sets
                        LineData data = new LineData(dataSets);

                        // black lines and points
                        set1.setColor(Color.BLACK);
                        set1.setCircleColor(Color.BLACK);

                        // set data
                        lineChart.setData(data);

                        set1.setLineWidth(2);
                        set1.setCircleRadius(6);
                        set1.setCircleColor(Color.parseColor("#FFA1B4DC"));
                        set1.setCircleColorHole(Color.BLUE);
                        set1.setColor(Color.parseColor("#FFA1B4DC"));
                        set1.setDrawCircleHole(true);
                        set1.setDrawCircles(true);
                        set1.setDrawHorizontalHighlightIndicator(false);
                        set1.setDrawHighlightIndicators(false);
                        set1.setValueTextSize(10f);
                        set1.setValueTypeface(tf);
                        set1.setValueFormatter(new MyValueFormatter2());
//                        set1.setDrawValues(false);

                        XAxis xAxis = lineChart.getXAxis();
                        xAxis.setDrawLabels(false);
                        xAxis.setDrawAxisLine(false);
//                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//                        xAxis.setTextColor(Color.BLACK);
//                        xAxis.enableGridDashedLine(8, 24, 0);

                        YAxis yLAxis = lineChart.getAxisLeft();
                        yLAxis.setDrawLabels(false);
                        yLAxis.setDrawAxisLine(false);
//                        yLAxis.setTextColor(Color.BLACK);

                        YAxis yRAxis = lineChart.getAxisRight();
                        yRAxis.setDrawLabels(false);
                        yRAxis.setDrawAxisLine(false);
//                        yRAxis.setDrawGridLines(false);

                        // 격자 없애기
                        lineChart.getAxisLeft().setDrawGridLines(false);
                        lineChart.getAxisRight().setDrawGridLines(false);
                        lineChart.getXAxis().setDrawGridLines(false);

                        lineChart.setDoubleTapToZoomEnabled(false);
                        lineChart.setDrawGridBackground(false);
                        lineChart.setDescription(description);
                        lineChart.animateY(2000, Easing.EasingOption.EaseInCubic);
                        lineChart.invalidate();

                        weight_mean /= weight.size();
                        weight_num.setText(Math.round(weight.get(weight.size()-1))+"kg");

                        Legend leg2 = lineChart.getLegend();
                        leg2.setEnabled(false);

                        /*** 혈당량 barchart ***/
                        ArrayList<BarEntry> bar_values = new ArrayList<>();
                        ArrayList<String> theDates = new ArrayList<>();
                        int blood_mean = 0;

                        for (int i = 0; i < blood.size(); i++) {
                            Log.e("확인 : ", weight.get(i) + "");
                            bar_values.add(new BarEntry(i, blood.get(i)));
                            blood_mean+= blood.get(i);
                            theDates.add(blood_date.get(i));
                        }

                        BarDataSet barDataSet = new BarDataSet(bar_values, "");

                        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(theDates));
                        BarData theData = new BarData(barDataSet);//----Line of error
                        barChart.setData(theData);

                        YAxis leftAxis = barChart.getAxisLeft();
                        YAxis yRAxiss = barChart.getAxisRight();
                        yRAxiss.setDrawLabels(false);
                        yRAxiss.setDrawAxisLine(false);
                        leftAxis.setDrawAxisLine(false);
                        leftAxis.setDrawLabels(false);
                        barDataSet.setValueTypeface(tf);

                        barChart.getAxisRight();
//                        leftAxis.setTypeface(tf);

                        XAxis xAxist = barChart.getXAxis();
                        xAxist.setTypeface(tf);

                        Legend l = barChart.getLegend();
                        l.setTypeface(tf);

                        // 라벨 제거
                        barChart.getLegend().setEnabled(false);

                        barChart.setScaleEnabled(true);
                        barChart.setTouchEnabled(false);

                        // 격자 없애기
                        barChart.getAxisLeft().setDrawGridLines(false);
                        barChart.getAxisRight().setDrawGridLines(false);
                        barChart.getXAxis().setDrawGridLines(false);


                        barDataSet.setValueFormatter(new MyValueFormatter());
                        XAxis xAxiss = barChart.getXAxis();
                        xAxiss.setPosition(XAxis.XAxisPosition.BOTTOM);
                        barChart.getAxisLeft().setAxisMinimum(0);
                        barChart.getAxisRight().setAxisMinimum(0);

                        barChart.animateY(1000);

                        // description 삭제
                        barChart.getDescription().setEnabled(false);
                        barDataSet.setValueTextSize(15f);

                        blood_mean /= blood.size();
                        blood_num.setText(Math.round(blood.get(blood.size()-1))+"");


                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                });
                Looper.loop();
            }
        }).start();

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
                        camera_pop.setVisibility(View.VISIBLE);
//                        bloodBtn.setVisibility(View.GONE);
                        return true;
                    case R.id.Records:
                        final Intent intent3 = new Intent(MainActivity.this, CalendarActivity.class);
                        startActivity(intent3);
                        finish();
                        overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
                        return true;
                }
                return false;
            }
        });
        /************* 하단바 *************/

        /*** 체중, 혈당량 입력 ***/
        input_weightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.input_weight, null);

                ll.setFocusable(true); // 외부 영역 선택시 PopUp 종료

                LinearLayout.LayoutParams paramll = new LinearLayout.LayoutParams
                        (LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);

                addContentView(ll, paramll);


                weight_date_text = ll.findViewById(R.id.weight_date_text);
                weight_text = ll.findViewById(R.id.weight_text);
                weight_saveBtn = ll.findViewById(R.id.weight_saveBtn);
                weight_cancleBtn = ll.findViewById(R.id.weight_cancleBtn);

                weight_date_text.setOnClickListener(new View.OnClickListener() {
                    String date2;

                    @Override
                    public void onClick(View view) {
                        if (weight_date_text.isClickable()) {
                            //오늘 날짜(년,월,일) 변수에 담기
                            Calendar calendar = Calendar.getInstance();
                            int pYear = calendar.get(Calendar.YEAR); //년
                            int pMonth = calendar.get(Calendar.MONTH);//월
                            int pDay = calendar.get(Calendar.DAY_OF_MONTH);//일


                            datePickerDialog = new DatePickerDialog(MainActivity.this,
                                    new DatePickerDialog.OnDateSetListener() {
                                        @Override
                                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                                            //1월은 0부터 시작하기 때문에 +1을 해준다.
                                            month = month + 1;
                                            String date = year + "/" + month + "/" + day;
                                            date2 = year + "-" + month + "-" + day;

                                            weight_date_text.setText(date);
                                        }
                                    }, pYear, pMonth, pDay);
                            datePickerDialog.show();
                        }
                        weight_saveBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                HashMap<String, Object> weightHash = new HashMap<>();
                                weightHash.put("user", ((GlobalApplication) getApplication()).getKakaoID());
                                weightHash.put("weight", weight_text.getText().toString());
                                weightHash.put("date", date2);

                                db.collection("weight").document(((GlobalApplication) getApplication()).getKakaoID() + "-" + date2)
                                        .set(weightHash)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                ((ViewManager) ll.getParent()).removeView(ll);
                                            }
                                        });

                            }
                        });
                        weight_cancleBtn.setOnClickListener(v -> ((ViewManager) ll.getParent()).removeView(ll));
                    }

                });

            }
        });

        input_bloodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.input_blood, null);

                ll.setFocusable(true); // 외부 영역 선택시 PopUp 종료

                LinearLayout.LayoutParams paramll = new LinearLayout.LayoutParams
                        (LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);

                addContentView(ll, paramll);


                blood_date_text = ll.findViewById(R.id.blood_date_text);
                blood_text = ll.findViewById(R.id.blood_text);
                blood_saveBtn = ll.findViewById(R.id.blood_saveBtn);
                blood_cancleBtn = ll.findViewById(R.id.blood_cancleBtn);

                blood_date_text.setOnClickListener(new View.OnClickListener() {
                    String date2;

                    @Override
                    public void onClick(View view) {
                        if (blood_date_text.isClickable()) {
                            //오늘 날짜(년,월,일) 변수에 담기
                            Calendar calendar = Calendar.getInstance();
                            int pYear = calendar.get(Calendar.YEAR); //년
                            int pMonth = calendar.get(Calendar.MONTH);//월
                            int pDay = calendar.get(Calendar.DAY_OF_MONTH);//일


                            datePickerDialog = new DatePickerDialog(MainActivity.this,
                                    new DatePickerDialog.OnDateSetListener() {
                                        @Override
                                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                                            //1월은 0부터 시작하기 때문에 +1을 해준다.
                                            month = month + 1;
                                            String date = year + "/" + month + "/" + day;
                                            date2 = year + "-" + month + "-" + day;

                                            blood_date_text.setText(date);
                                        }
                                    }, pYear, pMonth, pDay);
                            datePickerDialog.show();
                        }
                        blood_saveBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                HashMap<String, Object> weightHash = new HashMap<>();
                                weightHash.put("user", ((GlobalApplication) getApplication()).getKakaoID());
                                weightHash.put("blood", blood_text.getText().toString());
                                weightHash.put("date", date2);

                                db.collection("blood").document(((GlobalApplication) getApplication()).getKakaoID() + "-" + date2)
                                        .set(weightHash)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                ((ViewManager) ll.getParent()).removeView(ll);
                                            }
                                        });
                            }
                        });
                        blood_cancleBtn.setOnClickListener(v ->
                                ((ViewManager) ll.getParent()).removeView(ll));
                    }

                });

            }
        });
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

    String getDateFromTimeFormat(String time) {
        return time.substring(0, 10);
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
    public class MyValueFormatter implements IValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            // write your logic here
            return mFormat.format(value);
        }
    }

    public class MyValueFormatter2 implements IValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter2() {
            mFormat = new DecimalFormat("###,###,##0"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            // write your logic here
            return mFormat.format(value)+"kg";
        }
    }
}