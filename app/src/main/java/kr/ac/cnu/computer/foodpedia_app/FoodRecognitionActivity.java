package kr.ac.cnu.computer.foodpedia_app;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.os.Looper;

import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import kr.ac.cnu.computer.foodpedia_app.customview.OverlayView;
import kr.ac.cnu.computer.foodpedia_app.env.BorderedText;
import kr.ac.cnu.computer.foodpedia_app.env.ImageUtils;
import kr.ac.cnu.computer.foodpedia_app.env.Logger;
import kr.ac.cnu.computer.foodpedia_app.env.Utils;
import kr.ac.cnu.computer.foodpedia_app.tflite.Classifier;
import kr.ac.cnu.computer.foodpedia_app.tflite.YoloV5Classifier;
import kr.ac.cnu.computer.foodpedia_app.tracking.MultiBoxTracker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import android.view.View;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class FoodRecognitionActivity extends AppCompatActivity {

    public static Context contextFoodRecognition;
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.25f;

    List<String> foodName = new ArrayList<String>();    //인식된 식품 이름들(영어) 저장할 배열
    Map<String, String> foodKorName = new HashMap<>();
    List<Button> foodButtons = new ArrayList<Button>(); //인식된 식품 버튼 저장할 배열
    List<Double> intake = new ArrayList<Double>();    //인식된 식품 이름별 섭취량 저장할 배열(foodName index에 맞춰)
    Intent newIntent = null;
    Intent otherIntent = null;
    Intent feedbackIntent = null;

    ArrayList<FoodItem> foodItemArrayList, filteredList;
    FoodAdapter foodAdapter;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    SearchView searchView;
    LinearLayout.LayoutParams param;
    LinearLayout foodButtonLayout;

    String foodRecordId = "";
    int selectedEmoji;
    List<Integer> selectedFeedback;
    String memoText = "";

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    newIntent = result.getData();
                    if (newIntent.getStringExtra("modifiedIntakeFoodName") != null){
                        String modifiedIntakeFoodName = newIntent.getStringExtra("modifiedIntakeFoodName");
                        String modifiedIntake = newIntent.getStringExtra("modifiedIntake");
                        if (foodName.contains(modifiedIntakeFoodName)) {
                            intake.set(foodName.indexOf(modifiedIntakeFoodName), Double.parseDouble(modifiedIntake));
                        }
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> addNewFoodStartForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    otherIntent = result.getData();
                    String newFoodName = otherIntent.getStringExtra("newFoodName");
                    String newFoodEngName = otherIntent.getStringExtra("newFoodEngName");
                    String newFoodIntake = otherIntent.getStringExtra("newFoodIntake");
                    Log.e("newFood", newFoodName + " " + newFoodEngName+ " " + newFoodIntake);
                    foodName.add(newFoodEngName);
                    foodKorName.put(newFoodEngName, newFoodName);
                    foodButtons.add(new Button(this));
                    intake.add(Double.parseDouble(newFoodIntake));
                    param.weight = 1;
                    param.gravity = Gravity.CLIP_HORIZONTAL;

                    int lastIdx = foodButtons.size() - 1;
                    Button newFoodButton = foodButtons.get(lastIdx);
                    newFoodButton.setText(newFoodName);
                    foodButtonLayout.addView(newFoodButton, param);
                    Log.e("성공!", "FoodRecognitionActivity 129줄");

                    newFoodButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(), FoodNutritionInfoActivity.class);
                            updateIntake();

                            intent.putExtra("foodName", newFoodEngName);   //다음 페이지로 해당 식품 이름 전달
                            System.out.println("updateIntakeHere : " + intake.get(foodName.indexOf(newFoodEngName)).toString());
                            intent.putExtra("foodIntake", intake.get(foodName.indexOf(newFoodEngName)).toString());   //다음 페이지로 해당 식품 섭취량 전달
                            intent.putExtra("foodRecordId", foodRecordId);  //다음 페이지로 현재 식단 기록 id 전달
                            // TODO  다연
                            // 위 코드에서 foodRecordId가 빈 문자열인 것 같음
                            mStartForResult.launch(intent);
                        }
                    });

                }
            }
    );

    ActivityResultLauncher<Intent> feedbackStartForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    feedbackIntent = result.getData();
                    if (feedbackIntent != null){
                        selectedEmoji=feedbackIntent.getIntExtra("selectedEmoji", 1);
                        selectedFeedback=feedbackIntent.getIntegerArrayListExtra("selectedFeedback");
                        memoText = feedbackIntent.getStringExtra("memo");


                    }
                }
            }
    );

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodrecognition);
        contextFoodRecognition = this;

        // MainActivity3 -> MainActivity2 intent image
        Intent takePicture = getIntent();

        imageView = findViewById(R.id.imageView);

        foodButtonLayout = findViewById(R.id.foodButtonLayout);
        animationView = findViewById(R.id.lottie);
        animationView.setAnimation("loading2.json");
        animationView.playAnimation();
        animationView.setRepeatCount(ValueAnimator.INFINITE);

