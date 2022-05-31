package kr.ac.cnu.computer.foodpedia_app;

import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.os.Bundle;

import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity2 extends AppCompatActivity {
    private Button btnCapture;
    private ImageView imgCapture;
    private static final int Image_Capture_Code = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //여기 main2로 해야되는거 아니야?

        Interpreter lite = getTfliteInterpreter("converted_model.tflite");

        AssetManager am = getAssets();
        BufferedInputStream buf;

        float[][][][] input;
        float[][][] output  = new float[1][8400][35];

        try {
            buf = new BufferedInputStream(am.open("test.jpg"));
            Bitmap bitmap = BitmapFactory.decodeStream(buf);
            bitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, true);
            int batchNum = 0;
            input = new float[1][640][640][3];
            for (int x = 0; x < 640; x++) {
                for (int y = 0; y < 640; y++) {
                    int pixel = bitmap.getPixel(x, y);
                    input[batchNum][0][x][y] = Color.red(pixel) / 1.0f;
                    input[batchNum][1][x][y] = Color.green(pixel) / 1.0f;
                    input[batchNum][2][x][y] = Color.blue(pixel) / 1.0f;
                }
            }
            lite.run(input, output);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(MainActivity2.this, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

}