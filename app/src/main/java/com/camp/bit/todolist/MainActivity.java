package com.camp.bit.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.camp.bit.todolist.beans.Note;
import com.camp.bit.todolist.beans.State;
import com.camp.bit.todolist.db.TodoContract;
import com.camp.bit.todolist.db.TodoDbHelper;
import com.camp.bit.todolist.debug.DebugActivity;
import com.camp.bit.todolist.ui.NoteListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1002;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;

    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
            }
        });
        recyclerView.setAdapter(notesAdapter);

        TodoDbHelper todoDbHelper = new TodoDbHelper(MainActivity.this);
        database = todoDbHelper.getReadableDatabase();

        notesAdapter.refresh(loadNotesFromDatabase());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private List<Note> loadNotesFromDatabase() {
        // TODO 从数据库中查询数据，并转换成 JavaBeans

//        TodoDbHelper todoDbHelper = new TodoDbHelper(getBaseContext());
//        SQLiteDatabase db = todoDbHelper.getReadableDatabase();
//
        List<Note> result = new LinkedList<>();
        String[] projection = {
                BaseColumns._ID,
                TodoContract.FeedEntry.COLUMN_NAME_DATE,
                TodoContract.FeedEntry.COLUMN_NAME_STATE,
                TodoContract.FeedEntry.COLUMN_NAME_CONTENT
        };

        String selection = TodoContract.FeedEntry._ID + " = ?";
        String[] selectionArgs = {"*"};
        String sortOrder = TodoContract.FeedEntry._ID + " DESC";
        Cursor cursor = null;
        try {
             cursor = database.query(TodoContract.FeedEntry.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    sortOrder);

            while (cursor.moveToNext()) {
                String content = cursor.getString(cursor.getColumnIndex(TodoContract.FeedEntry.COLUMN_NAME_CONTENT));

                long dateMs = cursor.getLong(cursor.getColumnIndex(TodoContract.FeedEntry.COLUMN_NAME_DATE));
                int intState = cursor.getInt(cursor.getColumnIndex(TodoContract.FeedEntry.COLUMN_NAME_STATE));
                int ID = cursor.getInt(cursor.getColumnIndex(TodoContract.FeedEntry._ID));

                Note note = new Note(ID);
                note.setContent(content);
                note.setDate(new Date(dateMs));
                note.setState(State.from(intState));
                result.add(note);
            }
        }finally {
            if(cursor != null)
                cursor.close();
        }
        return result;
    }

    private void deleteNote(Note note) {
        // TODO 删除数据
        String selection = TodoContract.FeedEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(note.id) };
        int deletedRows = database.delete(TodoContract.FeedEntry.TABLE_NAME,selection,selectionArgs);
        notesAdapter.refresh(loadNotesFromDatabase());

    }

    private void updateNode(Note note) {
        // 更新数据
        ContentValues contentValues = new ContentValues();
        contentValues.put(TodoContract.FeedEntry.COLUMN_NAME_STATE,
                note.getState().intValue);

        String selction = TodoContract.FeedEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(note.id)};

        int count = database.update(TodoContract.FeedEntry.TABLE_NAME,
                contentValues,
                selction,
                selectionArgs);
        Log.i(MainActivity.class.getSimpleName(), "count " + count);
        notesAdapter.refresh(loadNotesFromDatabase());
    }

}
