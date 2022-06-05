package kr.ac.cnu.computer.foodpedia_app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import kr.ac.cnu.computer.foodpedia_app.customview.OverlayView;
import kr.ac.cnu.computer.foodpedia_app.env.BorderedText;
import kr.ac.cnu.computer.foodpedia_app.env.ImageUtils;
import kr.ac.cnu.computer.foodpedia_app.env.Logger;
import kr.ac.cnu.computer.foodpedia_app.env.Utils;
import kr.ac.cnu.computer.foodpedia_app.tflite.Classifier;
import kr.ac.cnu.computer.foodpedia_app.tflite.YoloV5Classifier;
import kr.ac.cnu.computer.foodpedia_app.tracking.MultiBoxTracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import android.view.View;
import android.widget.LinearLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FoodRecognitionActivity extends AppCompatActivity {

    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.25f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodrecognition);

        // MainActivity3 -> MainActivity2 intent image
        Intent takePicture = getIntent();

        imageView = findViewById(R.id.imageView);
        foodButtonLayout = findViewById(R.id.foodButtonLayout);
        animationView = findViewById(R.id.lottie);
        animationView.setAnimation("loading2.json");
        animationView.playAnimation();
        animationView.loop(true);

//        cameraButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DetectorActivity.class)));


        this.sourceBitmap = (Bitmap) takePicture.getParcelableExtra("image");
        this.cropBitmap = Utils.processBitmap(sourceBitmap, TF_OD_API_INPUT_SIZE);
        this.imageView.setImageBitmap(cropBitmap);


        Log.e("=== sourceBitmap : ", this.sourceBitmap + "");
        Log.e("=== cropBitmap : ", this.cropBitmap + "");

        initBox();
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();

        System.err.println(Double.parseDouble(configurationInfo.getGlEsVersion()));
        System.err.println(configurationInfo.reqGlEsVersion >= 0x30000);
        System.err.println(String.format("%X", configurationInfo.reqGlEsVersion));

        if (detector != null && cropBitmap != null) {
            Handler handler = new Handler();

            new Thread(() -> {
                final List<Classifier.Recognition> results = detector.recognizeImage(cropBitmap);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleResult(cropBitmap, results);
                    }
                });
            }).start();
        }

    }

    private static final Logger LOGGER = new Logger();

    public static final int TF_OD_API_INPUT_SIZE = 640;

    private static final boolean TF_OD_API_IS_QUANTIZED = false;

    private static final String TF_OD_API_MODEL_FILE = "best-fp16.tflite";

    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/customclasses.txt";

    // Minimum detection confidence to track a detection.
    private static final boolean MAINTAIN_ASPECT = true;
    private Integer sensorOrientation = 90;

    private Classifier detector;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;
    private MultiBoxTracker tracker;
    private OverlayView trackingOverlay;

    protected int previewWidth = 0;
    protected int previewHeight = 0;

    private Bitmap sourceBitmap;
    private Bitmap cropBitmap;

    private BorderedText borderedText;
    private Paint boxPaint = new Paint();


    //    private Button cameraButton;
    private Button detectButton;

    private ImageView imageView;
    private LinearLayout foodButtonLayout;
    private LottieAnimationView animationView;

    private String foodKorName = "";

    private void initBox() {
        previewHeight = TF_OD_API_INPUT_SIZE;
        previewWidth = TF_OD_API_INPUT_SIZE;
        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        tracker = new MultiBoxTracker(this);
        trackingOverlay = findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                canvas -> tracker.draw(canvas));

        tracker.setFrameConfiguration(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, sensorOrientation);

        try {
            detector =
                    YoloV5Classifier.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_IS_QUANTIZED,
                            TF_OD_API_INPUT_SIZE);
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
//            Toast toast =
//                    Toast.makeText(
//                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
//            toast.show();
            finish();
        }
    }
    private void getFoodKorName(FirebaseFirestore db, String foodEngName){
        db.collection("food").document(foodEngName).get().addOnCompleteListener(task->{
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                HashMap foodMap = (HashMap) document.getData();
                foodKorName = foodMap.get("korean").toString();
            }
        });
    }


    private void handleResult(Bitmap bitmap, List<Classifier.Recognition> results) {
        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint();

        borderedText = new BorderedText(30.0f);
        boxPaint.setColor(Color.RED);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(10.0f);
        boxPaint.setStrokeCap(Paint.Cap.ROUND);
        boxPaint.setStrokeJoin(Paint.Join.ROUND);
        boxPaint.setStrokeMiter(100);

        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);

        final List<Classifier.Recognition> mappedRecognitions =
                new LinkedList<Classifier.Recognition>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            animationView.setVisibility(View.GONE);
            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                canvas.drawRect(location, paint);
                Log.e("=== title : ", result.getTitle());
                Log.e("=== location : ", location + "");

                String foodEngName = result.getTitle();
                getFoodKorName(db, foodEngName);

                borderedText.drawText(
                        canvas, location.left , location.top, foodKorName, boxPaint);  //!여기
                cropToFrameTransform.mapRect(location);
//
                result.setLocation(location);
                mappedRecognitions.add(result);
            }
        }
//        tracker.trackResults(mappedRecognitions, new Random().nextInt());
//        trackingOverlay.postInvalidate();


        imageView.setImageBitmap(bitmap);
        recognizationFood(mappedRecognitions);

    }



    private void recognizationFood(List<Classifier.Recognition> results) {

        int foodNum = results.size(); // Number of Foods Detected
        List<Button> foodButtons = new ArrayList<Button>(); //인식된 식품 버튼 저장할 배열
        List<String> foodName = new ArrayList<String>();
        List<RectF> coordinates = new ArrayList<RectF>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (final Classifier.Recognition result : results) { // Detected Food label, coordinates
            final RectF location = result.getLocation();
            String foodEngName = result.getTitle();
            getFoodKorName(db, foodEngName);
            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                coordinates.add(location);
                foodName.add(foodKorName);
            }
        }

        for (int i = 0; i < foodNum; i++) {
            //인식된 식품 개수만큼 버튼 생성
            foodButtons.add(new Button(this));
            foodButtons.get(i).setText(foodName.get(i));
            foodButtonLayout.addView(foodButtons.get(i));

            //식품 버튼 누르면 해당 식품영양정보 페이지로 이동
            Button foodButton = foodButtons.get(i);
            foodButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), FoodNutritionInfoActivity.class);
                    intent.putExtra("foodName", foodButton.getText());   //다음 페이지로 식품 이름 전달
                    startActivity(intent);
                }
            });
        }
    }
}
