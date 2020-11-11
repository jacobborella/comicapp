package org.collectalot.comicapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.mongodb.User
import io.realm.kotlin.where
import io.realm.mongodb.sync.SyncConfiguration
import org.bson.types.ObjectId
import org.collectalot.comicapp.model.comic
import org.collectalot.comicapp.ui.ComicsRecyclerAdapter

class ComicListActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mRealm: Realm
    private var user: User? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ComicsRecyclerAdapter
    private lateinit var addComicButton: FloatingActionButton
    private lateinit var titleSearch: EditText


    override fun onStart() {
        super.onStart()
        try {
            user = comicApp.currentUser()
        } catch (e: IllegalStateException) {
            Log.w("no valid user", e)
        }
        if (user == null) {
            // if no user is currently logged in, start the login activity so the user can authenticate
            startActivity(Intent(this, LoginActivity::class.java))
        }
        else {
            //TODO: you don't want all of the initialization to be done each time you come back from a comic detail view
            val currentUser = user!!
            val config = SyncConfiguration.Builder(currentUser, currentUser.id.toString())
                .waitForInitialRemoteData()
                .allowQueriesOnUiThread(true)
                .build()
            // save this configuration as the default for this entire app so other activities and threads can open their own realm instances
            Realm.setDefaultConfiguration(config)

            // Sync all realm changes via a new instance, and when that instance has been successfully created connect it to an on-screen list (a recycler view)
            Realm.getInstanceAsync(config, object: Realm.Callback() {
                override fun onSuccess(realm: Realm) {
                    // since this realm should live exactly as long as this activity, assign the realm to a member variable
                    mRealm = realm
                    setUpRecyclerView(realm)
                }
            })
        }
        titleSearch.doOnTextChanged { text, start, before, count ->
            //TODO: Does this need to be optimized? One new query each time data is changed.
            adapter.updateData(mRealm.where<comic>().beginsWith("title", titleSearch.text.toString()).sort("title", Sort.ASCENDING, "subtitle", Sort.ASCENDING).findAllAsync())
        }
        addComicButton.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, ComicActivity::class.java))
        })
    }

    override fun onStop() {
        super.onStop()
        if(this::mRealm.isInitialized) {
            user.run {
                mRealm.close()
            }
        }
        recyclerView.adapter = null
    }
    override fun onDestroy() {
        super.onDestroy()
        recyclerView.adapter = null
        // if a user hasn't logged out when the activity exits, still need to explicitly close the realm
        if(this::mRealm.isInitialized) mRealm.close()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comic_list)
        titleSearch = findViewById(R.id.titleSearch)
        recyclerView = findViewById(R.id.task_list)
        addComicButton = findViewById(R.id.addComic)
    }

    override fun onClick(v: View?) {
        intent = Intent(this, ComicActivity::class.java)
        val comicId: ObjectId = v?.tag as ObjectId
        intent.putExtra("comicId", comicId)
        startActivity(intent)
    }
    private fun setUpRecyclerView(realm: Realm) {
        // a recyclerview requires an adapter, which feeds it items to display.
        // Realm provides RealmRecyclerViewAdapter, which you can extend to customize for your application
        // pass the adapter a collection of Tasks from the realm
        // sort this collection so that the displayed order of Tasks remains stable across updates
        var comics: RealmResults<comic> = realm.where<comic>().beginsWith("title", titleSearch.text.toString()).sort("title", Sort.ASCENDING, "subtitle", Sort.ASCENDING).findAll()
        adapter = ComicsRecyclerAdapter(this, comics)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }
}