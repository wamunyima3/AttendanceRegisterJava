package com.wsofts.attendance;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StudentAttendanceDetails extends AppCompatActivity {

    private TextView studentNameTextView;
    private TextView studentIdTextView;
    private RecyclerView attendanceRecyclerView;
    private AttendanceDetailsAdapter attendanceDetailsAdapter;
    private List<AttendanceDetailsModel> attendanceDetailsList = new ArrayList<>();
    private String studentId;
    private String classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance_details);

        studentNameTextView = findViewById(R.id.student_name);
        studentIdTextView = findViewById(R.id.student_id);
        attendanceRecyclerView = findViewById(R.id.attendance_details_recyclerview);

        studentId = getIntent().getStringExtra("studentId");
        classId = getIntent().getStringExtra("classId");

        fetchStudentDetails();
        fetchAttendanceDetails();
    }

    private void fetchStudentDetails() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference studentRef = db.collection("Student").document(studentId);

        studentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot studentDoc = task.getResult();
                if (studentDoc != null) {
                    studentNameTextView.setText(studentDoc.getString("name"));
                    studentIdTextView.setText(studentDoc.getId());
                }
            } else {
                Toast.makeText(this, "Failed to fetch student details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAttendanceDetails() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference classRef = db.collection("Class").document(classId);

        db.collection("Attendance")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("classId", classRef)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<AttendanceDetailsModel> attendanceDetails = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String date = document.getString("date");
                            String status = document.getString("status");

                            attendanceDetails.add(new AttendanceDetailsModel(date, status));
                        }

                        attendanceDetailsList = attendanceDetails;
                        setupRecyclerView();
                    } else {
                        Toast.makeText(this, "Failed to fetch attendance details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupRecyclerView() {
        attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        attendanceDetailsAdapter = new AttendanceDetailsAdapter(attendanceDetailsList);
        attendanceRecyclerView.setAdapter(attendanceDetailsAdapter);
    }
}
