package org.collectalot.comicapp.ui

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.collectalot.comicapp.R
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import org.collectalot.comicapp.model.comic

internal class ComicsRecyclerAdapter(data: OrderedRealmCollection<comic>) : RealmRecyclerViewAdapter<comic, ComicsRecyclerAdapter.ItemViewHolder?>(data, true) {
    internal inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name: TextView = view.findViewById(R.id.body)
        var data: comic? = null
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.comic_list_item, parent, false)
        return ItemViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val obj: comic? = getItem(position)
        holder.data = obj
        holder.name.text = listOfNotNull(obj?.title, obj?.subtitle).joinToString(" " )
    }
}