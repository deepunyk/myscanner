package id.tfn.code.myscanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import net.alhazmy13.imagefilter.ImageFilter;

import id.tfn.code.myscanner.data.Photo;
import id.tfn.code.myscanner.data.PhotoViewModel;

public class FilterActivity extends AppCompatActivity {

    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        img = (ImageView)findViewById(R.id.img);
        final byte[] imageArray = getIntent().getByteArrayExtra("image");
        final Bitmap bitmap = BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length);
        ImageFilter.applyFilter(bitmap, ImageFilter.Filter.INVERT);
        img.setImageBitmap(ImageFilter.applyFilter(bitmap, ImageFilter.Filter.GRAY));
    }
}