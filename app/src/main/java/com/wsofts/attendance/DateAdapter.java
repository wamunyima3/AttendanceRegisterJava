package com.wsofts.attendance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {

    private final Context context;
    private final List<String> dateHeaders;
    private final Map<String, String> attendanceStatusByDate;

    public DateAdapter(Context context, List<String> dateHeaders, Map<String, String> attendanceStatusByDate) {
        this.context = context;
        this.dateHeaders = dateHeaders;
        this.attendanceStatusByDate = attendanceStatusByDate;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_status, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        String date = dateHeaders.get(position);
        String status = attendanceStatusByDate.get(date);

        holder.dateStatus.setText(status != null ? status : "N/A");
    }

    @Override
    public int getItemCount() {
        return dateHeaders.size();
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView dateStatus;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            dateStatus = itemView.findViewById(R.id.date_status);
        }
    }
}
