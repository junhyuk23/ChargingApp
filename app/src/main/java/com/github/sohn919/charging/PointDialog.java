package com.github.sohn919.charging;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;


public class PointDialog extends Dialog {

    private int point = 0;
    Button button, button1, button2, button3, button4, button5;
    TextView pointtext;
    Context context;
    private int stuck = 10;

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = mDatabase.getReference();
    private FirebaseAuth firebaseAuth;

    public PointDialog(@NonNull Context context) {
        super(context);
    }

    protected void onCreate(Bundle savedInstanceStare) {
        super.onCreate(savedInstanceStare);

        requestWindowFeature(Window.FEATURE_NO_TITLE);   //다이얼로그의 타이틀바를 없애주는 옵션입니다.
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));  //다이얼로그의 배경을 투명으로 만듭니다.
        getWindow().setGravity(Gravity.BOTTOM);

        setContentView(R.layout.dialog_point);     //다이얼로그에서 사용할 레이아웃입니다.
        // 초기설정 - 해당 프로젝트(안드로이드)의 application id 값을 설정합니다. 결제와 통계를 위해 꼭 필요합니다.
        // 앱에서 확인하지 말고 꼭 웹 사이트에서 확인하자. 앱의 application id 갖다 쓰면 안됨!!!
//        BootpayAnalytics.init(this, "61910e247b5ba4b3a352b0d0");

        //포인트 충전 탭
        button = findViewById(R.id.test);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);
        pointtext = findViewById(R.id.pointtext2);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        //포인트 탭 버튼
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                point = 1000;
                pointtext.setText(Integer.toString(point));
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                point = 5000;
                pointtext.setText(Integer.toString(point));
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                point = 10000;
                pointtext.setText(Integer.toString(point));
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                point = 50000;
                pointtext.setText(Integer.toString(point));
            }
        });

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                point = 100000;
                pointtext.setText(Integer.toString(point));
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext().getApplicationContext(), PayActivity.class);
                intent.putExtra("point", point);
                getContext().startActivity(intent);
            }
        });

    }

}

