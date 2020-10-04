package id.tfn.code.myscanner.helpers;

import android.graphics.Bitmap;

public class MyConstants {
    public final static int GALLERY_IMAGE_LOADED = 1001;
    public static Bitmap selectedImageBitmap;
    public static int id;
    public static String filter;
    public static String url;
    public final static String directoryPath = android.os.Environment.getExternalStorageDirectory().toString();
}
