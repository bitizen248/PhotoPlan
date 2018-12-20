package ru.ada.adaphotoplan.interfaces;

import android.graphics.Bitmap;

/**
 * Created by Bitizen on 29.06.17.
 */

public interface OnTouchPreview {

    void onTouch(Bitmap preview, int x, int y);

    void onRelease();
}
