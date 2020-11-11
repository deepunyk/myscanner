package id.tfn.code.myscanner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Path;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.arasthel.asyncjob.AsyncJob;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.github.dhaval2404.imagepicker.provider.CameraProvider;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import id.tfn.code.myscanner.data.Photo;
import id.tfn.code.myscanner.data.PhotoViewModel;
import id.tfn.code.myscanner.helpers.MyConstants;

public class HomeActivity extends AppCompatActivity {

    ArrayList<Uri> selectedImageList;
    Bitmap selectedBitmap;
    ImageButton cameraButton, shareButton, galleryButton, addButton;
    private PhotoViewModel photoViewModel;
    RecyclerView recyclerView;
    Toolbar toolbar;
    ConstraintLayout emptyLayout;
    Menu homeMenu;
    List<Photo> allPhotos;
    String imageEncoded;
    List<String> imagesEncodedList;
    ProgressBar progressBar;
    AdView adView;
    private InterstitialAd mInterstitialAd;
    String task = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3691525575191501/7816824425");

        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        selectedImageList = new ArrayList<>();

        cameraButton = (ImageButton) findViewById(R.id.camera_button);
        addButton = (ImageButton) findViewById(R.id.add_button);
        shareButton = (ImageButton) findViewById(R.id.share_button);
        galleryButton = (ImageButton) findViewById(R.id.gallery_button);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recylerView);
        emptyLayout = findViewById(R.id.empty_layout);
        adView = findViewById(R.id.adView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(HomeActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        final PhotoAdapter adapter = new PhotoAdapter();
        recyclerView.setAdapter(adapter);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        photoViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(PhotoViewModel.class);


//        AdRequest adRequest = new AdRequest.Builder().build();
//        adView.loadAd(adRequest);

        photoViewModel.getAllPhotos().observe(this, new Observer<List<Photo>>() {
            @Override
            public void onChanged(List<Photo> photos) {
                allPhotos = photos;
                if (photos.size() == 0) {
                    shareButton.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.VISIBLE);
                    addButton.setVisibility(View.GONE);
                    cameraButton.setVisibility(View.VISIBLE);
                    galleryButton.setVisibility(View.VISIBLE);
                    if(homeMenu != null){
                        homeMenu.findItem(R.id.action_delete).setVisible(false);
                    }
                } else {
                    shareButton.setVisibility(View.VISIBLE);
                    emptyLayout.setVisibility(View.GONE);
                    addButton.setVisibility(View.VISIBLE);
                    cameraButton.setVisibility(View.GONE);
                    galleryButton.setVisibility(View.GONE);
                    if(homeMenu != null){
                        homeMenu.findItem(R.id.action_delete).setVisible(true);
                    }
                }
                adapter.setPhotos(photos, photoViewModel, HomeActivity.this);
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String directoryPath = android.os.Environment.getExternalStorageDirectory().toString() + "/MyScanner";
                MyConstants.selectedImageBitmapList.clear();
                MyConstants.count = 0;
                ImagePicker.Companion.with(HomeActivity.this)
                        .saveDir(directoryPath)
                        .cameraOnly()
                        .start();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
//                String directoryPath = android.os.Environment.getExternalStorageDirectory().toString() + "/MyScanner";
//                MyConstants.selectedImageBitmapList.clear();
//                MyConstants.count = 0;
//                ImagePicker.Companion.with(HomeActivity.this)
//                        .saveDir(directoryPath)
//                        .start();
                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle("Select application")
                        .setMessage("Please select your application to scan the photos")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String directoryPath = android.os.Environment.getExternalStorageDirectory().toString() + "/MyScanner";
                                MyConstants.selectedImageBitmapList.clear();
                                MyConstants.count = 0;
                                ImagePicker.Companion.with(HomeActivity.this)
                                        .saveDir(directoryPath)
                                        .cameraOnly()
                                        .start();
                            }
                        })
                        .setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                checkUserPermission(view);
                            }
                        })
                        .show();
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUserPermission(view);
                            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSaveDialog(HomeActivity.this);
            }
        });

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                new AsyncJob.AsyncJobBuilder<Boolean>()
                        .doInBackground(new AsyncJob.AsyncAction<Boolean>() {
                            @Override
                            public Boolean doAsync() {
                                try {
                                    imageToPDF(task);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return true;
                            }
                        })
                        .doWhenFinished(new AsyncJob.AsyncResultAction<Boolean>() {
                            @Override
                            public void onResult(Boolean result) {
                                progressBar.setVisibility(View.GONE);
                            }
                        }).create().start();
            }
        });
    }

    public void checkUserPermission(View view){
        String requiredPermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int checkVal = HomeActivity.this.checkCallingOrSelfPermission(requiredPermission);
        if (checkVal==PackageManager.PERMISSION_GRANTED){
            MyConstants.selectedImageBitmapList.clear();
            MyConstants.count = 0;
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 22);
        }else{
            requestCameraAndStorage(view);
        }
    }

    public void imageToPDF(String title) throws FileNotFoundException {

        try {
            Document document = new Document();

            String directoryPath = android.os.Environment.getExternalStorageDirectory().toString();

            if (title.equals("")) {
                title = "myscanner";
            }

            PdfWriter.getInstance(document, new FileOutputStream(directoryPath + "/" + title + ".pdf"));
            Log.i("DIR PATH", directoryPath);
            document.open();

            List<Photo> photos = photoViewModel.getAllPhotos().getValue();

            for (int i = 0; i < photos.size(); i++) {
                Photo photo = photos.get(i);
                Image image = Image.getInstance(photo.getUrl());


//                float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
//                        - document.rightMargin() - 0) / image.getWidth()) * 100;
//                float scaler = ((document.getPageSize().getWidth()) / image.getWidth()) * 100;
//                image.scalePercent(scaler);
                float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                        - document.rightMargin()) / image.getWidth()) * 100;
                image.scalePercent(scaler);
                image.setAlignment(Image.ALIGN_TOP);

                document.setPageSize(new Rectangle(document.getPageSize().getWidth(), image.getPlainHeight()+60));
                document.newPage();

                document.add(image);
            }
            document.close();
            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.setType("application/pdf");
            final File pdfFile = new File(directoryPath, title + ".pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", pdfFile));
            startActivity(Intent.createChooser(shareIntent, "Share image using"));
        } catch (Exception e) {
            Toast.makeText(HomeActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            Log.i("ERROR SAVING", e.toString());
        }
    }

    public void requestCameraAndStorage(final View view) {
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        Permissions.check(this, permissions, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                checkUserPermission(view);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        selectedImageList.clear();
        if (requestCode == 22) {
            try {
                if (resultCode == RESULT_OK
                        && null != data) {
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    imagesEncodedList = new ArrayList<String>();
                    if (data.getData() != null) {
                        Uri mImageUri = data.getData();
                        Cursor cursor = getContentResolver().query(mImageUri,
                                filePathColumn, null, null, null);
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        imageEncoded = cursor.getString(columnIndex);
                        cursor.close();
                        selectedImageList.add(data.getData());
                        this.loadImage();
                    } else {
                        if (data.getClipData() != null) {
                            ClipData mClipData = data.getClipData();
                            ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                            for (int i = 0; i < mClipData.getItemCount(); i++) {

                                ClipData.Item item = mClipData.getItemAt(i);
                                Uri uri = item.getUri();
                                mArrayUri.add(uri);
                                // Get the cursor
                                Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                                // Move to first row
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                imageEncoded = cursor.getString(columnIndex);
                                imagesEncodedList.add(imageEncoded);
                                cursor.close();
                                selectedImageList.add(uri);
                            }
                            this.loadImage();
                            Log.v("LOG_TAG", "Selected Images" + mArrayUri.size());
                        }
                    }
                }
            } catch (Exception e) {
                Log.i("Exception",e.toString());
            }
        } else {
            if (resultCode == RESULT_OK && data != null) {
                selectedImageList.add(data.getData());
                this.loadImage();
            }
        }
    }

    private void loadImage() {
        try {
            MyConstants.selectedImageBitmapList.clear();;
            MyConstants.count = 0;
            for(int i = 0;i<selectedImageList.size();i++){
                InputStream inputStream = getContentResolver().openInputStream(selectedImageList.get(i));
                selectedBitmap = BitmapFactory.decodeStream(inputStream);
                Log.i("YHOOOOOOOOOOOOOOOO", "loadImage: " +selectedImageList.get(i).getEncodedPath());
                MyConstants.selectedImageBitmapList.add(modifyOrientation(selectedBitmap, getPath(this, selectedImageList.get(i))));
            }
            Intent intent = new Intent(getApplicationContext(), CropActivity.class);
            startActivity(intent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.home_menu, menu);
        homeMenu = menu;
        if (photoViewModel.getAllPhotos().getValue() != null && photoViewModel.getAllPhotos().getValue().size() == 0) {
            menu.findItem(R.id.action_delete).setVisible(false);
        } else {
            menu.findItem(R.id.action_delete).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        try {
            if (photoViewModel.getAllPhotos().getValue().size() == 0) {
                menu.findItem(R.id.action_delete).setVisible(false);
            } else {
                menu.findItem(R.id.action_delete).setVisible(true);
            }
        } catch (Exception e) {

        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_delete) {
            showDeleteDiagog(HomeActivity.this, item);

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
                        task = String.valueOf(taskEditText.getText());
                        progressBar.setVisibility(View.VISIBLE);
                        if (mInterstitialAd.isLoaded()) {
                            //mInterstitialAd.show();
                        } else {
                            new AsyncJob.AsyncJobBuilder<Boolean>()
                                    .doInBackground(new AsyncJob.AsyncAction<Boolean>() {
                                        @Override
                                        public Boolean doAsync() {
                                            try {
                                                imageToPDF(task);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            return true;
                                        }
                                    })
                                    .doWhenFinished(new AsyncJob.AsyncResultAction<Boolean>() {
                                        @Override
                                        public void onResult(Boolean result) {
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }).create().start();
                        }
                        try {


                        } catch (Exception e) {
                            Toast.makeText(HomeActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private void showDeleteDiagog(Context c, final MenuItem item) {
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Delete all images")
                .setMessage("Are you sure you want to delete all images?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < allPhotos.size(); i++) {
                            File file = new File(allPhotos.get(i).getUrl());
                            boolean deleted = file.getAbsoluteFile().delete();
                        }
                        photoViewModel.deleteAllPhotos();
                        MyConstants.selectedImageBitmapList.clear();;
                        MyConstants.count = 0;
                    }
                })
                .setNegativeButton("No", null)
                .create();
        dialog.show();
    }

    public static Bitmap modifyOrientation(Bitmap bitmap, String image_absolute_path) throws IOException {
        ExifInterface ei = new ExifInterface(image_absolute_path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotate(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotate(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotate(bitmap, 270);

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return flip(bitmap, true, false);

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return flip(bitmap, false, true);

            default:
                return bitmap;
        }
    }

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}