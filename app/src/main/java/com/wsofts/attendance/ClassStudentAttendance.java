package com.wsofts.attendance;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.Map;

public class ClassStudentAttendance extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 1;
    private RecyclerView attendanceRecyclerView;
    private AttendanceAdapter attendanceAdapter;
    private final List<AttendanceModel> attendanceList = new ArrayList<>();
    private String classId;
    private String className;
    private BottomNavigationView bottomNavigationView;
    private TextView emptyView; // New TextView for showing 'No attendance' message

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
        setupEmptyView(); // Setup empty view
        fetchAttendanceData();
        setupToolbar();
        setupBottomNavigationView();
    }

    private void setupEmptyView() {
        emptyView = findViewById(R.id.empty_view); // Reference to the empty view TextView
        emptyView.setVisibility(View.GONE); // Hide it initially
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(className + " Attendance");
    }

    private void setupRecyclerView() {
        attendanceRecyclerView = findViewById(R.id.attendance_recycler_view);
        attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        attendanceAdapter = new AttendanceAdapter(new ArrayList<>(), new ArrayList<>(), classId,this);
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
                            if (result != null && !result.isEmpty()) {
                                attendanceList.clear();
                                List<String> dateHeaders = new ArrayList<>();

                                for (DocumentSnapshot document : result) {
                                    DocumentReference studentRef = document.getDocumentReference("studentId");
                                    String date = document.getString("date");
                                    String status = document.getString("status");

                                    if (date != null && studentRef != null) {
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

                                        if (!attendanceModel.getAttendanceStatusByDate().containsKey(date)) {
                                            attendanceModel.getAttendanceStatusByDate().put(date, status);
                                            if (!dateHeaders.contains(date)) {
                                                dateHeaders.add(date);
                                            }
                                        }
                                    }
                                }
                                emptyView.setVisibility(View.GONE);
                                attendanceRecyclerView.setVisibility(View.VISIBLE);
                                attendanceAdapter = new AttendanceAdapter(attendanceList, dateHeaders, classId, this);
                                attendanceRecyclerView.setAdapter(attendanceAdapter);
                            }else{
                                emptyView.setVisibility(View.VISIBLE);
                                attendanceRecyclerView.setVisibility(View.GONE);
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
                // Open file picker to pick excel/csv file of students
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                startActivityForResult(intent, FILE_SELECT_CODE);

                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                importExcelData(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error opening file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void importExcelData(InputStream inputStream) {
        try {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    // Skip the header row
                    continue;
                }

                // Get the id, which might be a numeric cell
                String id = "";
                Cell idCell = row.getCell(0);
                if (idCell.getCellType() == CellType.NUMERIC) {
                    id = String.valueOf((long) idCell.getNumericCellValue());
                } else if (idCell.getCellType() == CellType.STRING) {
                    id = idCell.getStringCellValue();
                }

                // Get the email, name, and phoneNumber, which should be string cells
                String email = row.getCell(1).getStringCellValue();
                String name = row.getCell(2).getStringCellValue();
                String phoneNumber = "";
                Cell phoneCell = row.getCell(3);
                if (phoneCell.getCellType() == CellType.NUMERIC) {
                    phoneNumber = String.valueOf((long) phoneCell.getNumericCellValue());
                } else if (phoneCell.getCellType() == CellType.STRING) {
                    phoneNumber = phoneCell.getStringCellValue();
                }

                // Add to Firestore Student collection
                Map<String, Object> studentData = new HashMap<>();
                studentData.put("email", email);
                studentData.put("name", name);
                studentData.put("phoneNumber", phoneNumber);

                DocumentReference studentRef = db.collection("Student").document(id);
                DocumentReference classRef = db.collection("Class").document(classId);
                studentRef.set(studentData).addOnSuccessListener(aVoid -> {

                    // Check if the student is already in the class
                    db.collection("ClassStudent")
                            .whereEqualTo("classId", classRef)
                            .whereEqualTo("studentId", studentRef)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult() != null && task.getResult().isEmpty()) {
                                    // Student is not yet in the class, so add them
                                    Map<String, Object> classStudentData = new HashMap<>();
                                    classStudentData.put("classId", classRef);
                                    classStudentData.put("studentId", studentRef);

                                    db.collection("ClassStudent").add(classStudentData)
                                            .addOnSuccessListener(documentReference -> {
                                                // Success
                                                Toast.makeText(ClassStudentAttendance.this, "Data imported successfully", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                // Failure
                                                Toast.makeText(ClassStudentAttendance.this, "Failed to import data", Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    // Student is already in the class
                                    Toast.makeText(ClassStudentAttendance.this, "Student " + name + " is already in this class.", Toast.LENGTH_SHORT).show();
                                }
                            });
                });
            }

            workbook.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show();
        }
    }
}
