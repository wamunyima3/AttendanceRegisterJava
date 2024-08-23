package com.wsofts.attendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttendanceDetailsAdapter extends RecyclerView.Adapter<AttendanceDetailsAdapter.AttendanceDetailsViewHolder> {

    private final List<AttendanceDetailsModel> attendanceDetailsList;

    public AttendanceDetailsAdapter(List<AttendanceDetailsModel> attendanceDetailsList) {
        this.attendanceDetailsList = attendanceDetailsList;
    }

    @NonNull
    @Override
    public AttendanceDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_details, parent, false);
        return new AttendanceDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceDetailsViewHolder holder, int position) {
        AttendanceDetailsModel attendanceDetails = attendanceDetailsList.get(position);
        holder.dateTextView.setText(attendanceDetails.getDate());
        holder.statusTextView.setText(attendanceDetails.getStatus());

        holder.editButton.setOnClickListener(v -> {
            // Handle editing attendance status here
            Toast.makeText(holder.itemView.getContext(), "Edit attendance for " + attendanceDetails.getDate(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return attendanceDetailsList.size();
    }

    static class AttendanceDetailsViewHolder extends RecyclerView.ViewHolder {

        TextView dateTextView;
        TextView statusTextView;
        ImageButton editButton;

        public AttendanceDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.date_textview);
            statusTextView = itemView.findViewById(R.id.status_textview);
            editButton = itemView.findViewById(R.id.edit_button);
        }
    }
}
