package id.tfn.code.myscanner.data;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import java.util.List;

public class PhotoRepository {
    private PhotoDao photoDao;

    private LiveData<List<Photo>> allPhotos;

    public PhotoRepository(Application application){
        PhotoDatabase photoDatabase = PhotoDatabase.getInstance(application);
        photoDao = photoDatabase.photoDao();
        allPhotos = photoDao.getAllPhotos();
    }

    public void insert(Photo photo){
        new InsertPhotoAsyncTask(photoDao).execute(photo);
    }

    public void update(Photo photo){
        new UpdatePhotoAsyncTask(photoDao).execute(photo);
    }
    public void delete(Photo photo){
        new DeletePhotoAsyncTask(photoDao).execute(photo);
    }

    public void deleteAllPhotos(){
        new DeleteAllPhotoAsyncTask(photoDao).execute();
    }

    public LiveData<List<Photo>> getAllPhotos() {
        return allPhotos;
    }

    public static class InsertPhotoAsyncTask extends AsyncTask<Photo, Void, Void>{

        private PhotoDao photoDao;

        private InsertPhotoAsyncTask(PhotoDao photoDao){
            this.photoDao = photoDao;
        }

        @Override
        protected Void doInBackground(Photo... photos) {
            photoDao.insert(photos[0]);
            return null;
        }
    }

    public static class UpdatePhotoAsyncTask extends AsyncTask<Photo, Void, Void>{

        private PhotoDao photoDao;

        private UpdatePhotoAsyncTask(PhotoDao photoDao){
            this.photoDao = photoDao;
        }

        @Override
        protected Void doInBackground(Photo... photos) {
            photoDao.update(photos[0]);
            return null;
        }
    }

    public static class DeletePhotoAsyncTask extends AsyncTask<Photo, Void, Void>{

        private PhotoDao photoDao;

        private DeletePhotoAsyncTask(PhotoDao photoDao){
            this.photoDao = photoDao;
        }

        @Override
        protected Void doInBackground(Photo... photos) {
            photoDao.delete(photos[0]);
            return null;
        }
    }

    public static class DeleteAllPhotoAsyncTask extends AsyncTask<Photo, Void, Void>{

        private PhotoDao photoDao;

        private DeleteAllPhotoAsyncTask(PhotoDao photoDao){
            this.photoDao = photoDao;
        }

        @Override
        protected Void doInBackground(Photo... photos) {
            photoDao.deleteAllPhotos();
            return null;
        }
    }
}
