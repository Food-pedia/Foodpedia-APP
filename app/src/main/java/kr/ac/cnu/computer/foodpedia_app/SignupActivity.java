package kr.ac.cnu.computer.foodpedia_app;

import android.os.Bundle;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;


public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        findViewById(R.id.check).setOnClickListener(onClickListener);
    }


    View.OnClickListener onClickListener = v -> {
        switch (v.getId()) {
            case R.id.check:
                signUp();
                break;
        }
    };


    private void signUp() {
        String name = ((EditText) findViewById(R.id.user_name)).getText().toString();
        String id = ((EditText) findViewById(R.id.user_id)).getText().toString(); // 이메일
        String password = ((EditText) findViewById(R.id.user_id)).getText().toString();
        String passwordCheck = ((EditText) findViewById(R.id.user_id)).getText().toString();

        if (id.length() > 0 && password.length() > 0 && passwordCheck.length() > 0) {
            if (password.equals(passwordCheck)) {
                mAuth.createUserWithEmailAndPassword(id, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "회원가입에 성공했습니다.", Toast.LENGTH_SHORT).show();
                                    addUserToDB(id, name);
                                    ((GlobalApplication) getApplication()).setBasicEmail(id);
                                    ((GlobalApplication) getApplication()).setBasicName(name);
                                } else {
                                    if (task.getException().toString() != null) {
//                                        Toast.makeText(SignupActivity.this, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                        Toast.makeText(SignupActivity.this, "이미 존재하는 계정입니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
            } else {
                Toast.makeText(SignupActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(SignupActivity.this, "아이디와 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addUserToDB(String id, String name) {
        databaseReference.child("EMAIL").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> map = (Map<String, Object>) snapshot.getValue();

                if (map != null) {
                    // Toast.makeText(getApplicationContext(),"이미 존재하는 그룹명입니다.",Toast.LENGTH_SHORT).show();//토스메세지 출력
                } else {
                    String delEmail = id.substring(0, id.indexOf(".")) + "@" + id.substring(id.indexOf(".") + 1);
                    databaseReference.child("EMAIL").child(delEmail).child("NAME").setValue(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 디비를 가져오던중 에러 발생 시
                //Log.e("MainActivity", String.valueOf(databaseError.toException())); // 에러문 출력
            }
        });
    }
}