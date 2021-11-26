package com.github.sohn919.charging;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ManageActivity extends AppCompatActivity {

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
    private DatabaseReference myRef = mDatabase.getReference(); // DB 테이블 연결
    private FirebaseAuth firebaseAuth;

    private TextView textView_man;
    private TextView textView_income;
    private TextView textView_ele;
    Button manBtn;

    private ListView listView_1;
    private ListView listView_2;
    private ListView listView_3;


    private ArrayAdapter<Object> dataAdapter_1;
    private ArrayAdapter<Object> dataAdapter_2;
    private ArrayAdapter<Object> dataAdapter_3;


    private int income = 0; //총수입
    private int ele = 0; //전력사용량


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

        textView_man = (TextView)findViewById(R.id.textView_man);       //어댑터번호
        textView_income = (TextView)findViewById(R.id.textView_income); //총수입
        textView_ele = (TextView)findViewById(R.id.textView_ele);       //전력사용량
        manBtn = findViewById(R.id.manBtn);

        listView_1 = (ListView) findViewById(R.id.listView_1);
        listView_2 = (ListView) findViewById(R.id.listView_2);
        listView_3 = (ListView) findViewById(R.id.listView_3);


        dataAdapter_1 = new ArrayAdapter<Object>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<Object>());
        dataAdapter_2 = new ArrayAdapter<Object>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<Object>());
        dataAdapter_3 = new ArrayAdapter<Object>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<Object>());


        listView_1.setAdapter(dataAdapter_1);
        listView_2.setAdapter(dataAdapter_2);
        listView_3.setAdapter(dataAdapter_3);



        manBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ManageActivity.this, ManageActivity2.class));
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser(); //현재 로그인한 유저정보


        //어댑터 번호저장
        myRef.child("Users").child(user.getUid()).child("admin").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object man = snapshot.getValue(Object.class);
                textView_man.setText(man.toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });



        //총 수입
        /*
        myRef.child("UHistory").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot_A : dataSnapshot.getChildren()){
                    for (DataSnapshot snapshot_B : snapshot_A.getChildren())
                    {
                        int value = (int)snapshot_B.getValue(Integer.class);
                        income = income + value;
                    }
                }
                myRef.child("income").setValue(income);
                textView_income.setText(income + " 원");
                ele = income * 2;                           // 총수입 -> 전력사용량
                textView_ele.setText(ele + " W ");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        */
        // income만 불러오기
        myRef.child("income").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int value = (int)snapshot.getValue(Integer.class);
                income = value;
                textView_income.setText(income + " 원");
                ele = income / 200 ;                           // 총수입 -> 전력사용량
                textView_ele.setText(ele + " kWh ");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


                // Listview1 차량번호
                myRef.child("UHistory").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                        dataAdapter_1.clear();
                        for (DataSnapshot snapshot_A : dataSnapshot.getChildren()) {
                            for (DataSnapshot snapshot_B : snapshot_A.getChildren()) {
                                Object value = snapshot_A.getKey();
                                dataAdapter_1.add(value);
                            }
                        }
                        dataAdapter_1.notifyDataSetChanged();
                        listView_1.setSelection(dataAdapter_1.getCount() - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    }
                });


        // Listview2 충전날짜
        myRef.child("UHistory").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                dataAdapter_2.clear();
                for (DataSnapshot snapshot_A : dataSnapshot.getChildren()) {
                    for (DataSnapshot snapshot_B : snapshot_A.getChildren()){
                        Object value = snapshot_B.getKey();
                        dataAdapter_2.add(value);
                    }
                }
                dataAdapter_2.notifyDataSetChanged();
                listView_2.setSelection(dataAdapter_2.getCount() - 1);
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });


        // Listview3 사용포인트
        myRef.child("UHistory").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                dataAdapter_3.clear();
                for (DataSnapshot snapshot_A : dataSnapshot.getChildren()) {
                    for (DataSnapshot snapshot_B : snapshot_A.getChildren()){
                        Object value = snapshot_B.getValue();
                        dataAdapter_3.add(value);
                    }
                }
                dataAdapter_3.notifyDataSetChanged();
                listView_3.setSelection(dataAdapter_3.getCount() - 1);
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });





    }


}