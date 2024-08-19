package com.wsofts.attendance;

import com.google.firebase.firestore.DocumentReference;

import java.util.Date;

public class MarkAttendanceModel {

    private DocumentReference classId;
    private DocumentReference studentId;
    private String status;
    private String date;  // New field to store the attendance date

    public MarkAttendanceModel() {
    }

    public MarkAttendanceModel(DocumentReference classId, DocumentReference studentId, String status, String date) {
        this.classId = classId;
        this.studentId = studentId;
        this.status = status;
        this.date = date;
    }

    public DocumentReference getClassId() {
        return classId;
    }

    public void setClassId(DocumentReference classId) {
        this.classId = classId;
    }

    public DocumentReference getStudentId() {
        return studentId;
    }

    public void setStudentId(DocumentReference studentId) {
        this.studentId = studentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
