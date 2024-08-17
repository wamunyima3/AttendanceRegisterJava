package com.wsofts.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ClassStudentAttendance extends AppCompatActivity {

    private RecyclerView attendanceRecyclerView;
    private AttendanceAdapter attendanceAdapter;
    private final List<AttendanceModel> attendanceList = new ArrayList<>();
    private String classId;
    private String className;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_class_student_attendance);

        classId = getIntent().getStringExtra("classId");
        className = getIntent().getStringExtra("className");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupRecyclerView();
        fetchAttendanceData();
        setupToolbar();
        setupBottomNavigationView();

    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(className + " Attendance");
    }

    private void setupRecyclerView() {
        attendanceRecyclerView = findViewById(R.id.attendance_recycler_view);
        attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        attendanceAdapter = new AttendanceAdapter(new ArrayList<>(), new ArrayList<>(), this);
        attendanceRecyclerView.setAdapter(attendanceAdapter);
    }

    private void fetchAttendanceData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (classId != null) {
            DocumentReference classRef = db.collection("Class").document(classId);

            db.collection("Attendance")
                    .whereEqualTo("classId", classRef)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot result = task.getResult();
                            if (result != null) {
                                attendanceList.clear();
                                List<String> dateHeaders = new ArrayList<>();

                                for (DocumentSnapshot document : result) {
                                    DocumentReference studentRef = document.getDocumentReference("studentId");
                                    Timestamp timestamp = document.getTimestamp("date");
                                    String status = document.getString("status");

                                    if (timestamp != null && studentRef != null) {
                                        String dateString = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                                .format(timestamp.toDate());

                                        AttendanceModel attendanceModel = null;
                                        for (AttendanceModel model : attendanceList) {
                                            if (model.getStudentId().equals(studentRef)) {
                                                attendanceModel = model;
                                                break;
                                            }
                                        }

                                        if (attendanceModel == null) {
                                            attendanceModel = new AttendanceModel();
                                            attendanceModel.setStudentId(studentRef);
                                            attendanceModel.setAttendanceStatusByDate(new HashMap<>());
                                            attendanceList.add(attendanceModel);

                                            AttendanceModel finalAttendanceModel = attendanceModel;
                                            studentRef.get().addOnCompleteListener(studentTask -> {
                                                if (studentTask.isSuccessful()) {
                                                    DocumentSnapshot studentDoc = studentTask.getResult();
                                                    if (studentDoc != null) {
                                                        finalAttendanceModel.setStudentName(studentDoc.getString("name"));
                                                        attendanceAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                            });
                                        }

                                        if (!attendanceModel.getAttendanceStatusByDate().containsKey(dateString)) {
                                            attendanceModel.getAttendanceStatusByDate().put(dateString, status);
                                            if (!dateHeaders.contains(dateString)) {
                                                dateHeaders.add(dateString);
                                            }
                                        }
                                    }
                                }

                                attendanceAdapter = new AttendanceAdapter(attendanceList, dateHeaders, this);
                                attendanceRecyclerView.setAdapter(attendanceAdapter);
                            }
                        } else {
                            Toast.makeText(ClassStudentAttendance.this, "Error fetching attendance data", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Class ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNavigationView() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_mark_attendance) {
                Intent intent = new Intent(ClassStudentAttendance.this, MarkAttendance.class);
                intent.putExtra("classId", classId);
                intent.putExtra("className", className);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_add_students) {
                //Open file picker to pick excel/csv file of students
                return true;
            } else {
                return false;
            }
        });

    }


}
