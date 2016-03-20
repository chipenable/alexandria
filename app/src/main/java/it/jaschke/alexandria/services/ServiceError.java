package it.jaschke.alexandria.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.IntDef;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import it.jaschke.alexandria.R;

/**
 * Created by Pashgan
 */
public class ServiceError {

    private static final String TAG = "ServiceError";
    private static final String PREF_ERROR = "pref_error";
    private static final String PREF_ERROR_TYPE = "error_type";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATUS_OK, STATUS_BAD_CONNECTION, STATUS_SERVER_DOWN, STATUS_UNKNOWN})
    public @interface ErrorStatus {}

    public static final int STATUS_OK = 0;
    public static final int STATUS_BAD_CONNECTION = 1;
    public static final int STATUS_SERVER_DOWN = 2;
    public static final int STATUS_UNKNOWN = 3;

    public static boolean isServiceError(String key) {
        if (PREF_ERROR_TYPE.equals(key)) {
            return true;
        }
        return false;
    }

    public static void setError(Context context, @ErrorStatus int error){
        Log.d(TAG, "error: " + Integer.toString(error));
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_ERROR, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putInt(PREF_ERROR_TYPE, error)
                .apply();
    }

    @ErrorStatus
    public static int getError(Context context){
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_ERROR, Context.MODE_PRIVATE);
            @ErrorStatus int status = sharedPreferences.getInt(PREF_ERROR_TYPE, 0);
            return status;
        }
        return ServiceError.STATUS_OK;
    }

    public static int getErrorStringId(Context context){
        int stringId;
        @ErrorStatus int status = getError(context);

        switch(status){
            case STATUS_OK:
                stringId = 0;
                break;

            case STATUS_BAD_CONNECTION:
                stringId = R.string.error_status_bad_connection;
                break;

            case STATUS_SERVER_DOWN:
                stringId = R.string.error_status_server_down;
                break;

            case STATUS_UNKNOWN:
            default:
                stringId = R.string.error_status_unknown;
        }

        return stringId;
    }

    public static void setServiceErrorListener(Context context,
            SharedPreferences.OnSharedPreferenceChangeListener listener){
        if (listener != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_ERROR, Context.MODE_PRIVATE);
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
        }
    }

}
