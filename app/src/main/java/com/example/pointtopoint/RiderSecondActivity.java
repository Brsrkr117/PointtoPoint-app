package com.example.pointtopoint;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RiderSecondActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private Button signout;
    private Button Viewprofile;
    private Button Vieworders;
    private EditText radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_second);


        firebaseAuth = FirebaseAuth.getInstance();
        signout=(Button)findViewById(R.id.buttonRsignout);
        Viewprofile=(Button) findViewById(R.id.btnviewprof);
        Vieworders=(Button) findViewById(R.id.btnViewOrder);
        radius=(EditText) findViewById(R.id.etradius);

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logout();
            }
        });

        Viewprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(RiderSecondActivity.this, RiderProfileViewActivity.class));
            }
        });

        Vieworders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rad=radius.getText().toString();
                //finish();
                Intent intent = new Intent(getApplicationContext(), RiderViewOrderActivity.class);
                //intent.putExtra("radius",rad);
                //finish();
                startActivity(intent);
            }
        });


    }
    private void Logout(){
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(RiderSecondActivity.this, LoginActivity.class));
    }
}
