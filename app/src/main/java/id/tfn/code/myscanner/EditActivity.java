package id.tfn.code.myscanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import net.alhazmy13.imagefilter.ImageFilter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import id.tfn.code.myscanner.data.Photo;
import id.tfn.code.myscanner.data.PhotoViewModel;
import id.tfn.code.myscanner.helpers.MyConstants;
import id.tfn.code.myscanner.libraries.NativeClass;

public class EditActivity extends AppCompatActivity {


    Toolbar toolbar;
    ImageView img;
    LinearLayout originalLayout, gothamLayout, oldLayout, sketchLayout, hdrLayout, magicLayout, grayLayout;
    ImageView originalImage, gothamImage, oldImage, sketchImage, hdrImage, magicImage, grayImage;
    private PhotoViewModel photoViewModel;
    int id = 0;
    String filter = "";
    String url = "";
    NativeClass nativeClass = new NativeClass();
    Bitmap originalBitmap;
    int code = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        photoViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(PhotoViewModel.class);


        img = (ImageView)findViewById(R.id.img);
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

        id = MyConstants.id;
        filter = MyConstants.filter;
        url = MyConstants.url;

        originalBitmap = BitmapFactory.decodeFile(url);
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
                updateImage(originalBitmap,filter);
            }
        });

        grayLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap tempBitMap = nativeClass.FilterGray(originalBitmap);
                img.setImageBitmap(tempBitMap);
                updateImage(tempBitMap,"gray");
            }
        });

        magicLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap tempBitMap = nativeClass.FilterMagic(originalBitmap);
                img.setImageBitmap(tempBitMap);
                updateImage(tempBitMap,"magic");
            }
        });

        gothamLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap tempBitMap = ImageFilter.applyFilter(originalBitmap, ImageFilter.Filter.GOTHAM);
                img.setImageBitmap(tempBitMap);
                updateImage(tempBitMap,"gotham");
            }
        });

        oldLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap tempBitMap = ImageFilter.applyFilter(originalBitmap, ImageFilter.Filter.OLD);
                img.setImageBitmap(tempBitMap);
                updateImage(tempBitMap,"old");
            }
        });

        sketchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap tempBitMap = ImageFilter.applyFilter(originalBitmap, ImageFilter.Filter.SKETCH);
                img.setImageBitmap(tempBitMap);
                updateImage(tempBitMap,"sketch");
            }
        });

        hdrLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap tempBitMap = ImageFilter.applyFilter(originalBitmap, ImageFilter.Filter.HDR);
                img.setImageBitmap(tempBitMap);
                updateImage(tempBitMap,"hdr");
            }
        });
    }

    void updateImage(Bitmap bitmap, String filter){
        try {
            OutputStream fOut = null;
            File file = new File(url);
            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        Photo photo = new Photo(url,filter);
        photo.setId(id);
        photoViewModel.update(photo);
        }catch (Exception e){

        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}