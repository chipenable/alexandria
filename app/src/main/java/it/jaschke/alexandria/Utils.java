package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {

    private static final String TAG = "Utils";

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager mgr = (InputMethodManager)activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void showToast(Context context, int stringId){
        if (stringId != 0) {
            Toast.makeText(context, stringId, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showToast(Context context, String str){
        if (str != null) {
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        }
    }

    //calculates image sizes
    public static Point getScreenSize(Activity activity){

        /*get full size of the display*/
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(screenSize);
        } else {
            display.getSize(screenSize);
        }

        /*get height of StatusBar*/
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = (resourceId > 0)? activity.getResources().getDimensionPixelSize(resourceId) : 0;
        Log.d(TAG, "height of status bar: " + Integer.toString(statusBarHeight));

        //get a height of ActionBar
        TypedArray styledAttributes = activity.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        int actionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        Log.d(TAG, "height of action bar: " + Integer.toString(actionBarHeight));

        //calculate available screen size
        screenSize.y -= (statusBarHeight + actionBarHeight);
        Log.d(TAG, "display size: " + Integer.toString(screenSize.x) + "x" + Integer.toString(screenSize.y));
        return screenSize;
    }

    //The function returns true if the Internet is available
    public static boolean checkConnection(Context context){
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }

}
