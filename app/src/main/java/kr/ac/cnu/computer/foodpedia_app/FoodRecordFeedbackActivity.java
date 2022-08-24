package kr.ac.cnu.computer.foodpedia_app;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

public class FoodRecordFeedbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodrecordfeedback);

        RadioGroup emojiGroup = (RadioGroup) findViewById(R.id.radioGroupEmoji);
        RadioButton selectedEmoji = (RadioButton) findViewById(emojiGroup.getCheckedRadioButtonId());
        //int getEmoji = selectedEmoji.getId();


    }
}
