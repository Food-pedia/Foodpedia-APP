package kr.ac.cnu.computer.foodpedia_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.ac.cnu.computer.foodpedia_app.tflite.Classifier;

public class FoodRecordDetailActivity extends AppCompatActivity {
    final int FEEDBACK_EMOJI_EXCELLENT = 2131362268;
    final int FEEDBACK_EMOJI_GOOD = 2131362269;
    final int FEEDBACK_EMOJI_NEUTRAL = 2131362270;
    final int FEEDBACK_EMOJI_BAD = 2131362271;
    final int FEEDBACK_EMOJI_TERRIBLE = 2131362272;

    final int FEEDBACK_TEXT_FIRST = 2131361944;
    final int FEEDBACK_TEXT_SECOND = 2131361945;
    final int FEEDBACK_TEXT_THIRD = 2131361946;
    final int FEEDBACK_TEXT_FOURTH = 2131361947;
    final int FEEDBACK_TEXT_FIFTH = 2131361948;

    List<String> foodName;    //인식된 식품 이름들(영어) 저장할 배열
    Map<String, String> foodKorName = new HashMap<>();
    List<Button> foodButtons = new ArrayList<Button>(); //인식된 식품 버튼 저장할 배열
    List<Double> intake;    //인식된 식품 이름별 섭취량 저장할 배열(foodName index에 맞춰)

    LinearLayout.LayoutParams param;


    String timezone;

    Number selectedEmoji;
    List<Number> selectedFeedback;
    List<TextView> selectedFeedbackTextView = new ArrayList<TextView>();
    String memoText;
    Intent newIntent;
    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Log.e("성공", "식품영양정보 보고옴");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodrecorddetail);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        LinearLayout foodButtonLayout = findViewById(R.id.foodButtonLayout);
        LinearLayout feedbackText = findViewById(R.id.feedbackText);
        Intent intent = getIntent();
        String recordId = intent.getStringExtra("foodRecordId");
        String date = intent.getStringExtra("date");
        Log.e("식단기록 id", ((GlobalApplication) getApplication()).getKakaoID() + "-" + recordId);

        /*식단 이미지 불러오기*/
        ImageView iv = (ImageView) findViewById(R.id.todayFoodImg);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl("gs://food-pedia-d2bbc.appspot.com/");
        Log.e("=== download the image" , ((GlobalApplication) getApplication()).getKakaoID() + "");
        storageReference = storageReference.child("images/" + ((GlobalApplication) getApplication()).getKakaoID() + "/" + date + "/");

        storageReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference item: listResult.getItems()) {
                    ImageView iv = (ImageView) findViewById(R.id.todayFoodImg);

//                    // image view 동적 생성
//                    iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//                    imageView.addView(iv);

                    item.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Glide.with(FoodRecordDetailActivity.this)
                                        .load(task.getResult())
                                        .into(iv);
                            } else {
                                Toast.makeText(FoodRecordDetailActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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


        Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                // UI 작업 수행 불가능

                /*식품기록 출력할 준비*/
                db.collection("foodRecord")
                        .document(((GlobalApplication) getApplication()).getKakaoID() + "-" + recordId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                HashMap recordMap = (HashMap) documentSnapshot.getData();
                                foodName = (ArrayList<String>) recordMap.get("foods");

                                Log.e("=== DEBUG", foodName + "");
                                intake = (ArrayList<Double>) recordMap.get("intake");
                                timezone = (String) recordMap.get("timezone");
                            } else {
                                Log.e("=== DEBUG", "no data");
                            }
                        });

                /*피드백 출력할 준비*/
                try {
                    db.collection("feedback")
                            .document(((GlobalApplication) getApplication()).getKakaoID() + "-" + recordId+"-"+"feedback")
                            .get()
                            .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot documentSnapshot = task.getResult();
                                            HashMap feedbackMap = (HashMap) documentSnapshot.getData();
                                            if(feedbackMap!=null){
                                                selectedEmoji= (Number)feedbackMap.get("emoji");
                                                Log.e("=== DEBUG selectedEmoji", selectedEmoji + "");
                                                selectedFeedback = (ArrayList<Number>) feedbackMap.get("feedback");
                                                memoText = (String) feedbackMap.get("memo");
                                            }
                                            else{
                                                selectedEmoji = 0;
                                                selectedFeedback = null;
                                                memoText = "";
                                                Log.e("=== DEBUG", "no data catch");
                                            }

                                        } else {
                                            Log.e("=== DEBUG", "no data");
                                        }
                                    }
                            );
                } catch (Exception e) {

                    e.printStackTrace();
                }



                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getFoodKorName(foodName);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("foodKorName : " + foodKorName);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // UI 작업 수행 가능
                        drawButton();

                        /*timezone 출력*/
                        TextView timezoneText = findViewById(R.id.timezone);
                        timezoneText.setText(timezone + " 식단");

                       /*식단 피드백 출력*/
                        ImageView feedbackEmoji = findViewById(R.id.feedbackEmogi);

                        //피드백 이모지
                        if(selectedEmoji.intValue() == 0) {
                            LinearLayout feedbackLayout = findViewById(R.id.feedbackalllayout);
                            feedbackLayout.setVisibility(View.GONE);
                            Log.e("이모지", "이모지안보임");
                        }
                        else {
                            switch(selectedEmoji.intValue()){
                                case FEEDBACK_EMOJI_EXCELLENT:
                                    Drawable drawable = getResources().getDrawable(R.drawable.excellent_off);
                                    feedbackEmoji.setImageDrawable(drawable);
                                case FEEDBACK_EMOJI_GOOD:
                                    Drawable drawable2 = getResources().getDrawable(R.drawable.good_off);
                                    feedbackEmoji.setImageDrawable(drawable2);
                                case FEEDBACK_EMOJI_NEUTRAL:
                                    Drawable drawable3 = getResources().getDrawable(R.drawable.neutral_off);
                                    feedbackEmoji.setImageDrawable(drawable3);
                                case FEEDBACK_EMOJI_BAD:
                                    Drawable drawable4 = getResources().getDrawable(R.drawable.bad_off);
                                    feedbackEmoji.setImageDrawable(drawable4);
                                case FEEDBACK_EMOJI_TERRIBLE:
                                    Drawable drawable5 = getResources().getDrawable(R.drawable.terrible_off);
                                    feedbackEmoji.setImageDrawable(drawable5);
                            }
                        }
                        //피드백 텍스트
                        if(selectedFeedback != null){
                            for(int i=0; i<selectedFeedback.size(); i++){
                                selectedFeedbackTextView.add(new TextView(getApplicationContext()));
                                switch(selectedFeedback.get(i).intValue()){
                                    case FEEDBACK_TEXT_FIRST:
                                        selectedFeedbackTextView.get(i).setText(R.string.feedbackTextFirst);
                                    case FEEDBACK_TEXT_SECOND:
                                        selectedFeedbackTextView.get(i).setText(R.string.feedbackTextSecond);
                                    case FEEDBACK_TEXT_THIRD:
                                        selectedFeedbackTextView.get(i).setText(R.string.feedbackTextThird);
                                    case FEEDBACK_TEXT_FOURTH:
                                        selectedFeedbackTextView.get(i).setText(R.string.feedbackTextFourth);
                                    case FEEDBACK_TEXT_FIFTH:
                                        selectedFeedbackTextView.get(i).setText(R.string.feedbackTextFifth);
                                }
                                selectedFeedbackTextView.get(i).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);

                                LinearLayout.LayoutParams params2 =  new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                params2.weight = 1;
                                params2.gravity = Gravity.CLIP_HORIZONTAL;
                                params2.topMargin = 10;
                                feedbackText.addView(selectedFeedbackTextView.get(i), params2);
                            }
                        }






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





        /*식품 버튼 그리기*/
