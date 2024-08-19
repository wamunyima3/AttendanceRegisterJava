package com.wsofts.attendance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private final List<AttendanceModel> attendanceList;
    private final List<String> dateHeaders; // List of dates to be displayed as columns
    private final Context context;

    public AttendanceAdapter(List<AttendanceModel> attendanceList, List<String> dateHeaders, Context context) {
        this.attendanceList = attendanceList;
        this.dateHeaders = dateHeaders;
        this.context = context;
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

        holder.deleteButton.setOnClickListener(v -> {
            // Remove item from list
            attendanceList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, attendanceList.size());

            // Implement Firebase deletion logic
            deleteStudentFromClass(attendanceModel.getStudentId().getId(), attendanceModel.getClassId().getId());
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

        // Step 1: Delete student from attendance records
        db.collection("Attendance")
                .whereEqualTo("studentId", "/Student/" + studentId)
                .whereEqualTo("classId", "/Class/" + classId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            db.collection("Attendance").document(document.getId()).delete();
                        }
                    }
                });

        // Step 2: Remove student from the class
        db.collection("ClassStudent")
                .whereEqualTo("studentId", "/Student/" + studentId)
                .whereEqualTo("classId", "/Class/" + classId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            db.collection("ClassStudent").document(document.getId()).delete();
                        }
                    }
                });

        // Step 3: Check if the student is associated with any other classes
        db.collection("ClassStudent")
                .whereEqualTo("studentId", "/Student/" + studentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            // Step 4: Delete student from the database
                            db.collection("Student").document(studentId).delete();
                        }
                    }
                });
    }
}
