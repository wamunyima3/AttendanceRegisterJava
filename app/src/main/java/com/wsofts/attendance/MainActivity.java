package com.wsofts.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView classesRecyclerView;
    private FloatingActionButton addClassFab;
    private BottomNavigationView bottomNavigationView;
    private ClassesAdapter classesAdapter;
    private List<ClassModel> classList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupEdgeToEdge();
        setupToolbar();
        setupRecyclerView();
        setupFloatingActionButton();
        setupBottomNavigationView();

        fetchClassesData();
    }

    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupRecyclerView() {
        classesRecyclerView = findViewById(R.id.classes_recycler_view);
        classesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        classesAdapter = new ClassesAdapter(classList, this);
        classesRecyclerView.setAdapter(classesAdapter);
    }

    private void setupFloatingActionButton() {
        addClassFab = findViewById(R.id.fab_add_class);
        addClassFab.setOnClickListener(v -> showAddEditClassDialog(null));
    }

    private void setupBottomNavigationView() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, Settings.class));
                return true;
            } else if (itemId == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, Login.class));
                finish();
                return true;
            } else {
                return false;
            }
        });

    }

    public void showAddEditClassDialog(ClassModel classModel) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_class, null);
        TextInputEditText classCodeInput = dialogView.findViewById(R.id.class_code_input);
        TextInputEditText classNameInput = dialogView.findViewById(R.id.class_name_input);
        TextInputEditText classDescriptionInput = dialogView.findViewById(R.id.class_description_input);

        if (classModel != null) {
            classCodeInput.setText(classModel.getClassCode());
            classNameInput.setText(classModel.getClassName());
            classDescriptionInput.setText(classModel.getDescription());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle(classModel == null ? "Add Class" : "Edit Class")
                .setPositiveButton("Save", (dialog, which) -> {
                    String classCode = classCodeInput.getText().toString().trim();
                    String className = classNameInput.getText().toString().trim();
                    String classDescription = classDescriptionInput.getText().toString().trim();

                    if ( TextUtils.isEmpty(classCode) ||TextUtils.isEmpty(className) || TextUtils.isEmpty(classDescription)) {
                        Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    } else {
                        if (classModel == null) {
                            addNewClassToFirebase(classCode ,className, classDescription);
                        } else {
                            updateClassInFirebase(classModel.getClassId(), classCode, className, classDescription);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void addNewClassToFirebase(String classCode ,String className, String description) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ClassModel newClass = new ClassModel();
        newClass.setClassCode(classCode);
        newClass.setClassName(className);
        newClass.setDescription(description);

        db.collection("Class")
                .add(newClass)
                .addOnSuccessListener(documentReference -> {
                    String documentId = documentReference.getId();
                    newClass.setClassId(documentId);
                    db.collection("Class").document(documentId).set(newClass);
                    fetchClassesData();
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to add class", Toast.LENGTH_SHORT).show());
    }

    private void updateClassInFirebase(String classId, String classCode ,String className, String description) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Class").document(classId)
                .update("classCode", classCode,"className", className, "description", description)
                .addOnSuccessListener(aVoid -> fetchClassesData())
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to update class", Toast.LENGTH_SHORT).show());
    }

    private void fetchClassesData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Class")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        if (result != null) {
                            classList.clear();
                            for (DocumentSnapshot document : result) {
                                ClassModel classModel = document.toObject(ClassModel.class);
                                if (classModel != null) {
                                    classModel.setClassId(document.getId());
                                    classList.add(classModel);
                                }
                            }
                            classesAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Error fetching classes data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}