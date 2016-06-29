package com.k1ngdr3w.supercontacts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;

import org.json.JSONArray;
import org.json.JSONObject;


public class ContactEditViewFragment extends Fragment {

    private ContactEditFragment_Listener contactEditFragment_listener;
    private long rowID;
    private MainActivity ma;
    public String state, firstName, lastName, tempName;
    private EditText address, birthday, phone, name;
    private TextView geoCords;
    private boolean isSharable = false;
    Bundle arguments;
    private int i_address, i_bday, i_phone, i_firstName, i_lastName, i_geoLoc;
    Activity act;

    MenuItem deleteMenuButton, shareMenuButton, saveMenuButton;


    public ContactEditViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        deleteMenuButton = menu.findItem(R.id.action_delete);
        shareMenuButton = menu.findItem(R.id.action_share);
        saveMenuButton = menu.findItem(R.id.action_save);
        deleteMenuButton.setVisible(true);
        saveMenuButton.setVisible(true);
        shareMenuButton.setVisible(true);
    }


    //Handle the delete
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //EDIT SELECTION
            case R.id.action_delete:
                deleteContact(rowID);
                return true;
            case R.id.action_share:
                spawnShareDialog(generateBarcode(generateSharableString()));
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
        shareMenuButton.setVisible(false);
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
        return rv;
    }

    private void spawnShareDialog(Bitmap bitmap) {
        if (isSharable) {
            Dialog builder = new Dialog(getActivity());
            builder.setTitle("Scan this barcode to share contact!");
            builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
            builder.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    //nada de particular senior!;
                }
            });

            ImageView imageView = new ImageView(getActivity());
            imageView.setImageBitmap(bitmap);
            builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            builder.show();
        }
    }


    public boolean validateInput() {
        isSharable = false;
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
        isSharable = true;
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

    //Generate A Shareable String
    public String generateSharableString() {
        JSONObject item = new JSONObject();
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();

        if (validateInput()) {
            updateAddContact();
            new GetContactFromDB().execute(rowID);
            String dataString = "LastName = " + lastName + ", FirstName = " + firstName + ", FullAddress = " + address.getText().toString() + ", DOB = " + birthday.getText().toString() + ", Phone = " + phone.getText().toString() + ", GeoCords = " + geoCords.getText().toString().trim();
            return dataString;
           /* updateAddContact();
            new GetContactFromDB().execute(rowID);
            parseName(name.getText().toString());
            try {
                item.put("first_name", firstName);
                item.put("last_name", lastName);
                item.put("address", address.getText().toString());
                item.put("dob", birthday.getText().toString());
                item.put("phone", phone.getText().toString());
                item.put("geoCords", geoCords.getText().toString());
                array.put(item);
                json.put("contact", array);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json;*/
        }
        return null;
    }

    public Bitmap generateBarcode(String contents) {


//            String s = contents.getString("contact");
//
//            int indexOfOpenBracket = s.indexOf("[");
//            int indexOfLastBracket = s.lastIndexOf("]");
//            s = s.substring(indexOfOpenBracket+1, indexOfLastBracket);

        Log.e("here w.json", contents);


        try {
            return BarcodeWriter.encodeStringToBitmap(contents);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
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
