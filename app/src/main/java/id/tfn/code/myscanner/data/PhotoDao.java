package id.tfn.code.myscanner.data;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PhotoDao {

    @Insert
    void insert(Photo photo);

    @Update
    void update(Photo photo);

    @Delete
    void delete(Photo photo);

    @Query("DELETE FROM photo_data ")
    void deleteAllPhotos();

    @Query("SELECT * FROM photo_data")
    LiveData<List<Photo>> getAllPhotos();

}
