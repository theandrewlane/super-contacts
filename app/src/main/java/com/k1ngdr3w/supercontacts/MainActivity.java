package com.k1ngdr3w.supercontacts;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class MainActivity extends AppCompatActivity implements ContactListFragment.ContactList_listener, ContactEditViewFragment.ContactEditFragment_Listener {

    ContactListFragment contactListFragment;
    ContactEditViewFragment contactEditFragment;
    MyLocation myLocation = new MyLocation();

    boolean cameraPermissionAccepted = false;

    boolean locationPermissionAccepted = false;
    View newContactFromScanButton, newContactButton;

    static DriversLicense license = null;
    Activity activity;
    private long rowID;
    String coords;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            requestPermissions();
            setContentView(R.layout.activity_main);
            newContactFromScanButton = findViewById(R.id.addNewContactButton);
            newContactButton = findViewById(R.id.addNewContactNoScanButton);
            hideMainActivityButtons(false);
            contactListFragment = new ContactListFragment();
            contactEditFragment = new ContactEditViewFragment();
            activity = this;
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.MainLayout, contactListFragment);
            transaction.commit();
        }
    }

    @Override
    public void onBackPressed() {
        hideMainActivityButtons(false);
        super.onBackPressed();
    }


    public void findCurrentLocation() {
        if (hasPermission("android.permission.ACCESS_FINE_LOCATION"))
            myLocation.getLocation(this, locationResult);

    }

    public MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            if (location != null) {
                coords = location.getLatitude() + ", " + location.getLongitude();
            }
        }
    };

    public void onClick_newScan(View view) {
        onAdd();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putLong(DatabaseHelper.KEY_ROWID, rowID);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(hasPermission("android.permission.ACCESS_FINE_LOCATION")){
            findCurrentLocation();

        }
        if (hasPermission("android.permission.CAMERA")) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Log.d("MainActivity", "Cancelled scan");
                    Toast.makeText(this, "You cancelled the scan!", Toast.LENGTH_LONG).show();
                } else {
                    license = new DriversLicense(result.getContents());
                    String address = null;
                    //IF this is an import, use imported data
                    if (license.getGeoCords() != null) {
                        coords = license.getGeoCords();
                    }
                    if (license.getCompleteAddress() != null) {
                        address = license.getCompleteAddress();
                    }
                    if (license.getCompleteAddress() == null) {
                        address = license.getAddress() + "\n" + license.getCity() + ", " + license.getState();

                    }

                    contactListFragment.addContactToDB(license.getLastName(), license.getFirstName(), address, license.getDOB(), license.getPhoneNumber(), coords);
                }
            } else {
                // This is important, otherwise the result will not be passed to the fragment
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
        requestPermissions();
        super.onActivityResult(0, 0, null);
    }


    @Override
    public void onAdd() {
        if (hasPermission("android.permission.CAMERA") && hasPermission("android.permission.ACCESS_FINE_LOCATION")) {
            findCurrentLocation();
            new IntentIntegrator(activity).setOrientationLocked(false).setCaptureActivity(CustomScannerActivity.class).initiateScan();
            contactListFragment = new ContactListFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.MainLayout, contactListFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else {
            requestPermissions();
        }
    }

    //Hide The Activity Button
    public void hideMainActivityButtons(boolean doHide) {
        if (doHide) {
            newContactButton.setVisibility(View.INVISIBLE);
            newContactFromScanButton.setVisibility(View.INVISIBLE);
        } else {
            newContactFromScanButton.setVisibility(View.VISIBLE);
            newContactButton.setVisibility(View.VISIBLE);
        }
    }

    // ******* Fragment Callbacks ******* \\

    @Override
    public void onSelect(long rowID) {
        findCurrentLocation();

        contactEditFragment = new ContactEditViewFragment();
        hideMainActivityButtons(true);
        Bundle arguments = new Bundle();
        arguments.putLong(DatabaseHelper.KEY_ROWID, rowID);
        contactEditFragment.setArguments(arguments);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.MainLayout, contactEditFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void onClick_newContact(View view) {
        findCurrentLocation();
        Log.e("oncreateMain", coords);
        contactEditFragment = new ContactEditViewFragment();
        Bundle arguments = new Bundle();
        arguments.putLong(DatabaseHelper.KEY_ROWID, rowID);
        arguments.putString(DatabaseHelper.KEY_GEOLOCATION, coords);
        contactEditFragment.setArguments(arguments);
        hideMainActivityButtons(true);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.MainLayout, contactEditFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    @Override
    public void onCompleteSave(long rowID) {
        findCurrentLocation();

        getFragmentManager().popBackStack();
        getFragmentManager().popBackStack();
        hideMainActivityButtons(false);
        contactListFragment.reloadContacts();
    }

    @Override
    public void onDeleteContact() {
        findCurrentLocation();

        getFragmentManager().popBackStack();
        getFragmentManager().popBackStack();
        contactListFragment.reloadContacts();
        hideMainActivityButtons(false);
    }
    // ******* Fragment Callbacks ******* \\



    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    // ******* Location Permissions ******* \\

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 200: {
                boolean locationPermissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean cameraPermissionAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            }
        }
    }

    private boolean hasPermission(String permission) {
        return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
    }

    public boolean requestPermissions() {
        String[] perms = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.CAMERA"};
        int permsRequestCode = 200;
        requestPermissions(perms, permsRequestCode);
        return true;
    }

    // ******* Location Permissions ******* \\

}

