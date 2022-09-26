package kr.ac.cnu.computer.foodpedia_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import kr.ac.cnu.computer.foodpedia_app.tflite.Classifier;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FoodRecordsActivity extends AppCompatActivity {
    final private static String TAG = "tag";
    public enum ViewMode {
        DAY, TODAY
    }
    Double calories = 0.0, fat = 0.0, protein = 0.0, carbohydrate = 0.0;
    String recordDate = "";
    String mode;
//    PieChart pieChartFat, pieChartProtein, pieChartCarb;
    // 일단 한번에 표현

    ImageView imageView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    String getTodayFromLocalDate() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    String getDateFromTimeFormat(String time) {
        return time.substring(0, 10);
    }

    String getDateDay(String date) throws ParseException {
        String day = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date nDate = dateFormat.parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(nDate);
        int dayNum = cal.get(Calendar.DAY_OF_WEEK);
        switch (dayNum) {
            case 1:
                day = "일";
                break;
            case 2:
                day = "월";
                break;
            case 3:
                day = "화";
                break;
            case 4:
                day = "수";
                break;
            case 5:
                day = "목";
                break;
            case 6:
                day = "금";
                break;
            case 7:
                day = "토";
                break;
        }
        return day;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodrecords);

        mode = getIntent().getStringExtra("mode");
        imageView = (ImageView) findViewById(R.id.imageView1);
        recordDate = (ViewMode.valueOf(mode) == ViewMode.DAY) ? getIntent().getStringExtra("recordDate") : getTodayFromLocalDate();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl("gs://food-pedia-d2bbc.appspot.com/");
        Log.e("=== download the image" , ((GlobalApplication) getApplication()).getKakaoID() + "");
        storageReference = storageReference.child("images/" + ((GlobalApplication) getApplication()).getKakaoID() + "/" + recordDate + "/");


        storageReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference item: listResult.getItems()) {
                    LinearLayout imageView = (LinearLayout) findViewById(R.id.imageView);

                    // image view 동적 생성
                    ImageView iv = new ImageView(FoodRecordsActivity.this);
                    iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    imageView.addView(iv);

                    item.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Glide.with(FoodRecordsActivity.this)
                                        .load(task.getResult())
                                        .into(iv);
                            } else {
                                Toast.makeText(FoodRecordsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Log.w("=== download image", "error!");
                        }
                    });
                }
            }
        });

        TextView recordDateTextView = (TextView) findViewById(R.id.recordDate);
        TextView eatenCaloriesTextView = (TextView) findViewById(R.id.eatenCalories);
        TextView eatenFatTextView = (TextView) findViewById(R.id.eatenFat);
        TextView eatenProteinTextView = (TextView) findViewById(R.id.eatenProtein);
        TextView eatenCarbohydrateTextView = (TextView) findViewById(R.id.eatenCarbohydrate);
        Handler handler = new Handler();

//        pieChartFat = (PieChart) findViewById(R.id.pieChartFat);
//        pieChartProtein = (PieChart) findViewById(R.id.pieChartProtein);
//        pieChartCarb = (PieChart) findViewById(R.id.pieChartCarb);

        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                Looper.prepare();
                // UI 작업 수행 불가능
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                List<String> foods = new ArrayList<>();
                List<Double> intake = new ArrayList<>();

                db.collection("foodRecord").whereEqualTo("member", ((GlobalApplication) getApplication()).getKakaoID())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    String key = document.getId(); // -> document id?
                                    if (key.contains(((GlobalApplication) getApplication()).getKakaoID() + "-" + recordDate)) {
                                        HashMap record = (HashMap) document.getData();
                                        for (Object foodName : (ArrayList)record.get("foods")) {
                                            foods.add((String) foodName);
                                        }
                                        for (Object amount : (ArrayList)record.get("intake")) {
                                            intake.add((Double) amount);
                                        }

                                    }
                                }
                            }
                        });

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for (int index = 0; index< foods.size(); index++) {

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
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        // UI 작업 수행 가능
                        try {
                            recordDateTextView.setText(recordDate + " (" + getDateDay(recordDate) + ")");
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        eatenCaloriesTextView.setText(Math.round(calories) + "kcal");
                        eatenFatTextView.setText(Math.round(fat) + "");
                        eatenProteinTextView.setText(Math.round(protein) + "");
                        eatenCarbohydrateTextView.setText(Math.round(carbohydrate) + "");

                        // pie chart 세팅하는 부분

                        PieChart pieChart = (PieChart) findViewById(R.id.pieChart);
                        ArrayList<PieEntry> numOfIntake = new ArrayList<>();

                        numOfIntake.add(new PieEntry(Math.round(fat),"fat"));
                        numOfIntake.add(new PieEntry(Math.round(protein), "protein"));
                        numOfIntake.add(new PieEntry(Math.round(carbohydrate), "carbohydrate"));
                        Log.e("=== DEBUG", numOfIntake + "");

                        Description description = new Description();
                        description.setText("섭취 비율"); // label
                        description.setTextSize(15);
                        pieChart.setDescription(description);

                        PieDataSet dataSet = new PieDataSet(numOfIntake, "");
                        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

                        PieData data = new PieData(dataSet);
                        pieChart.setData(data);
                        pieChart.invalidate();
                        pieChart.getDescription().setEnabled(false);
                        pieChart.animate();
                        pieChart.setNoDataText("No data");


                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                });
                Looper.loop();
            }
        }).start();
    }
}
