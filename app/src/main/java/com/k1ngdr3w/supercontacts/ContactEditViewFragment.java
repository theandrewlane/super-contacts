package com.k1ngdr3w.supercontacts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class ContactEditViewFragment extends Fragment {

    private ContactEditFragment_Listener contactEditFragment_listener;
    private long rowID;
    private MainActivity ma;
    public String state, firstName, lastName, tempName;
    private EditText address, birthday, phone, name;
    private TextView geoCords;
    Bundle arguments;
    private int i_address, i_bday, i_phone, i_firstName, i_lastName, i_geoLoc;
    Activity act;

    MenuItem deleteMenuButton, exportMenuButton, saveMenuButton;


    public ContactEditViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        deleteMenuButton = menu.findItem(R.id.action_delete);
        exportMenuButton = menu.findItem(R.id.action_importCourses);
        saveMenuButton = menu.findItem(R.id.action_save);
        deleteMenuButton.setVisible(true);
        saveMenuButton.setVisible(true);
        exportMenuButton.setVisible(false);
    }


    //Handle the delete
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //EDIT SELECTION
            case R.id.action_delete:
                deleteContact(rowID);
                return true;
            case R.id.action_importCourses:
                return true;
            case R.id.action_save:
                if (validateInput())
                    updateAddContact();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public interface ContactEditFragment_Listener {
        void onCompleteSave(long rowID);

        void onDeleteContact();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        act = activity;
        contactEditFragment_listener = (ContactEditFragment_Listener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_icons, menu);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        contactEditFragment_listener = null;
        deleteMenuButton.setVisible(false);
        exportMenuButton.setVisible(false);
        saveMenuButton.setVisible(false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setRetainInstance(true);
        ma = (MainActivity) getActivity();
        View rv = inflater.inflate(R.layout.fragment_contact_edit, container, false);
        Bundle savedData = getArguments();
        name = (EditText) rv.findViewById(R.id.name);
        address = (EditText) rv.findViewById(R.id.addressField);
        birthday = (EditText) rv.findViewById(R.id.birthdayField);
        phone = (EditText) rv.findViewById(R.id.phoneField);
        geoCords = (TextView) rv.findViewById(R.id.geoCordsField);

        if (savedData != null) {
            state = "edit";
            geoCords.setEnabled(true);
            geoCords.setClickable(true);
            geoCords.setFocusable(true);
            rowID = savedData.getLong(DatabaseHelper.KEY_ROWID);
            new GetContactFromDB().execute(rowID);

        } else {
            state = "create";
            geoCords.setEnabled(false);
            geoCords.setClickable(false);
            geoCords.setFocusable(false);
        }
        Log.e("CEVF ------", "the current state is " + state);
        return rv;
    }

    public boolean validateInput() {
        if (name.getText().toString().length() < 1) {
            Toast.makeText(act, "You must enter a contact name.",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        if (phone.getText().toString().length() < 1) {
            Toast.makeText(act, "You must enter a contact phone number.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public void updateAdd() {
        DatabaseHelper dbHelper = new DatabaseHelper(act);
        if (state == "create") {
            parseName(name.getText().toString());
            rowID = dbHelper.insertContact(lastName, firstName, address.getText().toString(), birthday.getText().toString(), phone.getText().toString(), geoCords.getText().toString());
        }

        if (state == "edit") {
            rowID = getArguments().getLong(DatabaseHelper.KEY_ROWID);
            parseName(name.getText().toString());
            Log.e("CEVF ------", "About to save " + " --- " + firstName + "  ---- " + lastName + " -----" + phone.getText());
            dbHelper.updateContact(rowID, lastName, firstName, address.getText().toString(), birthday.getText().toString(), phone.getText().toString(), geoCords.getText().toString());
        }
    }

    public void parseName(String name) {
        Log.e("CEVF ------", "APArsing e " + " --- " + name);
        String[] names = name.split(" ");
        firstName = names[0];
        if (names.length > 1)
            lastName = names[names.length - 1];
    }

    private class GetContactFromDB extends AsyncTask<Long, Object, Cursor> {
        final DatabaseHelper dbHelper = new DatabaseHelper(act);

        @Override
        protected Cursor doInBackground(Long... params) {
            dbHelper.open();
            Cursor cursor = dbHelper.getOneContact(params[0]);
            return cursor;
        }

        // use cursor returned from above
        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            if (cursor != null && cursor.moveToFirst()) {
                i_firstName = cursor.getColumnIndex(DatabaseHelper.KEY_FIRSTNAME);
                i_lastName = cursor.getColumnIndex(DatabaseHelper.KEY_LASTNAME);
                i_address = cursor.getColumnIndex(DatabaseHelper.KEY_ADDRESS);
                i_bday = cursor.getColumnIndex(DatabaseHelper.KEY_BIRTHDAY);
                i_geoLoc = cursor.getColumnIndex(DatabaseHelper.KEY_GEOLOCATION);
                i_phone = cursor.getColumnIndex(DatabaseHelper.KEY_PHONENUMBER);
                String tempName = cursor.getString(i_firstName) + " " + cursor.getString(i_lastName);

                firstName = cursor.getString(i_firstName);
                lastName = cursor.getString(i_lastName);
                name.setText(tempName);
                address.setText(cursor.getString(i_address));
                birthday.setText(cursor.getString(i_bday));
                phone.setText(cursor.getString(i_phone));
                geoCords.setText(cursor.getString(i_geoLoc));

                cursor.close();
                dbHelper.close();
            }
        }
    }

    public boolean updateAddContact() {
        AsyncTask<Object, Object, Object> addContactToDBTask =
                new AsyncTask<Object, Object, Object>() {
                    @Override
                    protected Object doInBackground(Object... params) {
                        updateAdd();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object result) {
                        //Written to the DB now notfivy main
                        contactEditFragment_listener.onCompleteSave(rowID);
                    }
                };
        addContactToDBTask.execute((Object[]) null);
        return true;
    }

    public void deleteContact(long row) {
        rowID = row;
        DeleteContactDialog.show(getFragmentManager(), "DELETE");
    }

    private final DialogFragment DeleteContactDialog = new DialogFragment() {
        @Override
        public Dialog onCreateDialog(Bundle bundle) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("This will permanently delete this contact");
            builder.setTitle("Are you sure?");
            builder.setPositiveButton("DELETE",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(
                                DialogInterface dialog, int button) {
                            final DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
                            AsyncTask<Long, Object, Object> deleteCourse =
                                    new AsyncTask<Long, Object, Object>() {
                                        @Override
                                        protected Object doInBackground(Long... params) {
                                            dbHelper.open();
                                            return dbHelper.deleteRow(params[0]);
                                        }

                                        @Override
                                        protected void onPostExecute(Object boo) {
                                            super.onPostExecute(boo);
                                            dbHelper.close();
                                            contactEditFragment_listener.onDeleteContact();
                                        }
                                    };
                            deleteCourse.execute(rowID);
                        }
                    }
            );
            builder.setNegativeButton("CANCEL", null);
            return builder.create(); // return the AlertDialog
        }
    };

}
