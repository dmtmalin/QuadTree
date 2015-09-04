package utils;

import java.util.ArrayList;

/**
 * Created by dmt on 31.08.2015.
 */
public class CommonUtil {
    public static String joinArray(ArrayList arr) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : arr) {
            sb.append(obj);
        }
        return sb.toString();
    }
}
