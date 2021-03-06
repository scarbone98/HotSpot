package com.example.scarb.firebasetest;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class HotSpotFragment extends android.app.Fragment implements OnMapReadyCallback,
        View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener{

    //Location manager used to check user's location
    private LocationRequest mLocationRequest;
    private GoogleApiClient googleApiClient;
    private Marker mCurrLocation;

    private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 30;
    //Max parties a user can have
    private final int maxParties = 50;
    //Checks if the create button has been clicked in order to add a pin to the map
    private boolean createParty = false;
    //Checks to see if a window is currently open in the fragment
    private boolean windowOpen = false;
    //Global view variable used to check if there is a created instance of the google maps
    //if there is then restore that instance
    private View view;
    //Reference to database, used to access/set values in the database
    private DatabaseReference databaseReference;
    //Button used to open the create party window
    private Button openCreatePWindow;
    //Global variable of map, used to access the map in other methods
    private GoogleMap userMap;
    //Amount of parties the user has created, limited to 50 (For debugging)
    private long userPartyCount;
    //Gets the current user's username to reduce amount of code later in the code
    private String currentUsername;
    //Instance of the current user in firebase
    private FirebaseUser currentUser;
    //Framelayouts used to hold the partyInfoWindow and createPartyWindow respectively
    private FrameLayout partyInfoWindow, createPartyWindow;
    //TestButton, just for testing, createPartyButton, checks to see if party description
    //isn't empty. If it isn't add the party to the database.
    private Button goingButton, createPartyButton, notGoingButton;
    //OwnderInfo, displays the party owner's username, partyDescription displays
    //the description added by the party's owner
    private TextView ownerInfo, partyDescription, partyLocation;
    //EditText box for description of party, used when creating a party
    private EditText partyInfoText;
    //Code used to identify party in database
    private String partyCode;
    //Provides the coordinates of the clicked map, made global to access in other methods
    private LatLng partyCoordinates;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup viewParent = (ViewGroup) view.getParent();
            if (viewParent != null)
                viewParent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_hot_spot, container, false);
        } catch (Exception e) {
            //Returns the view if exception was caught
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        openCreatePWindow = (Button) view.findViewById(R.id.makePartyButton);
        goingButton = (Button) view.findViewById(R.id.goingButton);
        createPartyButton = (Button) view.findViewById(R.id.createPartyButton);
        notGoingButton = (Button) view.findViewById(R.id.notGoingButton);

        partyLocation = (TextView) view.findViewById(R.id.partyLocation);
        ownerInfo = (TextView) view.findViewById(R.id.ownerInfo);
        partyDescription = (TextView) view.findViewById(R.id.partyDescriptionTextView);

        partyInfoText = (EditText) view.findViewById(R.id.partyDescriptionEditText);

        partyInfoWindow = (FrameLayout) view.findViewById(R.id.partyInfoWindow);
        createPartyWindow = (FrameLayout) view.findViewById(R.id.createPartyWindow);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Gets the user's username from the start of the fragment. Makes the code less verbose later on.
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.child("Users").child(currentUser.getUid())
                        .child("username").getValue().toString();
                setUsername(userName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Something happens
            }
        });

        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.mapContainer);
        mapFragment.getMapAsync(this);
        checkUserParties();
        openCreatePWindow.setOnClickListener(this);
        goingButton.setOnClickListener(this);
        createPartyButton.setOnClickListener(this);
        notGoingButton.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        userMap = googleMap;
        buildGoogleAPI();
        checkLocationPermission();
        googleApiClient.connect();

        final Calendar calendar = Calendar.getInstance();
        int amPm = calendar.get(Calendar.AM_PM);
        int currentHour = calendar.get(Calendar.HOUR);

        //Makes the map look dark if it is past 6PM
        if (amPm == Calendar.AM) {
            if (currentHour == 0 || (currentHour >= 1 && currentHour < 6)) {
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.style_json));
            }
        } else if (amPm == Calendar.PM) {
            if (currentHour >= 6 && currentHour <= 11) {
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.style_json));
            }
        }
        //Check permissions and load current location

        //Load all the party pins that are currently happening
        loadParties(googleMap);

        //Check when the user clicks on the maps
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (windowOpen) {
                    partyDescription.setText("");
                    ownerInfo.setText("");
                    partyInfoText.setText("");
                    partyInfoWindow.setVisibility(View.INVISIBLE);
                    createPartyWindow.setVisibility(View.INVISIBLE);
                    goingButton.setVisibility(View.VISIBLE);
                    notGoingButton.setVisibility(View.INVISIBLE);
                    windowOpen = false;
                    openCreatePWindow.setVisibility(View.VISIBLE);
                }
                //TODO fix if statement to addMarker, only if description is added to party
                //Check the number of parties the user has
                if (userPartyCount < maxParties && createParty) {
                    //ADDS MARKER EVEN IF DESCRIPTION ISN'T ADDED TO PARTY NEED TO FIX
                    partyCoordinates = latLng;
                    createPartyWindow.setVisibility(View.VISIBLE);
                    openCreatePWindow.setVisibility(View.INVISIBLE);
                    userPartyCount++;
                    createParty = false;
                    windowOpen = true;
                } else if (userPartyCount > maxParties) {
                    Toast.makeText(getActivity(), "Reached max parties you can post", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                //TODO load activity that gives information about party CHANGE HARD CODED STUFF LATER
                if (marker.getTag() != mCurrLocation.getTag()) {
                    databaseReference.child("Hotspots").child("Puerto Rico")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.child(marker.getTag().toString())
                                            .child("Going").child(currentUsername)
                                            .getValue() == null) {
                                        goingButton.setVisibility(View.VISIBLE);
                                        notGoingButton.setVisibility(View.INVISIBLE);
                                    } else {
                                        goingButton.setVisibility(View.INVISIBLE);
                                        notGoingButton.setVisibility(View.VISIBLE);
                                    }
                                    String owner = dataSnapshot.child(marker.getTag().toString())
                                            .child("Owner").getValue().toString();
                                    String description = dataSnapshot.child(marker.getTag().toString())
                                            .child("Description").getValue().toString();
                                    ownerInfo.setText(owner);
                                    partyDescription.setText(description);

                                    //Get the party's location
                                    //TODO probably get rid of later
                                    Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                                    StringBuilder stringBuilder = new StringBuilder();
                                    try {
                                        double lat = marker.getPosition().latitude;
                                        double lon = marker.getPosition().longitude;
                                        List<Address> addresses = geocoder.getFromLocation(lat,lon, 1);
                                        int maxAddresses = addresses.get(0).getMaxAddressLineIndex();
                                        for (int i = 0; i < maxAddresses; i++){
                                            String addressStr = addresses.get(0).getAddressLine(i);
                                            stringBuilder.append(addressStr);
                                            stringBuilder.append(" ");
                                        }
                                    }catch (IOException e){
                                        Toast.makeText(getActivity(), "Could not load current address", Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                    partyLocation.setText(stringBuilder);

                                    //Gets partyCode to check if going to party
                                    partyCode = marker.getTag().toString();
                                    partyInfoWindow.setVisibility(View.VISIBLE);
                                    openCreatePWindow.setVisibility(View.INVISIBLE);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    //Do something
                                }
                            });
                    windowOpen = true;
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference.child("Users").child(firebaseUser.getUid()).child("numParties")
                .setValue(userPartyCount);
    }

    public void loadParties(final GoogleMap googleMap) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot parties : dataSnapshot.child("Hotspots").child("Puerto Rico")
                        .getChildren()) {
                    String partyIdKey = parties.getKey();
                    double longitude = (double) parties.child("Longitude").getValue();
                    double latitude = (double) parties.child("Latitude").getValue();
                    String owner = parties.child("Owner").getValue().toString();
                    LatLng latLng = new LatLng(latitude, longitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Best party in PR created by " + owner);
                    /////////////////////////////////////////////////////////
                    Marker marker = googleMap.addMarker(markerOptions);
                    marker.setTag(partyIdKey);
                    //////////////////////////////////////////////////////////
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Change it with a refresh button later
                Toast.makeText(getActivity(), "Error, please try again", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    //Method to add marker to the map
    public void addMarker(LatLng latLng) {
        String partyIdKey = databaseReference.push().getKey();
        partyCode = partyIdKey;
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Lat: " + latLng.latitude + " Lon: " + latLng.longitude);
        databaseReference.child("Hotspots").child("Puerto Rico").child(partyIdKey)
                .child("Longitude").setValue(latLng.longitude);
        databaseReference.child("Hotspots").child("Puerto Rico").child(partyIdKey)
                .child("Latitude").setValue(latLng.latitude);
        databaseReference.child("Hotspots").child("Puerto Rico").child(partyIdKey)
                .child("Owner").setValue(currentUsername);
        userMap.addMarker(markerOptions);
    }

    //Check the number of parties the user has made (limited to 2 parties)
    public void checkUserParties() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                try {
                    long numParties = (long) dataSnapshot.child("Users")
                            .child(firebaseUser.getUid()).child("numParties").getValue();
                    setPartyCounter(numParties);
                } catch (Exception e) {
                    databaseReference.child("Users").child(firebaseUser.getUid())
                            .child("numParties").setValue(0);
                    setPartyCounter(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Error, please try again", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    public void setUsername(String username) {
        currentUsername = username;
    }

    public void setPartyCounter(long counter) {
        userPartyCount = counter;
    }

    @Override
    public void onClick(View v) {
        if (v == openCreatePWindow) {
            Toast.makeText(getActivity(), "Please click the map to create party", Toast.LENGTH_LONG)
                    .show();
            createParty = true;

        }
        else if (v == goingButton) {
            Toast.makeText(getActivity(), "Added to list", Toast.LENGTH_LONG)
                    .show();
            databaseReference.child("Hotspots").child("Puerto Rico")
                    .child(partyCode).child("Going").child(currentUsername)
                    .setValue("going").addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    notGoingButton.setVisibility(View.VISIBLE);
                    goingButton.setVisibility(View.INVISIBLE);
                }
            });
        }

        else if (v == createPartyButton) {
            if (partyInfoText != null && partyInfoText.getText().toString().equals("")) {
                Toast.makeText(getActivity(), "Please enter a description", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            addMarker(partyCoordinates);
            databaseReference.child("Hotspots").child("Puerto Rico")
                    .child(partyCode).child("Description")
                    .setValue(partyInfoText.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getActivity(), "Party created", Toast.LENGTH_SHORT)
                                    .show();
                            windowOpen = false;
                            createParty = false;
                            createPartyWindow.setVisibility(View.INVISIBLE);
                            openCreatePWindow.setVisibility(View.VISIBLE);
                            partyInfoText.setText("");
                        }
                    });
        }

        else if (v == notGoingButton) {
            databaseReference.child("Hotspots").child("Puerto Rico")
                    .child(partyCode).child("Going").child(currentUsername)
                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getActivity(), "Removed from list", Toast.LENGTH_SHORT)
                            .show();
                    notGoingButton.setVisibility(View.INVISIBLE);
                    goingButton.setVisibility(View.VISIBLE);
                }
            });
        }
    }




    /*
        User location section
     */






    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission. ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission. ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle("Location Permission")
                        .setMessage("Need location to access map or something")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission. ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int code,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (code) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission. ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        userMap.setMyLocationEnabled(true);
                        googleApiClient.connect();
                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    userMap.setMyLocationEnabled(false);

                }
            }

        }
    }

    //Initialize the googleAPIService
    protected synchronized void buildGoogleAPI(){
        googleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onLocationChanged(Location location) {
        //remove previous current location marker and add new one at current position
        if (mCurrLocation != null) {
            mCurrLocation.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title("You");
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocation = userMap.addMarker(markerOptions);
        mCurrLocation.setTag("CurrentUser");

        //If you only need one location, unregister the listener
        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkLocationPermission();
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (mLastLocation != null) {
            //place marker at current position
            userMap.clear();
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            ////////////////////////////////////////////////////
            //Camera animation to your initial location
            CameraUpdate zoomIn = CameraUpdateFactory.zoomTo(15);
            userMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            userMap.animateCamera(zoomIn);
            ///////////////////////////////////////////////////
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.title("You");
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            mCurrLocation = userMap.addMarker(markerOptions);
            mCurrLocation.setTag("CurrentUser");
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); //5 seconds
        mLocationRequest.setFastestInterval(3000); //3 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        StringBuilder stringBuilder = new StringBuilder();
        try {
            double lat = mCurrLocation.getPosition().latitude;
            double lon = mCurrLocation.getPosition().longitude;
            List<Address> addresses = geocoder.getFromLocation(lat,lon, 1);
            int maxAddresses = addresses.get(0).getMaxAddressLineIndex();
            for (int i = 0; i < maxAddresses; i++){
                String addressStr = addresses.get(0).getAddressLine(i);
                stringBuilder.append(addressStr);
                stringBuilder.append(" ");
            }
        }catch (IOException e){
            Toast.makeText(getActivity(), "Could not load current address", Toast.LENGTH_SHORT)
                    .show();
        }
        Toast.makeText(getActivity(), stringBuilder, Toast.LENGTH_SHORT)
                .show();

        //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