//        cameraButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DetectorActivity.class)));

        this.sourceBitmap = (Bitmap) takePicture.getParcelableExtra("image");
        System.out.println("DEBUG : " + sourceBitmap);
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

            // 참고 : https://brunch.co.kr/@mystoryg/84
            new Thread(new Runnable() {
                final List<Classifier.Recognition> results = detector.recognizeImage(cropBitmap);

                @Override
                public void run() {
                    Looper.prepare();
                    // UI 작업 수행 불가능
                    getFoodKorName(results);

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // UI 작업 수행 가능
                            handleResult(cropBitmap, results);
                            drawButton();
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
        }

        /* 검색창 구현*/
        recyclerView = findViewById(R.id.recyclerview);
        searchView = findViewById(R.id.searchFood);

        filteredList=new ArrayList<>();
        foodItemArrayList = new ArrayList<>();

        foodAdapter = new FoodAdapter(foodItemArrayList, this);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(foodAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, 1));

        FirebaseFirestore db = FirebaseFirestore.getInstance();



        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 검색 버튼이 눌리면
//                foodItemArrayList.add(new FoodItem("닭가슴살"));
//                foodItemArrayList.add(new FoodItem("피자"));
//                foodItemArrayList.add(new FoodItem("햄버"));
//                foodItemArrayList.add(new FoodItem("제육볶음"));
//                foodItemArrayList.add(new FoodItem("닭갈비"));
//                foodItemArrayList.add(new FoodItem("바지락 칼국수"));
//                foodItemArrayList.add(new FoodItem("닭볶음탕"));
//                foodAdapter.notifyDataSetChanged();

//                db.collection("food").addSnapshotListener(new EventListener<QuerySnapshot>() {
//                    @Override
//                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                        foodItemArrayList.clear();
//                        foodAdapter.notifyDataSetChanged();
//                        Snapshot snapshot;
//                        for snapshot in value
//                    }
//                });

                filteredList.clear();
                db.collection("food")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String koreanName = String.valueOf(document.get("korean"));
                                        if (koreanName.contains(query)){

                                            FoodItem foodItem = new FoodItem(koreanName,document.getId());
                                            foodItemArrayList.add(foodItem);
                                            foodAdapter.notifyDataSetChanged();
                                            filteredList.add(foodItem);
                                            Log.d(koreanName, koreanName);
                                        }
                                    }
                                } else {
                                    Log.d("FirebaseError", "Error getting documents: ", task.getException());
                                }
                            }
                        });

