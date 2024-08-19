package com.wsofts.attendance;

import com.google.firebase.firestore.DocumentReference;

public class ClassDayModel {
    private DocumentReference classRef;
    private String date;

    public ClassDayModel() {
        // Default constructor
    }

    public ClassDayModel(DocumentReference classRef, String date) {
        this.classRef = classRef;
        this.date = date;
    }

    // Getters and Setters
    public DocumentReference getClassRef() {
        return classRef;
    }

    public void setClassRef(DocumentReference classRef) {
        this.classRef = classRef;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
