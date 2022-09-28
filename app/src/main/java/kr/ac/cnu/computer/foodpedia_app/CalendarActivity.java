package kr.ac.cnu.computer.foodpedia_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.prolificinteractive.materialcalendarview.*;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter;
import com.prolificinteractive.materialcalendarview.format.TitleFormatter;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.Calendar;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private MaterialCalendarView calendarView;
    private LinearLayout eatLogInfo;
    private LinearLayout eatLogView;

    final static int TAKE_PICTURE = 1;
    final static int GET_FROM_GALLERY = 2;
    private View camera_pop;
    private Button btn_camera, btn_gallery;
    Intent bottom_nav_intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        calendarView = findViewById(R.id.calendarView);
        eatLogView = (LinearLayout) findViewById(R.id.eatLogView);
        eatLogInfo = (LinearLayout) findViewById(R.id.eatLogInfo);
        eatLogView.setVisibility(View.GONE);
        eatLogInfo.setVisibility(View.GONE);
        camera_pop = findViewById(R.id.camera_pop);
        camera_pop.setVisibility(View.GONE);
        btn_camera = findViewById(R.id.btn_camera);
        btn_gallery = findViewById(R.id.btn_gallery);

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_camera:
                        bottom_nav_intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(bottom_nav_intent, TAKE_PICTURE);
                        break;
                }
            }
        });

        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_gallery:
                        bottom_nav_intent = new Intent(Intent.ACTION_PICK);
                        bottom_nav_intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                        bottom_nav_intent.setType("image/*");
                        startActivityForResult(bottom_nav_intent, GET_FROM_GALLERY);
                        break;
                }
            }
        });

        // 첫 시작 요일은 월요일
        calendarView.state()
                .edit()
                .setFirstDayOfWeek(DayOfWeek.of(Calendar.MONDAY))
                .commit();

        // 월, 요일을 한글로 보이게 설정 (MonthArrayTitleFormatter의 작동을 확인하려면 밑의 setTitleFormatter()를 지운다)
        calendarView.setTitleFormatter(new MonthArrayTitleFormatter(getResources().getTextArray(R.array.months)));
        calendarView.setWeekDayFormatter(new ArrayWeekDayFormatter(getResources().getTextArray(R.array.weekdays)));

        // 좌우 화살표 사이 연, 월의 폰트 스타일 설정
        calendarView.setHeaderTextAppearance(R.style.CalendarWidgetHeader);

        // 요일 선택 시 내가 정의한 드로어블이 적용되도록 함
        calendarView.setOnRangeSelectedListener(new OnRangeSelectedListener() {
            @Override
            public void onRangeSelected(@NonNull MaterialCalendarView widget, @NonNull List<CalendarDay> dates) {
                // UTC 시간을 구하려는 경우 이 라이브러리에서 제공하지 않으니 별도의 로직을 짜야 한다
                String startDay = dates.get(0).getDate().toString();
                String endDay = dates.get(dates.size() - 1).getDate().toString();
                Log.e(TAG, "시작일 : " + startDay + ", 종료일 : " + endDay);
            }
        });

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                // needs date click event login

//                eatLogView.setVisibility(View.VISIBLE);
//                eatLogInfo.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getApplicationContext(), FoodRecordsActivity.class);
                intent.putExtra("recordDate", date.getDate().toString());
                intent.putExtra("mode", "DAY");
                Log.e("=== selected date ",  date.getDate().toString());
                startActivity(intent);
            }
        });

        // 일자 선택 시 내가 정의한 드로어블이 적용되도록
        calendarView.addDecorators(new DayDecorator(this));

        // 좌우 화살표 가운데의 연/월이 보이는 방식 커스텀
        calendarView.setTitleFormatter(new TitleFormatter() {
            @Override
            public CharSequence format(CalendarDay day) {
                // CalendarDay라는 클래스는 LocalDate 클래스를 기반으로 만들어진 클래스
                // 때문에 MaterialCalendarView에서 연/월 보여주기를 커스텀하려면
                // CalendarDay 객체의 getDate()로 연/월을 구한 다음 LocalDate 객체에 넣어서 LocalDate로 변환하는 처리 필요
                LocalDate inputText = day.getDate();
                String[] calendarHeaderElements = inputText.toString().split("-");
                StringBuilder calendarHeaderBuilder = new StringBuilder();
                calendarHeaderBuilder.append(calendarHeaderElements[0])
                        .append(" ")
                        .append(calendarHeaderElements[1]);
                return calendarHeaderBuilder.toString();
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
                        final Intent intent = new Intent(CalendarActivity.this, MainActivity.class);
                        startActivity(intent);

                        finish();
                        overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                        return true;

                    case R.id.Camera:
                        camera_pop.setVisibility(View.VISIBLE);
//                        bloodBtn.setVisibility(View.GONE);
                        return true;
                    case R.id.Records:
                        return true;
                }
                return false;
            }
        });
        /************* 하단바 *************/
    }

    /* 선택된 요일의 background를 설정하는 Decorator 클래스 */
    private static class DayDecorator implements DayViewDecorator {

        private final Drawable drawable;

        public DayDecorator(Context context) {
            drawable = ContextCompat.getDrawable(context, R.drawable.calendar_selector);
        }

        // true를 리턴 시 모든 요일에 내가 설정한 드로어블이 적용된다
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return true;
        }

        // 일자 선택 시 내가 정의한 드로어블이 적용되도록 한다
        @Override
        public void decorate(DayViewFacade view) {
            view.setSelectionDrawable(drawable);
//            view.addSpan(new StyleSpan(Typeface.BOLD));   // 달력 안의 모든 숫자들이 볼드 처리됨
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case TAKE_PICTURE: // 카메라로 촬영하는 경우
                if (resultCode == RESULT_OK && intent.hasExtra("data")) {
                    Bitmap bitmap = (Bitmap) intent.getExtras().get("data");

                    Intent intent2 = new Intent(getApplicationContext(), FoodRecognitionActivity.class);
                    intent2.putExtra("image", bitmap);
                    startActivity(intent2);
                }
                break;
            case GET_FROM_GALLERY:
                if (resultCode == RESULT_OK) {
                    Uri uri = intent.getData();
                    GlideApp.with(getApplicationContext()).asBitmap().load(uri).override(300, 300).into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Bitmap bitmap = resource;
                            Intent intent2 = new Intent(getApplicationContext(), FoodRecognitionActivity.class);
                            intent2.putExtra("image", bitmap);
                            startActivity(intent2);
                        }
                    });
                }
                break;
        }
    }
}
