package com.neeraj.users;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;


public class UsersFragment extends Fragment {


    public UsersFragment() {
        // Required empty public constructor
    }

    private ProgressDialog progressDialog;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview =  inflater.inflate(R.layout.fragment_users, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        recyclerView = rootview.findViewById(R.id.all_user);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(true);
        return rootview;
    }

    @Override
    public void onStart() {
        super.onStart();
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Query query = databaseReference.child("users").orderByChild("count");


        FirebaseRecyclerOptions<User> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User.class).build();
        FirebaseRecyclerAdapter<User, RecyclerviewHolder>adapter = new FirebaseRecyclerAdapter<User, RecyclerviewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull RecyclerviewHolder recyclerviewHolder, int i, @NonNull User user) {
                String year = new SimpleDateFormat("yyyy").format(new Date());
                int int_year = Integer.parseInt(year);
                String str = user.getDob();
                int len = str.length();
                String temp = str.substring(len-4,len);
                int data_year = Integer.parseInt(temp);
                int age = int_year - data_year;
                String image_url = user.getProfile();
                Picasso.with(getContext()).load(image_url).into(recyclerviewHolder.imageView);
                recyclerviewHolder.nameText.setText(user.getFirst_name() + " " + user.getLast_name());
                recyclerviewHolder.descText.setText(user.getGender() +" | " + age + " | " + user.getHome());
                recyclerviewHolder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        databaseReference.child("users").child(user.getCount()+"").removeValue();
                    }
                });
                progressDialog.dismiss();
            }

            @NonNull
            @Override
            public RecyclerviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item,parent, false);
                return new RecyclerviewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    public static class RecyclerviewHolder extends RecyclerView.ViewHolder {
        CircleImageView imageView;
        TextView nameText;
        TextView descText;
        ImageButton delete;

        public RecyclerviewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            nameText = itemView.findViewById(R.id.name);
            descText = itemView.findViewById(R.id.desc);
            delete = itemView.findViewById(R.id.delete);
        }

    }
}