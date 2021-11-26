package com.github.sohn919.charging;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {


    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
    private DatabaseReference myRef = mDatabase.getReference(); // DB 테이블 연결
    private FirebaseAuth firebaseAuth;

    private String sDate = "2021-11-01";
    private String eDate = "2021-12-31";
    private String dDate = "";
    String s_month = "";
    String s_day = "";
    private String CarNumber = "";
    Button sButton;

    private ListView listView;
    private ListView listView2;
    private ArrayAdapter<Object> dataAdapter;
    private ArrayAdapter<Object> dataAdapter2;


    private TextView textView_Date;
    private TextView textView_Date2;
    private DatePickerDialog.OnDateSetListener callbackMethod;
    private DatePickerDialog.OnDateSetListener callbackMethod2;

    private Button btn1, btn2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        firebaseAuth = FirebaseAuth.getInstance();


        //유저가 로그인 하지 않은 상태라면 null 상태이고 이 액티비티를 종료하고 로그인 액티비티를 연다.
        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        //유저가 있다면, null이 아니면 계속 진행
        FirebaseUser user = firebaseAuth.getCurrentUser();

        //차량번호 저장
        myRef.child("Users").child(user.getUid()).child("number").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                CarNumber = snapshot.getValue(String.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


//        this.InitializeView();
        this.InitializeListener();
        this.InitializeListener2();


        listView = (ListView) findViewById(R.id.listView);
        listView2 = (ListView) findViewById(R.id.listView2);

        dataAdapter = new ArrayAdapter<Object>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<Object>());
        dataAdapter2 = new ArrayAdapter<Object>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<Object>());

        listView.setAdapter(dataAdapter);
        listView2.setAdapter(dataAdapter2);

        sButton = findViewById(R.id.s_button);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);


        sButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef.child("UHistory").child(CarNumber).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                        dataAdapter.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Object value = snapshot.getKey();
                            dDate = value.toString();

                            if(isWithinRange(dDate, sDate, eDate) == true){
                                value = dDate;
                                dataAdapter.add(value);
                            }

                        }
                        dataAdapter.notifyDataSetChanged();
                        listView.setSelection(dataAdapter.getCount() - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    }
                });

                myRef.child("UHistory").child(CarNumber).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                        dataAdapter2.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Object value2 = snapshot.getValue(Object.class);
                            Object key = snapshot.getKey();
                            dDate = key.toString();
                            if(isWithinRange(dDate, sDate, eDate) == true){
                                dataAdapter2.add(value2);
                            }
                        }
                        dataAdapter2.notifyDataSetChanged();
                        listView2.setSelection(dataAdapter2.getCount() - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    }
                });

            }
        });









        /*
        //유저가 로그인 하지 않은 상태라면 null 상태이고 이 액티비티를 종료하고 로그인 액티비티를 연다.
        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        //유저가 있다면, null이 아니면 계속 진행
        FirebaseUser user = firebaseAuth.getCurrentUser();


        //차량번호 저장
        myRef.child("Users").child(user.getUid()).child("number").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                CarNumber = snapshot.getValue(String.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


         */

    }

//    public void InitializeView()
//    {
//        textView_Date = (TextView)findViewById(R.id.textView_date);
//        textView_Date2 = (TextView)findViewById(R.id.textView_date2);
//    }

    public void InitializeListener()
    {
        callbackMethod = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
            {
                monthOfYear +=1;
                if(monthOfYear < 10) {
                    s_month = "0" + monthOfYear;
                }
                else{
                    s_month = Integer.toString(monthOfYear);
                }
                if(dayOfMonth < 10){
                    s_day = "0" + dayOfMonth;
                }
                else{
                    s_day = Integer.toString(dayOfMonth);
                }
                btn1.setText(year + "-" + s_month + "-" + s_day);
                sDate = year + "-" + s_month + "-" + s_day;
            }
        };
    }

    public void InitializeListener2()
    {
        callbackMethod2 = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
            {
                monthOfYear +=1;
                if(monthOfYear < 10) {
                    s_month = "0" + monthOfYear;
                }
                else{
                    s_month = Integer.toString(monthOfYear);
                }
                if(dayOfMonth < 10){
                    s_day = "0" + dayOfMonth;
                }
                else{
                    s_day = Integer.toString(dayOfMonth);
                }
                btn2.setText(year + "-" + s_month + "-" + s_day);
                eDate = year + "-" + s_month + "-" + s_day;
            }
        };
    }

    public void OnClickHandler(View view)
    {
        DatePickerDialog dialog = new DatePickerDialog(this, callbackMethod, 2021, 10, 1);
        dialog.show();
    }

    public void OnClickHandler2(View view)
    {
        DatePickerDialog dialog = new DatePickerDialog(this, callbackMethod2, 2021, 11, 31);
        dialog.show();
    }

    /*
     * date가 startDate와 EndDate 사이에 있는지 반환
     *
     * @param date yyyymmdd
     * @param startDate yyyymmdd
     * @param endDate yyyymmdd
     * @return
     */
    public static boolean isWithinRange(String date, String startDate, String endDate) {
        /*
        if(date.length() != 8 || startDate.length() != 8 || endDate.length() != 8){
            return false;
        }

        date = date.substring(0,4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
        startDate = startDate.substring(0,4) + "-" + startDate.substring(4, 6) + "-" + startDate.substring(6, 8);
        endDate = endDate.substring(0,4) + "-" + endDate.substring(4, 6) + "-" + endDate.substring(6, 8);
         */

        LocalDate localdate = LocalDate.parse(date);
        LocalDate startLocalDate = LocalDate.parse(startDate);
        LocalDate endLocalDate = LocalDate.parse(endDate);
        endLocalDate = endLocalDate.plusDays(1); // endDate는 포함하지 않으므로 +1일을 해줘야함.

        return ( ! localdate.isBefore( startLocalDate ) ) && ( localdate.isBefore( endLocalDate ) );
    }


}