package edu.skku.map.movier;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    private EditText idInput;
    private EditText passwordInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginButton = findViewById(R.id.login_login_button);
        LinearLayout signUpLayout = findViewById(R.id.login_signup_layout);

        idInput = findViewById(R.id.login_id_input);
        passwordInput = findViewById(R.id.login_password_input);

        progressDialog = new ProgressDialog(LoginActivity.this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idInput.getText().toString().equals("")) {
                    // id 입력부분이 공백일 때
                    new AlertDialog.Builder(LoginActivity.this)
                            .setMessage("아이디를 써주세요")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {}
                            }).show();
                } else if (passwordInput.getText().toString().equals("")) {
                    // 비밀번호 입력부분이 공백일 때
                    new AlertDialog.Builder(LoginActivity.this)
                            .setMessage("비밀번호를 써주세요")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {}
                            }).show();
                } else {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                    // 데이터베이스와 연결하는데 지연되는 시간동안 dialog를 표시
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setMessage("잠시만 기다려주세요");
                    progressDialog.show();

                    databaseReference.child(UserAccountPost.ACCOUNT_TABLE_NAME).child(idInput.getText().toString())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    UserAccountPost post = dataSnapshot.getValue(UserAccountPost.class);

                                    if (post != null) {
                                        // 데이터베이스에 같은 id가 존재할 때
                                        if (post.password.equals(passwordInput.getText().toString())) {
                                            // 비밀번호가 데이터베이스의 비밀번호와 같을 때
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                                            intent.putExtra("account", post);

                                            startActivity(intent);
                                            overridePendingTransition(R.anim.do_nothing, R.anim.slide_to_bottom);
                                        } else {
                                            // 비밀번호가 데이터베이스의 비밀번호와 다를 때
                                            new AlertDialog.Builder(LoginActivity.this)
                                                    .setMessage("비밀번호가 다르네요")
                                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {}
                                                    }).show();
                                        }
                                    } else {
                                        // 데이터베이스에 같은 id가 존재하지 않을 때
                                        new AlertDialog.Builder(LoginActivity.this)
                                                .setMessage("그런 아이디가 없네요")
                                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {}
                                                }).show();
                                    }

                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }
            }
        });

        signUpLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);

                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_bottom, android.R.anim.fade_out);
            }
        });
    }
}
