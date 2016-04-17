package com.christiankula.messagedisplay.utils;

import org.apache.commons.lang3.StringUtils;

public class ColorUtils {
    private ColorUtils() {
    }

    private static final double FACTOR = 0.7;

    public static String getBrighterHex(String hexcolor) {
        if (!StringUtils.isEmpty(hexcolor) && hexcolor.charAt(0) == '#') {
            hexcolor = hexcolor.substring(1);
        }

        int r = Integer.parseInt(hexcolor.substring(0, 2), 16);
        int g = Integer.parseInt(hexcolor.substring(2, 4), 16);
        int b = Integer.parseInt(hexcolor.substring(4, 6), 16);

        int i = (int) (1.0 / (1.0 - FACTOR));
        if (r == 0 && g == 0 && b == 0) {
            String s_i = Integer.toHexString(i);
            if (s_i.length() == 1) {
                s_i = s_i + s_i;
            }

            return "#" + s_i + s_i + s_i;
        }
        if (r > 0 && r < i) r = i;
        if (g > 0 && g < i) g = i;
        if (b > 0 && b < i) b = i;

        String s_r = "" + Integer.toHexString(Math.min((int) (r / FACTOR), 255));
        String s_g = "" + Integer.toHexString(Math.min((int) (g / FACTOR), 255));
        String s_b = "" + Integer.toHexString(Math.min((int) (b / FACTOR), 255));

        if (s_r.length() == 1) {
            s_r = "0" + s_r;
        }
        if (s_g.length() == 1) {
            s_g = "0" + s_g;
        }

        if (s_b.length() == 1) {
            s_b = "0" + s_b;
        }

        return "#" + s_r + s_g + s_b;
    }

    public static String getDarkerHex(String hexcolor) {
        if (!StringUtils.isEmpty(hexcolor) && hexcolor.charAt(0) == '#') {
            hexcolor = hexcolor.substring(1);
        }

        int r = Integer.parseInt(hexcolor.substring(0, 2), 16);
        int g = Integer.parseInt(hexcolor.substring(2, 4), 16);
        int b = Integer.parseInt(hexcolor.substring(4, 6), 16);


        String s_r = "" + Integer.toHexString(Math.max((int) (r * FACTOR), 0));
        String s_g = "" + Integer.toHexString(Math.max((int) (g * FACTOR), 0));
        String s_b = "" + Integer.toHexString(Math.max((int) (b * FACTOR), 0));

        if (s_r.length() == 1) {
            s_r = "0" + s_r;
        }
        if (s_g.length() == 1) {
            s_g = "0" + s_g;
        }
        if (s_b.length() == 1) {
            s_b = "0" + s_b;
        }

        return "#" + s_r + s_g + s_b;
    }

    public static boolean hasEnoughContrastComparedToWhite(String hexcolor) {
        if (!StringUtils.isEmpty(hexcolor) && hexcolor.charAt(0) == '#') {
            hexcolor = hexcolor.substring(1);
        }

        int r = Integer.parseInt(hexcolor.substring(0, 2), 16);
        int g = Integer.parseInt(hexcolor.substring(2, 4), 16);
        int b = Integer.parseInt(hexcolor.substring(4, 6), 16);

        double h = 1 - (0.299 * r + 0.587 * g + 0.114 * b) / 255;

        return h > 0.5;
    }
}
