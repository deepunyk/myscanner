package id.tfn.code.myscanner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import id.tfn.code.myscanner.data.Photo;
import id.tfn.code.myscanner.data.PhotoViewModel;

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
        final Bitmap bitmap = BitmapFactory.decodeByteArray(curPhoto.getBitmap(), 0, curPhoto.getBitmap().length);
        holder.img.setImageBitmap(bitmap);
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteDiagog(context, curPhoto);
            }
        });
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditActivity.class);
                intent.putExtra("image", curPhoto.getBitmap());
                intent.putExtra("filter", curPhoto.getFilter());
                intent.putExtra("id", curPhoto.getId());
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
        private Button deleteButton, editButton;

        public PhotoHolder(View itemView){
            super(itemView);
            img = itemView.findViewById(R.id.scan_img);
            deleteButton = itemView.findViewById(R.id.delete_button);
            editButton = itemView.findViewById(R.id.edit_button);
        }
    }

    private void showDeleteDiagog(Context c, final Photo photo) {
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Delete image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        photoViewModel.delete(photo);
                    }
                })
                .setNegativeButton("No", null)
                .create();
        dialog.show();
    }
}
