package com.example.scarb.firebasetest;

import java.util.Calendar;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class HotSpotFragment extends android.app.Fragment implements OnMapReadyCallback, View.OnClickListener{


    private boolean createParty = false;
    private View view;
    private DatabaseReference databaseReference;
    private Button makePartyButton;
    private GoogleMap userMap;
    private long userPartyCount;
    private String currentUsername;
    private FirebaseUser currentUser;

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
        makePartyButton = (Button) view.findViewById(R.id.makePartyButton);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
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
        makePartyButton.setOnClickListener(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        userMap = googleMap;
        final Calendar calendar = Calendar.getInstance();
        int amPm = calendar.get(Calendar.AM_PM);
        int currentHour = calendar.get(Calendar.HOUR);

        //Makes the map look dark if it is past 6PM
        if (amPm == Calendar.AM){
            if (currentHour == 0 || (currentHour >= 1 && currentHour < 6)){
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.style_json));
            }
        }else if (amPm == Calendar.PM){
            if (currentHour >= 6 && currentHour <= 11){
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.style_json));
            }
        }
        //Check permissions and load current location



        //Load all the party pins that are currently happening
        loadParties(googleMap);

        //Check when the user clicks on the maps
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            //If user clicks the map
            public void onMapClick(LatLng latLng) {

                //Check the number of parties the user has
                if (userPartyCount < 2 && createParty){
                    addMarker(latLng);
                    userPartyCount++;
                    createParty = false;
                }else if (userPartyCount > 2){
                    Toast.makeText(getActivity(), "Reached max parties you can post", Toast.LENGTH_SHORT)
                            .show();
                }
                //Add marker to the map
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

    public void loadParties(final GoogleMap googleMap){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot parties : dataSnapshot.child("Hotspots").child("Puerto Rico")
                        .getChildren()){
                    String partyIdKey = parties.getKey();
                    double longitude = (double) parties.child("Longitude").getValue();
                    double latitude = (double) parties.child("Latitude").getValue();
                    String owner = parties.child("Owner").getValue().toString();
                    LatLng latLng = new LatLng(latitude, longitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Best party in PR created by " + owner);
                    /////////////////////////////////////////////////////////
                    CameraUpdate zoomIn = CameraUpdateFactory.zoomTo(14);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    googleMap.animateCamera(zoomIn);
                    googleMap.addMarker(markerOptions);
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
    public void addMarker(LatLng latLng){
        String partyIdKey = databaseReference.push().getKey();
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
    public void checkUserParties(){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                try {
                    long numParties = (long) dataSnapshot.child("Users")
                            .child(firebaseUser.getUid()).child("numParties").getValue();
                    setPartyCounter(numParties);
                } catch (Exception e){
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

    public void setUsername(String username){
        currentUsername = username;
    }

    public void setPartyCounter(long counter){
        userPartyCount = counter;
    }

    @Override
    public void onClick(View v) {
        if (v == makePartyButton){
            Toast.makeText(getActivity() ,"Please click the map to create party", Toast.LENGTH_LONG)
                    .show();
            createParty = true;
        }
    }
}
