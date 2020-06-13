package edu.skku.map.movier;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUpActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;

    private ProgressDialog progressDialog;

    private ScrollView scrollView;
    private EditText idInput;
    private EditText passwordInput;
    private Button manButton;
    private Button womanButton;
    private Button signUpButton;
    private TextView idAlertText;
    private TextView passwordAlertText;
    private LinearLayout loginLayout;
    private TextView introductionText1;
    private TextView introductionText2;

    private boolean isMan = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ImageButton cancleButton = findViewById(R.id.signup_cancle_button);
        TextView greetingText = findViewById(R.id.signup_greeting_text);

        scrollView = findViewById(R.id.signup_scroll_view);
        idInput = findViewById(R.id.signup_id_input);
        passwordInput = findViewById(R.id.signup_password_input);
        manButton = findViewById(R.id.signup_man_button);
        womanButton = findViewById(R.id.signup_woman_button);
        signUpButton = findViewById(R.id.signup_signup_button);
        idAlertText = findViewById(R.id.signup_id_alert_text);
        passwordAlertText = findViewById(R.id.signup_password_alert_text);
        loginLayout = findViewById(R.id.signup_login_layout);
        introductionText1 = findViewById(R.id.signup_introduction_text1);
        introductionText2 = findViewById(R.id.signup_introduction_text2);

        progressDialog = new ProgressDialog(SignUpActivity.this);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        greetingText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_from_left));

        introductionText1.setVisibility(View.INVISIBLE);
        introductionText2.setVisibility(View.INVISIBLE);
        loginLayout.setVisibility(View.INVISIBLE);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {}

                SignUpActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        introductionText1.setVisibility(View.VISIBLE);
                        introductionText2.setVisibility(View.VISIBLE);
                        loginLayout.setVisibility(View.VISIBLE);
                        introductionText1.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_from_left));
                        introductionText2.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_from_left));
                        loginLayout.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_from_left));
                    }
                });
            }
        });

        loginLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLoginPage();
            }
        });

        idInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollView.smoothScrollTo(0, idInput.getTop());
            }
        });

        passwordInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollView.smoothScrollTo(0, passwordInput.getTop());
            }
        });

        idInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                idAlertText.setText("");

                if (!s.toString().equals("")) {
                    if (!s.toString().contains(".") && !s.toString().contains(" ") && !s.toString().contains("$") && !s.toString().contains("#") && !s.toString().contains("[") && !s.toString().contains("]")) {
                        databaseReference.child(UserAccountPost.ACCOUNT_TABLE_NAME).child(idInput.getText().toString())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        UserAccountPost post = dataSnapshot.getValue(UserAccountPost.class);

                                        if (post != null) {
                                            // 데이터베이스에 같은 id가 존재할 때
                                            if (!post.getId().equals(s.toString())) {
                                                // 빠른 입력으로 어긋나는 현상 방지
                                                return;
                                            }

                                            idAlertText.setText("다른 아이디를 사용해주세요");
                                            signUpButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.white_rounded_grey_bordered_background));
                                            signUpButton.setTextColor(Color.parseColor("#BBBBBB"));
                                        } else {
                                            if (!idInput.getText().toString().equals("") && !passwordInput.getText().toString().equals("")) {
                                                signUpButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.white_rounded_red_bordered_background));
                                                signUpButton.setTextColor(Color.parseColor("#FF0000"));
                                            } else {
                                                signUpButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.white_rounded_grey_bordered_background));
                                                signUpButton.setTextColor(Color.parseColor("#BBBBBB"));
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                    } else {
                        idAlertText.setText("'.', '#', '$', '[', ']'과 공백은 사용할 수 없습니다");
                        signUpButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.white_rounded_grey_bordered_background));
                        signUpButton.setTextColor(Color.parseColor("#BBBBBB"));
                    }
                }
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

                                        newAccountPost.postFirebaseDatabase();

                                        goToLoginPage();
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
                goToLoginPage();
            }
        });
    }

    private void goToLoginPage() {
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_to_bottom);
    }

}
