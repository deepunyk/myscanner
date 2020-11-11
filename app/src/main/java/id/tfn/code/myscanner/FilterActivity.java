package id.tfn.code.myscanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.arasthel.asyncjob.AsyncJob;

import net.alhazmy13.imagefilter.ImageFilter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Random;

import id.tfn.code.myscanner.data.Photo;
import id.tfn.code.myscanner.data.PhotoViewModel;
import id.tfn.code.myscanner.helpers.MyConstants;
import id.tfn.code.myscanner.libraries.NativeClass;

public class FilterActivity extends AppCompatActivity {

    Toolbar toolbar;
    ImageView img;
    LinearLayout originalLayout, gothamLayout, oldLayout, sketchLayout, hdrLayout, magicLayout, grayLayout;
    ImageView originalImage, gothamImage, oldImage, sketchImage, hdrImage, magicImage, grayImage;
    int id = 0;
    NativeClass nativeClass = new NativeClass();
    Bitmap originalBitmap;
    int code = 0;
    private PhotoViewModel photoViewModel;
    Button applyButton, applyAllButton;
    Bitmap tempBitMap;
    String filter = "original";
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        photoViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(PhotoViewModel.class);


        img = (ImageView) findViewById(R.id.img);
        originalLayout = (LinearLayout) findViewById(R.id.original_layout);
        gothamLayout = (LinearLayout) findViewById(R.id.gotham_layout);
        oldLayout = (LinearLayout) findViewById(R.id.old_layout);
        sketchLayout = (LinearLayout) findViewById(R.id.sketch_layout);
        hdrLayout = (LinearLayout) findViewById(R.id.hdr_layout);
        magicLayout = (LinearLayout) findViewById(R.id.magic_layout);
        grayLayout = (LinearLayout) findViewById(R.id.gray_layout);

        originalImage = (ImageView) findViewById(R.id.original_image);
        gothamImage = (ImageView) findViewById(R.id.gotham_image);
        oldImage = (ImageView) findViewById(R.id.old_image);
        sketchImage = (ImageView) findViewById(R.id.sketch_image);
        hdrImage = (ImageView) findViewById(R.id.hdr_image);
        magicImage = (ImageView) findViewById(R.id.magic_image);
        grayImage = (ImageView) findViewById(R.id.gray_image);
        applyButton = (Button) findViewById(R.id.apply_button);
        applyAllButton = (Button) findViewById(R.id.apply_all_button);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        originalBitmap = MyConstants.selectedImageBitmapList.get(MyConstants.count);
        tempBitMap = originalBitmap;
        img.setImageBitmap(originalBitmap);
        originalImage.setImageBitmap(originalBitmap);
        gothamImage.setImageBitmap(ImageFilter.applyFilter(originalBitmap, ImageFilter.Filter.GOTHAM));
        oldImage.setImageBitmap(ImageFilter.applyFilter(originalBitmap, ImageFilter.Filter.OLD));
        sketchImage.setImageBitmap(ImageFilter.applyFilter(originalBitmap, ImageFilter.Filter.SKETCH));
        hdrImage.setImageBitmap(ImageFilter.applyFilter(originalBitmap, ImageFilter.Filter.HDR));
        grayImage.setImageBitmap(nativeClass.FilterGray(originalBitmap));
        magicImage.setImageBitmap(nativeClass.FilterMagic(originalBitmap));


        originalLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                img.setImageBitmap(originalBitmap);
                filter = "original";
            }
        });

        grayLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempBitMap = nativeClass.FilterGray(originalBitmap);
                img.setImageBitmap(tempBitMap);
                filter = "gray";
            }
        });

        magicLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempBitMap = nativeClass.FilterMagic(originalBitmap);
                img.setImageBitmap(tempBitMap);
                filter = "magic";
            }
        });

        gothamLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempBitMap = ImageFilter.applyFilter(originalBitmap, ImageFilter.Filter.GOTHAM);
                img.setImageBitmap(tempBitMap);
                filter = "gotham";
            }
        });

        oldLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempBitMap = ImageFilter.applyFilter(originalBitmap, ImageFilter.Filter.OLD);
                img.setImageBitmap(tempBitMap);
                filter = "old";
            }
        });

        sketchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempBitMap = ImageFilter.applyFilter(originalBitmap, ImageFilter.Filter.SKETCH);
                img.setImageBitmap(tempBitMap);
                filter = "sketch";
            }
        });

        hdrLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempBitMap = ImageFilter.applyFilter(originalBitmap, ImageFilter.Filter.HDR);
                img.setImageBitmap(tempBitMap);
                filter = "hdr";
            }
        });

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                applyAllButton.setEnabled(false);
                applyButton.setEnabled(false);
                new AsyncJob.AsyncJobBuilder<Boolean>()
                        .doInBackground(new AsyncJob.AsyncAction<Boolean>() {
                            @Override
                            public Boolean doAsync() {
                                try {
                                    saveImage(tempBitMap, filter);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return true;
                            }
                        })
                        .doWhenFinished(new AsyncJob.AsyncResultAction<Boolean>() {
                            @Override
                            public void onResult(Boolean result) {
                                applyAllButton.setEnabled(true);
                                applyButton.setEnabled(true);
                            }
                        }).create().start();
            }
        });

        applyAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                applyAllButton.setEnabled(false);
                applyButton.setEnabled(false);
                new AsyncJob.AsyncJobBuilder<Boolean>()
                        .doInBackground(new AsyncJob.AsyncAction<Boolean>() {
                            @Override
                            public Boolean doAsync() {
                                try {
                                    for (int i = MyConstants.count; i < MyConstants.selectedImageBitmapList.size(); i++) {
                                        try {
                                            Random r = new Random();
                                            int name = r.nextInt(10000000) + 10000000;
                                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                            tempBitMap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                            OutputStream fOut = null;
                                            String path = MyConstants.directoryPath + "/" + name + ".png";
                                            File file = new File(path);
                                            fOut = new FileOutputStream(file);
                                            tempBitMap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                                            fOut.flush();
                                            fOut.close();
                                            Photo photo = new Photo(path, filter);
                                            photoViewModel.insert(photo);
                                            MyConstants.count += 1;
                                            Log.i("COUNT", MyConstants.count + "");
                                            if (MyConstants.selectedImageBitmapList.size() <= MyConstants.count) {
                                                Intent intent = new Intent(FilterActivity.this, HomeActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                            }else{
                                                tempBitMap = getBitmap(MyConstants.selectedImageBitmapList.get(MyConstants.count),filter);
                                                img.setImageBitmap(tempBitMap);
                                            }
                                        } catch (Exception e) {

                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return true;
                            }
                        })
                        .doWhenFinished(new AsyncJob.AsyncResultAction<Boolean>() {
                            @Override
                            public void onResult(Boolean result) {
                            }
                        }).create().start();
            }
        });
    }

    void saveImage(Bitmap bitmap, String filter) {
        try {
            Random r = new Random();
            int name = r.nextInt(10000000) + 10000000;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            OutputStream fOut = null;
            String path = MyConstants.directoryPath + "/" + name + ".png";
            File file = new File(path);
            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
            Photo photo = new Photo(path, filter);
            photoViewModel.insert(photo);
            MyConstants.count += 1;
            if (MyConstants.selectedImageBitmapList.size() > MyConstants.count) {

                Intent intent = new Intent(FilterActivity.this, FilterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                Intent intent = new Intent(FilterActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        } catch (Exception e) {

        }
    }

    void saveAllmage(Bitmap bitmap, String filter) {
        for (int i = MyConstants.count; i < MyConstants.selectedImageBitmapList.size(); i++) {
            try {
                Random r = new Random();
                int name = r.nextInt(10000000) + 10000000;
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                OutputStream fOut = null;
                String path = MyConstants.directoryPath + "/" + name + ".png";
                File file = new File(path);
                fOut = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                fOut.flush();
                fOut.close();
                Photo photo = new Photo(path, filter);
                photoViewModel.insert(photo);
                MyConstants.count += 1;
                Log.i("COUNT", MyConstants.count + "");
                if (MyConstants.selectedImageBitmapList.size() <= MyConstants.count) {
                    Intent intent = new Intent(FilterActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }else{
                    bitmap = getBitmap(MyConstants.selectedImageBitmapList.get(MyConstants.count),filter);
                }
            } catch (Exception e) {

            }
        }
    }

    Bitmap getBitmap(Bitmap bitmap, String filter) {
        switch (filter) {
            case "original":
                bitmap = bitmap;
                break;
            case "gray":
                bitmap = nativeClass.FilterGray(bitmap);
                break;
            case "magic":
                bitmap = nativeClass.FilterMagic(bitmap);
                break;
            case "gotham":
                bitmap = ImageFilter.applyFilter(originalBitmap, ImageFilter.Filter.GOTHAM);
                break;
            case "old":
                bitmap = ImageFilter.applyFilter(bitmap, ImageFilter.Filter.OLD);
                break;
            case "sketch":
                bitmap = ImageFilter.applyFilter(bitmap, ImageFilter.Filter.SKETCH);
                break;
            case "hdr":
                bitmap = ImageFilter.applyFilter(bitmap, ImageFilter.Filter.HDR);
                break;
            default:
                bitmap = bitmap;
        }
        return bitmap;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}