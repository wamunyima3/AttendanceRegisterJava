package com.wsofts.attendance;
import com.google.firebase.firestore.DocumentReference;

public class ClassModel {
    private String classId;
    private String classCode;
    private String className;
    private String description;
    private DocumentReference lecturerId;  // Change this to DocumentReference

    public ClassModel() {
        // Default constructor required for Firebase deserialization
    }

    public ClassModel(String classId, String classCode, String className, String description, DocumentReference lecturerId) {
        this.classId = classId;
        this.classCode = classCode;
        this.className = className;
        this.description = description;
        this.lecturerId = lecturerId;
    }

    public String getClassId() {
        return classId;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DocumentReference getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(DocumentReference lecturerId) {
        this.lecturerId = lecturerId;
    }
}
