package com.example.pointtopoint;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;


public class OrderConfirmationActivity extends AppCompatActivity {
    private TextView ordertype,fullprice,customertype;
    private EditText pickuplocation,droplocation;
    private Button Placeorder;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private String UserID;
    private String auserfullname,ausername,auserid,ausernumber,auseremail;
    private String usertype;


    private String ordertypebundle;
    private String price;
    private String dlat,dlng;
    private String plat,plng;





    protected String Getaddress(double latitude, double longitude) {
        StringBuffer msg = new StringBuffer();



        //call GeoCoder getFromLocation to get address
        //returns list of addresses, take first one and send info to result receiver
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1);
        } catch (Exception ioException) {
            Timber.e("Error in getting address for the location");
        }

        if (addresses == null || addresses.size() == 0) {
            msg.append("No address found for the location");

        } else {
            Address address = addresses.get(0);
            msg.append(address.getFeatureName());
            msg.append(",");
            msg.append(address.getThoroughfare());
            msg.append(",");
            msg.append(address.getLocality());
            msg.append(",");
            msg.append(address.getSubAdminArea());

        }
        return msg.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        ordertype = findViewById(R.id.ordertype);
        fullprice= findViewById(R.id.fullprice);
        customertype=findViewById(R.id.customertype);
        pickuplocation= findViewById(R.id.etpickup);
        droplocation= findViewById(R.id.etdrop);
        Placeorder=findViewById(R.id.confirmorder);


        firebaseAuth = FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();
        UserID=firebaseAuth.getCurrentUser().getUid();

        Bundle b = getIntent().getExtras();
        ordertypebundle= b.getString("ordertype");
        price= b.getString("price");
        dlat=b.getString("dlat");
        dlng=b.getString("dlng");
        plat=b.getString("plat");
        plng=b.getString("plng");



        /*Intent intent = getIntent();
        final String aordertype = intent.getStringExtra("ordertype");
        final String afullprice = intent.getStringExtra("price");*/

        String pick = Getaddress(Double.parseDouble(plat),Double.parseDouble(plng));
        String drop = Getaddress(Double.parseDouble(dlat),Double.parseDouble(dlng));

        pickuplocation.setText(pick);
        droplocation.setText(drop);
        ordertype.setText(ordertypebundle);

        DocumentReference docref=db.collection("users").document(UserID);

        docref.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                auserfullname=documentSnapshot.getString("name");
                ausername= documentSnapshot.getString("Username");
                ausernumber=documentSnapshot.getString("Mobilenumber");
                auseremail=documentSnapshot.getString("Email");
                usertype=documentSnapshot.getString("CustomerType");


                customertype.setText(documentSnapshot.getString("CustomerType"));

                String comp=customertype.getText().toString();

                if(comp.equals("Regular")){
                    double tempprice = Double.parseDouble(price);
                    tempprice *= (0.8);
                    price=Double.toString(tempprice);
                    fullprice.setText("To be paid:Rs " + price + "(discount applied)");
                }
                else{
                    fullprice.setText("To be paid:Rs " + price);
                }

            }
        });




        Placeorder.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                String newpick=pickuplocation.getText().toString();
                String newdrop=droplocation.getText().toString();

                Map<String, Object> order = new HashMap<>();
                order.putIfAbsent("orderstatus","pending");
                order.putIfAbsent("userid",UserID);
                order.putIfAbsent("ordertype",ordertypebundle);
                order.putIfAbsent("price",price);
                order.putIfAbsent("userfullname",auserfullname);
                order.putIfAbsent("username",ausername);
                order.putIfAbsent("useremail",auseremail);
                order.putIfAbsent("usernumber",ausernumber);
                order.putIfAbsent("droplat",dlat);
                order.putIfAbsent("droplong",dlng);
                order.putIfAbsent("pickuplat",plat);
                order.putIfAbsent("pickuplong",plng);
                order.putIfAbsent("pickaddr",newpick);
                order.putIfAbsent("dropaddr",newdrop);


                db.collection("orders").add(order).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String passorderid=documentReference.getId();
                        Map<String, Object> usermap = new HashMap<>();
                        usermap.putIfAbsent("orderid",passorderid);
                        db.collection("orders").document(passorderid).update(usermap);

                        Toast.makeText(OrderConfirmationActivity.this, "Order placed.Wait for OTP", Toast.LENGTH_SHORT).show();
                        finish();
                        Intent intent = new Intent(getApplicationContext(), UserOtpActivity.class);
                        intent.putExtra("orderid",passorderid);
                        startActivity(intent);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OrderConfirmationActivity.this, "Order cannot be placed", Toast.LENGTH_SHORT).show();
                    }
                });

                //Toast.makeText(OrderConfirmationActivity.this, "Order placed", Toast.LENGTH_SHORT).show();

                /*finish();
                startActivity(new Intent(OrderViewActivity.this,ItemListActivity.class));*/
            }
        });






    }
}
