package com.wsofts.attendance;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private final List<AttendanceModel> attendanceList;
    private final List<String> dateHeaders; // List of dates to be displayed as columns
    private final Context context;
    private final String classId;

    public AttendanceAdapter(List<AttendanceModel> attendanceList, List<String> dateHeaders, String classId, Context context) {
        this.attendanceList = attendanceList;
        this.dateHeaders = dateHeaders;
        this.context = context;
        this.classId = classId;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceModel attendanceModel = attendanceList.get(position);
        holder.studentId.setText(attendanceModel.getStudentId().getId());

        // Setup inner RecyclerView for date columns
        DateAdapter dateAdapter = new DateAdapter(context, dateHeaders, attendanceModel.getAttendanceStatusByDate());
        holder.dateRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.dateRecyclerView.setAdapter(dateAdapter);

        // Set click listener for navigation to the details page
        holder.itemView.setOnClickListener(v -> {
            // Create an intent to navigate to the details page
            Intent intent = new Intent(context, StudentAttendanceDetails.class);
            intent.putExtra("studentId", attendanceModel.getStudentId().getId());
            intent.putExtra("classId", classId);
            context.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            // Remove item from list
            attendanceList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, attendanceList.size());

            // Delete the student from Firebase
            deleteStudentFromClass(attendanceModel.getStudentId().getId(), classId);
        });
    }


    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView studentId;
        RecyclerView dateRecyclerView;
        ImageButton deleteButton; // Reference to the delete button

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            studentId = itemView.findViewById(R.id.student_id);
            dateRecyclerView = itemView.findViewById(R.id.date_recycler_view);
            deleteButton = itemView.findViewById(R.id.delete_button); // Initialize the delete button
        }
    }

    private void deleteStudentFromClass(String studentId, String classId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference studentRef = db.collection("Student").document(studentId);
        DocumentReference classRef = db.collection("Class").document(classId);

        // Step 1: Delete all attendance records for this student in the class
        db.collection("Attendance")
                .whereEqualTo("classId", classRef)
                .whereEqualTo("studentId", studentRef)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                        }
                    }
                });

        // Step 2: Delete the student from the ClassStudent collection
        db.collection("ClassStudent")
                .whereEqualTo("classId", classRef)
                .whereEqualTo("studentId", studentRef)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                        }

                        // Step 3: Check if the student is associated with any other classes
                        db.collection("ClassStudent")
                                .whereEqualTo("studentId", studentRef)
                                .get()
                                .addOnCompleteListener(classTask -> {
                                    if (classTask.isSuccessful() && classTask.getResult() != null && classTask.getResult().isEmpty()) {
                                        // Step 4: If not associated with any other classes, delete the student from the Student collection
                                        studentRef.delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(context, "Student deleted successfully", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(context, "Failed to delete student", Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                });
                    }
                });
    }
}
