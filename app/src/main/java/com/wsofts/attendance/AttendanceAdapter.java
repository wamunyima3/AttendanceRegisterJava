package com.wsofts.attendance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        holder.studentName.setText(attendanceModel.getStudentName());
        holder.studentId.setText(attendanceModel.getStudentId().getId());

        // Setup inner RecyclerView for date columns
        DateAdapter dateAdapter = new DateAdapter(context, dateHeaders, attendanceModel.getAttendanceStatusByDate());
        holder.dateRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.dateRecyclerView.setAdapter(dateAdapter);
    }


    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView studentId;
        TextView studentName;
        RecyclerView dateRecyclerView; // RecyclerView to hold dynamic date columns

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.student_name);
            studentId = itemView.findViewById(R.id.student_id);
            dateRecyclerView = itemView.findViewById(R.id.date_recycler_view); // Find the nested RecyclerView
        }
    }
}
