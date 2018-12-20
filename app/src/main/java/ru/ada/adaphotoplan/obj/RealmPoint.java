package ru.ada.adaphotoplan.obj;

import android.graphics.Point;

import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

/**
 * Created by Bitizen on 14.06.17.
 */

public class RealmPoint extends RealmObject {

    public int x;
    public int y;

    public RealmPoint() {}

    public RealmPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RealmPoint) {
            return this.x == ((RealmPoint) obj).x
                    && this.y == ((RealmPoint) obj).y;
        }
        return super.equals(obj);
    }
}
