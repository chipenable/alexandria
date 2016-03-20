package it.jaschke.alexandria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/*Receiver is used to observe network state
* It allows to load data when Internet becomes available.*/
public class ConnectionStateReceiver extends BroadcastReceiver{

    private ConnectionListener mListener;

    /*Callback interface that is used to update fragment's data*/
    public interface ConnectionListener{
        void updateData(boolean connectionState);
    }

    public ConnectionStateReceiver(ConnectionListener listener){
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mListener != null){
            mListener.updateData(checkConnection(context));
        }
    }

    /*The function returns true if Internet is available*/
    public static boolean checkConnection(Context context){
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }

}
