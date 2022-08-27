package kr.ac.cnu.computer.foodpedia_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FoodRecordFeedbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodrecordfeedback);

        //String getfoodRecordId = getIntent().getStringExtra("recordId");    //나중에 식품인식페이지랑 연결해서 intent값 가져오기
        String getfoodRecordId = "foodRecordid";

        RadioGroup emojiGroup = (RadioGroup) findViewById(R.id.radioGroupEmoji);
        ChipGroup feedbackGroup = (ChipGroup) findViewById(R.id.chipGroupFeedback);
        EditText memoEditText = findViewById(R.id.editTextTextMultiLine);
        Button saveBtn = findViewById(R.id.buttonSave);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedEmoji = emojiGroup.getCheckedRadioButtonId();
                List<Integer> selectedFeedback = feedbackGroup.getCheckedChipIds();
                String memoText = memoEditText.getText().toString();

                HashMap<String, Object> feedbackResult = new HashMap<>();
                feedbackResult.put("foodRecordId", getfoodRecordId);
                feedbackResult.put("emoji", selectedEmoji);
                feedbackResult.put("feedback", selectedFeedback);
                feedbackResult.put("memo", memoText);

                Task<DocumentReference> addedDocRef = db.collection("feedback").add(feedbackResult);
                addedDocRef.addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getApplicationContext(), "저장을 완료했습니다", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "저장에 실패했습니다", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });




    }
}
