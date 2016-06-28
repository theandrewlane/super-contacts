package com.k1ngdr3w.supercontacts;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
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

    boolean gps_enabled = false;
    View newContactFromScanButton, newContactButton;

    static DriversLicense license = null;
    Activity activity;
    private long rowID;
    String coords;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        newContactFromScanButton = findViewById(R.id.addNewContactButton);
        newContactButton = findViewById(R.id.addNewContactNoScanButton);
        hideMainActivityButtons(false);
        contactListFragment = new ContactListFragment();
        contactEditFragment = new ContactEditViewFragment();
        findCurrentLocation();
        activity = this;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.MainLayout, contactListFragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        hideMainActivityButtons(false);
        super.onBackPressed();
    }


    private void findCurrentLocation() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        if (checkLocationPermission())
            myLocation.getLocation(this, locationResult);
    }

    //TODO put a toast here for loc fail
    public MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {

        @Override
        public void gotLocation(Location location) {
            if (location != null) {
                coords = String.valueOf("lat: " + location.getLatitude()) + "lon: " + String.valueOf(location.getLongitude());
            }
        }
    };

    public void onClick_newScan(View view) {
        onAdd();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                Toast.makeText(this, "Cancelled ID scan!", Toast.LENGTH_LONG).show();
            } else {
                license = new DriversLicense(result.getContents());
                license.toJson();
                String address = license.getAddress() + "\n" + license.getCity() + ", " + license.getState();
                contactListFragment.addContactToDB(license.getLastName(), license.getFirstName(), address, license.getDOB(), null, coords);
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onAdd() {
        new IntentIntegrator(activity).setOrientationLocked(false).setCaptureActivity(CustomScannerActivity.class).initiateScan();
        contactListFragment = new ContactListFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.MainLayout, contactListFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    //Hide The Activity Button
    public void hideMainActivityButtons(boolean doHide){
        if (doHide) {
            newContactButton.setVisibility(View.INVISIBLE);
            newContactFromScanButton.setVisibility(View.INVISIBLE);
        }
        else {
            newContactFromScanButton.setVisibility(View.VISIBLE);
            newContactButton.setVisibility(View.VISIBLE);
        }
    }

    // ******* Fragment Callbacks ******* \\
    @Override
    public void onLongSelect(long rowID) {
        contactEditFragment.deleteContact(rowID);
        contactListFragment.reloadContacts();

    }

    @Override
    public void onSelect(long rowID) {
        contactEditFragment = new ContactEditViewFragment();
        hideMainActivityButtons(true);
        Bundle arguments = new Bundle();
        Log.e("MAIN ------", "Clicked " + rowID);

        arguments.putLong(DatabaseHelper.KEY_ROWID, rowID);
        contactEditFragment.setArguments(arguments);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.MainLayout, contactEditFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void onClick_newContact(View view) {
        contactEditFragment = new ContactEditViewFragment();
        hideMainActivityButtons(true);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.MainLayout, contactEditFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    @Override
    public void onCompleteSave(long rowID) {
        getFragmentManager().popBackStack();
        getFragmentManager().popBackStack();
        contactListFragment.reloadContacts();
    }

    @Override
    public void onDeleteContact() {
        getFragmentManager().popBackStack();
        getFragmentManager().popBackStack();
        contactListFragment.reloadContacts();
        hideMainActivityButtons(false);
    }
    // ******* Fragment Callbacks ******* \\


    // ******* Location Permissions ******* \\
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    gps_enabled = true;
                else
                    gps_enabled = false;
                return;
            }
        }
    }

    public boolean checkLocationPermission() {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }


    // ******* Location Permissions ******* \\

}

