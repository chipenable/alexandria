package it.jaschke.alexandria;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;
import it.jaschke.alexandria.services.ServiceError;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener, ConnectionStateReceiver.ConnectionListener{

    private static final String TAG = "AddBook";
    private static final String EAN_CONTENT = "eanContent";

    private static final int RC_BARCODE_CAPTURE = 1;
    private static final int LOADER_ID = 1;

    private EditText mIsbn;
    private View mRootView;
    private TextView mServiceError;
    private ConnectionStateReceiver mReceiver;


    public AddBook() {
    }

    @Override
    public void onStart() {
        super.onStart();

        /*register receiver to control the network state*/
        mReceiver = new ConnectionStateReceiver(AddBook.this);
        getActivity().registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BARCODE_OBJECT);
                    mIsbn.setText(barcode.rawValue);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mIsbn != null) {
            outState.putString(EAN_CONTENT, mIsbn.getText().toString());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        mIsbn = (EditText) mRootView.findViewById(R.id.ean);
        mServiceError = (TextView) mRootView.findViewById(R.id.service_error);

        mIsbn.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                fetchBook(s.toString());
            }
        });

        Button scanBut = (Button) mRootView.findViewById(R.id.scan_button);
        scanBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AUTO_FOCUS, true);
                intent.putExtra(BarcodeCaptureActivity.USE_FLASH, false);
                startActivityForResult(intent, RC_BARCODE_CAPTURE);
            }
        });

        //Check if this device has a camera
        boolean hasCamera = (Camera.getNumberOfCameras() > 0);
        scanBut.setEnabled(hasCamera);

        mRootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsbn.setText("");
            }
        });

        mRootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, mIsbn.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                mIsbn.setText("");
            }
        });

        if (savedInstanceState != null) {
            mIsbn.setText(savedInstanceState.getString(EAN_CONTENT));
            //mIsbn.setHint("");
        }

        ServiceError.setServiceErrorListener(getActivity(), this);
        showError();

        return mRootView;
    }


    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mIsbn.getText().length() == 0) {
            return null;
        }
        String eanStr = mIsbn.getText().toString();
        if (eanStr.length() == 10 && !eanStr.startsWith("978")) {
            eanStr = "978" + eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) mRootView.findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) mRootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if (authors != null) {
            String[] authorsArr = authors.split(",");
            ((TextView) mRootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) mRootView.findViewById(R.id.authors)).setText(authors.replace(",", "\n"));
        }
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
            new DownloadImage((ImageView) mRootView.findViewById(R.id.bookCover)).execute(imgUrl);
            mRootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) mRootView.findViewById(R.id.categories)).setText(categories);

        mRootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields() {
        ((TextView) mRootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) mRootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) mRootView.findViewById(R.id.authors)).setText("");
        ((TextView) mRootView.findViewById(R.id.categories)).setText("");
        mRootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        mRootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        mRootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    //onAttach(Activity activity) is deprecated
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getActivity().setTitle(R.string.scan);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (ServiceError.isServiceError(key)){
            showError();
        }
    }

    private void showError(){
        int stringId = ServiceError.getErrorStringId(getContext());
        if (stringId > 0) {
            mServiceError.setText(stringId);
            mServiceError.setVisibility(View.VISIBLE);
        }
        else if (stringId == 0){
            mServiceError.setText("");
            mServiceError.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void updateData(boolean connectionState) {

        if (connectionState) {
            if (ServiceError.getError(getContext()) != ServiceError.STATUS_OK) {
                Log.d(TAG, "restart loader");
                fetchBook(mIsbn.getText().toString());
            }
        }

    }


    private void fetchBook(String ean) {
        if (ean == null){
            return;
        }

        //catch isbn10 numbers
        if (ean.length() == 10 && !ean.startsWith("978")) {
            ean = "978" + ean;
        }
        if (ean.length() < 13) {
            clearFields();
            return;
        }
        //Once we have an ISBN, start a book intent
        Intent bookIntent = new Intent(getActivity(), BookService.class);
        bookIntent.putExtra(BookService.EAN, ean);
        bookIntent.setAction(BookService.FETCH_BOOK);
        getActivity().startService(bookIntent);
        restartLoader();
    }

}
