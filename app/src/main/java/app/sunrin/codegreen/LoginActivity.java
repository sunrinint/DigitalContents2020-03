package app.sunrin.codegreen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    String[][] finalValue;
    String[] newValue;
    int idIndex;

    FirebaseDatabase database;
    DatabaseReference myRef;
    SharedPreferences preferencesGetLogined, preferencesSaveAll, preferencesID, preferencesPW;

    TextInputLayout id_Layout, pw_Layout;
    TextInputEditText id_ET, pw_ET;
    MaterialButton sign_in_btn, sign_up_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        preferencesGetLogined = getSharedPreferences("getLogined", 0);
        preferencesSaveAll = getSharedPreferences("saveAll", 0);
        preferencesID = getSharedPreferences("ID", 0);
        preferencesPW = getSharedPreferences("PW", 0);
        Boolean getLogined = this.preferencesGetLogined.getBoolean("getLogined", false);

        if(getLogined)
        {
            Toast.makeText(LoginActivity.this, "자동 로그인 되었습니다!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        else
        {
            id_Layout = findViewById(R.id.TextInput_ID);
            pw_Layout = findViewById(R.id.TextInput_PW);
            id_ET = findViewById(R.id.edit_id);
            pw_ET = findViewById(R.id.edit_pw);
            sign_in_btn = findViewById(R.id.signInBtn);
            sign_up_btn = findViewById(R.id.signUpBtn);


            database = FirebaseDatabase.getInstance();
            myRef = database.getReference("user");
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    String value = dataSnapshot.getValue().toString();
                    System.out.println(value);
                    value = value.substring(0, value.length()-2);
                    value = value.replace("}, ", "☆");
                    //굳이 별로 바꾼 이유는 split에서 정규식을 사용하기 때문에 이렇게 안 하면 에러가 발생하기 때문
                    //자세한 것은 이 링크 참고 : https://mytory.net/archives/285
                    System.out.println(value);


                    newValue = value.split("☆");
                    System.out.println(newValue.length);


                    finalValue = new String[newValue.length][3];

                    for(int i = 0; i<newValue.length; i++)
                    {
                        finalValue[i][0] = newValue[i].substring(newValue[i].indexOf("D=")+2, newValue[i].lastIndexOf(", "));
                        finalValue[i][1] = newValue[i].substring(newValue[i].indexOf("W=")+2, newValue[i].indexOf(", "));
                        finalValue[i][2] = newValue[i].substring(newValue[i].indexOf("e=")+2);

                        System.out.println("가" + finalValue[i][0]);
                        System.out.println("나" + finalValue[i][1]);
                        System.out.println("다" + finalValue[i][2]);

                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    System.out.println("ERROR");

                }
            });


            sign_in_btn.setOnClickListener(v -> {
                String input_id = id_ET.getText().toString().replaceAll(" ", "");
                String input_pw = pw_ET.getText().toString().replaceAll(" ", "");  // 공백 처리


                boolean idExist = false;

                for(int i = 0; i<newValue.length; i++)
                {
                    if(finalValue[i][0].equals(input_id)){
                        idExist = true;
                        idIndex = i;
                        break;
                    }

                }



                if (!idExist)
                {   // ID가 존재하지 않을 때
                    id_Layout.setError("'" + input_id + "' is not existed");
                }
                else
                {
                    id_Layout.setErrorEnabled(false);
                    if (finalValue[idIndex][1].equals(input_pw))
                    {  // 두 String 비교
                        pw_Layout.setErrorEnabled(false);
                        Toast.makeText(LoginActivity.this, "환영합니다!", Toast.LENGTH_SHORT).show();
                        this.preferencesGetLogined.edit().putBoolean("getLogined", true).commit();


                        SharedPreferences.Editor editorID = preferencesID.edit();
                        editorID.putString("id", input_id);
                        editorID.commit();

                        SharedPreferences.Editor editorPW = preferencesPW.edit();
                        editorPW.putString("pw", input_pw);
                        editorPW.commit();

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("user/"+input_id+"/value");
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                SharedPreferences.Editor editorSaveAll = preferencesSaveAll.edit();
                                editorSaveAll.putString("saveAll", snapshot.getValue(String.class));
                                editorSaveAll.commit();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                        Intent intent = new Intent(LoginActivity.this, SplashActivity.class);
                        startActivity(intent);
                        finish();



                    }
                    else{
                        pw_Layout.setError("비밀번호가 다릅니다.");
                    }
                }
                if (input_id.length() <= 0)
                    id_Layout.setError("아이디를 입력해주세요.");
                if (input_pw.length() <= 0)
                    pw_Layout.setError("비밀번호를 입력해주세요.");
            });

            // sign up 버튼 누르면 화면 이동
            sign_up_btn.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                finish();
            });
        }



    }
}