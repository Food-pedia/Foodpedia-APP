package kr.ac.cnu.computer.foodpedia_app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class FoodRecordsActivity extends AppCompatActivity {
    final private static String TAG = "tag";
    Long calories = 0L, fat = 0L, protein = 0L, carbohydrate = 0L;

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


                            calories += (Long) foodMap.get("energy");
                            fat += (Integer) foodMap.get("fat");
                            protein += (Integer) foodMap.get("protein");
                            carbohydrate += (Integer) foodMap.get("carbohydrate");
                            Log.e("=== 중간 계산", calories + " " + fat + " " + protein + " " + carbohydrate);
                        }
                        else {
                            Log.w(TAG, "Error => ", getFoodInfoTask.getException());
                        }
                    });
                }

                eatenCalories.setText(calories + "kcal");
                eatenFat.setText(fat + "");
                eatenProtein.setText(protein + "");
                eatenCarbohydrate.setText(carbohydrate + "");
            }
             else {
                Log.w(TAG, "Error => ", task.getException());
            }
        });
//        db.collection("food").document(foodName).get().addOnCompleteListener(task->{
//            //작업이 성공적으로 마쳤을때
//            if (task.isSuccessful()) {
//                DocumentSnapshot document = task.getResult();
//                HashMap foodMap = (HashMap)document.getData();
//
//                energy = foodMap.get("energy").toString();
//                protein = foodMap.get("protein").toString();
//                carbs = foodMap.get("carbohydrate").toString();
//                fat = foodMap.get("fat").toString();
//                sugar = foodMap.get("total-sugar").toString();
//
//                foodNameTextView.setText(foodMap.get("korean").toString()); //식품 이름 맞게 출력
//                energyTextView.setText(NutToText(energy, intake) + "kcal");   //칼로리 맞게 출력
//                proteinTextView.setText(NutToText(protein, intake)+"g");
//                carbsTextView.setText(NutToText(carbs, intake)+"g");
//                fatTextView.setText(NutToText(fat, intake)+"g");
//                sugarTextView.setText(NutToText(sugar, intake)+"g");
//
//                intakeInput.setText(foodIntake);
//            }
//            // 데이터를 가져오는 작업이 에러났을 때
//            else {
//                Log.w(TAG, "Error => ", task.getException());
//            }
//
//        });
    }
}
