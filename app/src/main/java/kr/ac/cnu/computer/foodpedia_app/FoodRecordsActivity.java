package kr.ac.cnu.computer.foodpedia_app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import kr.ac.cnu.computer.foodpedia_app.tflite.Classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FoodRecordsActivity extends AppCompatActivity {
    final private static String TAG = "tag";
    Double calories = 0.0, fat = 0.0, protein = 0.0, carbohydrate = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodrecords);

        String recordId = getIntent().getStringExtra("recordId");
        TextView recordDate = (TextView) findViewById(R.id.recordDate);
        TextView eatenCalories = (TextView) findViewById(R.id.eatenCalories);
        TextView eatenFat = (TextView) findViewById(R.id.eatenFat);
        TextView eatenProtein = (TextView) findViewById(R.id.eatenProtein);
        TextView eatenCarbohydrate = (TextView) findViewById(R.id.eatenCarbohydrate);

        Handler handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                // UI 작업 수행 불가능
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("foodRecord").document(recordId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        DocumentSnapshot documentSnapshot = task.getResult();
                        HashMap recordMap = (HashMap) documentSnapshot.getData();
                        recordDate.setText(recordMap.get("time").toString());
                        ArrayList<String> foods = (ArrayList<String>) recordMap.get("foods");

                        for (int foodIdx = 0; foodIdx < foods.size(); foodIdx++) {
                            db.collection("food").document(foods.get(foodIdx)).get().addOnCompleteListener(getFoodInfoTask -> {
                                if (getFoodInfoTask.isSuccessful()) {
                                    DocumentSnapshot document = getFoodInfoTask.getResult();
                                    HashMap foodMap = (HashMap) document.getData();

                                    calories += Double.parseDouble(String.valueOf(foodMap.get("energy")));
                                    fat += Double.parseDouble(String.valueOf(foodMap.get("fat")));
                                    protein += Double.parseDouble(String.valueOf(foodMap.get("protein")));
                                    carbohydrate += Double.parseDouble(String.valueOf(foodMap.get("carbohydrate")));
                                    Log.e("=== DEBUG: ", calories + "");
                                }
                                else {
                                    Log.w(TAG, "Error => ", getFoodInfoTask.getException());
                                }
                            });
                        }
                        Log.e("=== DEBUG: ", calories + "");
                    }
                    else {
                        Log.w(TAG, "Error => ", task.getException());
                    }
                });

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // UI 작업 수행 가능
                        eatenCalories.setText(Math.round(calories) + "kcal");
                        eatenFat.setText(Math.round(fat) + "");
                        eatenProtein.setText(Math.round(protein) + "");
                        eatenCarbohydrate.setText(Math.round(carbohydrate) + "");

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
