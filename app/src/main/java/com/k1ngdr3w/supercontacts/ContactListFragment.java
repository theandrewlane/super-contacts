package com.k1ngdr3w.supercontacts;


import android.app.Activity;
import android.app.ListFragment;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactListFragment extends ListFragment {


    public ContactListFragment() {
        // Required empty public constructor
    }

    private CursorAdapter cursorAdapter;
    private ContactList_listener contactList_listener;

    private ListView contactListView;
    private MainActivity ma;
    Activity act;
    private View rv;
    long rowID;

    // callback methods implemented by MainActivity
    public interface ContactList_listener {
        void onAdd();
        void onCompleteSave(long rowID);
        void onSelect(long rowID);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        act = activity;
        contactList_listener = (ContactList_listener) activity;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        contactList_listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);

        rv = inflater.inflate(R.layout.fragment_contact_list, container, false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true); // save fragment across config changes
        ma = (MainActivity) getActivity();

        contactListView = getListView();
        contactListView.setOnItemClickListener(viewContactListener);
        contactListView.setSelector(R.drawable.list_item_selector);

        int[] resultView = new int[]{R.id.firstName, R.id.lastName};
        String[] firstName = new String[]{DatabaseHelper.KEY_FIRSTNAME, DatabaseHelper.KEY_LASTNAME};


        cursorAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.fragment_contact_list, null, firstName, resultView, 0);
        setListAdapter(cursorAdapter);
        setEmptyText("You don't have any contacts yet!");

    }

    private final AdapterView.OnItemClickListener viewContactListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
            view.setSelected(true);
            contactList_listener.onSelect(id); // pass selection to MainActivity
        }

    };

    @Override
    public void onResume() {
        super.onResume();
        new getContactsFromDB().execute("");
    }


    public void updateAdd(String lastName, String firstName, String address, String bday, String phoneNumber, String geoLoc) {
        DatabaseHelper dbHelper = new DatabaseHelper(act);
        rowID = dbHelper.insertContact(lastName, firstName, address, bday, phoneNumber, geoLoc);
    }

    public boolean addContactToDB(final String lastName, final String firstName, final String address, final String bday, final String phoneNumber, final String geoLoc) {
        AsyncTask<Object, Object, Object> addContactToDBTask =
                new AsyncTask<Object, Object, Object>() {
                    @Override
                    protected Object doInBackground(Object... params) {
                        updateAdd(lastName, firstName, address, bday, phoneNumber, geoLoc);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object result) {
                        //Written to the DB now notfivy main
                        contactList_listener.onCompleteSave(rowID);
                    }
                };
        addContactToDBTask.execute((Object[]) null);
        return true;
    }

    private class getContactsFromDB extends AsyncTask<String, Integer, Cursor> {
        final DatabaseHelper dbHelper = new DatabaseHelper(act);

        @Override
        protected Cursor doInBackground(String... params) {
            dbHelper.open();
            return dbHelper.getAllContacts();
        }

        //TODO implement change cursor
        @Override
        protected void onPostExecute(Cursor cursor) {
            cursorAdapter.changeCursor(cursor);
            dbHelper.close();
        }
    }

    //Kill the cursor
    public void onStop() {
        Cursor cursor = cursorAdapter.getCursor();
        cursorAdapter.swapCursor(null);
        if (cursor != null) {
            cursor.close();
        }
        super.onStop();
    }


    public void reloadContacts() {
        new getContactsFromDB().execute("");
    }


}

