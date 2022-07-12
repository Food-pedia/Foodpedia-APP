package kr.ac.cnu.computer.foodpedia_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity3 extends AppCompatActivity {
    final private static String TAG = "tag";
    Button btn_camera, btn_gallery;
    final static int TAKE_PICTURE = 1;
    final static int GET_FROM_GALLERY = 2;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        btn_camera = findViewById(R.id.btn_camera);
        btn_gallery = findViewById(R.id.btn_gallery);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "권한 설정 완료");
            } else {
                Log.d(TAG, "권한 설정 요청");
                ActivityCompat.requestPermissions(MainActivity3.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_camera:
                        intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, TAKE_PICTURE);
                        break;
                }
            }
        });

        btn_gallery.setOnClickListener(v -> {
            switch (v.getId()) {
                case R.id.btn_gallery:
                    intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    intent.setType("image/*");
                    startActivityForResult(intent, GET_FROM_GALLERY);
                    break;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case TAKE_PICTURE: // 카메라로 촬영하는 경우
                if (resultCode == RESULT_OK && intent.hasExtra("data")) {
                    Bitmap bitmap = (Bitmap) intent.getExtras().get("data");
//                    bitmap = bitmap.createScaledBitmap(bitmap,640,640,true);
//                    if (bitmap != null) {
//                       ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                       float scale = (float) (1024/(float)bitmap.getWidth());
//                       int image_w = (int) (bitmap.getWidth() * scale);
//                       int image_h = (int) (bitmap.getHeight() * scale);
//                       Bitmap resize = Bitmap.createScaledBitmap(bitmap, image_w, image_h, true);
//                       resize.compress(Bitmap.CompressFormat.JPEG, 100, stream);
////                       byte[] byteArray = stream.toByteArray();

                       Intent intent2 = new Intent(this, FoodRecognitionActivity.class);
                       intent2.putExtra("image", bitmap);
                       startActivity(intent2);
                    }
                break;
            case GET_FROM_GALLERY:
                Log.e("DEBUG : result code is ", resultCode + "");
                if (resultCode == RESULT_OK) {
                    Uri uri = intent.getData();
                    Log.e("DEBUG :  ", uri + "");
                    GlideApp.with(getApplicationContext()).asBitmap().load(uri).override(300, 300).into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Bitmap bitmap = resource;
                            Log.e("DEBUG : ", bitmap + "");
                            Intent intent2 = new Intent(MainActivity3.this, FoodRecognitionActivity.class);
                            intent2.putExtra("image", bitmap);
                            startActivity(intent2);
                        }
                    });
                }
                break;
            }
        }
    }


