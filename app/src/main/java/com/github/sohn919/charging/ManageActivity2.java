package com.github.sohn919.charging;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ManageActivity2 extends AppCompatActivity {


    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
    private DatabaseReference myRef = mDatabase.getReference(); // DB 테이블 연결
    private FirebaseAuth firebaseAuth;

    private ListView listView_4;
    private ListView listView_5;

    private ArrayAdapter<Object> dataAdapter_4;
    private ArrayAdapter<Object> dataAdapter_5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage2);


        listView_4 = (ListView) findViewById(R.id.listView_4);
        listView_5 = (ListView) findViewById(R.id.listView_5);

        dataAdapter_4 = new ArrayAdapter<Object>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<Object>());
        dataAdapter_5 = new ArrayAdapter<Object>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<Object>());

        listView_4.setAdapter(dataAdapter_4);
        listView_5.setAdapter(dataAdapter_5);




        // Listview4 신고날짜
        myRef.child("Breakdown").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                dataAdapter_4.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Object value = snapshot.getKey();
                    dataAdapter_4.add(value);
                }
                dataAdapter_4.notifyDataSetChanged();
                listView_4.setSelection(dataAdapter_4.getCount() - 1);
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        // Listview5 신고사유
        myRef.child("Breakdown").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                dataAdapter_5.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Object value = snapshot.getValue();
                    dataAdapter_5.add(value);
                }
                dataAdapter_5.notifyDataSetChanged();
                listView_5.setSelection(dataAdapter_5.getCount() - 1);
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });


    }
}