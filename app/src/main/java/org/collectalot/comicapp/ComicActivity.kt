package org.collectalot.comicapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_comic.*
import org.bson.types.ObjectId
import org.collectalot.comicapp.model.comic

class ComicActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var titleEdit: EditText
    private lateinit var subtitleEdit: EditText
    private lateinit var authorEdit: EditText
    private lateinit var publisherEdit: EditText
    private lateinit var yearEdit: EditText
    private lateinit var editionEdit: EditText
    private lateinit var printingEdit: EditText
    private lateinit var countryEdit: EditText
    private lateinit var bindingEdit: EditText
    private lateinit var isbnEdit: EditText
    private lateinit var saveButton: FloatingActionButton
    private lateinit var id: ObjectId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comic)
        titleEdit = findViewById(R.id.editTitle)
        subtitleEdit = findViewById(R.id.editSubTitle)
        authorEdit = findViewById(R.id.editAuthor)
        publisherEdit = findViewById(R.id.editPublisher)
        yearEdit = findViewById(R.id.editYear)
        editionEdit = findViewById(R.id.editEdition)
        printingEdit = findViewById(R.id.editPrinting)
        countryEdit = findViewById(R.id.editCountry)
        bindingEdit = findViewById(R.id.editBinding)
        isbnEdit = findViewById(R.id.editISBN)
        saveButton = findViewById(R.id.saveComicButton)
        saveButton.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        if(intent.extras != null && intent.extras?.get("comicId") != null) {
            Realm.getDefaultInstance().use {
                id = intent.extras?.get("comicId") as ObjectId
                //TODO: save the reference to the comic so we don't have to look it up each time
                val comic = it.where(comic::class.java).equalTo("_id", id).findFirst()
                titleEdit.setText(comic?.title)
                subtitleEdit.setText(comic?.subtitle)
                //TODO: ikke i model endnu authorEdit.setText(comic?.)
                publisherEdit.setText(comic?.publisher)
                yearEdit.setText(comic?.year)
                editionEdit.setText(comic?.edition)
                //TODO: ikke i model endnu printingEdit.setText(comic?.)
                countryEdit.setText(comic?.country)
                //TODO: ikke i model endnu bindingEdit.setText(comic?.)
                //TODO: ikke i model endnu isbnEdit = bindingEdit.setText(comic?.)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.comic_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.comicActivityMenuDelete -> {
                if (this::id.isInitialized) {
                    var realm: Realm = Realm.getDefaultInstance()
                    try {
                        realm.executeTransactionAsync() { t ->
                            var c: comic = t.where<comic>().equalTo("_id", id).findFirst()!!
                            c.deleteFromRealm()
                        }
                    } finally {
                        realm.close()
                    }
                }
                finish()//when saved close the window
                return true
            }
            else -> {
                return false
            }
        }
    }

    override fun onClick(v: View?) {
        var realm: Realm = Realm.getDefaultInstance()
        try {
            realm.executeTransactionAsync() { t ->
                var c: comic
                if (this::id.isInitialized) {
                    c = t.where<comic>().equalTo("_id", id).findFirst()!!
                } else {
                    c = t.createObject(comic::class.java, ObjectId())
                }
                c.title = titleEdit.text.toString()
                c.subtitle = subtitleEdit.text.toString()
                c.publisher = publisherEdit.text.toString()
                c.year = yearEdit.text.toString()
                c.edition = editionEdit.text.toString()
                c.country = countryEdit.text.toString()
                c.owner_id = comicApp.currentUser()!!.id.toString()
            }
        } finally {
            realm.close()
            finish()//when saved close the window
        }
    }
}