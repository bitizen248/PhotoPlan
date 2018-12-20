package ru.ada.adaphotoplan.obj;

import android.graphics.Point;
import android.support.annotation.NonNull;

import io.realm.RealmObject;

/**
 * Created by Bitizen on 27.04.17.
 */

public class Line extends RealmObject {

    @NonNull
    public RealmPoint start;
    @NonNull
    public RealmPoint end;

    private String textOnLine;

    public Line() {}

    public Line(Line line) {
        this.start = line.start;
        this.end = line.end;
        this.textOnLine = line.textOnLine;
    }

    public Line(@NonNull RealmPoint start, @NonNull RealmPoint end) {
        this.start = start;
        this.end = end;
    }

    public String getTextOnLine() {
        return textOnLine;
    }

    public void setTextOnLine(String textOnLine) {
        this.textOnLine = textOnLine;
    }
}
