package kr.ac.cnu.computer.foodpedia_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;


public class FoodRecognitionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodrecognition);

        ImageView ivImage = findViewById(R.id.imageView);
        LinearLayout foodButtonLayout = findViewById(R.id.foodButtonLayout);

        int foodNum = 2;    //인식된 식품 개수 !수정필요
        Button[] foodButtons = new Button[foodNum]; //인식된 식품 버튼 저장할 배열
        String[] foodname = {"김치전", "김치찌개"};    //인식된 식품 이름 -> 배열로 되어야할듯 !수정필요
        for(int i=0; i<foodNum; i++){
            //인식된 식품 개수만큼 버튼 생성
            foodButtons[i] = new Button(this);
            foodButtons[i].setText(foodname[i]);
            foodButtonLayout.addView(foodButtons[i]);

            //식품 버튼 누르면 해당 식품영양정보 페이지로 이동
            Button foodButton = foodButtons[i];
            foodButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), FoodNutritionInfoActivity.class);
                    intent.putExtra("foodName",foodButton.getText());   //다음 페이지로 식품 이름 전달
                    startActivity(intent);
                }
            });
        }

//        //이미지 출럭 -> 모델에서 전달받은 이미지로 수정해야함. !수정필요
//        byte[] byteArray = getIntent().getByteArrayExtra("image");
//        Bitmap image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
//        ivImage.setImageBitmap(image);




    }

}
