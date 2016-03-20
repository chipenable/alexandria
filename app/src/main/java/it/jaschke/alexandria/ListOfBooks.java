package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import it.jaschke.alexandria.api.BookListAdapter;
import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.data.AlexandriaContract;


public class ListOfBooks extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String TAG = "ListOfBooks";

    private BookListAdapter bookListAdapter;
    private ListView bookList;
    private int position = ListView.INVALID_POSITION;
    private EditText searchText;

    private final int LOADER_SEARCH_BOOK = 10;
    private final int LOADER_GET_BOOKS = 11;

    public ListOfBooks() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_GET_BOOKS, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //It's bad practice to perform database queries on the UI thread.
        /*Cursor cursor = getActivity().getContentResolver().query(
                AlexandriaContract.BookEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );*/


        bookListAdapter = new BookListAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_list_of_books, container, false);
        searchText = (EditText) rootView.findViewById(R.id.searchText);
        rootView.findViewById(R.id.searchButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListOfBooks.this.restartLoader();
                    }
                }
        );

        bookList = (ListView) rootView.findViewById(R.id.listOfBooks);
        bookList.setAdapter(bookListAdapter);
        bookList.setEmptyView(rootView.findViewById(R.id.empty_view));

        bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = bookListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    ((Callback)getActivity())
                            .onItemSelected(cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID)));
                }
            }
        });

        return rootView;
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_SEARCH_BOOK, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");
        CursorLoader cursorLoader = null;

        switch(id){
            case LOADER_GET_BOOKS:
                Log.d(TAG, "LOADER_GET_BOOKS");
                cursorLoader = new CursorLoader(getActivity(),
                        AlexandriaContract.BookEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);
                break;

            case LOADER_SEARCH_BOOK:
                Log.d(TAG, "LOADER_SEARCH_BOOK");
                final String selection = AlexandriaContract.BookEntry.TITLE +" LIKE ? OR "
                        + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";
                String searchString = searchText.getText().toString();

                if(searchString.length()>0){
                    searchString = "%"+searchString+"%";
                    cursorLoader = new CursorLoader(
                            getActivity(),
                            AlexandriaContract.BookEntry.CONTENT_URI,
                            null,
                            selection,
                            new String[]{searchString,searchString},
                            null
                    );
                }
                else{
                    cursorLoader = new CursorLoader(
                            getActivity(),
                            AlexandriaContract.BookEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null
                    );
                }
                break;

        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished");
        int loaderId = loader.getId();

        switch(loaderId){
            case LOADER_GET_BOOKS:
                bookListAdapter.swapCursor(data);
                break;

            case LOADER_SEARCH_BOOK:
                bookListAdapter.swapCursor(data);
                if (position != ListView.INVALID_POSITION) {
                    bookList.smoothScrollToPosition(position);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");
        bookListAdapter.swapCursor(null);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getActivity().setTitle(R.string.books);
    }
}
