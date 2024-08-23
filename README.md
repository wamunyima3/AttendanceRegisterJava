# Attendance Register App - Android (Java + Firebase)

## Overview

This is an Android app built using Java and Firebase for managing attendance in educational institutions. The app allows lecturers to create classes, add students, and track attendance efficiently. Users can either register as new users or log in if they already have an account. The app provides a dashboard where lecturers can manage their classes and attendance.

## Features

- **User Authentication:**
  - **Registration:** New users can sign up with their email and password.
  - **Login:** Returning users can log in with their credentials.
  - Upon successful registration or login, users are directed to the dashboard.

- **Dashboard:**
  - **Class Management:** Lecturers can create new classes.
  - **Add Students:** Students can be added to a class by uploading an Excel file.
  - **Attendance Management:** Lecturers can view and mark the attendance of students for specific dates.

## Firebase Setup

To use this project, you need to set up the following Firebase Firestore collections:

### 1. Attendance
- **Document:** `0BCDRcJ8S63eDBjdsoJg`
- **Fields:**
  - `classId`: `/Class/7VIVD3eFjrcPTis1tmSu`
  - `date`: `"August 18, 2024"`
  - `status`: `"P"`
  - `studentId`: `/Student/2110403`

### 2. Class
- **Document:** `405QBTdX9IlAuALdKmic`
- **Fields:**
  - `classCode`: `"BIT 1100"`
  - `className`: `"Programming"`
  - `description`: `"Introduction to programming"`
  - `lecturerId`: `/Lecturer/GRCp4otz7dP4QpltZ6CigpN91IC2`

### 3. ClassDays
- **Document:** `"August 18, 2024"`
- **Fields:**
  - `classRef`: `/Class/405QBTdX9IlAuALdKmic`
  - `date`: `"August 18, 2024"`

### 4. ClassStudent
- **Document:** `0GseRqVX51InrYA0zwKN`
- **Fields:**
  - `classId`: `/Class/405QBTdX9IlAuALdKmic`
  - `studentId`: `/Student/2110405`

### 5. Lecturer
- **Document:** `GRCp4otz7dP4QpltZ6CigpN91IC2`
- **Fields:**
  - `email`: `"violetchandachilufya@gmail.com"`
  - `firstname`: `"Violet"`
  - `surname`: `"Chilufya"`

### 6. Student
- **Document:** `2110402`
- **Fields:**
  - `email`: `"inongemusonda@gmail.com"`
  - `name`: `"Inonge Wamunyima"`
  - `phoneNumber`: `"979004530"`

## Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/wamunyima3/AttendanceRegisterJava.git
   ```
2. **Open the project in Android Studio.**
3. **Set up Firebase:**
   - Add your Firebase project configuration file (`google-services.json`) to the app.
   - Enable Firebase Authentication and Firestore in your Firebase console.
4. **Build and Run the app** on your preferred Android device or emulator.

## Sample APK

For a quick preview of the app, you can download the [sample APK here](https://drive.google.com/file/d/10a1UGOKLBwwtSmBjNCAtcGtJ01f5uiGW/view?usp=drive_link)
## Contributing

Feel free to submit issues or pull requests to improve the app. Contributions are always welcome!

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Author:** Wamunyima Mukelabai  
**Email:** wamunyimamukelabai3@gmail.com  
**GitHub:** wamunyima3(https://github.com/wamunyima3)

---
