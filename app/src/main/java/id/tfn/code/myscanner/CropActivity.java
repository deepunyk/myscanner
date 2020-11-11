package id.tfn.code.myscanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import id.tfn.code.myscanner.data.Photo;
import id.tfn.code.myscanner.data.PhotoViewModel;
import id.tfn.code.myscanner.helpers.MyConstants;
import id.tfn.code.myscanner.libraries.NativeClass;
import id.tfn.code.myscanner.libraries.PolygonView;
import id.zelory.compressor.Compressor;

public class CropActivity extends AppCompatActivity {

    FrameLayout holderImageCrop;
    ImageView imageView;
    PolygonView polygonView;
    Bitmap selectedImageBitmap;
    Button btnImageEnhance;

    NativeClass nativeClass;
    Toolbar toolbar;

    private PhotoViewModel photoViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        photoViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(PhotoViewModel.class);

        initializeElement();
    }

    private void initializeElement() {
        nativeClass = new NativeClass();
        btnImageEnhance = findViewById(R.id.btnImageEnhance);
        holderImageCrop = findViewById(R.id.holderImageCrop);
        imageView = findViewById(R.id.imageView);
        polygonView = findViewById(R.id.polygonView);

        holderImageCrop.post(new Runnable() {
            @Override
            public void run() {
                initializeCropping();
            }
        });
        btnImageEnhance.setOnClickListener(btnImageEnhanceClick);

    }

    private void initializeCropping() {

        try {
            selectedImageBitmap = MyConstants.selectedImageBitmapList.get(MyConstants.count);

            Bitmap scaledBitmap = scaledBitmap(selectedImageBitmap, holderImageCrop.getWidth(), holderImageCrop.getHeight());



            imageView.setImageBitmap(scaledBitmap);

            Bitmap tempBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            Map<Integer, PointF> pointFs = getEdgePoints(tempBitmap);


            polygonView.setPoints(pointFs);
            polygonView.setVisibility(View.VISIBLE);

            int padding = (int) getResources().getDimension(R.dimen.scanPadding);

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(tempBitmap.getWidth() + 2 * padding, tempBitmap.getHeight() + 2 * padding);
            layoutParams.gravity = Gravity.CENTER;

            polygonView.setLayoutParams(layoutParams);
        }
        catch (Exception e){
            try {
            Toast.makeText(this, "No document found", Toast.LENGTH_SHORT).show();
            File tempDir= Environment.getExternalStorageDirectory();
            tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
            tempDir.mkdir();

                File tempFile = File.createTempFile("cropping", ".jpg", tempDir);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            byte[] bitmapData = bytes.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
            UCrop.Options options = new UCrop.Options();
            options.setFreeStyleCropEnabled(true);
            options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.colorAccent));
            options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            options.setHideBottomControls(false);
            options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
            options.setCompressionQuality(80);
            options.withMaxResultSize(1080,1080);
            UCrop.of(Uri.fromFile(tempFile),Uri.fromFile(tempFile))
                    .withOptions(options)
                    .start(CropActivity.this);
            }catch (Exception ee){

            }
        }
    }

    private View.OnClickListener btnImageEnhanceClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Bitmap bitmap = getCroppedImage();
            try {
                MyConstants.selectedImageBitmapList.set(MyConstants.count, bitmap);
                try {
                    File tempDir= Environment.getExternalStorageDirectory();
                    tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
                    tempDir.mkdir();

                    File tempFile = File.createTempFile(MyConstants.count+"ms", ".jpg", tempDir);

                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    byte[] bitmapData = bytes.toByteArray();

                    //write the bytes in file
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    fos.write(bitmapData);
                    fos.flush();
                    fos.close();
                    UCrop.Options options = new UCrop.Options();
                    options.setFreeStyleCropEnabled(true);
                    options.setActiveControlsWidgetColor(ContextCompat.getColor(CropActivity.this, R.color.colorAccent));
                    options.setStatusBarColor(ContextCompat.getColor(CropActivity.this, R.color.colorPrimaryDark));
                    options.setHideBottomControls(false);
                    options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
                    options.setCompressionQuality(80);
                    options.withMaxResultSize(1080,1080);
                    UCrop.of(Uri.fromFile(tempFile),Uri.fromFile(tempFile))
                            .withOptions(options)
                            .start(CropActivity.this);
                }catch (Exception ee){

                }
            }catch (Exception e){

            }
        }
    };

    protected Bitmap getCroppedImage() {

        Map<Integer, PointF> points = polygonView.getPoints();

        float xRatio = (float) selectedImageBitmap.getWidth() / imageView.getWidth();
        float yRatio = (float) selectedImageBitmap.getHeight() / imageView.getHeight();

        float x1 = (points.get(0).x) * xRatio;
        float x2 = (points.get(1).x) * xRatio;
        float x3 = (points.get(2).x) * xRatio;
        float x4 = (points.get(3).x) * xRatio;
        float y1 = (points.get(0).y) * yRatio;
        float y2 = (points.get(1).y) * yRatio;
        float y3 = (points.get(2).y) * yRatio;
        float y4 = (points.get(3).y) * yRatio;

        return nativeClass.getScannedBitmap(selectedImageBitmap, x1, y1, x2, y2, x3, y3, x4, y4);

    }

    private Bitmap scaledBitmap(Bitmap bitmap, int width, int height) {
        Log.v("tfn-tag", "scaledBitmap");
        Log.v("tfn-tag", width + " " + height);
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

    private Map<Integer, PointF> getEdgePoints(Bitmap tempBitmap) {
        Log.v("tfn-tag", "getEdgePoints");
        List<PointF> pointFs = getContourEdgePoints(tempBitmap);
        Log.i("ORDERSLIST",pointFs.toString());
        Map<Integer, PointF> orderedPoints = orderedValidEdgePoints(tempBitmap, pointFs);
        return orderedPoints;
    }

    private List<PointF> getContourEdgePoints(Bitmap tempBitmap) {
        Log.v("tfn-tag", "getContourEdgePoints");

        MatOfPoint2f point2f = nativeClass.getPoint(tempBitmap);

        List<Point> points = Arrays.asList(point2f.toArray());
        List<PointF> result = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            result.add(new PointF(((float) points.get(i).x), ((float) points.get(i).y)));
        }
        Log.i("tfn-tag","HERE");
        return result;

    }

    private Map<Integer, PointF> getOutlinePoints(Bitmap tempBitmap) {
        Log.v("tfn-tag", "getOutlinePoints");
        Map<Integer, PointF> outlinePoints = new HashMap<>();
        outlinePoints.put(0, new PointF(0, 0));
        outlinePoints.put(1, new PointF(tempBitmap.getWidth(), 0));
        outlinePoints.put(2, new PointF(0, tempBitmap.getHeight()));
        outlinePoints.put(3, new PointF(tempBitmap.getWidth(), tempBitmap.getHeight()));
        return outlinePoints;
    }

    private Map<Integer, PointF> orderedValidEdgePoints(Bitmap tempBitmap, List<PointF> pointFs) {
        Log.v("tfn-tag", "orderedValidEdgePoints");
        Map<Integer, PointF> orderedPoints = polygonView.getOrderedPoints(pointFs);
        if (!polygonView.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints(tempBitmap);
        }
        return orderedPoints;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                MyConstants.selectedImageBitmapList.set(MyConstants.count, bitmap);
                MyConstants.count +=1;
                if(MyConstants.selectedImageBitmapList.size() > MyConstants.count){
                    Intent intent = new Intent(CropActivity.this, CropActivity.class);
                    startActivity(intent);
                }else {
                    MyConstants.count = 0;
                    Intent intent = new Intent(CropActivity.this, FilterActivity.class);
                    startActivity(intent);
                }
                finish();
            }catch(Exception e){
                Intent intent = new Intent(CropActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Toast.makeText(this,cropError+"", Toast.LENGTH_SHORT).show();
            finish();
        }else{
            finish();
        }
    }
}