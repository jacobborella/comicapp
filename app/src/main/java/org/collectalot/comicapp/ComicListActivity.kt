package org.collectalot.comicapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.syncSession
import io.realm.mongodb.User
import io.realm.kotlin.where
import io.realm.log.RealmLog
import io.realm.mongodb.sync.SyncConfiguration
import org.collectalot.comicapp.model.comic
import org.collectalot.comicapp.ui.ComicsRecyclerAdapter

class ComicListActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm
    private var user: User? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ComicsRecyclerAdapter
    private lateinit var fab: FloatingActionButton
    private lateinit var titleSearch: EditText
    private var partitionValue: String = comicApp.currentUser()?.id.toString()


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
            // configure realm to use the current user and the partition corresponding to "My Project"
            val config = SyncConfiguration.Builder(user!!, partitionValue)
                .waitForInitialRemoteData()
                .allowQueriesOnUiThread(true)
                .build()
            // save this configuration as the default for this entire app so other activities and threads can open their own realm instances
            Realm.setDefaultConfiguration(config)

            // Sync all realm changes via a new instance, and when that instance has been successfully created connect it to an on-screen list (a recycler view)
            Thread({
                Realm.getInstance(config).use {
                    var count = it.where<comic>().count().toString()
                    RealmLog.error(count)
                    Log.v("foo", "countx: ${count}")
                };
            }).start()
            Realm.getInstanceAsync(config, object: Realm.Callback() {
                override fun onSuccess(realm: Realm) {
                    // since this realm should live exactly as long as this activity, assign the realm to a member variable
                    mRealm = realm
                    var count1 = realm.where<comic>().count().toString()
                    realm.executeTransactionAsync(Realm.Transaction { realm ->
                        realm.syncSession.downloadAllServerChanges()
                        var count2 = realm.where<comic>().count().toString()
                        Log.v("foo", "count2: ${count2}")
                    })
                    Log.v("foo", "count1: ${count1}")
                    setUpRecyclerView(realm)
                }
            })
        }
        titleSearch.doOnTextChanged { text, start, before, count ->
            //TODO: Does this need to be optimized? One new query each time data is changed.
            adapter.updateData(mRealm.where<comic>().beginsWith("title", titleSearch.text.toString()).sort("title", Sort.ASCENDING, "subtitle", Sort.ASCENDING).findAllAsync())
        }
    }

    override fun onStop() {
        super.onStop()
        if(this::mRealm.isInitialized) {
            user.run {
                mRealm.close()
            }
        }
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
    }
    private fun setUpRecyclerView(realm: Realm) {
        // a recyclerview requires an adapter, which feeds it items to display.
        // Realm provides RealmRecyclerViewAdapter, which you can extend to customize for your application
        // pass the adapter a collection of Tasks from the realm
        // sort this collection so that the displayed order of Tasks remains stable across updates
        adapter = ComicsRecyclerAdapter(realm.where<comic>().beginsWith("title", titleSearch.text.toString()).sort("title", Sort.ASCENDING, "subtitle", Sort.ASCENDING).findAllAsync())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }
}