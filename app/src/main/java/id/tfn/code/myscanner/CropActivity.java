package id.tfn.code.myscanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.tfn.code.myscanner.data.Photo;
import id.tfn.code.myscanner.data.PhotoViewModel;
import id.tfn.code.myscanner.helpers.MyConstants;
import id.tfn.code.myscanner.libraries.NativeClass;
import id.tfn.code.myscanner.libraries.PolygonView;

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
            selectedImageBitmap = MyConstants.selectedImageBitmap;
            MyConstants.selectedImageBitmap = null;

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
            Toast.makeText(this, "No document found", Toast.LENGTH_SHORT).show();
            Bitmap tempBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(tempBitmap.getWidth(), tempBitmap.getHeight());
            layoutParams.gravity = Gravity.CENTER;

            polygonView.setVisibility(View.VISIBLE);

            polygonView.setLayoutParams(layoutParams);
        }
    }

    private View.OnClickListener btnImageEnhanceClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Bitmap bitmap = getCroppedImage();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            bitmap.recycle();
            Photo photo = new Photo(byteArray,"original");
            photoViewModel.insert(photo);
            Intent intent = new Intent(CropActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
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
}