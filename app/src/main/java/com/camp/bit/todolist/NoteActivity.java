package com.camp.bit.todolist;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.camp.bit.todolist.beans.Priority;
import com.camp.bit.todolist.db.TodoContract;
import com.camp.bit.todolist.db.TodoDbHelper;


public class NoteActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editText;
    private Button addBtn;
    private ImageView[] prioritieViews;
    SQLiteDatabase database;
    TodoDbHelper todoDbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        setTitle(R.string.take_a_note);

        todoDbHelper = new TodoDbHelper(this);
        database = todoDbHelper.getWritableDatabase();

        editText = findViewById(R.id.edit_text);
        editText.setFocusable(true);
        editText.requestFocus();
        prioritieViews = new ImageView[3];
        prioritieViews[0] = findViewById(R.id.iv_high_priority);
        prioritieViews[1] = findViewById(R.id.iv_medium_priority);
        prioritieViews[2] = findViewById(R.id.iv_low_priority);
        selectPriority(R.id.iv_medium_priority);
        for (ImageView imageView : prioritieViews) {
            imageView.setOnClickListener(this);
        }

        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(editText, 0);
        }

        addBtn = findViewById(R.id.btn_add);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence content = editText.getText();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(NoteActivity.this,
                            "No content to add", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean succeed = saveNote2Database(content.toString().trim());
                if (succeed) {
                    Toast.makeText(NoteActivity.this,
                            "Note added", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                } else {
                    Toast.makeText(NoteActivity.this,
                            "Error", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean saveNote2Database(String content) {
        // TODO 插入一条新数据，返回是否插入成功

        try{
            ContentValues values = new ContentValues();
            values.put(TodoContract.FeedEntry.COLUMN_NAME_CONTENT,content);
            values.put(TodoContract.FeedEntry.COLUMN_NAME_DATE,(System.currentTimeMillis()));
            values.put(TodoContract.FeedEntry.COLUMN_NAME_STATE,0);
            values.put(TodoContract.FeedEntry.COLUMN_NAME_PRIORITY, selectedPriority);
            long newRowId = database.insert(TodoContract.FeedEntry.TABLE_NAME,null,values);
            Log.i(NoteActivity.class.getSimpleName(), "row id" + newRowId);
        }catch (Exception e){
            return false;
        }

        return true;
    }

    private void selectPriority(int selectViewId) {
        PropertyValuesHolder zoomInX = PropertyValuesHolder.ofFloat(View.SCALE_X, .6f, 1f);
        PropertyValuesHolder zoomInY = PropertyValuesHolder.ofFloat(View.SCALE_Y, .6f, 1f);
        PropertyValuesHolder zoomOutX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, .6f);
        PropertyValuesHolder zoomOutY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, .6f);


        for (View view : prioritieViews) {
            if (view.getId() == selectViewId) {
                // zoomOut
                ObjectAnimator zoomInAniamtor = ObjectAnimator.ofPropertyValuesHolder(view, zoomOutX, zoomOutY);
                zoomInAniamtor.setInterpolator(new AccelerateDecelerateInterpolator());
                zoomInAniamtor.start();
            } else if (view.getScaleX() < 0.7f) {
                // zoomIn
                ObjectAnimator zoomInAniamtor = ObjectAnimator.ofPropertyValuesHolder(view, zoomInX, zoomInY);
                zoomInAniamtor.setInterpolator(new AccelerateDecelerateInterpolator());
                zoomInAniamtor.start();
            }
        }
    }

    private int selectedPriority = Priority.MEDIUM.intValue;

    @Override
    public void onClick(View view) {
        selectPriority(view.getId());
        switch (view.getId()) {
            case R.id.iv_high_priority:
                selectedPriority = Priority.HIGH.intValue;
                break;
            case R.id.iv_medium_priority:
                selectedPriority = Priority.MEDIUM.intValue;
                break;
            case R.id.iv_low_priority:
                selectedPriority = Priority.LOW.intValue;
                break;
        }
    }
}
