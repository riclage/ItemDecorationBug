package com.riclage.itemdecorationbug

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        val mainAdapter = MainAdapter(this)

        val spaceBeforeMeItem = "[Space Before Me]"
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mainAdapter

            addItemDecoration(object : RecyclerView.ItemDecoration() {

                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                    state: RecyclerView.State) {

                    val adapter = parent.adapter as MainAdapter
                    val pos = parent.getChildAdapterPosition(view)
                    if (adapter.items.getOrNull(pos + 1) == spaceBeforeMeItem) {
                        outRect.bottom = 42
                    }
                }
            })
        }

        val items = listOf("Cat", spaceBeforeMeItem, "Tiger", spaceBeforeMeItem)
        mainAdapter.updateItems(items)

        Handler().postDelayed({
            //There should not be a vertical space between the "Cat" and "Other Cat" items
            //Calling recyclerView.invalidateItemDecorations() fixes the problem.

            //Internally, it calls markItemDecorInsetsDirty(), which forces all
            //((LayoutParams) child.getLayoutParams()).mInsetsDirty = true.
            //Without that, the first view (the one corresponding to the "Cat" view holder does not get its
            //mInsetsDirty updated.
//            recyclerView.invalidateItemDecorations()

            mainAdapter.updateItems(
                listOf("Cat", "Other Cat", spaceBeforeMeItem, "Tiger", "Other Tiger", spaceBeforeMeItem, "Dog"))
        }, 5000)

    }
}

class MainViewHolder(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
    fun bind(title: String) {
        textView.text = title
    }
}

class MainAdapter(private val context: Context) : RecyclerView.Adapter<MainViewHolder>() {

    var items = emptyList<String>()
        private set

    fun updateItems(newItems: List<String>) {
        val oldItems = this.items
        this.items = newItems

        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItems[oldItemPosition] == newItems[newItemPosition]
            }

            override fun getOldListSize(): Int {
                return oldItems.size
            }

            override fun getNewListSize(): Int {
                return newItems.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItems[oldItemPosition] == newItems[newItemPosition]
            }

        }).dispatchUpdatesTo(this)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_title, parent, false)
        return MainViewHolder(view as TextView)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.bind(items[position])
    }
}
