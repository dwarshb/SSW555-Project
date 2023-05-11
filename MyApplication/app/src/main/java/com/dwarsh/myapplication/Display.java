package com.dwarsh.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class Display extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    TextView titleTextView,descriptionTextView,deadlineTextView;
    private static final String TAG = "Display";

    RecyclerView membersrecyclerView, managersrecyclerView;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private final ArrayList<Uri> imageUris = new ArrayList<>();
    private final ArrayList<User> mUsers = new ArrayList<>();
    private final ArrayList<User> managers = new ArrayList<>();
    private final ArrayList<User> workers = new ArrayList<>();
    private TextView Name,Email;
    private ImageView profilePhoto;

    private User user;

    private Project mProject;

    private CollectionReference usersReference,projectReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        deadlineTextView = findViewById(R.id.deadlineTextView);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View view = navigationView.getHeaderView(0);
        Name = view.findViewById(R.id.name);
        Email = view.findViewById(R.id.email);
        profilePhoto = view.findViewById(R.id.profilephoto);
        Intent intent = getIntent();
        if(intent.hasExtra("project")){
            mProject = (Project) intent.getSerializableExtra("project");
        }

        titleTextView.setText(""+ mProject.getTitle());
        descriptionTextView.setText("" + mProject.getDescription());
        String deadline = "Deadline         : " + mProject.getDayOfMonth() + "/" + mProject.getMonth() + "/" + mProject.getYear();

        deadlineTextView.setText(deadline);
        membersrecyclerView = findViewById(R.id.members_recycler_view);
        managersrecyclerView = findViewById(R.id.managers_recycler_view);

        mAuth = FirebaseAuth.getInstance();

        usersReference = FirebaseFirestore.getInstance().collection("Users");
        projectReference = FirebaseFirestore.getInstance().collection("Projects");
        setupFirebaseAuth();
    }

    private void getData(){
        final FirebaseUser user1 = mAuth.getCurrentUser();
        FirebaseFirestore.getInstance().collection("Users").document(user1.getUid()).get().addOnCompleteListener(new OnCompleteListener <DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task <DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    user = task.getResult().toObject(User.class);
                    Name.setText(user.getName());
                    Email.setText(user.getEmail());
                    StorageReference photoReference = (StorageReference) FirebaseStorage.getInstance().getReference("uploads").child(user.getUrl());

                    photoReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener <Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(Display.this).load(uri).into(profilePhoto);
                        }
                    });
                }
                else{

                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void getUsers(){
        mUsers.clear();
        projectReference.document(mProject.getProjectID()).collection("ProjectMembers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    List<DocumentSnapshot> docs = task.getResult().getDocuments();

                    for(DocumentSnapshot documentSnapshot : docs){
                        mUsers.add(documentSnapshot.toObject(User.class));
                    }
                    calculate();
                    setupRecyclerView();
                }
                else{

                }
            }
        });



    }

    private void calculate(){
        workers.clear();
        managers.clear();
        for(User mentor: mUsers){
            if(mentor.getAccounttype().equals("manager")){
                managers.add(mentor);
            }
            else{
                workers.add(mentor);
            }
        }

    }



    private void setupRecyclerView(){
        Log.d(TAG, "setupRecyclerView: Users: " + mUsers);
        Log.d(TAG, "setupRecyclerView: Image Urls" + imageUris);
        RecyclerViewAdapterMembers recyclerViewAdapterMembers = new RecyclerViewAdapterMembers(workers,imageUris,Display.this,mAuth);
        membersrecyclerView.setLayoutManager(new LinearLayoutManager(this));
        membersrecyclerView.setAdapter(recyclerViewAdapterMembers);

        RecyclerViewAdapterMembers recyclerViewAdapterMembers1 = new RecyclerViewAdapterMembers(managers,imageUris,Display.this,mAuth);
        managersrecyclerView.setLayoutManager(new LinearLayoutManager(this));
        managersrecyclerView.setAdapter(recyclerViewAdapterMembers1);

    }
    private void setupFirebaseAuth(){
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();

                if(user != null){
                    Log.d(TAG, "onAuthStateChanged: Signed in");
                    getData();
                    getUsers();
                }
                else{
                    Log.d(TAG, "onAuthStateChanged: Singed out");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
