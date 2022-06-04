package kr.ac.cnu.computer.foodpedia_app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FoodNutritionInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodnutritioninfo);

        String foodName = getIntent().getStringExtra("foodName");
        TextView foodNameTextView = findViewById(R.id.foodName);

        //식품 이름 맞게 출력
        foodNameTextView.setText(foodName);

        //
    }
}
