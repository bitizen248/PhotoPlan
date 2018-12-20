package ru.ada.adaphotoplan.obj;

/**
 * Created by Bitizen on 01.07.17.
 */

public class MeterEvent {

    private String result;

    public MeterEvent(String result) {
        this.result = result.trim();
    }

    public String getResult() {
        return result;
    }
}
