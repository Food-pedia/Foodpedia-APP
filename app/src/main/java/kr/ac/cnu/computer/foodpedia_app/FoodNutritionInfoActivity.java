package kr.ac.cnu.computer.foodpedia_app;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class FoodNutritionInfoActivity extends AppCompatActivity {
    final private static String TAG = "tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodnutritioninfo);

        TextView foodNameTextView = findViewById(R.id.foodName);
        TextView energyTextView = findViewById(R.id.energy);
        TextView proteinTextView = findViewById(R.id.protein);
        TextView carbsTextView = findViewById(R.id.carbs);
        TextView fatTextView = findViewById(R.id.fat);
        TextView sugarTextView = findViewById(R.id.sugar);

        String foodName = getIntent().getStringExtra("foodName");
        //String foodName = "galbiguyi";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("food").document(foodName).get().addOnCompleteListener(task->{
                //작업이 성공적으로 마쳤을때
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    HashMap foodMap = (HashMap)document.getData();

                    foodNameTextView.setText(foodMap.get("korean").toString()); //식품 이름 맞게 출력
                    energyTextView.setText(foodMap.get("energy").toString() + "kcal");   //칼로리 맞게 출력
                    proteinTextView.setText(foodMap.get("protein").toString() + "g");   //단백질 맞게 출력
                    carbsTextView.setText(foodMap.get("carbohydrate").toString() + "g");   //탄수화물 맞게 출력
                    fatTextView.setText(foodMap.get("fat").toString() + "g");   //지방 맞게 출력
                    sugarTextView.setText(foodMap.get("total-sugar").toString() + "g");   //총당류 맞게 출력
                }
                // 데이터를 가져오는 작업이 에러났을 때
                 else {
                 Log.w(TAG, "Error => ", task.getException());
                 }

        });

    }
}
