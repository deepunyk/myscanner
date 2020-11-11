package id.tfn.code.myscanner.helpers;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class MyConstants {
    public final static int GALLERY_IMAGE_LOADED = 1001;
    public static ArrayList<Bitmap> selectedImageBitmapList = new ArrayList<>();
    public static int count = 0;
    public static int id;
    public static String filter;
    public static String url;
    public final static String directoryPath = android.os.Environment.getExternalStorageDirectory().toString();
}
