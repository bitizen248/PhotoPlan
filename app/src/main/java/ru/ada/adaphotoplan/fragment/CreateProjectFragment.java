package ru.ada.adaphotoplan.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.ada.adaphotoplan.R;
import ru.ada.adaphotoplan.activity.ProjectActivity;
import ru.ada.adaphotoplan.helpers.DrawHelper;
import ru.ada.adaphotoplan.helpers.ProjectFactory;


/**
 * Created by Bitizen on 28.03.17.
 */

public class CreateProjectFragment extends Fragment {
    private static final String TAG = "CreateProjectFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_project, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup takePhoto = view.findViewById(R.id.take_photo);
        ViewGroup fromGallery = view.findViewById(R.id.fromGallery);

        ViewGroup grid = view.findViewById(R.id.grid);


        takePhoto.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(getContext(),
                            "ru.ada.adaphotoplan.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, 1);
                }
            }
        });

        fromGallery.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, 0);
        });

        grid.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ProjectActivity.class);
            intent.putExtra(ProjectActivity.PROJECT_ID, ProjectFactory.createGrid("Проект " + ProjectFactory.getNextKey(), 10000, 10000));
            getContext().startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: ");
        View view = getView();
        if (view != null) {
            ViewGroup lastProject = view.findViewById(R.id.lastProject);
            ImageView thumbnail = view.findViewById(R.id.thumbnail);
            TextView name = view.findViewById(R.id.name);
            Observable
                    .create(e -> {
                        Integer id = ProjectFactory.getLastProject();
                        e.onNext(id != null ? id : -1);
                        e.onComplete();
                    })
                    .flatMap(o -> Observable
                            .create(e -> {
                                e.onNext(o);
                                if ((int) o != -1) {
                                    Bitmap thumb = DrawHelper.renderThumbnail(getContext(), 1080, 1920, (Integer) o);
                                    if (thumb != null)
                                        e.onNext(thumb);
                                    e.onNext(ProjectFactory.openProject((Integer) o, false, false).getName());
                                }
                            }))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .doOnNext(o -> {
                        if (Objects.equals(o.getClass().getSimpleName(), "Integer")) {
                            if ((int) o != -1) {
                                lastProject.setOnClickListener(v -> {
                                    Intent intent = new Intent(getContext(), ProjectActivity.class);
                                    intent.putExtra(ProjectActivity.PROJECT_ID, (Integer) o);
                                    startActivity(intent);
                                });
                            } else {
                                name.setText(R.string.no_project);
                                lastProject.setOnClickListener(v -> {
                                    Toast.makeText(getContext(), R.string.no_last_project, Toast.LENGTH_SHORT).show();
                                });
                            }
                        } else if (Objects.equals(o.getClass().getSimpleName(), "String")) {
                            name.setText((String) o);
                        } else if (Objects.equals(o.getClass().getSimpleName(), "Bitmap")) {
                            thumbnail.setImageBitmap((Bitmap) o);
                        }
                    })
                    .subscribe();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            Cursor cursor = null;
            try {
                String[] proj = {MediaStore.Images.Media.DATA};
                cursor = getActivity().getContentResolver().query(data.getData(), proj, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                Intent intent = new Intent(getContext(), ProjectActivity.class);
                int id = ProjectFactory.createFromImage(getContext(), getString(R.string.project) + " " + ProjectFactory.getNextKey(), cursor.getString(column_index));
                intent.putExtra(ProjectActivity.PROJECT_ID, id);
                startActivity(intent);

            } catch (IOException e) {
                // Вывод ошибки
                e.printStackTrace();
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(getContext(), ProjectActivity.class);
            try {
                intent.putExtra(ProjectActivity.PROJECT_ID, ProjectFactory.createFromImage(getContext(), "Проект " + ProjectFactory.getNextKey(), currentPhotoPath));
                getContext().startActivity(intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
