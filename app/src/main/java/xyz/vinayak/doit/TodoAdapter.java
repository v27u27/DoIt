package xyz.vinayak.doit;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static android.view.View.GONE;
import static xyz.vinayak.doit.MainActivity.ivNoNote;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.ViewHolder> {

    public static final int EDITNOTE_ACTIVITY_REQUEST_CODE = 0032100;

    Context ctx;
    Context ctx2;
    ArrayList<Todo> todoArrayList;
    NoteDb noteDb;
    boolean visible = false;

    public TodoAdapter(Context ctx, ArrayList<Todo> todoArrayList, NoteDb noteDb) {
        this.ctx = ctx;
        this.todoArrayList = todoArrayList;
        this.noteDb = noteDb;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(ctx);
        View v = li.inflate(R.layout.note_row, parent, false);
        ctx2 = parent.getContext();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Todo currentTodo = todoArrayList.get(position);
        holder.tvTodoTitle.setText(currentTodo.getNoteText());
        if (currentTodo.isMarkDone()) {
            holder.tvTodoTitle.setPaintFlags(holder.tvTodoTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else if(!currentTodo.isMarkDone()){
            holder.tvTodoTitle.setPaintFlags(holder.tvTodoTitle.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        }
        holder.tvTodoCreationDate.setText(currentTodo.getNoteDateTime());
        holder.cardView.setCardBackgroundColor(currentTodo.getCatgeoryColor());
        if (!currentTodo.getNoteDescription().trim().isEmpty()) {
            holder.tvNoteDescription.setText(currentTodo.getNoteDescription());
        }
        if (currentTodo.getReminderDateTime() != null) {
            holder.tvReminderDate.setText(getReminderDate(currentTodo.getReminderDateTime()));
            holder.llreminder.setVisibility(View.VISIBLE);
        }

    }

    private String getReminderDate(Long reminderDateTime) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(reminderDateTime);
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy hh:mm aaa");
        return df.format(c.getTime());
    }

    @Override
    public int getItemCount() {
        return todoArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTodoTitle, tvTodoCreationDate, tvReminderDate, tvNoteDescription;
        ImageView ivTodoAction;
        CardView cardView;
        LinearLayout llreminder;

        public ViewHolder(View v) {
            super(v);

            tvTodoTitle = v.findViewById(R.id.tvTodoText);
            tvTodoCreationDate = v.findViewById(R.id.tvTodoCreationDateTime);
            tvReminderDate = v.findViewById(R.id.tvReminderDateTime);
            ivTodoAction = v.findViewById(R.id.ivTodoAction);
            cardView = v.findViewById(R.id.card_view);
            llreminder = v.findViewById(R.id.llreminder);
            tvNoteDescription = v.findViewById(R.id.tvNoteDescription);

            Typeface typeface = Typeface.createFromAsset(ctx.getAssets(), "fonts/Avenir-Light.ttf");
            Typeface typeface2 = Typeface.createFromAsset(ctx.getAssets(), "fonts/Avenir-Medium.ttf");
            tvTodoTitle.setTypeface(typeface2);
            tvTodoCreationDate.setTypeface(typeface);
            tvReminderDate.setTypeface(typeface);

            ivTodoAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Todo currentTodo = todoArrayList.get(getAdapterPosition());
                    Intent intent = new Intent(ctx, WallpaperActivity.class);
                    intent.putExtra("todoObject", currentTodo);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(intent);
                }
            });

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!visible && !tvNoteDescription.getText().toString().isEmpty()) {
                        tvNoteDescription.setVisibility(View.VISIBLE);
                    } else if (visible) {
                        tvNoteDescription.setVisibility(GONE);
                    }
                    visible = !visible;
                }
            });

            cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Context wrapper = new ContextThemeWrapper(ctx, R.style.PopupMenu);
                    PopupMenu popup = new PopupMenu(wrapper, v);

                    popup.inflate(R.menu.context_menu_list);

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            Todo todo = todoArrayList.get(getAdapterPosition());
                            switch (item.getItemId()) {
                                case R.id.done:
                                    todo.setMarkDone();
                                    ContentValues cv = new ContentValues();
                                    cv.put(Constants.COLUMN_MARKDONE,String.valueOf(todo.isMarkDone()));
                                    noteDb.getWritableDatabase().update(Constants.TABLE_NAME, cv, Constants.COLUMN_ID + " = ? ", new String[]{String.valueOf(todo.getId())});
                                    notifyItemChanged(getAdapterPosition());
                                    return true;
                                case R.id.delete:
                                    noteDb.getWritableDatabase().delete(Constants.TABLE_NAME, Constants.COLUMN_ID + " = ?", new String[]{String.valueOf(todo.getId())});
                                    todoArrayList.remove(getAdapterPosition());
                                    notifyItemRemoved(getAdapterPosition());
                                    Toast.makeText(ctx,"Deleted",Toast.LENGTH_SHORT).show();
                                    recyclerViewUpdateData();
                                    return true;
                                case R.id.edit:
                                    Intent intent = new Intent(ctx, EditNoteActivity.class);
                                    intent.putExtra("position", getAdapterPosition());
                                    intent.putExtra("todoObject", todo);
                                    ((Activity) ctx2).startActivityForResult(intent, EDITNOTE_ACTIVITY_REQUEST_CODE);
                                    recyclerViewUpdateData();
                                    return true;
                            }
                            return true;
                        }
                    });

                    popup.show();
                    return false;
                }
            });


        }

    }

    private void recyclerViewUpdateData() {
        if (todoArrayList.size() > 0) {
            ivNoNote.setVisibility(View.GONE);
        } else
            ivNoNote.setVisibility(View.VISIBLE);
    }
}
