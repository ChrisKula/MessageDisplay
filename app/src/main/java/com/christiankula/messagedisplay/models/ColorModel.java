package com.christiankula.messagedisplay.models;

/**
 * Created by Chris on 17/04/2016.
 */
public class ColorModel {
    private String name;
    private String hexColor;

    public ColorModel(String hexColor, String name) {
        this.hexColor = hexColor;
        this.name = name;
    }

    public String getHexColor() {
        return hexColor;
    }

    public String getName() {
        return name;
    }
}
