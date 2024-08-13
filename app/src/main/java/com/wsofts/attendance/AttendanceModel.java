package com.wsofts.attendance;

import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;

public class AttendanceModel {
    private DocumentReference studentId;
    private String studentName;
    private DocumentReference classId;
    private Map<String, String> attendanceStatusByDate; // Map to hold date as key and status as value

    public AttendanceModel() {
        this.attendanceStatusByDate = new HashMap<>();
    }

    public DocumentReference getStudentId() {
        return studentId;
    }

    public void setStudentId(DocumentReference studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public DocumentReference getClassId() {
        return classId;
    }

    public void setClassId(DocumentReference classId) {
        this.classId = classId;
    }

    public Map<String, String> getAttendanceStatusByDate() {
        return attendanceStatusByDate;
    }

    public void setAttendanceStatusByDate(Map<String, String> attendanceStatusByDate) {
        this.attendanceStatusByDate = attendanceStatusByDate;
    }
}
