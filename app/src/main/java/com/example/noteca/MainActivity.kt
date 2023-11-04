package com.example.noteca

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Note
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.noteca.adapter.NotesAdapter
import com.example.noteca.database.NoteDatabase
import com.example.noteca.databinding.ActivityMainBinding
import com.example.noteca.models.NoteViewModel

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), NotesAdapter.NotesClickListener,
    PopupMenu.OnMenuItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: NoteDatabase
    lateinit var viewModel: NoteViewModel
    lateinit var adapter: NotesAdapter
    lateinit var selectedNote: com.example.noteca.models.Note
    private val updateNote = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == Activity.RESULT_OK) {

            val note = result.data?.getSerializableExtra("note") as? com.example.noteca.models.Note

            if (note != null) {

                viewModel.updateNote(note)

            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()

        viewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application))
            .get(NoteViewModel::class.java)

        viewModel.allnotes.observe(this) { list ->
            list?.let {

                adapter.updateList(list)
            }
        }
        database = NoteDatabase.getDatabase(this)
    }

    private fun initUI() {

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
        adapter = NotesAdapter(this, this)
        binding.recyclerView.adapter = adapter

        val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                val note = result.data?.getSerializableExtra("note") as? com.example.noteca.models.Note

                if (note != null) {
                    viewModel.insertNote(note)
                }
            }
        }
        binding.fbAddNote.setOnClickListener{
            val intent = Intent(this, AddNote::class.java)
            getContent.launch(intent)
        }
        binding
    }

    override fun onItemClicked(note: com.example.noteca.models.Note) {
        val intent = Intent(this@MainActivity, AddNote::class.java)
        intent.putExtra("current_note", note)
        updateNote.launch(intent)
    }

    override fun onLongItemClicked(note: com.example.noteca.models.Note, cardView: CardView) {
        selectedNote = note
        popUpDisplay(cardView)
    }



    private fun popUpDisplay(cardView: CardView) {

        val popUp = PopupMenu(this, cardView)

        popUp.setOnMenuItemClickListener(this@MainActivity)
        popUp.inflate(R.menu.pop_up_menu)
        popUp.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {

        if (item?.itemId == R.id.delete_note) {
            viewModel.deleteNote(selectedNote)
            return true
        }
        return false
    }
}