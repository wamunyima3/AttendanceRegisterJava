package com.wsofts.attendance;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ClassesAdapter extends RecyclerView.Adapter<ClassesAdapter.ClassViewHolder> {

    private final List<ClassModel> classList;
    private final Context context;

    public ClassesAdapter(List<ClassModel> classList, Context context) {
        this.classList = classList;
        this.context = context;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        ClassModel classModel = classList.get(position);
        holder.className.setText(classModel.getClassName());
        holder.classDescription.setText(classModel.getDescription());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ClassStudentAttendance.class);
            intent.putExtra("classId", classModel.getClassId());
            intent.putExtra("className", classModel.getClassName());
            context.startActivity(intent);
        });

        holder.editButton.setOnClickListener(v -> {
            if (context instanceof MainActivity) {
                ((MainActivity) context).showAddEditClassDialog(classModel);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Get the class ID
            String classId = classModel.getClassId();

            // Start deleting related data
            db.collection("Class").document(classId).delete().addOnSuccessListener(aVoid -> {
                // Step 1: Delete Class-Student Associations
                db.collection("ClassStudent")
                        .whereEqualTo("classId", classId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult().getDocuments()) {
                                    String studentId = document.getString("studentId");
                                    document.getReference().delete(); // Delete the ClassStudent document

                                    // Check if the student is associated with any other class
                                    db.collection("ClassStudent")
                                            .whereEqualTo("studentId", studentId)
                                            .get()
                                            .addOnCompleteListener(studentTask -> {
                                                if (studentTask.isSuccessful() && studentTask.getResult().isEmpty()) {
                                                    // The student is not associated with any other class, delete them
                                                    db.collection("Student").document(studentId).delete();
                                                }
                                            });
                                }
                            }
                        });

                // Step 2: Delete Class Days
                db.collection("ClassDays") // Replace with your actual collection name
                        .whereEqualTo("classId", classId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult().getDocuments()) {
                                    document.getReference().delete(); // Delete ClassDay document
                                }
                            }
                        });

                // Step 3: Delete Attendance Records
                db.collection("Attendance")
                        .whereEqualTo("classId", classId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult().getDocuments()) {
                                    document.getReference().delete(); // Delete Attendance document
                                }
                            }
                        });

                // Step 4: Notify user and remove the class from the list
                classList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, classList.size());
                Toast.makeText(context, "Class and related data deleted successfully", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Failed to delete class", Toast.LENGTH_SHORT).show();
            });
        });

    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    public static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView className, classDescription;
        ImageButton editButton, deleteButton;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            className = itemView.findViewById(R.id.class_name);
            classDescription = itemView.findViewById(R.id.class_description);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}