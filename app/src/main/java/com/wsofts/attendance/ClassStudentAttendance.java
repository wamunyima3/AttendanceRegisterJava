package com.wsofts.attendance;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClassStudentAttendance extends AppCompatActivity {

    private RecyclerView attendanceRecyclerView;
    private AttendanceAdapter attendanceAdapter;
    private List<AttendanceModel> attendanceList = new ArrayList<>();
    private String classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_class_student_attendance);

        classId = getIntent().getStringExtra("classId");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupRecyclerView();
        fetchAttendanceData();
        setupToolbar();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Attendance Records");
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
                                    AttendanceModel attendanceModel = new AttendanceModel();
                                    attendanceModel.setStudentId(document.getDocumentReference("studentId"));
                                    attendanceModel.setAttendanceStatusByDate(new HashMap<>());

                                    // Process date and status
                                    Timestamp timestamp = document.getTimestamp("date");
                                    if (timestamp != null) {
                                        String dateString = timestamp.toDate().toString(); // Adjust format as needed
                                        String status = document.getString("status");

                                        // Populate model and headers
                                        attendanceModel.getAttendanceStatusByDate().put(dateString, status);
                                        if (!dateHeaders.contains(dateString)) {
                                            dateHeaders.add(dateString);
                                        }
                                    }

                                    // Fetch student name using studentId
                                    DocumentReference studentRef = attendanceModel.getStudentId();
                                    studentRef.get().addOnCompleteListener(studentTask -> {
                                        if (studentTask.isSuccessful()) {
                                            DocumentSnapshot studentDoc = studentTask.getResult();
                                            if (studentDoc != null) {
                                                attendanceModel.setStudentName(studentDoc.getString("name"));
                                                attendanceList.add(attendanceModel);
                                                attendanceAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                                }

                                // Set adapter with the new data
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

}
