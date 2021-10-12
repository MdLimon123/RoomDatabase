package com.example.roomdbapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.roomdbapp.notedb.Note;
import com.example.roomdbapp.notedb.NoteDatabase;
import com.google.android.material.textfield.TextInputEditText;

import java.lang.ref.WeakReference;

public class AddNoteActivity extends AppCompatActivity {

    private TextInputEditText ed_title,ed_content;
    private Button button;
    private boolean update;


    private NoteDatabase noteDatabase;
    private Note note;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        ed_title = findViewById(R.id.ed_title);
        ed_content = findViewById(R.id.et_content);
        button = findViewById(R.id.but_save);


        noteDatabase = NoteDatabase.getInstance(AddNoteActivity.this);

        if ((note = (Note) getIntent().getSerializableExtra("note")) != null){

            getSupportActionBar().setTitle("Update Note");
            update = true;
            button.setText("Update");
            ed_title.setText(note.getTitle());
            ed_content.setText(note.getContent());

        }


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (update){
                    note.setContent(ed_content.getText().toString());
                    note.setTitle(ed_title.getText().toString());
                    noteDatabase.getNoteDao().updateNote(note);
                    setResult(note,2);
                }else{
                    note = new Note(ed_content.getText().toString(),
                            ed_title.getText().toString());
                    new InsertTask(AddNoteActivity.this,note).execute();

                }
            }
        });
    }

    private void setResult(Note note, int flag){

        setResult(flag, new Intent().putExtra("note",note));
        finish();
    }

    private static class InsertTask extends AsyncTask<Void,Void,Boolean>{

        private WeakReference<AddNoteActivity> activityWeakReference;
        private Note note;



        InsertTask(AddNoteActivity content, Note note){
            activityWeakReference = new WeakReference<>(content);
            this.note = note;

        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            long j = activityWeakReference.get().noteDatabase.getNoteDao().insertNote(note);
            note.setNote_id(j);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean){
                activityWeakReference.get().setResult(note,1);
                activityWeakReference.get().finish();

            }
        }
    }
}