//                foodAdapter.notifyDataSetChanged();


                foodAdapter.filterList(filteredList);
                Log.e("검색된 이름 query:",query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
//            @Override
//            public boolean onQueryTextChange(String newText) {
//
//                // 검색창에 글자를 쓰면 여기로 옴
//
//                searchFilter(newText);
//                Log.e("검색된 이름 newText:",newText);
//                return true;
//            }
        });

        Button feedbackBtn = findViewById(R.id.feedbackBtn);
        feedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FoodRecordFeedbackActivity.class);
                feedbackStartForResult.launch(intent);
            }
        });


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

    private LottieAnimationView animationView;

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

    private void getFoodKorName(List<Classifier.Recognition> results) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (final Classifier.Recognition result : results) {
            db.collection("food").document(result.getTitle()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    HashMap foodMap = (HashMap) document.getData();
                    foodKorName.put(result.getTitle(), foodMap.get("korean").toString());
                    Log.e("=== getFoodKorName ", result.getTitle() + " " + foodMap.get("korean").toString());
                }
            });
        }
    }

    private void handleResult(Bitmap bitmap, List<Classifier.Recognition> results) {
        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint();

        borderedText = new BorderedText(30.0f);

        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(10.0f);
        boxPaint.setStrokeCap(Paint.Cap.ROUND);
        boxPaint.setStrokeJoin(Paint.Join.ROUND);
        boxPaint.setStrokeMiter(100);


        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);

        final List<Classifier.Recognition> mappedRecognitions =
                new LinkedList<Classifier.Recognition>();

        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            animationView.setVisibility(View.GONE);

            // *** 랜덤 색깔 *** //
            int randomC = getRandomColor();
            boxPaint.setColor(randomC);
            paint.setColor(randomC);

            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                Log.e("=== title : ", result.getTitle());
                Log.e("=== location : ", location + "");
                canvas.drawRect(location, paint);

                String foodName = (foodKorName.get(result.getTitle()) == null ? "" : foodKorName.get(result.getTitle()));
                borderedText.drawText(
                        canvas, location.left, location.top, foodName, boxPaint);
                cropToFrameTransform.mapRect(location);

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
        List<RectF> coordinates = new ArrayList<RectF>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (final Classifier.Recognition result : results) { // Detected Food label, coordinates
            final RectF location = result.getLocation();
            String foodEngName = result.getTitle();

            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                coordinates.add(location);
                foodName.add(foodEngName);
            }
        }

        for (int i = 0; i < foodName.size(); i++) {   //인식된 식품 개수에 맞게 섭취량 배열 1로 초기화
            intake.add(1.0);
        }

        Button updateButton = findViewById(R.id.updateBtn);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

