package id.tfn.code.myscanner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import id.tfn.code.myscanner.data.Photo;
import id.tfn.code.myscanner.data.PhotoViewModel;
import id.tfn.code.myscanner.helpers.MyConstants;

public class HomeActivity extends AppCompatActivity {

    Uri selectedImage;
    Bitmap selectedBitmap;
    Button deleteBut, saveBut;
    ImageButton scanButton;
    private PhotoViewModel photoViewModel;
    RecyclerView recyclerView;
    Toolbar toolbar;
    ConstraintLayout emptyLayout;
    Menu homeMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        scanButton = (ImageButton) findViewById(R.id.scan_button);
        recyclerView = findViewById(R.id.recylerView);
        emptyLayout = findViewById(R.id.empty_layout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        final PhotoAdapter adapter = new PhotoAdapter();
        recyclerView.setAdapter(adapter);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        photoViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(PhotoViewModel.class);

        photoViewModel.getAllPhotos().observe(this, new Observer<List<Photo>>() {
            @Override
            public void onChanged(List<Photo> photos) {
                if(photos.size()==0){
                    emptyLayout.setVisibility(View.VISIBLE);
                }else{
                    emptyLayout.setVisibility(View.GONE);
                }
                adapter.setPhotos(photos, photoViewModel, HomeActivity.this);
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.Companion.with(HomeActivity.this)
                        .compress(2000)
                        .maxResultSize(2000, 2000)
                        .start();
            }
        });

    }

    public void imageToPDF(String title) throws FileNotFoundException {
        try {
            Document document = new Document();

            String directoryPath = android.os.Environment.getExternalStorageDirectory().toString();

            if(title.equals("")){
                title = "myscanner";
            }

            PdfWriter.getInstance(document, new FileOutputStream(directoryPath + "/"+title+".pdf"));
            Log.i("DIR PATH",directoryPath);
            document.open();

            List<Photo> photos = photoViewModel.getAllPhotos().getValue();

            for(int i = 0; i<photos.size();i++) {
                Photo photo = photos.get(i);
                Image image = Image.getInstance(photo.getUrl());

                float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                        - document.rightMargin() - 0) / image.getWidth()) * 100;
                image.scalePercent(scaler);
                image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);

                document.add(image);
            }
            document.close();

            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.setType("application/pdf");
            final File pdfFile = new File(directoryPath, title+".pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", pdfFile));
            startActivity(Intent.createChooser(shareIntent, "Share image using"));
        } catch (Exception e) {
            Toast.makeText(HomeActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            Log.i("ERROR SAVING", e.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            selectedImage = data.getData();
            this.loadImage();
        }
    }

    private void loadImage() {
        try {
            InputStream inputStream = getContentResolver().openInputStream(this.selectedImage);
            selectedBitmap = BitmapFactory.decodeStream(inputStream);
            MyConstants.selectedImageBitmap = selectedBitmap;
            Intent intent = new Intent(getApplicationContext(), CropActivity.class);
            startActivity(intent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.home_menu, menu);
        homeMenu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        try {
            if (photoViewModel.getAllPhotos().getValue().size() == 0) {
                menu.findItem(R.id.action_pdf).setVisible(false);
                menu.findItem(R.id.action_delete).setVisible(false);
            } else {
                menu.findItem(R.id.action_pdf).setVisible(true);
                menu.findItem(R.id.action_delete).setVisible(true);
            }
        }catch(Exception e){

        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_pdf) {
            try {
                showSaveDialog(HomeActivity.this);
                }catch (Exception e){
                    Toast.makeText(HomeActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
               }
            return true;
        }else if(id == R.id.action_delete){
            showDeleteDiagog(HomeActivity.this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSaveDialog(Context c) {
        final EditText taskEditText = new EditText(c);
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Save to PDF")
                .setMessage("Enter name of the file")
                .setView(taskEditText)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String task = String.valueOf(taskEditText.getText());
                        try {
                            imageToPDF(task);
                        }catch (Exception e){
                            Toast.makeText(HomeActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private void showDeleteDiagog(Context c) {
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Delete all iamges")
                .setMessage("Are you sure you want to delete all images?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        photoViewModel.deleteAllPhotos();
                    }
                })
                .setNegativeButton("No", null)
                .create();
        dialog.show();
    }
}