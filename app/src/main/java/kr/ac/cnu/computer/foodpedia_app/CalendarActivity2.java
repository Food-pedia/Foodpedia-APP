package kr.ac.cnu.computer.foodpedia_app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CalendarActivity2 extends AppCompatActivity {

    CompactCalendarView compactCalendarView;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm-yyyy", Locale.getDefault());
    private SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    TextView tx_date;
    LinearLayout ly_detail;
    LinearLayout ly_left, ly_right;
    Calendar myCalendar;
    Date c;
    SimpleDateFormat df;
    String formattedDate;
    RecyclerView recyclerView;
    TextView tx_item;
    View camera_pop;
    Intent bottom_nav_intent;

    final static int TAKE_PICTURE = 1;
    final static int GET_FROM_GALLERY = 2;
    private Button btn_camera, btn_gallery;

    int who;

    private FirebaseStorage storage;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    ArrayList<String> day = new ArrayList<>();
    ArrayList<String> month = new ArrayList<>();
    ArrayList<String> year = new ArrayList<>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar2);

        storage = FirebaseStorage.getInstance();
//        camera_pop = findViewById(R.id.camera_pop);
//        camera_pop.setVisibility(View.GONE);

        setDate();
        init();

        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Intent intent = new Intent(getApplicationContext(), FoodRecordsActivity.class);
                intent.putExtra("recordDate", format.format(dateClicked));
                intent.putExtra("mode", "DAY");
                Log.e("=== selected date", format.format(dateClicked));
                startActivity(intent);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                Log.e("calendar", "scrolled");
            }
        });

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

        /************* 하단바 *************/
        BottomNavigationView bottomNav = findViewById(R.id.navigationView);

        // item selection part
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.Today:
                        return true;

                    case R.id.Camera:
                        camera_pop.setVisibility(View.VISIBLE);
                        return true;
                }
                return false;
            }
        });
        /************* 하단바 *************/
    }

    // variable initialization
    public void init() {
        compactCalendarView = findViewById(R.id.compactcalendar_view);
        tx_date = findViewById(R.id.text);
        ly_left = findViewById(R.id.layout_left);
        ly_right = findViewById(R.id.layout_right);
        camera_pop = findViewById(R.id.camera_pop);
        camera_pop.setVisibility(View.GONE);
        btn_camera = findViewById(R.id.btn_camera);
        btn_gallery = findViewById(R.id.btn_gallery);
    }

    // get current date
    public void setDate() {
        c = Calendar.getInstance().getTime();
        df = new SimpleDateFormat("yyyy-MM-dd");
        formattedDate = df.format(c);
        getEventDateFromStorage();
        myCalendar = Calendar.getInstance();
    }

    private void getEventDateFromStorage() {
        db.collection("foodRecord").whereEqualTo("member", ((GlobalApplication) getApplication()).getKakaoID())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            HashMap record = (HashMap) document.getData();

                            int len = record.get("time").toString().split("-").length;

                            Log.e("날짜 원본 : ", record.get("time").toString());

                            String measureYear = record.get("time").toString().split("-")[0];
                            String measureMonth = record.get("time").toString().split("-")[1];
                            String measureDay = record.get("time").toString().split("-")[2];

                            Log.e("년도 : ", measureYear);
                            Log.e("월 : ", measureMonth);
                            Log.e("일 : ", measureDay);

                            year.add(measureYear);
                            month.add(measureMonth);
                            day.add(measureDay);
                        }
                        Log.e("사이잉즈 : ",month.size()+"");
                        for (int j = 0; j < month.size(); j++) {
                            Log.e("할로롤 : ","!!!!");
                            int mon = Integer.parseInt(month.get(j));
                            myCalendar.set(Calendar.YEAR, Integer.parseInt(year.get(j)));
                            myCalendar.set(Calendar.MONTH, mon - 1);
                            myCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day.get(j)));

                            Event event = new Event(Color.parseColor("#275C3C"), myCalendar.getTimeInMillis(), "test");
                            compactCalendarView.addEvent(event);
                        }
                    }
                });
    }
}