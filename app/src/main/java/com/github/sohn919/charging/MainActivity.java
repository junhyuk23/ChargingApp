package com.github.sohn919.charging;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.text.SimpleDateFormat;
import java.util.Date;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.annotations.NotNull;
import com.john.waveview.WaveView;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private DrawerLayout mDrawerLayout;
    private Context context = this;

    //????????????
    private GoogleMap mMap;
    private double longitude;
    private double latitude;
    private LoadingDialog loadingDialog;

    private PointDialog pointDialog;

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = mDatabase.getReference();
    private FirebaseAuth firebaseAuth;


    private TextView textViewUserEmail;
    private TextView textViewUPoint;
    private TextView textViewCarNumber;
    private TextView chargetext;
    private int c_point = 0; // ????????? ?????????
    private int u_point = 0; // ?????? ????????? ?????? ?????????
    private int CPoint = 0; //  db?????? ????????? ???????????????
    private int rtcharge = 0; // ?????? ?????????
    private double dc_point = 0;
    private double c_amount = 0; // ????????? ?????????
    private String adminCheck = "0";

    private int count, i = 1;

    private WaveView waveView;



    //?????? ?????? ????????????
    private String getTime() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String getTime = dateFormat.format(date);
        return getTime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();


        //????????? ????????? ?????? ?????? ???????????? null ???????????? ??? ??????????????? ???????????? ????????? ??????????????? ??????.
        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        //????????? ?????????, null??? ????????? ?????? ??????
        FirebaseUser user = firebaseAuth.getCurrentUser();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false); // ?????? title ?????????
        actionBar.setDisplayHomeAsUpEnabled(true); // ???????????? ?????? ?????????
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu); //???????????? ?????? ????????? ??????

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();

                int id = menuItem.getItemId();
                String title = menuItem.getTitle().toString();

                if(id == R.id.pointcharging){
                    menuItem.setChecked(false);

                    DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics(); //???????????? ??????????????? ???????????????
                    int width = dm.widthPixels; //???????????? ?????? ??????
                    int height = dm.heightPixels; //???????????? ?????? ??????
                    //??????????????? gif ??????
                    pointDialog = new PointDialog(MainActivity.this);
                    WindowManager.LayoutParams wm = pointDialog.getWindow().getAttributes();  //?????????????????? ?????? ?????? ??????????????????
                    wm.copyFrom(pointDialog.getWindow().getAttributes());  //????????? ??????????????? ????????? ?????????????????? ??????????????????
                    wm.width = (int)(width *0.5);  //?????? ????????? ??????
                    wm.height = (int)(height *0.5);
                    pointDialog.show();
                }
                else if(id == R.id.history){
                    menuItem.setChecked(false);
                    Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                    startActivity(intent);
                }
                else if(id == R.id.payment){
                    Intent intent = new Intent(MainActivity.this, NfcActivity.class);
                    startActivity(intent);

                    /*
                    myRef.child("nfc").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Object num = snapshot.getKey();
                                if(Integer.toString(i).equals(num.toString())) {
                                    i++;
                                } else {
                                    count = i;
                                    Log.e("?????? ???????????????????????????????", "" + count);

                                    Intent intent = new Intent(MainActivity.this, NfcActivity.class);
                                    intent.putExtra("count", count);
                                    startActivity(intent);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        }
                     */
                }
                else if(id == R.id.admin){
                    myRef.child("Users").child(user.getUid()).child("admin").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                           Object admin = snapshot.getValue();
                           Log.e("????????????",""+admin);
                           adminCheck = admin.toString();

                            if(adminCheck.equals("0")){
                                Toast.makeText(context, "????????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Intent intent = new Intent(MainActivity.this, ManageActivity.class);
                                startActivity(intent);
                                Toast.makeText(context, "????????? ??????", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });


                }
                return true;
            }
        });


        //????????????
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //??????
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                    {android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            longitude = location.getLongitude();
//            latitude = location.getLatitude();

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, gpsLocationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, gpsLocationListener);

        }




        //????????? ??????
        View header = navigationView.getHeaderView(0);
        textViewUserEmail = (TextView) header.findViewById(R.id.textViewUserEmail);
        textViewUPoint = (TextView) header.findViewById(R.id.textViewUPoint);
        textViewCarNumber = (TextView) header.findViewById(R.id.textViewCarNumber);
        chargetext = (TextView) findViewById(R.id.chargetext);

        //textViewUserEmail??? ????????? ????????? ??????.
        textViewUserEmail.setText(user.getEmail() + "?????? ????????? ???????????????.");


        //???????????? ??????
        myRef.child("Users").child(user.getUid()).child("number").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                textViewCarNumber.setText(snapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //??????????????? ??????
        myRef.child("Users").child(user.getUid()).child("point").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                u_point = snapshot.getValue(Integer.class);
                textViewUPoint.setText("???????????????: " + u_point + " P");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        waveView = (WaveView) findViewById(R.id.wave_view);

        //1.DB?????? ???????????????(chargepoint -> CPoint??? ??????)
        myRef.child("Users").child(user.getUid()).child("chargepoint").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                CPoint = (int) snapshot.getValue(Integer.class);
                Log.e("db?????? ????????? ??????????????? : ",""+CPoint);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //2.?????? ???????????? ?????????(charge)
        //????????? ??????
        myRef.child("charge").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer ch = snapshot.getValue(Integer.class);
                Log.e("uid???????? :",""+user.getUid());
                Log.e("db?????? ????????? ???????????????222 : ",""+CPoint);
                if(CPoint == 0){
                    CPoint = 100;
                }
                ch = (ch * 100) / CPoint ;
                Log.e("???????????????222: ",""+ch);;
                if(ch >= 100){
                    ch = 100;
                    waveView.setProgress(ch);
                    chargetext.setText(ch + "%");
                    Toast.makeText(MainActivity.this, "????????? ?????? ??????????????? !", Toast.LENGTH_SHORT).show();
                    myRef.child("ready").setValue(0);
                }
                waveView.setProgress(ch);
                chargetext.setText(ch + "%");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        /*
        myRef.child("Users").child(user.getUid()).child("electric").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                chargetext.setText(value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Log.e("MainActivity", String.valueOf(databaseError.toException())); // ????????? ??????
            }
        });
         */

        /*
        //???????????? ???
        myRef.child("UHistory").child(CarNumber).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Object value = snapshot.getValue(Object.class);
                t_text.setText(value.toString());

                for(DataSnapshot snapshot2 : snapshot.getChildren()){ // ??????????????? ??????????????? ??????
                   Object c_time = snapshot2.getKey().toString();
                   Object c_charge = snapshot2.getValue().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
         */



    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        LatLng SEOUL = new LatLng(35.02197, 126.78415);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(SEOUL);
        markerOptions.title("?????? ??????");
        markerOptions.snippet("??????kdn");
        mMap.addMarker(markerOptions);

        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.marker_img);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 150, 150, false);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

        mMap.addMarker(markerOptions);

        mMap.setOnMarkerClickListener(this);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL, 18));


    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Toast.makeText(this, marker.getTitle() + "\n" + marker.getPosition(), Toast.LENGTH_SHORT).show();
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics(); //???????????? ??????????????? ???????????????
        int width = dm.widthPixels; //???????????? ?????? ??????
        int height = dm.heightPixels; //???????????? ?????? ??????

        //??????????????? gif ??????
        loadingDialog = new LoadingDialog(this);
        WindowManager.LayoutParams wm = loadingDialog.getWindow().getAttributes();  //?????????????????? ?????? ?????? ??????????????????
        wm.copyFrom(loadingDialog.getWindow().getAttributes());  //????????? ??????????????? ????????? ?????????????????? ??????????????????
        wm.width = (int)(width *0.5);  //?????? ????????? ??????
        wm.height = (int)(height *0.5);
        loadingDialog.show();

        return true;
    }


    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        } public void onStatusChanged(String provider, int status, Bundle extras) {

        } public void onProviderEnabled(String provider) {

        } public void onProviderDisabled(String provider) {

        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ // ?????? ?????? ?????? ????????? ???
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }


}
