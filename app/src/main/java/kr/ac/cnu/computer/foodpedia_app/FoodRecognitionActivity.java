package kr.ac.cnu.computer.foodpedia_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;


public class FoodRecognitionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodrecognition);

        ImageView ivImage = findViewById(R.id.imageView);
        Button foodButton = findViewById(R.id.food1);

        byte[] byteArray = getIntent().getByteArrayExtra("image");
        Bitmap image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        ivImage.setImageBitmap(image);

        foodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FoodNutritionInfoActivity.class);
                startActivity(intent);
            }
        });

    }

}
