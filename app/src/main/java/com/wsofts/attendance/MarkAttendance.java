package com.wsofts.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MarkAttendance extends AppCompatActivity {

    private RecyclerView markAttendanceRecyclerView;
    private MarkAttendanceAdapter markAttendanceAdapter;
    private List<StudentModel> studentList = new ArrayList<>();
    private String classId;
    private String className;
    private Date currentDate;
    private TextView noStudentsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);

        classId = getIntent().getStringExtra("classId");
        className = getIntent().getStringExtra("className");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        setupToolbar();
        setupRecyclerView();
        fetchStudentsData();

        Button saveButton = findViewById(R.id.markAttendanceSaveButton);
        saveButton.setOnClickListener(view -> checkAndSaveAttendance());
    }

    private void setupToolbar() {
        currentDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(className);
        getSupportActionBar().setSubtitle(sdf.format(currentDate));
    }

    private void setupRecyclerView() {
        noStudentsTextView = findViewById(R.id.noStudentsTextView);
        markAttendanceRecyclerView = findViewById(R.id.markAttendanceRecyclerView);
        markAttendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        markAttendanceAdapter = new MarkAttendanceAdapter(studentList, this, classId, currentDate);
        markAttendanceRecyclerView.setAdapter(markAttendanceAdapter);
    }

    private void fetchStudentsData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("ClassStudent")
                .whereEqualTo("classId", db.collection("Class").document(classId))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        if (result != null && !result.isEmpty()) {
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
                                                noStudentsTextView.setVisibility(View.GONE);
                                                markAttendanceRecyclerView.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                                }
                            }
                        } else {
                            noStudentsTextView.setVisibility(View.VISIBLE);
                            markAttendanceRecyclerView.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(MarkAttendance.this, "Error fetching students data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkAndSaveAttendance() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference classRef = db.collection("Class").document(classId);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String dateId = dateFormat.format(currentDate);

        // Add a new document to the ClassDays collection for this date if not already present
        DocumentReference classDayRef = db.collection("ClassDays").document(dateId);
        classDayRef.get().addOnCompleteListener(task -> {
            if (!task.getResult().exists()) {
                classDayRef.set(new ClassDayModel(classRef, dateId));
            }
        });

        for (StudentModel student : studentList) {
            String status = student.getAttendanceStatus();
            if (status != null) {
                DocumentReference studentRef = db.collection("Student").document(student.getStudentId());

                db.collection("Attendance")
                        .whereEqualTo("classId", classRef)
                        .whereEqualTo("studentId", studentRef)
                        .whereEqualTo("date", dateId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult().isEmpty()) {
                                    // No existing record, save a new one
                                    saveAttendanceRecord(classRef, studentRef, status, dateId);
                                } else {
                                    // Record exists, update it
                                    updateAttendanceRecord(task.getResult().getDocuments().get(0).getId(), status);
                                }
                            } else {
                                Toast.makeText(MarkAttendance.this, "Error checking attendance", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    private void saveAttendanceRecord(DocumentReference classRef, DocumentReference studentRef, String status, String date) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Attendance")
                .add(new MarkAttendanceModel(classRef, studentRef, status, date))
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(MarkAttendance.this, "Attendance saved!", Toast.LENGTH_SHORT).show();
                    navigateToClassStudentAttendance();
                })
                .addOnFailureListener(e -> Toast.makeText(MarkAttendance.this, "Error saving attendance", Toast.LENGTH_SHORT).show());
    }

    private void updateAttendanceRecord(String attendanceId, String status) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Attendance")
                .document(attendanceId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MarkAttendance.this, "Attendance updated!", Toast.LENGTH_SHORT).show();
                    navigateToClassStudentAttendance();
                })
                .addOnFailureListener(e -> Toast.makeText(MarkAttendance.this, "Error updating attendance", Toast.LENGTH_SHORT).show());
    }

    private void navigateToClassStudentAttendance() {
        Intent intent = new Intent(this, ClassStudentAttendance.class);
        intent.putExtra("classId", classId);
        intent.putExtra("className", className);
        startActivity(intent);
        finish();
    }
}

