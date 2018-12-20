package ru.ada.adaphotoplan.obj;

/**
 * Created by Bitizen on 29.06.17.
 */

public class OpenPageEvent {

    private int page;

    public OpenPageEvent(int page) {
        this.page = page;
    }

    public int getPage() {
        return page;
    }
}
