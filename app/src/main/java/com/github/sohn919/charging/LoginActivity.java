package com.github.sohn919.charging;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class LoginActivity extends AppCompatActivity {

        Button  mLoginBtn;
        TextView mResigettxt;
        EditText mEmailText, mPasswordText;

        private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        private DatabaseReference myRef = mDatabase.getReference();
        private FirebaseAuth firebaseAuth;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);


            firebaseAuth =  FirebaseAuth.getInstance();
            //버튼 등록하기
            mResigettxt = findViewById(R.id.register_t2);
            mLoginBtn = findViewById(R.id.login_btn);
            mEmailText = findViewById(R.id.emailEt);
            mPasswordText = findViewById(R.id.passwordEdt);


            //가입 버튼이 눌리면
            mResigettxt.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    //intent함수를 통해 register액티비티 함수를 호출한다.
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class));

                }
            });

            //로그인 버튼이 눌리면
            mLoginBtn.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    String email = mEmailText.getText().toString().trim();
                    String pwd = mPasswordText.getText().toString().trim();
                    firebaseAuth.signInWithEmailAndPassword(email,pwd)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        myRef.child("charge").setValue(0);
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);

                                    }else{
                                        Toast.makeText(LoginActivity.this,"로그인 오류",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
            });
        }

}

