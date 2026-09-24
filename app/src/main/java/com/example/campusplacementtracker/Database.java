
package com.example.campusplacementtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import java.util.ArrayList;

public class Database extends SQLiteOpenHelper {

    public Database(@Nullable Context context) {
        super(context, "campusplacement", null, 10);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table 1: users - stores login + student/company profile details + role + account status + resume
        String usersTable = "create table users(username text primary key, email text, password text, " +
                "fullname text, rollno text, branch text, cgpa text, role text, status text, tech_stack text, resume_uri text)";
        db.execSQL(usersTable);
        // Table 2: applications - stores every job application a student makes
        String applicationsTable = "create table applications(username text, company text, role text, " +
                "package text, eligibility text, applieddate text, interviewdate text, interviewtime text, status text)";
        db.execSQL(applicationsTable);

        // Table 3: companies - stores company details added by admin + logo
        String companiesTable = "create table companies(name text primary key, role text, package text, eligibility text, tech_stack text, min_cgpa text, logo_uri text)";
        db.execSQL(companiesTable);

        // Table 4: slots - stores available interview slots for companies
        String slotsTable = "create table slots(id integer primary key autoincrement, company text, date text, time text, is_booked integer)";
        db.execSQL(slotsTable);

        // Table 5: cgpa_requests - requests from students to update their CGPA
        String cgpaRequestsTable = "create table cgpa_requests(id integer primary key autoincrement, username text, requested_cgpa text, status text)";
        db.execSQL(cgpaRequestsTable);

        // Insert default admin
        db.execSQL("insert into users values('admin', 'admin@placement.com', 'admin123', 'Admin User', '0', 'Placement Office', '10', 'admin', 'active', '', '')");

        // Insert some default companies with diverse requirements
        db.execSQL("insert into companies values('Google', 'Software Engineer', '30 LPA', 'MCA, B.Tech CSE', 'Java, Go, C++', '8.5', '')");
        db.execSQL("insert into companies values('Microsoft', 'SDE-1', '25 LPA', 'B.Tech, M.Tech, MCA', 'C#, Azure, SQL', '8.0', '')");
        db.execSQL("insert into companies values('Amazon', 'SDE', '22 LPA', 'Any Technical Degree (B.Tech, BCA, MCA)', 'Linux, AWS, Java', '7.5', '')");
        db.execSQL("insert into companies values('Apple', 'iOS Developer', '28 LPA', 'B.Tech, M.Tech', 'Swift, Objective-C', '8.0', '')");
        db.execSQL("insert into companies values('Netflix', 'Frontend Engineer', '35 LPA', 'B.Tech, MCA, BCA', 'React, Node.js', '9.0', '')");
        db.execSQL("insert into companies values('Meta', 'Product Engineer', '32 LPA', 'B.Tech, MCA', 'Python, PHP, React', '8.5', '')");
        db.execSQL("insert into companies values('TCS', 'Ninja/Digital', '3.6/7 LPA', 'BCA, B.Sc, B.Tech, MCA', 'C, C++, Java', '6.0', '')");
        db.execSQL("insert into companies values('Infosys', 'Systems Engineer', '4.5 LPA', 'B.A, B.Sc, BCA, B.Tech', 'Java, Python', '6.5', '')");
        db.execSQL("insert into companies values('Wipro', 'Project Engineer', '4.0 LPA', 'B.Sc, BCA, MCA', 'Java, .NET', '6.0', '')");

        // Create default company logins (Standardized Passwords)
        db.execSQL("insert into users values('google_hr', 'hr@google.com', 'google123', 'Google', '', '', '', 'company', 'active', '', '')");
        db.execSQL("insert into users values('microsoft_hr', 'hr@microsoft.com', 'microsoft123', 'Microsoft', '', '', '', 'company', 'active', '', '')");
        db.execSQL("insert into users values('amazon_hr', 'hr@amazon.com', 'amazon123', 'Amazon', '', '', '', 'company', 'active', '', '')");
        db.execSQL("insert into users values('apple_hr', 'hr@apple.com', 'apple123', 'Apple', '', '', '', 'company', 'active', '', '')");
        db.execSQL("insert into users values('netflix_hr', 'hr@netflix.com', 'netflix123', 'Netflix', '', '', '', 'company', 'active', '', '')");
        db.execSQL("insert into users values('meta_hr', 'hr@meta.com', 'meta123', 'Meta', '', '', '', 'company', 'active', '', '')");
        db.execSQL("insert into users values('tcs_hr', 'hr@tcs.com', 'tcs123', 'TCS', '', '', '', 'company', 'active', '', '')");
        db.execSQL("insert into users values('infosys_hr', 'hr@infosys.com', 'infosys123', 'Infosys', '', '', '', 'company', 'active', '', '')");
        db.execSQL("insert into users values('wipro_hr', 'hr@wipro.com', 'wipro123', 'Wipro', '', '', '', 'company', 'active', '', '')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS applications");
        db.execSQL("DROP TABLE IF EXISTS companies");
        db.execSQL("DROP TABLE IF EXISTS slots");
        db.execSQL("DROP TABLE IF EXISTS cgpa_requests");
        onCreate(db);
    }

