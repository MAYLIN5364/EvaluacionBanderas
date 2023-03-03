package Utiles;

import android.util.Log;

public class ErrorLog {
    private static String label = "ErrorLog";

    public ErrorLog() {
    }

    public ErrorLog(String label) {
        this.label = label;
    }

    public static String getLabel() {
        return label;
    }

    public static void setLabel(String label) {
        ErrorLog.label = label;
    }

    public static void error(String text) {
        Log.e(label, text);
    }

    public static void warning(String text) {
        Log.w(label, text);
    }

    public static void info(String text) {
        Log.i(label, text);
    }

    public static void depuration(String text) {
        Log.d(label, text);
    }

    public static void detailedLog(String text) {
        Log.v(label, text);
    }
}
