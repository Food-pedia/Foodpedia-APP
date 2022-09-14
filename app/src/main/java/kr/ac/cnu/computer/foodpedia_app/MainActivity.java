package kr.ac.cnu.computer.foodpedia_app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        // PieChart 메소드
//        PieChart pieChart = (PieChart) findViewById(R.id.chart1);
//        ArrayList<Entry> entries = new ArrayList<>();
//        for(int i=0; i < valList.size();i++){
//            entries.add(new Entry((Integer) valList.get(i), i));
//        }
//        PieDataSet depenses = new PieDataSet (entries, "월별 가입자수");
//        depenses.setAxisDependency(YAxis.AxisDependency.LEFT);
//        ArrayList<String> labels = new ArrayList<String>();
//        for(int i=0; i < labelList.size(); i++){
//            labels.add((String) labelList.get(i));
//        }
//        PieData data = new PieData(labels,depenses); // 라이브러리 v3.x 사용하면 에러 발생함
//        depenses.setColors(ColorTemplate.COLORFUL_COLORS);
//        pieChart.setData(data);
//        pieChart.animateXY(1000,1000);
//        pieChart.invalidate();
    }
}