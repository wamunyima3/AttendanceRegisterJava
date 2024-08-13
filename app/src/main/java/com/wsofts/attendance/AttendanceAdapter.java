package com.wsofts.attendance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

        // Clear the container first to avoid duplications
        holder.dateContainer.removeAllViews();

        // Dynamically populate attendance status for each date
        for (String date : dateHeaders) {
            String status = attendanceModel.getAttendanceStatusByDate().get(date);

            // Create a new TextView for each date's status
            TextView statusView = new TextView(context);
            statusView.setTextSize(18);
            statusView.setPadding(8, 8, 8, 8);
            statusView.setText(status != null ? status : "N/A");

            // Optionally, add some layout parameters to align TextViews properly
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(8, 0, 8, 0); // Add some margin if necessary
            statusView.setLayoutParams(layoutParams);

            // Add the status view to the date container
            holder.dateContainer.addView(statusView);
        }
    }


    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView studentId;
        TextView studentName;
        ViewGroup dateContainer; // ViewGroup to hold dynamic date columns

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.student_name);
            studentId = itemView.findViewById(R.id.student_id);
            dateContainer = itemView.findViewById(R.id.date_container); // Assume you have a LinearLayout or other ViewGroup here
        }
    }
}
