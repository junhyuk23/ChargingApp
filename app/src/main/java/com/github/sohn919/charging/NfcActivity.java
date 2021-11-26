package com.github.sohn919.charging;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class NfcActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = mDatabase.getReference();

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private TextView tagDesc;
    Context context;

    private int db_count = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        //tagDesc = (TextView)findViewById(R.id.tagDesc);

        firebaseAuth = FirebaseAuth.getInstance();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //NFC DB 순서 불러오기
        myRef.child("count").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                db_count = (int) snapshot.getValue(Integer.class);
                Log.e("db카운트 값 : ",""+db_count);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });




        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter == null){
            Toast.makeText(this,
                    "NFC를 지원하지 않는 기기입니다.",
                    Toast.LENGTH_LONG).show();
            finish();
        }else if(!nfcAdapter.isEnabled()){
            Toast.makeText(this,
                    "NFC를 활성화 시켜주세요",
                    Toast.LENGTH_LONG).show();
            finish();
        }

    }



    @Override
    protected void onPause() {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
        super.onPause();
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            byte[] tagId = tag.getId();

            FirebaseUser user = firebaseAuth.getCurrentUser();


            myRef.child("Users").child(user.getUid()).child("nfc").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    db_count += 1;

                    myRef.child("nfc").child(Integer.toString(db_count)).child("nfc").setValue(toHexString(tagId));
                    myRef.child("nfc").child(Integer.toString(db_count)).child("uid").setValue(user.getUid());
                    myRef.child("count").setValue(db_count);

                    myRef.child("Users").child(user.getUid()).child("nfc").setValue(toHexString(tagId));
                    Toast.makeText(NfcActivity.this,"NFC카드가 등록되었습니다.",Toast.LENGTH_SHORT).show();
                    finish();

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

        }
    }


    public static final String CHARS = "0123456789ABCDEF";
    public static String toHexString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; ++i) {
            sb.append(CHARS.charAt((data[i] >> 4) & 0x0F))
                    .append(CHARS.charAt(data[i] & 0x0F));
        }
        return sb.toString();
    }
}

