package kr.ac.cnu.computer.foodpedia_app;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.CalendarView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.Calendar;

public class CalendarActivity extends AppCompatActivity {
    MaterialCalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        calendarView = findViewById(R.id.calendarView);
//        calendarView.state().edit()
//                .setFirstDayOfWeek(Calendar.WEDNESDAY)
//                .setCalendarDisplayMode(CalendarMode.MONTHS)
//                .commit();

        calendarView.setOnDateChangedListener({
        });
    }
}
