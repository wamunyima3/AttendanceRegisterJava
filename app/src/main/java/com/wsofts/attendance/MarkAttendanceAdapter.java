package com.wsofts.attendance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MarkAttendanceAdapter extends RecyclerView.Adapter<MarkAttendanceAdapter.ViewHolder> {

    private final List<StudentModel> studentList;
    private final Context context;

    public MarkAttendanceAdapter(List<StudentModel> studentList, Context context) {
        this.studentList = studentList;
        this.context = context;
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

        holder.attendanceRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.presentRadioButton) {
                student.setAttendanceStatus("P");
            } else if (checkedId == R.id.absentRadioButton) {
                student.setAttendanceStatus("A");
            } else if (checkedId == R.id.absentWithPermissionRadioButton) { // New condition for AP
                student.setAttendanceStatus("AP");
            } else if (checkedId == R.id.sickRadioButton) {
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
        RadioGroup attendanceRadioGroup;
        RadioButton presentRadioButton, absentRadioButton, sickRadioButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.markAttendanceStudentName);
            attendanceRadioGroup = itemView.findViewById(R.id.attendanceRadioGroup);
            presentRadioButton = itemView.findViewById(R.id.presentRadioButton);
            absentRadioButton = itemView.findViewById(R.id.absentRadioButton);
            sickRadioButton = itemView.findViewById(R.id.sickRadioButton);
        }
    }
}
