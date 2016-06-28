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

        // called when user selects a course
        void onSelect(long rowID);

        void onLongSelect(long rowID);
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

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        menu.clear();
//        inflater.inflate(R.menu.menu_icons, menu);
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rv = inflater.inflate(R.layout.fragment_contact_list, container, false);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

//
//    public void deleteAll() {
//        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
//        dbHelper.open();
//        dbHelper.deleteAll();
//        dbHelper.close();
//        reloadContacts();
//    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true); // save fragment across config changes
        ma = (MainActivity) getActivity();
//        ma.hideAddButton(false);
//        ma.hideSaveButton(true);

        setEmptyText("Click the add button to insert a course!");
        contactListView = getListView();
        contactListView.setOnItemClickListener(viewContactListener);
        contactListView.setSelector(R.drawable.list_item_selector);

        int[] resultView = new int[]{R.id.firstName, R.id.lastName};
        String[] firstName = new String[]{DatabaseHelper.KEY_FIRSTNAME, DatabaseHelper.KEY_LASTNAME};


        cursorAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.fragment_contact_list, null, firstName, resultView, 0);
        setListAdapter(cursorAdapter);
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
            Cursor cursor = dbHelper.getAllContacts();
            return cursor;
        }

        //TODO implement change cursor
        @Override
        protected void onPostExecute(Cursor cursor) {
            cursorAdapter.changeCursor(cursor);
            dbHelper.close();
        }
    }

//    public void getCourses() {
//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//
//            getFragmentManager().popBackStack();
//        }
//        getFragmentManager().popBackStack();
//
//        new getCanvasCourses().execute("");
//    }

    //Kill the cursor
    public void onStop() {
        Cursor cursor = cursorAdapter.getCursor();
        cursorAdapter.swapCursor(null);
        if (cursor != null) {
            cursor.close();
        }
        super.onStop();
    }


//    public class getCanvasCourses extends AsyncTask<String, Integer, String> {
//        final DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
//        String rawJSON = "";
//        int stat;
//
//        //Send result to onPost execute
//        @Override
//        protected String doInBackground(String... params) {
//            try {
//                URL url = new URL("https://weber.instructure.com/api/v1/courses");
//                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
//                conn.setRequestMethod("GET");
//                conn.setRequestProperty("Authorization", "Bearer " + MainActivity.AUTH_TOKEN);
//                conn.connect();
//                Log.w("CLF - ", url.toString());
//                int status = conn.getResponseCode();
//                if (status < 199 || status > 202) {
//                    stat = status;
//                }
//                switch (status) {
//                    case 200:
//                    case 201:
//                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                        rawJSON = br.readLine();
//                        Log.w("RAQ _--------------", rawJSON);
//                }
//            } catch (MalformedURLException e) {
//                Log.w("MUE _--------------", e.getMessage());
//            } catch (IOException e) {
//                Log.w("IOE _--------------", e.getMessage());
//            }
//
//            return rawJSON;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//            dbHelper.open();
//            try {
//                CanvasObjects.Course[] courses = jsonParse(result);
//                for (CanvasObjects.Course course : courses) {
//                    dbHelper.insertCourse(course.id, course.name, course.course_code, course.start_at, course.end_at);
//                }
//            } catch (Exception e) {
//
//            }
//            reloadContacts();
//            ma.toastGTFO(stat, 1);
//            dbHelper.close();
//        }
//    }
//
//
//    private CanvasObjects.Course[] jsonParse(String rawJson) {
//        GsonBuilder gsonb = new GsonBuilder();
//        Gson gson = gsonb.create();
//
//        CanvasObjects.Course[] courses = null;
//
//        try {
//            courses = gson.fromJson(rawJson, CanvasObjects.Course[].class);
//        } catch (Exception e) {
//
//        }
//        return courses;
//    }


    public void reloadContacts() {
        new getContactsFromDB().execute("");
    }


}

