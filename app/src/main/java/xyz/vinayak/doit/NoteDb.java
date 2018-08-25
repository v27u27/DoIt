package xyz.vinayak.doit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import static xyz.vinayak.doit.Constants.COLUMN_CATEGORY;
import static xyz.vinayak.doit.Constants.COLUMN_DATETIME;
import static xyz.vinayak.doit.Constants.COLUMN_DESCRIPTION;
import static xyz.vinayak.doit.Constants.COLUMN_ID;
import static xyz.vinayak.doit.Constants.COLUMN_MARKDONE;
import static xyz.vinayak.doit.Constants.COLUMN_REMINDERDATETIME;
import static xyz.vinayak.doit.Constants.COLUMN_TITLE;
import static xyz.vinayak.doit.Constants.COMMA;
import static xyz.vinayak.doit.Constants.CREATE;
import static xyz.vinayak.doit.Constants.DB_NAME;
import static xyz.vinayak.doit.Constants.INTEGER;
import static xyz.vinayak.doit.Constants.LBR;
import static xyz.vinayak.doit.Constants.NOT_NULL;
import static xyz.vinayak.doit.Constants.PRIMARY_KEY;
import static xyz.vinayak.doit.Constants.RBR;
import static xyz.vinayak.doit.Constants.TABLE_NAME;
import static xyz.vinayak.doit.Constants.TERMINATION;
import static xyz.vinayak.doit.Constants.TEXT;

public class NoteDb extends SQLiteOpenHelper {
    private static final String TAG = "NoteDb";
    public NoteDb(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = CREATE + TABLE_NAME +
                RBR +
                COLUMN_ID + INTEGER + PRIMARY_KEY + COMMA +
                COLUMN_TITLE + TEXT + NOT_NULL + COMMA +
                COLUMN_DATETIME + TEXT + NOT_NULL + COMMA +
                COLUMN_CATEGORY + INTEGER + NOT_NULL + COMMA +
                COLUMN_DESCRIPTION + TEXT + COMMA +
                COLUMN_MARKDONE + TEXT + COMMA +
                COLUMN_REMINDERDATETIME + INTEGER +
                LBR +
                TERMINATION;

        Log.e(TAG, "onCreate: " + query );
        sqLiteDatabase.execSQL(query);
    }

    public long insertNote(Todo todo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID, todo.getId());
        contentValues.put(COLUMN_TITLE, todo.getNoteText());
        contentValues.put(COLUMN_DATETIME, todo.getNoteDateTime());
        contentValues.put(COLUMN_CATEGORY, todo.getCategory());
        contentValues.put(COLUMN_DESCRIPTION, todo.getNoteDescription());
        contentValues.put(COLUMN_MARKDONE, String.valueOf(todo.isMarkDone()));
        contentValues.put(COLUMN_REMINDERDATETIME, todo.getReminderDateTime());

        long id = getWritableDatabase().insert(TABLE_NAME, null, contentValues);
        return id;
    }


    public ArrayList<Todo> getAllTodos(){
        ArrayList<Todo> notes= new ArrayList<>();

        Cursor cursor = getReadableDatabase().query(TABLE_NAME,null,null,null,null,null,null);

        while (cursor.moveToNext()) {
            Long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID.trim()));
            String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE.trim()));
            String dateTime = cursor.getString(cursor.getColumnIndex(COLUMN_DATETIME.trim()));
            Integer category = cursor.getInt(cursor.getColumnIndex(COLUMN_CATEGORY.trim()));
            String description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION.trim()));
            Boolean isDone = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(COLUMN_MARKDONE.trim())));
            Long reminderDateTime = cursor.getLong(cursor.getColumnIndex(COLUMN_REMINDERDATETIME.trim()));
            Todo todo = new Todo(id, title, dateTime, description, category);
            if(!reminderDateTime.equals(0L)){
                todo.setReminderDateTime(reminderDateTime);
            }
            if(isDone) todo.setMarkDone();
            notes.add(todo);
        }

        cursor.close();
        return notes;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}