//        getFoodKorName(foodName);







//피드백 페이지 출력하기

//                foodName = (ArrayList<String>) recordMap.get("foods");
//                intake = (ArrayList<Double>) recordMap.get("intake");
//                timezone = (String) recordMap.get("timezone");
//                for(int i=0; i<foodsEngName.size(); i++){
//                    foodButtons.add(new Button(this));
//                    foodButtons.get(i).setText(curFoodKorName);
//
//                    param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                    param.weight = 1;
//                    param.gravity = Gravity.CLIP_HORIZONTAL;
//
//                    foodButtonLayout.addView(foodButtons.get(idx), param);


    }

    private void drawButton() {
        Log.e("=== drawButton", foodKorName.size() + "");
        LinearLayout foodButtonLayout = findViewById(R.id.foodButtonLayout);
        Iterator<String> foodEngNames = foodKorName.keySet().iterator();
        int idx = 0;
        while (foodEngNames.hasNext()) {
            String curFoodEngName = foodEngNames.next();
            String curFoodKorName = foodKorName.get(curFoodEngName);
            foodButtons.add(new Button(getApplicationContext()));
            foodButtons.get(idx).setText(curFoodKorName);

            param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            param.weight = 1;
            param.gravity = Gravity.CLIP_HORIZONTAL;

            foodButtonLayout.addView(foodButtons.get(idx), param);

            //식품 버튼 누르면 해당 식품영양정보 페이지로 이동
            Button foodButton = foodButtons.get(idx);
            String foodButtonEngName = curFoodEngName;
            String foodButtonIntake = "";
            foodButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), FoodRecordDetailNutActivity.class);
                    //foodButtonIntake = intake.get(foodName.indexOf(foodButtonEngName)).toString();
                    Log.e("touched foodName : ",  foodButtonEngName);
                    intent.putExtra("foodName", foodButtonEngName);   //다음 페이지로 해당 식품 이름 전달
                    System.out.println("updateIntakeHere : " + intake.get(foodName.indexOf(foodButtonEngName)).toString());
                    intent.putExtra("foodIntake", intake.get(foodName.indexOf(foodButtonEngName)).toString());   //다음 페이지로 해당 식품 섭취량 전달
                    mStartForResult.launch(intent);
                }
            });
            idx++;
        }
    }
    private void getFoodKorName(List<String> foodNames) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (final String foodName : foodNames) {
            db.collection("food").document(foodName).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    HashMap foodMap = (HashMap) document.getData();
                    foodKorName.put(foodName, foodMap.get("korean").toString());
                    Log.e("=== getFoodKorName ", foodName + " " + foodMap.get("korean").toString());
                }
            });
        }
    }
}
