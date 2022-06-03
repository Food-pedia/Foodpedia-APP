package kr.ac.cnu.computer.foodpedia_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;

public class MainActivity3 extends AppCompatActivity {
    final private static String TAG = "tag";
    Button btn_camera;
    ImageView img;
    final static int TAKE_PICTURE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        btn_camera = findViewById(R.id.btn_camera);

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
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, TAKE_PICTURE);
                        break;
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == RESULT_OK && intent.hasExtra("data")) {
                    Bitmap bitmap = (Bitmap) intent.getExtras().get("data");
                    if (bitmap != null) {
                       ByteArrayOutputStream stream = new ByteArrayOutputStream();
                       float scale = (float) (1024/(float)bitmap.getWidth());
                       int image_w = (int) (bitmap.getWidth() * scale);
                       int image_h = (int) (bitmap.getHeight() * scale);
                       Bitmap resize = Bitmap.createScaledBitmap(bitmap, image_w, image_h, true);
                       resize.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                       byte[] byteArray = stream.toByteArray();

                       Intent intent2 = new Intent(this, FoodRecognitionActivity.class);
                       intent2.putExtra("image", byteArray);
                       startActivity(intent2);


                    }
                }
                break;
        }
    }
}

