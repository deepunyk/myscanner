package id.tfn.code.myscanner.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = Photo.class, version = 2)
public abstract class PhotoDatabase extends RoomDatabase {

    private static PhotoDatabase instance;

    public abstract PhotoDao photoDao();

    public static synchronized PhotoDatabase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    PhotoDatabase.class,
                    "photo_database").fallbackToDestructiveMigration().build();
        }
        return instance;
    }

}
