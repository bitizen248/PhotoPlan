package ru.ada.adaphotoplan.helpers;

import android.graphics.Point;

import ru.ada.adaphotoplan.obj.RealmPoint;

/**
 * Created by Bitizen on 09.06.17.
 */

public class MathHelper {

    public static double distance(RealmPoint start, RealmPoint end) {
        return Math.sqrt(Math.pow(end.x - start.x, 2) + Math.pow(end.y - start.y, 2));
    }
}
