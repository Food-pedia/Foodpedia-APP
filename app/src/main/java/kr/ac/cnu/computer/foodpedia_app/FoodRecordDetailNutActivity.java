package kr.ac.cnu.computer.foodpedia_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.HashMap;

public class FoodRecordDetailNutActivity extends AppCompatActivity {
    final private static String TAG = "tag";

    String foodName = "";
    String foodIntake = "";
    String energy = "";
    String protein = "";
    String carbs = "";
    String fat = "";
    String sugar = "";
    double intake = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodnutritioninfo);

        ConstraintLayout modifyIntakeLayout = findViewById(R.id.modifyIntakeLayout);
        TextView intakeInput = findViewById(R.id.intakeInput);
        TextView textView13 = findViewById(R.id.textView13);
        Button intakeModifyBtn = findViewById(R.id.intakeModifyBtn);
        Button foodNutSaveBtn = findViewById(R.id.foodNutSaveBtn);

        modifyIntakeLayout.setVisibility(View.GONE);
        textView13.setVisibility(View.GONE);
        intakeModifyBtn.setVisibility(View.GONE);
        foodNutSaveBtn.setVisibility(View.GONE);

        foodName = getIntent().getStringExtra("foodName");
        foodIntake = getIntent().getStringExtra("foodIntake");
        intake = isInteger(foodIntake)? (double) Integer.parseInt(foodIntake) :Double.parseDouble(foodIntake);
        //String foodName = "gimbap";

        TextView foodNameTextView = findViewById(R.id.foodName);
        TextView energyTextView = findViewById(R.id.energy);
        TextView proteinTextView = findViewById(R.id.protein);
        TextView carbsTextView = findViewById(R.id.carbs);
        TextView fatTextView = findViewById(R.id.fat);
        TextView sugarTextView = findViewById(R.id.sugar);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("food").document(foodName).get().addOnCompleteListener(task->{
            //작업이 성공적으로 마쳤을때
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                HashMap foodMap = (HashMap)document.getData();

                energy = foodMap.get("energy").toString();
                protein = foodMap.get("protein").toString();
                carbs = foodMap.get("carbohydrate").toString();
                fat = foodMap.get("fat").toString();
                sugar = foodMap.get("total-sugar").toString();

                foodNameTextView.setText(foodMap.get("korean").toString()); //식품 이름 맞게 출력
                energyTextView.setText(NutToText(energy, intake) + "kcal");   //칼로리 맞게 출력
                proteinTextView.setText(NutToText(protein, intake)+"g");
                carbsTextView.setText(NutToText(carbs, intake)+"g");
                fatTextView.setText(NutToText(fat, intake)+"g");
                sugarTextView.setText(NutToText(sugar, intake)+"g");

            }
            // 데이터를 가져오는 작업이 에러났을 때
            else {
                Log.w(TAG, "Error => ", task.getException());
            }
        });
    }

    private boolean isInteger(String num) {
        try {
            Integer.parseInt(num);    // int 형으로 변환해보고
            return true;                      // 이상없으면 true를 리턴
        }
        catch (NumberFormatException e) {
            return false;                    // 이상 있으면 false를 리턴
        }
    }

    private String NutToText(String nut, Double intake){
        //영양소 섭취량에 맞게 계산
        double CalculateNut = Double.parseDouble(nut)*intake;
        //소수점 둘째자리까지만 나오게 계산
        double decimalizedValue = Math.round(CalculateNut*100)/100.0;
        //불필요한 0 제거
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(decimalizedValue);
    }
}