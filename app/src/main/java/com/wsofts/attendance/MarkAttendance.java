package com.wsofts.attendance;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarkAttendance extends AppCompatActivity {

    private RecyclerView markAttendanceRecyclerView;
    private MarkAttendanceAdapter markAttendanceAdapter;
    private List<StudentModel> studentList = new ArrayList<>();
    private String classId;
    private String className;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);

        classId = getIntent().getStringExtra("classId");
        className = getIntent().getStringExtra("className");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupToolbar();
        setupRecyclerView();
        fetchStudentsData();

        Button saveButton = findViewById(R.id.markAttendanceSaveButton);
        saveButton.setOnClickListener(view -> saveAttendance());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(className);
    }

    private void setupRecyclerView() {
        markAttendanceRecyclerView = findViewById(R.id.markAttendanceRecyclerView);
        markAttendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        markAttendanceAdapter = new MarkAttendanceAdapter(studentList, this);
        markAttendanceRecyclerView.setAdapter(markAttendanceAdapter);
    }

    private void fetchStudentsData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (classId != null) {
            db.collection("ClassStudent")
                    .whereEqualTo("classId", db.collection("Class").document(classId))
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot result = task.getResult();
                            if (result != null) {
                                for (DocumentSnapshot document : result) {
                                    DocumentReference studentRef = document.getDocumentReference("studentId");
                                    if (studentRef != null) {
                                        studentRef.get().addOnCompleteListener(studentTask -> {
                                            if (studentTask.isSuccessful()) {
                                                DocumentSnapshot studentDoc = studentTask.getResult();
                                                if (studentDoc != null) {
                                                    String studentName = studentDoc.getString("name");
                                                    String studentId = studentRef.getId();

                                                    studentList.add(new StudentModel(studentId, studentName));
                                                    markAttendanceAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(MarkAttendance.this, "Error fetching students data", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Class ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAttendance() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Date currentDate = new Date();  // Get the current date

        for (StudentModel student : studentList) {
            String status = student.getAttendanceStatus();
            if (status != null) {
                DocumentReference classRef = db.collection("Class").document(classId);
                DocumentReference studentRef = db.collection("Student").document(student.getStudentId());

                // Create a new attendance record with the current date
                db.collection("Attendance").add(new MarkAttendanceModel(classRef, studentRef, status, currentDate))
                        .addOnSuccessListener(documentReference -> Toast.makeText(MarkAttendance.this, "Attendance saved!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(MarkAttendance.this, "Error saving attendance", Toast.LENGTH_SHORT).show());
            }
        }
    }

}
