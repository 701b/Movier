package edu.skku.map.movier;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUpActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    private EditText idInput;
    private EditText passwordInput;
    private Button manButton;
    private Button womanButton;
    private Button signUpButton;
    private TextView idAlertText;
    private TextView passwordAlertText;

    private boolean isMan = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ImageButton cancleButton = findViewById(R.id.signup_cancle_button);
        TextView loginText = findViewById(R.id.signup_login_text);

        idInput = findViewById(R.id.signup_id_input);
        passwordInput = findViewById(R.id.signup_password_input);
        manButton = findViewById(R.id.signup_man_button);
        womanButton = findViewById(R.id.signup_woman_button);
        signUpButton = findViewById(R.id.signup_signup_button);
        idAlertText = findViewById(R.id.signup_id_alert_text);
        passwordAlertText = findViewById(R.id.signup_password_alert_text);

        progressDialog = new ProgressDialog(SignUpActivity.this);

        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent);
            }
        });

        idInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!idInput.getText().toString().equals("") && !passwordInput.getText().toString().equals("")) {
                    signUpButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.white_rounded_red_bordered_background));
                    signUpButton.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    signUpButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.white_rounded_grey_bordered_background));
                    signUpButton.setTextColor(Color.parseColor("#BBBBBB"));
                }

                idAlertText.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!passwordInput.getText().toString().equals("") && !idInput.getText().toString().equals("")) {
                    signUpButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.white_rounded_red_bordered_background));
                    signUpButton.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    signUpButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.white_rounded_grey_bordered_background));
                    signUpButton.setTextColor(Color.parseColor("#BBBBBB"));
                }

                passwordAlertText.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        manButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMan = true;

                manButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.white_rounded_red_bordered_background));
                manButton.setTextColor(Color.parseColor("#FF0000"));
                womanButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.white_rounded_grey_bordered_background));
                womanButton.setTextColor(Color.parseColor("#BBBBBB"));
            }
        });

        womanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMan = false;

                womanButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.white_rounded_red_bordered_background));
                womanButton.setTextColor(Color.parseColor("#FF0000"));
                manButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.white_rounded_grey_bordered_background));
                manButton.setTextColor(Color.parseColor("#BBBBBB"));
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idInput.getText().toString().equals("")) {
                    // id 입력부분이 공백일 때
                    idAlertText.setText("아이디를 작성해주세요");
                }

                if (passwordInput.getText().toString().equals("")) {
                    // 비밀번호 입력부분이 공백일 때
                    passwordAlertText.setText("비밀번호를 작성해주세요");
                }

                if (!passwordInput.getText().toString().equals("") && !idInput.getText().toString().equals("")) {
                    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                    // 데이터베이스와 연결하는데 지연되는 시간동안 dialog를 표시
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setMessage("잠시만 기다려주세요");
                    progressDialog.show();

                    databaseReference.child(UserAccountPost.ACCOUNT_TABLE_NAME).child(idInput.getText().toString())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    UserAccountPost post = dataSnapshot.getValue(UserAccountPost.class);

                                    if (post == null) {
                                        // 데이터베이스에 같은 id가 존재하지 않을 때
                                        UserAccountPost newAccountPost= new UserAccountPost(
                                                idInput.getText().toString(),
                                                passwordInput.getText().toString(),
                                                isMan);

                                        newAccountPost.postFirebaseDatabase(databaseReference);
                                        finish();
                                        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_to_bottom);
                                    } else {
                                        // 데이터베이스에 같은 id가 존재할 때
                                        idAlertText.setText("다른 아이디를 사용해주세요");
                                    }

                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {}
                            });
                }
            }
        });

        cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_to_bottom);
            }
        });
    }

}