    // ---------- USER / LOGIN / REGISTER METHODS ----------

    public void register(String username, String email, String password) {
        registerUser(username, email, password, "student");
    }

    public void registerUser(String username, String email, String password, String role) {
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("email", email);
        cv.put("password", password);
        cv.put("fullname", "");
        cv.put("rollno", "");
        cv.put("branch", "");
        cv.put("cgpa", "0");
        cv.put("role", role);
        cv.put("status", "active");
        cv.put("tech_stack", "");
        cv.put("resume_uri", "");
        SQLiteDatabase db = getWritableDatabase();
        db.insert("users", null, cv);
        db.close();
    }

    public String login(String username, String password) {
        String role = "";
        String[] args = {username, password};
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select role from users where username=? and password=? and status='active'", args);
        if (c.moveToFirst()) {
            role = c.getString(0);
        }
        c.close();
        db.close();
        return role;
    }

    public boolean usernameExists(String username) {
        boolean exists = false;
        String[] args = {username};
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select * from users where username=?", args);
        if (c.moveToFirst()) {
            exists = true;
        }
        c.close();
        db.close();
        return exists;
    }

    // ---------- STUDENT PROFILE METHODS ----------

    public void updateProfile(String username, String fullname, String rollno, String branch, String cgpa, String techStack, String resumeUri) {
        ContentValues cv = new ContentValues();
        cv.put("fullname", fullname);
        cv.put("rollno", rollno);
        cv.put("branch", branch);
        cv.put("cgpa", cgpa);
        cv.put("tech_stack", techStack);
        cv.put("resume_uri", resumeUri);
        SQLiteDatabase db = getWritableDatabase();
        db.update("users", cv, "username=?", new String[]{username});
        db.close();
    }

