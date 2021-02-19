package com.neeraj.users;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


public class AddUserFragment extends Fragment {




    public AddUserFragment() {
        // Required empty public constructor
    }
    public static final int PICK_IMAGE_REQUEST = 1;
    Uri mImageUri;
    private EditText first_name_edit;
    private EditText last_name_edit;
    private TextView dob_edit;
    private EditText country_edit;
    private EditText state_edit;
    private EditText home_edit;
    private EditText phone_Edit;
    private Button select_date;
    private Spinner gender_spinner;
    private Button save_button;
    private CircleImageView profile_image;
    private FloatingActionButton floatingActionButton;



    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private String gender;
    private String dob;

    private ProgressDialog progressDialog;

    private int count = 0;
    SharedPreferences preferences;
    SharedPreferences.Editor sharedEditor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_add_user, container, false);

        first_name_edit = rootView.findViewById(R.id.first_name_edit);
        last_name_edit = rootView.findViewById(R.id.last_name_edit);
        dob_edit = rootView.findViewById(R.id.in_date);
        select_date = rootView.findViewById(R.id.btn_date);
        gender_spinner = rootView.findViewById(R.id.spinner_gender);
        country_edit = rootView.findViewById(R.id.country_edit);
        state_edit = rootView.findViewById(R.id.state_edit);
        home_edit = rootView.findViewById(R.id.home_town_edit);
        phone_Edit = rootView.findViewById(R.id.phone_edit);
        save_button = rootView.findViewById(R.id.register_user);
        floatingActionButton = rootView.findViewById(R.id.fab);
        profile_image = rootView.findViewById(R.id.profile_picture);

        ArrayAdapter<CharSequence> spin_gender = ArrayAdapter.createFromResource(getActivity(), R.array.gender,R.layout.support_simple_spinner_dropdown_item);
        gender_spinner.setAdapter(spin_gender);

        gender_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gender = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenImageChooser();
            }
        });


        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        databaseReference.child("count").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                count = Integer.parseInt(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        select_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                dob_edit.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                dob = year+"";

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ph = phone_Edit.getText().toString();
                Query query = databaseReference.child("users").orderByChild("phone").equalTo(ph);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.exists()){
                            saveUser();

                        }else{
                            Toast.makeText(getContext(), "Phone number Already Exists. Try Another !", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        return rootView;
    }

    private void saveUser(){
        String first_name = first_name_edit.getText().toString().trim();
        String last_name = last_name_edit.getText().toString().trim();
        String country = country_edit.getText().toString().trim();
        String state = state_edit.getText().toString().trim();
        String home = home_edit.getText().toString().trim();
        String phone = phone_Edit.getText().toString().trim();
        String dob = dob_edit.getText().toString().trim();

        if(first_name.length()==0){
            Toast.makeText(getActivity(), "Please Enter first name", Toast.LENGTH_SHORT).show();
            return;

        }
        if(last_name.length()==0){
            Toast.makeText(getActivity(), "Please Enter  LAST Name", Toast.LENGTH_SHORT).show();
            return;
        }
        if(dob.length()==0){

            Toast.makeText(getActivity(), "Please Select Date of Birth", Toast.LENGTH_SHORT).show();
            return;
        }
        if(country.length()==0){
            Toast.makeText(getActivity(), "Please Enter Country Name", Toast.LENGTH_SHORT).show();

            return;
        }
        if(state.length()==0){
            Toast.makeText(getActivity(), "Please Enter State Name", Toast.LENGTH_SHORT).show();

            return;
        }
        if(home.length()==0){
            Toast.makeText(getActivity(), "Please Enter HomeTown Name", Toast.LENGTH_SHORT).show();

            return;
        }
        if(phone.length()==0){
            Toast.makeText(getActivity(), "Please Enter Phone Number", Toast.LENGTH_SHORT).show();

            return;
        }
        if(phone.length()!=10){
            Toast.makeText(getActivity(), "Please Enter 10 digit Phone Number", Toast.LENGTH_SHORT).show();

            return;
        }

        count += 1;
        StorageReference filepath = storageReference.child(""+count).child("image");
        if (mImageUri==null){
            Toast.makeText(getActivity(), "Please Upload Profile Image", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Saving user...");
        progressDialog.show();
        filepath.putFile(mImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String profie = uri.toString();
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("first_name", first_name);
                            hashMap.put("last_name", last_name);
                            hashMap.put("dob",dob);
                            hashMap.put("gender",gender);
                            hashMap.put("country",country);
                            hashMap.put("state",state);
                            hashMap.put("home",home);
                            hashMap.put("phone",phone);
                            hashMap.put("count",count);
                            hashMap.put("profile",profie);

                            HashMap<String,Object>objectHashMap = new HashMap<>();
                            objectHashMap.put("count",count);

                            databaseReference.child("users").child(""+count).updateChildren(hashMap);
                            databaseReference.updateChildren(objectHashMap);

                            Toast.makeText(getActivity(), "User Saved", Toast.LENGTH_LONG).show();
                            dataRemove();
                            progressDialog.dismiss();



                        }
                    });
                }
            }
        });


    }

    public  void OpenImageChooser(){
        Intent intent = new Intent();

        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), mImageUri);
                profile_image.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    private byte[] fromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return outputStream.toByteArray();
    }
    private void dataRemove(){
        first_name_edit.setText("");
        last_name_edit.setText("");
        dob_edit.setText("");
        dob = "";
        country_edit.setText("");
        state_edit.setText("");
        home_edit.setText("");
        phone_Edit.setText("");
        profile_image.setImageResource(R.drawable.account_circle_black_24dp);
        mImageUri = null;
    }
}