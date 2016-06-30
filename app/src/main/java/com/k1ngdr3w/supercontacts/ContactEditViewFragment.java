package com.k1ngdr3w.supercontacts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.zxing.WriterException;

import org.json.JSONArray;
import org.json.JSONObject;


public class ContactEditViewFragment extends Fragment {

    private ContactEditFragment_Listener contactEditFragment_listener;
    private long rowID;
    private MainActivity ma;
    public String state, firstName, lastName, tempName;
    private EditText address, birthday, phone, name;
    private boolean isSharable = false;
    double lat, lon;
    private int i_address, i_bday, i_phone, i_firstName, i_lastName, i_geoLoc;
    Activity act;
    Button geoCords;

    String locationCoords;
    GoogleMap map;

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

    public void onClick_showMap() {


        Dialog dialog = new Dialog(act);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_map);
        dialog.show();
        MapView mMapView = (MapView) dialog.findViewById(R.id.mapView);
        MapsInitializer.initialize(act);

        mMapView = (MapView) dialog.findViewById(R.id.mapView);
        mMapView.onCreate(dialog.onSaveInstanceState());
        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(lat,
                lon));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        map = mMapView.getMap();
        map.moveCamera(center);
        map.addCircle(new CircleOptions()
                .center(new LatLng(lat, lon))
                .radius(100)
                .strokeColor(Color.BLACK)
                .fillColor(0x5500ff00)
                .strokeWidth(2));
        map.animateCamera(zoom);
        map.setBuildingsEnabled(true);
        if (ActivityCompat.checkSelfPermission(act, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //The user will have permission at this point anyway... Stupid android
            return;
        }
        map.setMyLocationEnabled(true);
        mMapView.onResume();// needed to get the map to display immediately


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
        geoCords = (Button) rv.findViewById(R.id.geoCords);

        geoCords.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                onClick_showMap();
            }
        });

        if (savedData.toString().contains(DatabaseHelper.KEY_ROWID))

        {
            state = "edit";
            geoCords.setEnabled(true);
            geoCords.setClickable(true);
            geoCords.setFocusable(true);
            rowID = savedData.getLong(DatabaseHelper.KEY_ROWID);
            new GetContactFromDB().execute(rowID);
        }
        if (savedData.toString().contains(DatabaseHelper.KEY_GEOLOCATION))
        {
            state = "create";
            locationCoords = savedData.getString(DatabaseHelper.KEY_GEOLOCATION);
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

            rowID = dbHelper.insertContact(lastName, firstName, address.getText().toString(), birthday.getText().toString(), phone.getText().toString(), locationCoords);
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
                locationCoords = cursor.getString(i_geoLoc);
                geoCords.setText(locationCoords);
                cursor.close();
                dbHelper.close();
                String[] locationArray = locationCoords.split(",");
                lat = Double.parseDouble(locationArray[0]);
                lon = Double.parseDouble(locationArray[1]);
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
        }
        return null;
    }

    public Bitmap generateBarcode(String contents) {
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
