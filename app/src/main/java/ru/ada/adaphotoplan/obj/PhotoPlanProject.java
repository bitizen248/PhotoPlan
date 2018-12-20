package ru.ada.adaphotoplan.obj;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import ru.ada.adaphotoplan.helpers.IRealmCascade;

/**
 * Created by Bitizen on 09.06.17.
 */

public class PhotoPlanProject extends RealmObject implements Serializable, IRealmCascade {
    private static final String TAG = "PhotoPlanProject";
    public static final int TYPE_GRID = 0;
    public static final int TYPE_BITMAP = 1;

    @PrimaryKey
    private int id;

    private long lastOpen;

    private String name;
    private RealmList<Line> lines = new RealmList<>();

    private int type;
    private int interval;
    private String pathToBackground;
    @Ignore
    @Nullable
    private transient Bitmap background;

    private int width;
    private int height;

    public PhotoPlanProject() {}

    public PhotoPlanProject(int id, String name, int width, int height) {
        this.id = id;
        this.name = name;
        this.interval = 50;
        this.type = TYPE_GRID;
        this.background = null;
        this.width = width;
        this.height = height;
        this.lastOpen = System.currentTimeMillis();
    }

    public PhotoPlanProject(int id, String name, String pathToBackground) {
        this.id = id;
        this.name = name;
        this.interval = 1;
        this.type = TYPE_BITMAP;
        this.pathToBackground = pathToBackground;
        this.lastOpen = System.currentTimeMillis();
    }


    public void addLine(Line line) {
        lines.add(line);
    }

    public void movePoint(RealmPoint point, RealmPoint newPoint) {
        for (Line line : lines) {
            if (line.start.equals(point))
                line.start = newPoint;
            if (line.end.equals(point))
                line.end = newPoint;
        }
    }

    public List<Line> deleteByPoint(RealmPoint point) {
        RealmList<Line> newLineSet = new RealmList<>();
        List<Line> deletedLines = new ArrayList<>();
        for (Line line : lines) {
            if (!line.start.equals(point) && !line.end.equals(point))
                newLineSet.add(line);
            else
                deletedLines.add(line);
        }

        lines = newLineSet;
        return deletedLines;
    }

    public void deleteLine(Line line) {
        lines.remove(line);
    }

    public String getPathToBackground() {
        return pathToBackground;
    }

    public void setPathToBackground(String pathToBackground) {
        this.pathToBackground = pathToBackground;
    }

    public Set<RealmPoint> getPoints() {
        Set<RealmPoint> points = new HashSet<>();
        for (Line line : lines) {
            points.add(line.start);
            points.add(line.end);
        }
        return points;
    }

    @Nullable
    public RealmPoint[] getCorners() {
        Set<RealmPoint> points = getPoints();
        int cornerLeftX = Integer.MAX_VALUE;
        int cornerLeftY = Integer.MAX_VALUE;
        int cornerRightX = 0;
        int cornerRightY = 0;
        if (!points.isEmpty()) {
            for (RealmPoint point : points) {
                if (cornerLeftX > point.x)
                    cornerLeftX = point.x;
                if (cornerLeftY > point.y)
                    cornerLeftY = point.y;
                if (cornerRightX < point.x)
                    cornerRightX = point.x;
                if (cornerRightY < point.y)
                    cornerRightY = point.y;
            }
            return new RealmPoint[]{new RealmPoint(cornerLeftX, cornerLeftY), new RealmPoint(cornerRightX, cornerRightY)};
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Bitmap getBitmap() {
        return background;
    }

    public void setBitmap(Bitmap bitmap) {
        this.background = bitmap;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = (RealmList<Line>) lines;
    }

    public int getInterval() {
        return this.interval;
    }

    public long getLastOpen() {
        return lastOpen;
    }

    public void setLastOpen(long lastOpen) {
        this.lastOpen = lastOpen;
    }

    public void setTextLine(Line line, String text) {
        if (text != null && text.length() > 9)
            text = text.substring(0, 9);
        Log.e(TAG, "setTextLine: " + lines.indexOf(line));
        int i = lines.indexOf(line);
        lines.get(i).setTextOnLine(text);
        Log.d(TAG, "setTextLine: " + lines.get(i).getTextOnLine());
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
