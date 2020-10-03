package id.tfn.code.myscanner.data;

import android.graphics.Bitmap;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "photo_data")
public class Photo {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private byte[] bitmap;

    private String filter;

    public void setId(int id) {
        this.id = id;
    }

    public Photo(byte[] bitmap, String filter) {
        this.bitmap = bitmap;
        this.filter = filter;
    }

    public int getId() {
        return id;
    }

    public byte[] getBitmap() {
        return bitmap;
    }

    public String getFilter() {
        return filter;
    }
}
