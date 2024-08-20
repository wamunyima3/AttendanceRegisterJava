package com.wsofts.attendance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;
import java.util.Locale;

public class MarkAttendanceAdapter extends RecyclerView.Adapter<MarkAttendanceAdapter.ViewHolder> {

    private final List<StudentModel> studentList;
    private final Context context;
    private final String classId;
    private final Date currentDate;

    public MarkAttendanceAdapter(List<StudentModel> studentList, Context context, String classId, Date currentDate) {
        this.studentList = studentList;
        this.context = context;
        this.classId = classId;
        this.currentDate = currentDate;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mark_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentModel student = studentList.get(position);
        holder.studentName.setText(student.getStudentName());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference classRef = db.collection("Class").document(classId);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate); // Use the formatted date for attendance check
        DocumentReference studentRef = db.collection("Student").document(student.getStudentId());

        db.collection("Attendance")
                .whereEqualTo("classId", classRef)
                .whereEqualTo("studentId", studentRef)
                .whereEqualTo("date", formattedDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        String status = doc.getString("status");
                        if (status != null) {
                            switch (status) {
                                case "P":
                                    holder.presentRadioButton.setChecked(true);
                                    break;
                                case "A":
                                    holder.absentRadioButton.setChecked(true);
                                    break;
                                case "AP":
                                    holder.absentWithPermissionRadioButton.setChecked(true);
                                    break;
                                case "S":
                                    holder.sickRadioButton.setChecked(true);
                                    break;
                            }
                        }
                    }
                });

        holder.attendanceStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == holder.presentRadioButton.getId()) {
                student.setAttendanceStatus("P");
            } else if (checkedId == holder.absentRadioButton.getId()) {
                student.setAttendanceStatus("A");
            } else if (checkedId == holder.absentWithPermissionRadioButton.getId()) {
                student.setAttendanceStatus("AP");
            } else if (checkedId == holder.sickRadioButton.getId()) {
                student.setAttendanceStatus("S");
            }
        });
    }


    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView studentName;
        RadioGroup attendanceStatus;
        RadioButton presentRadioButton, absentRadioButton, absentWithPermissionRadioButton, sickRadioButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.studentName);
            attendanceStatus = itemView.findViewById(R.id.attendanceStatus);
            presentRadioButton = itemView.findViewById(R.id.presentRadioButton);
            absentRadioButton = itemView.findViewById(R.id.absentRadioButton);
            sickRadioButton = itemView.findViewById(R.id.sickRadioButton);
            absentWithPermissionRadioButton = itemView.findViewById(R.id.absentWithPermissionRadioButton);
        }
    }
}
