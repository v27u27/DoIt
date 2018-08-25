package xyz.vinayak.doit;

import android.graphics.Color;

import java.io.Serializable;

public class Todo implements Serializable {

    private final int NOTE_CATEGORY_URGENT = 0;
    private final int NOTE_CATEGORY_WORK = 1;
    private final int NOTE_CATEGORY_PERSONAL = 2;
    private final int NOTE_CATEGORY_OTHERS = 3;

    long id;
    private String noteText, noteDateTime, noteDescription;
    private int category;
    private Long reminderDateTime = null;
    boolean markDone;

    public boolean isMarkDone() {
        return markDone;
    }

    public void setMarkDone() {
        this.markDone = !markDone;
    }

    public Todo(long id, String noteText, String noteDateTime, String noteDescription, int category) {
        this.id = id;
        this.noteText = noteText;
        this.noteDateTime = noteDateTime;
        this.noteDescription = noteDescription;
        this.category = category;
        markDone = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public String getNoteDateTime() {
        return noteDateTime;
    }

    public void setNoteDateTime(String noteDateTime) {
        this.noteDateTime = noteDateTime;
    }

    public String getNoteDescription() {
        return noteDescription;
    }

    public void setNoteDescription(String noteDescription) {
        this.noteDescription = noteDescription;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public Long getReminderDateTime() {
        return reminderDateTime;
    }

    public void setReminderDateTime(long reminderDateTime) {
        this.reminderDateTime = reminderDateTime;
    }

    public int getCatgeoryColor() {
        String color = "#000000";

        if (getCategory() == NOTE_CATEGORY_URGENT)
            color = "#DEF2FF";
        else if (getCategory() == NOTE_CATEGORY_WORK)
            color = "#D3F3D2";
        else if (getCategory() == NOTE_CATEGORY_PERSONAL)
            color = "#FFECDE";
        else if (getCategory() == NOTE_CATEGORY_OTHERS)
            color = "#F7F6C4";

        return Color.parseColor(color);
    }

}
