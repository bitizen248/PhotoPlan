package ru.ada.adaphotoplan.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import io.reactivex.Observable;
import ru.ada.adaphotoplan.obj.PhotoPlanProject;

/**
 * Created by Bitizen on 16.06.17.
 */

public class ShareHelper {

    public static Observable<Boolean> renderAndOutputImage(Context context, PhotoPlanProject project) {
        return Observable
                .create(e -> {
                    Bitmap bitmap = DrawHelper.renderPlan(context, project.getId());
                    if (bitmap != null) {
                        Calendar calendar = Calendar.getInstance();
                        File output = new File(Environment.getExternalStorageDirectory() + "/ADA Photo Plan/",
                                project.getName().replaceAll(" ", "_") + "_" +
                                        calendar.get(Calendar.DAY_OF_MONTH) +
                                        calendar.get(Calendar.MONTH) +
                                        calendar.get(Calendar.YEAR) + "_" +
                                        calendar.get(Calendar.HOUR_OF_DAY) +
                                        calendar.get(Calendar.MINUTE) +
                                        calendar.get(Calendar.SECOND) + ".png");
                        output.getParentFile().mkdirs();
                        output.createNewFile();
                        FileOutputStream fos = new FileOutputStream(output);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                        fos.close();
                        e.onNext(true);
                    } else {
                        e.onNext(false);
                    }
                });
    }
}
