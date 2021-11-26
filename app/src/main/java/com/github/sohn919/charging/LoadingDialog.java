package com.github.sohn919.charging;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;


public class LoadingDialog extends Dialog {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = mDatabase.getReference();

    Button button1, button2;
    Context context;
    private ChargingDialog chargingDialog;
    private AlertDialog alertDialog;
    private int inum;

    LoadingDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

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

        requestWindowFeature(Window.FEATURE_NO_TITLE);   //다이얼로그의 타이틀바를 없애주는 옵션입니다.
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));  //다이얼로그의 배경을 투명으로 만듭니다.
        getWindow().setGravity(Gravity.TOP);

        setContentView(R.layout.dialog_loading);     //다이얼로그에서 사용할 레이아웃입니다.

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(context);
                dlg.setTitle("고장신고"); //제목
                final String[] versionArray = new String[] {"어댑터 외부가 망가졌어요","충전이 안돼요","NFC카드가 인식이 안돼요"};

                inum = 0;
                dlg.setSingleChoiceItems(versionArray, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inum = which;
                    }
                });
//                버튼 클릭시 동작
                dlg.setPositiveButton("확인",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        myRef.child("Breakdown").child(getTime()).setValue(versionArray[inum]);
                        //토스트 메시지
                        Toast.makeText(context, "신고접수 되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.show();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics(); //디바이스 화면크기를 구하기위해
                int width = dm.widthPixels; //디바이스 화면 너비
                int height = dm.heightPixels; //디바이스 화면 높이
                chargingDialog = new ChargingDialog(getContext());
                WindowManager.LayoutParams wm = chargingDialog.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
                wm.copyFrom(chargingDialog.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
                wm.width = (int)(width *0.5);  //화면 너비의 절반
                wm.height = (int)(height *0.5);
                chargingDialog.show();
                dismiss();
            }
        });

    }



}