    // returns: [fullname, rollno, branch, cgpa, email, role, tech_stack, resume_uri]
    public String[] getProfile(String username) {
        String[] profile = {"", "", "", "0", "", "", "", ""};
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select fullname, rollno, branch, cgpa, email, role, tech_stack, resume_uri from users where username=?",
                new String[]{username});
        if (c.moveToFirst()) {
            for (int i = 0; i < 8; i++) {
                String val = c.getString(i);
                if (val != null) {
                    profile[i] = val;
                }
            }
        }
        c.close();
        db.close();
        return profile;
    }

    // ---------- APPLICATION METHODS ----------

    public void addApplication(String username, String company, String role, String pkg, String eligibility,
                               String appliedDate, String interviewDate, String interviewTime, String status) {
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("company", company);
        cv.put("role", role);
        cv.put("package", pkg);
        cv.put("eligibility", eligibility);
        cv.put("applieddate", appliedDate);
        cv.put("interviewdate", interviewDate);
        cv.put("interviewtime", interviewTime);
        cv.put("status", status);
        SQLiteDatabase db = getWritableDatabase();
        db.insert("applications", null, cv);
        db.close();
    }

    public boolean alreadyApplied(String username, String company) {
        boolean exists = false;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select * from applications where username=? and company=?",
                new String[]{username, company});
        if (c.moveToFirst()) {
            exists = true;
        }
        c.close();
        db.close();
        return exists;
    }

    // Returns each application as a single string, fields separated by "|"
    // Order: company|role|package|eligibility|applieddate|interviewdate|interviewtime|status|rowid
    public ArrayList<String> getApplications(String username) {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select rowid, company, role, package, eligibility, applieddate, " +
                        "interviewdate, interviewtime, status from applications where username=?",
                new String[]{username});
        if (c.moveToFirst()) {
            do {
                String row = c.getString(1) + "|" + c.getString(2) + "|" + c.getString(3) + "|" +
                        c.getString(4) + "|" + c.getString(5) + "|" + c.getString(6) + "|" +
                        c.getString(7) + "|" + c.getString(8) + "|" + c.getString(0);
                list.add(row);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    // Returns each application as a single string, fields separated by "|"
    // Order: company|role|package|eligibility|applieddate|interviewdate|interviewtime|status|rowid|username
    public ArrayList<String> getAllApplications() {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select rowid, company, role, package, eligibility, applieddate, " +
                        "interviewdate, interviewtime, status, username from applications", null);
        if (c.moveToFirst()) {
            do {
                String row = c.getString(1) + "|" + c.getString(2) + "|" + c.getString(3) + "|" +
                        c.getString(4) + "|" + c.getString(5) + "|" + c.getString(6) + "|" +
                        c.getString(7) + "|" + c.getString(8) + "|" + c.getString(0) + "|" + c.getString(9);
                list.add(row);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    // Returns each application for a specific company
    public ArrayList<String> getCompanyApplications(String companyName) {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select rowid, company, role, package, eligibility, applieddate, " +
                        "interviewdate, interviewtime, status, username from applications where company=?",
                new String[]{companyName});
        if (c.moveToFirst()) {
            do {
                String row = c.getString(1) + "|" + c.getString(2) + "|" + c.getString(3) + "|" +
                        c.getString(4) + "|" + c.getString(5) + "|" + c.getString(6) + "|" +
                        c.getString(7) + "|" + c.getString(8) + "|" + c.getString(0) + "|" + c.getString(9);
                list.add(row);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public void updateStatus(String rowId, String newStatus) {
        ContentValues cv = new ContentValues();
        cv.put("status", newStatus);
        SQLiteDatabase db = getWritableDatabase();
        db.update("applications", cv, "rowid=?", new String[]{rowId});
        db.close();
    }
    public void deleteApplication(String rowId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("applications", "rowid=?", new String[]{rowId});
        db.close();
    }

    // ---------- COMPANY METHODS ----------

    public void addCompany(String name, String role, String pkg, String eligibility, String techStack, String minCgpa, String logoUri) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("role", role);
        cv.put("package", pkg);
        cv.put("eligibility", eligibility);
        cv.put("tech_stack", techStack);
        cv.put("min_cgpa", minCgpa);
        cv.put("logo_uri", logoUri);
        SQLiteDatabase db = getWritableDatabase();
        db.insert("companies", null, cv);
        db.close();
    }

    public ArrayList<String[]> getCompanies() {
        ArrayList<String[]> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select name, role, package, eligibility, tech_stack, min_cgpa, logo_uri from companies", null);
        if (c.moveToFirst()) {
            do {
                list.add(new String[]{c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6)});
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public void deleteCompany(String name) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("companies", "name=?", new String[]{name});
        db.close();
    }

    public void updateCompany(String oldName, String name, String role, String pkg, String eligibility, String techStack, String minCgpa, String logoUri) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("role", role);
        cv.put("package", pkg);
        cv.put("eligibility", eligibility);
        cv.put("tech_stack", techStack);
        cv.put("min_cgpa", minCgpa);
        cv.put("logo_uri", logoUri);
        SQLiteDatabase db = getWritableDatabase();
        db.update("companies", cv, "name=?", new String[]{oldName});
        db.close();
    }

    public String getUserPassword(String username) {
        String pass = "";
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select password from users where username=?", new String[]{username});
        if (c.moveToFirst()) {
            pass = c.getString(0);
        }
        c.close();
        db.close();
        return pass;
    }

    // ---------- NEW ADMIN EXTENDED METHODS ----------

    public ArrayList<String[]> getAllUsers() {
        ArrayList<String[]> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select username, email, fullname, rollno, branch, cgpa, status, tech_stack from users where role='student'", null);
        if (c.moveToFirst()) {
            do {
                list.add(new String[]{c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6), c.getString(7)});
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public void adminUpdateUser(String username, String fullname, String rollno, String branch, String cgpa, String status, String techStack, String resumeUri) {
        ContentValues cv = new ContentValues();
        cv.put("fullname", fullname);
        cv.put("rollno", rollno);
        cv.put("branch", branch);
        cv.put("cgpa", cgpa);
        cv.put("status", status);
        cv.put("tech_stack", techStack);
        cv.put("resume_uri", resumeUri);
        SQLiteDatabase db = getWritableDatabase();
        db.update("users", cv, "username=?", new String[]{username});
        db.close();
    }

    // SLOTS METHODS
    public void addSlot(String company, String date, String time) {
        ContentValues cv = new ContentValues();
        cv.put("company", company);
        cv.put("date", date);
        cv.put("time", time);
        cv.put("is_booked", 0);
        SQLiteDatabase db = getWritableDatabase();
        db.insert("slots", null, cv);
        db.close();
    }

    public ArrayList<String[]> getAvailableSlots(String company) {
        ArrayList<String[]> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select id, date, time from slots where company=? and is_booked=0", new String[]{company});
        if (c.moveToFirst()) {
            do {
                list.add(new String[]{c.getString(0), c.getString(1), c.getString(2)});
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public void bookSlot(String slotId) {
        ContentValues cv = new ContentValues();
        cv.put("is_booked", 1);
        SQLiteDatabase db = getWritableDatabase();
        db.update("slots", cv, "id=?", new String[]{slotId});
        db.close();
    }

    public void deleteSlot(String slotId) {
        SQLiteDatabase db = getWritableDatabase();
        
        // 1. Get slot details to find associated applications
        Cursor c = db.rawQuery("select company, date, time from slots where id=?", new String[]{slotId});
        if (c.moveToFirst()) {
            String company = c.getString(0);
            String date = c.getString(1);
            String time = c.getString(2);
            
            // 2. Delete applications matching this slot
            db.delete("applications", "company=? and interviewdate=? and interviewtime=?", 
                    new String[]{company, date, time});
        }
        c.close();

        // 3. Delete the slot itself
        db.delete("slots", "id=?", new String[]{slotId});
        db.close();
    }

    // CGPA REQUEST METHODS
    public void addCgpaRequest(String username, String requestedCgpa) {
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("requested_cgpa", requestedCgpa);
        cv.put("status", "Pending");
        SQLiteDatabase db = getWritableDatabase();
        db.insert("cgpa_requests", null, cv);
        db.close();
    }

    public ArrayList<String[]> getAllCgpaRequests() {
        ArrayList<String[]> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select id, username, requested_cgpa, status from cgpa_requests where status='Pending'", null);
        if (c.moveToFirst()) {
            do {
                list.add(new String[]{c.getString(0), c.getString(1), c.getString(2), c.getString(3)});
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public void updateCgpaRequestStatus(String requestId, String newStatus) {
        ContentValues cv = new ContentValues();
        cv.put("status", newStatus);
        SQLiteDatabase db = getWritableDatabase();
        db.update("cgpa_requests", cv, "id=?", new String[]{requestId});
        db.close();
    }

    public void updateCgpa(String username, String cgpa) {
        ContentValues cv = new ContentValues();
        cv.put("cgpa", cgpa);
        SQLiteDatabase db = getWritableDatabase();
        db.update("users", cv, "username=?", new String[]{username});
        db.close();
    }

    public void updatePassword(String username, String newPassword) {
        ContentValues cv = new ContentValues();
        cv.put("password", newPassword);
        SQLiteDatabase db = getWritableDatabase();
        db.update("users", cv, "username=?", new String[]{username});
        db.close();
    }

    public ArrayList<String[]> getCompanySlots(String company) {
        ArrayList<String[]> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select id, date, time, is_booked from slots where company=?", new String[]{company});
        if (c.moveToFirst()) {
            do {
                list.add(new String[]{c.getString(0), c.getString(1), c.getString(2), c.getString(3)});
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public ArrayList<String[]> getAllSlots() {
        ArrayList<String[]> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select id, company, date, time, is_booked from slots", null);
        if (c.moveToFirst()) {
            do {
                list.add(new String[]{c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4)});
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public String getLatestCgpaRequestStatus(String username) {
        String status = "";
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select status, requested_cgpa from cgpa_requests where username=? order by id desc limit 1", new String[]{username});
        if (c != null && c.moveToFirst()) {
            status = c.getString(0) + " (" + c.getString(1) + ")";
        }
        if (c != null) c.close();
        db.close();
        return status;
    }
}
