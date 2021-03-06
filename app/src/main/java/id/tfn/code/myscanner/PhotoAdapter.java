package id.tfn.code.myscanner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import id.tfn.code.myscanner.data.Photo;
import id.tfn.code.myscanner.data.PhotoViewModel;
import id.tfn.code.myscanner.helpers.MyConstants;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoHolder> {

    private List<Photo> photos = new ArrayList<>();
    private PhotoViewModel photoViewModel;
    private Context context;

    @NonNull
    @Override
    public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_item, parent, false);

        return new PhotoHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
        final Photo curPhoto = photos.get(position);
        Log.i("VIEWID", curPhoto.getId() + "");

        File imgFile = new  File(curPhoto.getUrl());
        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        holder.img.setImageBitmap(getResizedBitmap(bitmap,800));
        String curPage = ""+(position+1);
        holder.numberText.setText(curPage);
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteDiagog(context, curPhoto);
            }
        });
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditActivity.class);
                MyConstants.filter = curPhoto.getFilter();
                MyConstants.id = curPhoto.getId();
                MyConstants.url = curPhoto.getUrl();
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public void setPhotos(List<Photo> photos, PhotoViewModel photoViewModel, Context context){
        this.photos = photos;
        this.photoViewModel = photoViewModel;
        this.context = context;
        notifyDataSetChanged();
    }


    class PhotoHolder extends RecyclerView.ViewHolder{
        private ImageView img;
        private Button deleteButton;
        private CardView parentLayout;
        private TextView numberText;

        public PhotoHolder(View itemView){
            super(itemView);
            img = itemView.findViewById(R.id.scan_img);
            deleteButton = itemView.findViewById(R.id.delete_button);
            parentLayout = itemView.findViewById(R.id.parent_layout);
            numberText = itemView.findViewById(R.id.number_txt);
        }
    }

    private void showDeleteDiagog(Context c, final Photo photo) {
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Delete image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File file = new File(photo.getUrl());
                        boolean deleted = file.getAbsoluteFile().delete();
                        photoViewModel.delete(photo);
                    }
                })
                .setNegativeButton("No", null)
                .create();
        dialog.show();
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}
