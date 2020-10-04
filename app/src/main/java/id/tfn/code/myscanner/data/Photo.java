package id.tfn.code.myscanner.data;

import android.graphics.Bitmap;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "photo_data")
public class Photo {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String url;


    private String filter;

    public void setId(int id) {
        this.id = id;
    }

    public Photo(String url, String filter) {
        this.url = url;
        this.filter = filter;
    }

    public int getId() {
        return id;
    }

    public String getBitmap() {
        return url;
    }

    public String getFilter() {
        return filter;
    }
}
