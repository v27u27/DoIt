package xyz.vinayak.doit;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView rv;
    public  static ImageView ivNoNote;
    ArrayList<Todo> todoArrayList;
    TodoAdapter todoAdapter;
    CoordinatorLayout coordinatorLayout;
    NoteDb noteDb;
    public static final int ADDNOTE_ACTIVITY_REQUEST_CODE = 0012300;
    public static final int  EDITNOTE_ACTIVITY_REQUEST_CODE = 0032100;
    SharedPreferences prefs = null;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("xyz.vinayak.doit", MODE_PRIVATE);

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        MyReceiver mReceiver = new MyReceiver (this);
        registerReceiver(mReceiver, filter);

        noteDb = new NoteDb(this);

        todoArrayList = noteDb.getAllTodos();

        coordinatorLayout = findViewById(R.id.coordinatorLayoutMain);
        rv = findViewById(R.id.recyclerView);
        ivNoNote = findViewById(R.id.ivNoNotes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rv.setLayoutManager(new LinearLayoutManager(this));

        if (todoArrayList.size() > 0) {
            ivNoNote.setVisibility(View.GONE);
        }


        fab = findViewById(R.id.fab);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), AddNewNoteActivity.class);
                startActivityForResult(intent, ADDNOTE_ACTIVITY_REQUEST_CODE);
            }
        });

        todoAdapter = new TodoAdapter(getBaseContext(), todoArrayList, noteDb);

        rv.setAdapter(todoAdapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADDNOTE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            todoArrayList.add((Todo) data.getSerializableExtra("newtodo"));
            Snackbar.make(coordinatorLayout, "Note Saved", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            recyclerViewUpdateData();
        }
        else if (requestCode == EDITNOTE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            int index = data.getIntExtra("position",-1);
            Todo newTodo = (Todo) data.getSerializableExtra("newtodo");
            noteDb.getWritableDatabase().delete(Constants.TABLE_NAME, Constants.COLUMN_ID + " = ?", new String[]{String.valueOf(todoArrayList.get(index).getId())});
            todoArrayList.remove(index);
            todoArrayList.add(index, newTodo);
            recyclerViewUpdateData();
            //Handle here deletion of Todo and addition of new todo in database after editing is Done

            Snackbar.make(coordinatorLayout, "Note Saved with Changes", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();

        }
    }

    private void recyclerViewUpdateData() {
        todoAdapter.notifyDataSetChanged();
        if (todoArrayList.size() > 0) {
            ivNoNote.setVisibility(View.GONE);
        } else
            ivNoNote.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onStop() {
        for (Todo todo : todoArrayList){
            noteDb.insertNote(todo);
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (prefs.getBoolean("firstrun", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs

//                    TapTargetView.showFor(this,                 // `this` is an Activity
//                TapTarget.forView(fab, "Add New Note", "Click this button to create new Note"),
//                        // All options below are optional
//
//                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
//                    @Override
//                    public void onTargetClick(TapTargetView view) {
//                        super.onTargetClick(view);      // This call is optional
//                    }
//                });

            prefs.edit().putBoolean("firstrun", false).commit();
        }
    }
}
