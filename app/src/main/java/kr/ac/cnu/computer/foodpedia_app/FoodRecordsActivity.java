package kr.ac.cnu.computer.foodpedia_app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import kr.ac.cnu.computer.foodpedia_app.tflite.Classifier;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FoodRecordsActivity extends AppCompatActivity {
    final private static String TAG = "tag";
    public enum ViewMode {
        UPDATE, DAY, TODAY
    }
    Double calories = 0.0, fat = 0.0, protein = 0.0, carbohydrate = 0.0;
    String recordDate = "";
    String mode;
//    PieChart pieChartFat, pieChartProtein, pieChartCarb;
    // 일단 한번에 표현

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodrecords);

        mode = getIntent().getStringExtra("mode");
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
                switch (ViewMode.valueOf(mode)) {
                    case UPDATE:
                        String recordId = getIntent().getStringExtra("recordId");
                        db.collection("foodRecord").document(recordId).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                                DocumentSnapshot documentSnapshot = task.getResult();
                                HashMap recordMap = (HashMap) documentSnapshot.getData();
                                ArrayList<String> foods = (ArrayList<String>) recordMap.get("foods");
                                recordDate = getDateFromTimeFormat(recordMap.get("time").toString());

                                for (int foodIdx = 0; foodIdx < foods.size(); foodIdx++) {
                                    db.collection("food").document(foods.get(foodIdx)).get().addOnCompleteListener(getFoodInfoTask -> {
                                        if (getFoodInfoTask.isSuccessful()) {
                                            DocumentSnapshot document = getFoodInfoTask.getResult();
                                            HashMap foodMap = (HashMap) document.getData();

                                            calories += Double.parseDouble(String.valueOf(foodMap.get("energy")));
                                            fat += Double.parseDouble(String.valueOf(foodMap.get("fat")));
                                            protein += Double.parseDouble(String.valueOf(foodMap.get("protein")));
                                            carbohydrate += Double.parseDouble(String.valueOf(foodMap.get("carbohydrate")));
                                        }
                                        else {
                                            Log.w(TAG, "Error => ", getFoodInfoTask.getException());
                                        }
                                    });
                                }
                            }
                            else {
                                Log.w(TAG, "Error => ", task.getException());
                            }
                        });
                        break;
                    case DAY: case TODAY:
                        recordDate = (ViewMode.valueOf(mode) == ViewMode.DAY) ? getIntent().getStringExtra("recordDate") : getTodayFromLocalDate();
                        db.collection("foodRecord").get().addOnSuccessListener( result -> {
                            for (QueryDocumentSnapshot document : result) {
                                HashMap record = (HashMap) document.getData();
                                if (!record.get("time").equals("")) { // test data에 time이 없는 데이터가 있으므로
                                    if (getDateFromTimeFormat(record.get("time").toString()).equals(recordDate)) {
                                        ArrayList<String> foods = (ArrayList<String>) record.get("foods");

                                        for (int foodIdx = 0; foodIdx < foods.size(); foodIdx++) {
                                            db.collection("food").document(foods.get(foodIdx)).get().addOnCompleteListener(getFoodInfoTask -> {
                                                if (getFoodInfoTask.isSuccessful()) {
                                                    DocumentSnapshot foodSnapshot = getFoodInfoTask.getResult();
                                                    HashMap foodMap = (HashMap) foodSnapshot.getData();

                                                    calories += Double.parseDouble(String.valueOf(foodMap.get("energy")));
                                                    fat += Double.parseDouble(String.valueOf(foodMap.get("fat")));
                                                    protein += Double.parseDouble(String.valueOf(foodMap.get("protein")));
                                                    carbohydrate += Double.parseDouble(String.valueOf(foodMap.get("carbohydrate")));
                                                }
                                                else {
                                                    Log.w(TAG, "Error => ", getFoodInfoTask.getException());
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }).addOnFailureListener( exception -> {
                            Log.w("=== FoodRecords", "ViewMode.DAY" + exception);
                        });
                        break;
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        Log.e("=== DEBUG: ", calories + "");

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
                        Log.e("=== DEBUG1: ", Math.round(fat) + "");
                        numOfIntake.add(new PieEntry(Math.round(protein), "protein"));
                        Log.e("=== DEBUG2: ", Math.round(protein) + "");
                        numOfIntake.add(new PieEntry(Math.round(carbohydrate), "carbohydrate"));                                        Log.e("=== DEBUG: ", calories + "");
                        Log.e("=== DEBUG3: ", Math.round(carbohydrate) + "");
                        Log.e("=== numOfIntake", numOfIntake + "");

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
