package kr.ac.cnu.computer.foodpedia_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.HashMap;

public class FoodNutritionInfoActivity extends AppCompatActivity {
    final private static String TAG = "tag";

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

        String foodName = getIntent().getStringExtra("foodName");
        String foodIntake = getIntent().getStringExtra("foodIntake");
        intake = isInteger(foodIntake)? (double) Integer.parseInt(foodIntake) :Double.parseDouble(foodIntake);
        //String foodName = "gimbap";

        TextView foodNameTextView = findViewById(R.id.foodName);
        TextView energyTextView = findViewById(R.id.energy);
        TextView proteinTextView = findViewById(R.id.protein);
        TextView carbsTextView = findViewById(R.id.carbs);
        TextView fatTextView = findViewById(R.id.fat);
        TextView sugarTextView = findViewById(R.id.sugar);
        EditText intakeInput = findViewById(R.id.intakeInput);
        Button intakeModifyBtn = findViewById(R.id.intakeModifyBtn);
        Button foodNutSaveBtn = findViewById(R.id.foodNutSaveBtn);

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

                    intakeInput.setText(foodIntake);
                }
                // 데이터를 가져오는 작업이 에러났을 때
                 else {
                 Log.w(TAG, "Error => ", task.getException());
                 }

        });

        intakeModifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String intakeString = intakeInput.getText().toString();
                try{
                    intake = isInteger(intakeString)? (double) Integer.parseInt(intakeString) :Double.parseDouble(intakeString);
                } catch(Exception e){
                    //섭취량이 빈칸이거나 숫자가 입력되지않았을때
                    Toast.makeText(getApplicationContext(), "섭취량에 숫자를 입력해주세요.",Toast.LENGTH_SHORT).show();
                }

                energyTextView.setText(NutToText(energy, intake)+ "kcal");
                proteinTextView.setText(NutToText(protein, intake)+"g");
                carbsTextView.setText(NutToText(carbs, intake)+"g");
                fatTextView.setText(NutToText(fat, intake)+"g");
                sugarTextView.setText(NutToText(sugar, intake)+"g");
            }
        });

        foodNutSaveBtn.setOnClickListener(new View.OnClickListener() {  //저장 버튼 누르면 식품인식페이지로 섭취량 전달
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FoodRecognitionActivity.class);
                intent.putExtra("modifiedIntakeFoodName", foodName);   //다음 페이지로 식품 이름 전달
                intent.putExtra("modifiedIntake", String.valueOf(intake));  //다음 페이지로 섭취량 전달
                setResult(RESULT_OK, intent);
                finish();
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

//    private String intakeToText(String intake){
//        DecimalFormat df = new DecimalFormat("#.##");
//        return df.format(intake);
//    }
}
