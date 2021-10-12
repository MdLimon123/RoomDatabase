package com.example.roomdbapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.TextView;

import com.example.roomdbapp.adapters.NotesAdapter;
import com.example.roomdbapp.notedb.Note;
import com.example.roomdbapp.notedb.NoteDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class NoteListActivity extends AppCompatActivity implements NotesAdapter.OnNoteItemClick {

    private TextView textViewMsg;
    private RecyclerView recyclerView;
    private NoteDatabase noteDatabase;
    private List<Note> notes;
    private NotesAdapter notesAdapter;
    private int pos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        displayList();

    }

    private void displayList() {

        noteDatabase = NoteDatabase.getInstance(NoteListActivity.this);
        new RetrieveTask(this).execute();
    }



    @Override
    public void onNoteClick(int pos) {

        new AlertDialog.Builder(NoteListActivity.this)
                .setItems(new String[]{"Delete", "Update"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        switch (i){
                            case 0:
                                noteDatabase.getNoteDao().deleteNote(notes.get(pos));
                                notes.remove(pos);
                                listVisibility();
                                break;
                            case 1:
                                NoteListActivity.this.pos = pos;
                                startActivityForResult(
                                        new Intent(NoteListActivity.this,
                                                AddNoteActivity.class).putExtra("note",notes.get(pos))
                                        ,100);
                                break;
                        }


                    }
                }).show();

    }

    private static class RetrieveTask extends AsyncTask<Void,Void,List<Note>>{

        private WeakReference<NoteListActivity> activityReference;

        RetrieveTask(NoteListActivity context){
            activityReference = new WeakReference<>(context);
        }


        @Override
        protected List<Note> doInBackground(Void... voids) {
            if (activityReference.get() != null){
                return activityReference.get().noteDatabase.getNoteDao().getNotes();
            }
            else
              return null;
        }


        @Override
        protected void onPostExecute(List<Note> notes) {
            if (notes != null && notes.size() > 0){
                activityReference.get().notes.clear();
                activityReference.get().notes.addAll(notes);


                activityReference.get().textViewMsg.setVisibility(View.GONE);
                activityReference.get().notesAdapter.notifyDataSetChanged();

            }

        }
    }

    private void initializeViews() {
        //setSupportActionBar(toolbar);
        textViewMsg = findViewById(R.id.tv_empty);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(listener);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(NoteListActivity.this));
        notes = new ArrayList<>();
        notesAdapter = new NotesAdapter(notes,NoteListActivity.this);
        recyclerView.setAdapter(notesAdapter);



    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivityForResult(new Intent(NoteListActivity.this, AddNoteActivity.class),100);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 100 && resultCode > 0){

            if (resultCode == 1){
                notes.add((Note) data.getSerializableExtra("note"));

            }else if(resultCode == 2){
                notes.set(pos,(Note) data.getSerializableExtra("note"));
            }
            listVisibility();
        }
    }

    private void listVisibility() {

        int emptyMsgVisibility = View.GONE;
        if (notes.size()>0){

            if (textViewMsg.getVisibility() == View.GONE)
                emptyMsgVisibility = View.VISIBLE;
        }

        textViewMsg.setVisibility(emptyMsgVisibility);
        notesAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        noteDatabase.cleanUp();
        super.onDestroy();
    }
}