//              카카오id-yyyy-MM-dd-HH-mm-ss
                LocalDateTime now = LocalDateTime.now();
                String getFormatedNow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
                foodRecordId = ((GlobalApplication) getApplication()).getKakaoID()+"-"+getFormatedNow;

                // upload image
                FirebaseStorage storage = FirebaseStorage.getInstance();
                // Create a storage reference from our app
                StorageReference storageReference = storage.getReferenceFromUrl("gs://food-pedia-d2bbc.appspot.com/");
                //Create a reference to image
                StorageReference imageReference = storageReference.child("images/" + foodRecordId + ".jpg");

                imageView.setDrawingCacheEnabled(true);
                imageView.buildDrawingCache();
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = imageReference.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Log.e("=== upload image", "fail to upload the image");
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        Log.e("=== upload image", "success to upload the image");
                    }
                });

                String getName = ((GlobalApplication)getApplication()).getKakaoID(); // 나중에 사용자 이름이나 id 저장

                RadioGroup timezoneGroup = (RadioGroup) findViewById(R.id.radioGroupTimezone);
                RadioButton selectedTimezone = (RadioButton) findViewById(timezoneGroup.getCheckedRadioButtonId());
                String getTimezone = selectedTimezone.getText().toString();

                HashMap<String, Object> result = new HashMap<>();
                result.put("member", getName);
                result.put("time", getFormatedNow);
                result.put("timezone", getTimezone);
                result.put("foods", foodName);
                result.put("intake", intake);

                if(feedbackIntent != null){
                    HashMap<String, Object> feedbackResult = new HashMap<>();
                    feedbackResult.put("foodRecordId", foodRecordId);
                    feedbackResult.put("emoji", selectedEmoji);
                    feedbackResult.put("feedback", selectedFeedback);
                    feedbackResult.put("memo", memoText);

                    db.collection("feedback").document(foodRecordId).set(feedbackResult)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.e("DB 저장 완료", "피드백 저장 성공");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("DB 저장 실패", "피드백 저장 실패");
                                }
                            });
                }

                db.collection("foodRecord").document(foodRecordId).set(result)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), "저장을 완료했습니다", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), FoodRecordsActivity.class);
                                intent.putExtra("recordId", foodRecordId);
                                intent.putExtra("mode", "UPDATE");
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "저장에 실패했습니다", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void drawButton() {
        Log.e("=== drawButton", foodKorName.size() + "");
        Iterator<String> foodEngNames = foodKorName.keySet().iterator();
        int idx = 0;
        while (foodEngNames.hasNext()) {
            String curFoodEngName = foodEngNames.next();
            String curFoodKorName = foodKorName.get(curFoodEngName);
            foodButtons.add(new Button(this));
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
                    Intent intent = new Intent(getApplicationContext(), FoodNutritionInfoActivity.class);
                    updateIntake();
                    //foodButtonIntake = intake.get(foodName.indexOf(foodButtonEngName)).toString();
                    System.out.println("foodName : " + foodName);
                    System.out.println("foodKorName : " + foodKorName);
                    System.out.println("foodButtonEngName : " + foodButtonEngName);
                    System.out.println("updateIntake : " + intake);

                    intent.putExtra("foodName", foodButtonEngName);   //다음 페이지로 해당 식품 이름 전달
                    System.out.println("updateIntakeHere : " + intake.get(foodName.indexOf(foodButtonEngName)).toString());
                    intent.putExtra("foodIntake", intake.get(foodName.indexOf(foodButtonEngName)).toString());   //다음 페이지로 해당 식품 섭취량 전달
                    intent.putExtra("foodRecordId", foodRecordId);  //다음 페이지로 현재 식단 기록 id 전달
                    mStartForResult.launch(intent);
                }
            });
            idx++;
        }
    }

    private void updateIntake() {
        if (newIntent != null) {
            Log.e("updateIntake", "here");
            String modifiedIntakeFoodName = newIntent.getStringExtra("modifiedIntakeFoodName");
            String modifiedIntake = newIntent.getStringExtra("modifiedIntake");
            Log.e("modifiedIntakeFoodName", modifiedIntakeFoodName);
            Log.e("modifiedIntake", modifiedIntake);
            if (foodName.contains(modifiedIntakeFoodName)) {
                intake.set(foodName.indexOf(modifiedIntakeFoodName), Double.parseDouble(modifiedIntake));
            }
        } else {
            Log.e("updateIntake", "intentNull");
            if (intake.size() != foodName.size()) {
                for (int i = 0; i < foodName.size(); i++) {
                    intake.add(1.0);
                }
            } else {
                System.out.print("intake: " + intake);
            }

        }

    }

    public int getRandomColor(){
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

//    public void searchFilter(String searchText) {
//        filteredList.clear();
//        Log.e("searchFilter 1","도착");
//        for (int i = 0; i < foodItemArrayList.size(); i++) {
//            if (foodItemArrayList.get(i).getFoodName().toLowerCase().contains(searchText.toLowerCase())) {
//                Log.e("searchFilter 2","도착");
//                filteredList.add(foodItemArrayList.get(i));
//            }
//        }
////        Log.e("searchFilter 3", String.valueOf(filteredList.get(0)));
//        foodAdapter.filterList(filteredList);
//    }

}