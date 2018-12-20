package ru.ada.adaphotoplan.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.Sort;
import ru.ada.adaphotoplan.R;
import ru.ada.adaphotoplan.obj.PhotoPlanProject;

/**
 * Created by Bitizen on 12.06.17.
 */

public final class ProjectFactory {
    private static final String TAG = "ProjectFactory";

    public static int createGrid(String name, int width, int height) {
        Realm realm = Realm.getDefaultInstance();
        PhotoPlanProject project = new PhotoPlanProject(getNextKey(), name, width, height);
        realm.executeTransaction(realm1 -> realm1.copyToRealmOrUpdate(project));
        realm.close();
        return project.getId();
    }

    public static int createFromImage(Context context, String name, String path) throws IOException {
        // TODO Чек места
        Realm realm = Realm.getDefaultInstance();
        int id = getNextKey();
        File source = new File(path);
        File destImage = new File(context.getFilesDir().getAbsolutePath() + "/background/" + id + "_background.jpg");
        destImage.getParentFile().mkdirs();
        destImage.createNewFile();
        FileChannel src = new FileInputStream(source).getChannel();
        FileChannel dst = new FileOutputStream(destImage).getChannel();
        dst.transferFrom(src, 0, src.size());
        src.close();
        dst.close();
        PhotoPlanProject project = new PhotoPlanProject(id, name, destImage.getAbsolutePath());
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        Bitmap bitmap = BitmapFactory.decodeFile(project.getPathToBackground());
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        boolean isWidthPref = true;

        float k = (float) width / (float) screenWidth;

        if (height / k < screenHeight) {
            k = (float) height / (float) screenHeight;
            isWidthPref = false;
        }

        width /= k;
        height /= k;


        if (width > 4096 || height > 4096) {
            width *= k;
            height *= k;
            if (isWidthPref) {
                k = (float) width / (float) 4096;
            } else {
                k = (float) height / (float) 4096;
            }
            width /= k;
            height /= k;
        }

        project.setWidth(width);
        project.setHeight(height);

        realm.executeTransaction(realm1 -> realm1.copyToRealm(project));
        realm.close();
        return project.getId();
    }

    @Nullable
    public static PhotoPlanProject openProject(int id, boolean openBitmap, boolean registerOpen){
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<PhotoPlanProject> q = realm
                .where(PhotoPlanProject.class)
                .equalTo("id", id);
        PhotoPlanProject rProject = q.findFirst();
        if (registerOpen)
            realm.executeTransaction(realm1 -> {
                rProject.setLastOpen(System.currentTimeMillis());
                realm1.copyToRealmOrUpdate(rProject);
            });
        PhotoPlanProject project = realm.copyFromRealm(rProject);
        if (project.getPathToBackground() != null && openBitmap) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(project.getPathToBackground());
                project.setBitmap(Bitmap.createScaledBitmap(bitmap, project.getWidth(), project.getHeight(), false));
            } catch (Exception|OutOfMemoryError e) {
                return null;
            }
        }
        realm.close();
        return project;
    }

    public static void saveProject(PhotoPlanProject project) {
        Realm.getDefaultInstance().executeTransaction(realm -> {
            realm.copyToRealmOrUpdate(project);
        });
    }


    public static Integer getLastProject() {
        Realm realm = Realm.getDefaultInstance();
        Integer id = null;
        try {
            id = realm
                    .where(PhotoPlanProject.class)
                    .findAllSorted("lastOpen", Sort.DESCENDING)
                    .first()
                    .getId();
        } catch (Exception e){
            e.printStackTrace();
        }
        return id;
    }

    public static Observable<PhotoPlanProject> getProjects() {
        return Observable
            .create(e -> {
                Realm realm = Realm.getDefaultInstance();

                List<PhotoPlanProject> projects =
                        realm.copyFromRealm(
                            realm
                                    .where(PhotoPlanProject.class)
                                    .findAll());
                for (PhotoPlanProject p : projects)
                    e.onNext(p);
                realm.close();
                e.onComplete();
            }
        );
    }


    public static void copyProject(Context context, int id) throws IOException {
        PhotoPlanProject project = openProject(id, false, false);
        Integer newCopy = null;
        if (project.getType() == PhotoPlanProject.TYPE_BITMAP) {
            newCopy = createFromImage(context, context.getString(R.string.copy) + " " + project.getName(), project.getPathToBackground());
        } else if (project.getType() == PhotoPlanProject.TYPE_GRID){
            newCopy = createGrid(context.getString(R.string.copy) + " " + project.getName(), project.getWidth(), project.getHeight());
        }
        if (newCopy != null) {
            PhotoPlanProject newProject = openProject(newCopy, false, false);
            newProject.setLines(project.getLines());
            saveProject(newProject);
        }
    }

    public static void deleteProject(int id) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        PhotoPlanProject result
                = realm
                    .where(PhotoPlanProject.class)
                    .equalTo("id", id)
                    .findFirst();
        RealmUtils.deleteCascade(result);
        realm.commitTransaction();
    }

    public static int getNextKey() {
        Realm realm = Realm.getDefaultInstance();
        int index;
        try {
            index = realm.where(PhotoPlanProject.class).max("id").intValue() + 1;
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) { 
            index = 0;
        }
        realm.close();
        return index;
    }
}
