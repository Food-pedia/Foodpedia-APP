package kr.ac.cnu.computer.foodpedia_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FoodRecordFeedbackActivity extends AppCompatActivity {
    View camera_pop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodrecordfeedback);

        String recordId = getIntent().getStringExtra("recordId");    //나중에 식품인식페이지랑 연결해서 intent값 가져오기
        //String getfoodRecordId = "foodRecordid";

        RadioGroup emojiGroup = (RadioGroup) findViewById(R.id.radioGroupEmoji);
        ChipGroup feedbackGroup = (ChipGroup) findViewById(R.id.chipGroupFeedback);
        EditText memoEditText = findViewById(R.id.editTextTextMultiLine);
        Button saveBtn = findViewById(R.id.buttonSave);
//        camera_pop = findViewById(R.id.camera_pop);
//        camera_pop.setVisibility(View.GONE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedEmoji = emojiGroup.getCheckedRadioButtonId();
                List<Integer> selectedFeedback = feedbackGroup.getCheckedChipIds();
                String memoText = memoEditText.getText().toString();

                Intent intent = new Intent(getApplicationContext(), FoodRecognitionActivity.class);
                intent.putExtra("selectedEmoji", selectedEmoji);   //다음 페이지로 식품 이름
                intent.putIntegerArrayListExtra("selectedFeedback", (ArrayList<Integer>) selectedFeedback);
                intent.putExtra("memoText", memoText);  //다음 페이지로 섭취량 전달
                setResult(RESULT_OK ,intent);
                finish();

//                HashMap<String, Object> feedbackResult = new HashMap<>();
//                feedbackResult.put("foodRecordId", recordId);
//                feedbackResult.put("emoji", selectedEmoji);
//                feedbackResult.put("feedback", selectedFeedback);
//                feedbackResult.put("memo", memoText);
//
//                db.collection("feedback").document(recordId+"-"+"feedback").set(feedbackResult)
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                Toast.makeText(getApplicationContext(), "저장을 완료했습니다", Toast.LENGTH_SHORT).show();
//                                finish();
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Toast.makeText(getApplicationContext(), "저장에 실패했습니다", Toast.LENGTH_SHORT).show();
//                            }
//                        });

            }
        });
        /************* 하단바 *************/
        BottomNavigationView bottomNav = findViewById(R.id.navigationView);

        // item selection part
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.Today:
                        final Intent intent = new Intent(FoodRecordFeedbackActivity.this, MainActivity.class);
                        startActivity(intent);

                        finish();
                        overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                        return true;

                    case R.id.Camera:
                        camera_pop.setVisibility(View.VISIBLE);
//                        bloodBtn.setVisibility(View.GONE);
                        return true;
                    case R.id.Records:
                        final Intent intent3 = new Intent(FoodRecordFeedbackActivity.this, CalendarActivity.class);
                        startActivity(intent3);
                        finish();
                        overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
                        return true;
                }
                return false;
            }
        });
        /************* 하단바 *************/



    }
}
