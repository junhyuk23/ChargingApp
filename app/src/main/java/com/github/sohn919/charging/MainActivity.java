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

    //구글지도
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
    private int c_point = 0; // 충전탭 포인트
    private int u_point = 0; // 현재 사용자 보유 포인트
    private int CPoint = 0; //  db에서 가져온 목표충전량
    private int rtcharge = 0; // 현재 충전량
    private double dc_point = 0;
    private double c_amount = 0; // 충전탭 전력량
    private String adminCheck = "0";

    private int count, i = 1;

    private WaveView waveView;



    //현재 시간 불러오기
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


        //유저가 로그인 하지 않은 상태라면 null 상태이고 이 액티비티를 종료하고 로그인 액티비티를 연다.
        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        //유저가 있다면, null이 아니면 계속 진행
        FirebaseUser user = firebaseAuth.getCurrentUser();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false); // 기존 title 지우기
        actionBar.setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 만들기
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu); //뒤로가기 버튼 이미지 지정

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

                    DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics(); //디바이스 화면크기를 구하기위해
                    int width = dm.widthPixels; //디바이스 화면 너비
                    int height = dm.heightPixels; //디바이스 화면 높이
                    //로딩이미지 gif 형식
                    pointDialog = new PointDialog(MainActivity.this);
                    WindowManager.LayoutParams wm = pointDialog.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
                    wm.copyFrom(pointDialog.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
                    wm.width = (int)(width *0.5);  //화면 너비의 절반
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
                                    Log.e("현재 값은?????????????????????????", "" + count);

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
                           Log.e("가져온거",""+admin);
                           adminCheck = admin.toString();

                            if(adminCheck.equals("0")){
                                Toast.makeText(context, "관리자 계정이 아닙니다.", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Intent intent = new Intent(MainActivity.this, ManageActivity.class);
                                startActivity(intent);
                                Toast.makeText(context, "관리자 메뉴", Toast.LENGTH_SHORT).show();
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


        //구글지도
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //위치
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




        //로그인 표시
        View header = navigationView.getHeaderView(0);
        textViewUserEmail = (TextView) header.findViewById(R.id.textViewUserEmail);
        textViewUPoint = (TextView) header.findViewById(R.id.textViewUPoint);
        textViewCarNumber = (TextView) header.findViewById(R.id.textViewCarNumber);
        chargetext = (TextView) findViewById(R.id.chargetext);

        //textViewUserEmail의 내용을 변경해 준다.
        textViewUserEmail.setText(user.getEmail() + "으로 로그인 하였습니다.");


        //차량번호 표시
        myRef.child("Users").child(user.getUid()).child("number").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                textViewCarNumber.setText(snapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //보유포인트 표시
        myRef.child("Users").child(user.getUid()).child("point").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                u_point = snapshot.getValue(Integer.class);
                textViewUPoint.setText("보유포인트: " + u_point + " P");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        waveView = (WaveView) findViewById(R.id.wave_view);

        //1.DB에서 목표충전량(chargepoint -> CPoint에 저장)
        myRef.child("Users").child(user.getUid()).child("chargepoint").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                CPoint = (int) snapshot.getValue(Integer.class);
                Log.e("db에서 가져온 목표충전량 : ",""+CPoint);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //2.현재 충전량을 가져옴(charge)
        //충전량 표시
        myRef.child("charge").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer ch = snapshot.getValue(Integer.class);
                Log.e("uid는????? :",""+user.getUid());
                Log.e("db에서 가져온 목표충전량222 : ",""+CPoint);
                if(CPoint == 0){
                    CPoint = 100;
                }
                ch = (ch * 100) / CPoint ;
                Log.e("현재충전량222: ",""+ch);;
                if(ch >= 100){
                    ch = 100;
                    waveView.setProgress(ch);
                    chargetext.setText(ch + "%");
                    Toast.makeText(MainActivity.this, "충전이 완료 되었습니다 !", Toast.LENGTH_SHORT).show();
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
                //Log.e("MainActivity", String.valueOf(databaseError.toException())); // 에러문 출력
            }
        });
         */

        /*
        //사용내역 탭
        myRef.child("UHistory").child(CarNumber).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Object value = snapshot.getValue(Object.class);
                t_text.setText(value.toString());

                for(DataSnapshot snapshot2 : snapshot.getChildren()){ // 하위노드가 없을때까지 반복
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
        markerOptions.title("현재 위치");
        markerOptions.snippet("한전kdn");
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
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics(); //디바이스 화면크기를 구하기위해
        int width = dm.widthPixels; //디바이스 화면 너비
        int height = dm.heightPixels; //디바이스 화면 높이

        //로딩이미지 gif 형식
        loadingDialog = new LoadingDialog(this);
        WindowManager.LayoutParams wm = loadingDialog.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
        wm.copyFrom(loadingDialog.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
        wm.width = (int)(width *0.5);  //화면 너비의 절반
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
            case android.R.id.home:{ // 왼쪽 상단 버튼 눌렀을 때
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
