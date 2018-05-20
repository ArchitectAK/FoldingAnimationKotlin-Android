package com.freeankit.foldinganimation

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.recyclerview_item.view.*


/**
 * @author Ankit Kumar (ankitdroiddeveloper@gmail.com) on 20/05/2018 (MM/DD/YYYY)
 */
class SampleAdapter : RecyclerView.Adapter<SampleAdapter.ViewHolder>() {

    val unfoldedIndexes = HashSet<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 10
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) = with(itemView) {

            if (unfoldedIndexes.contains(position)) {
                cell_title_view.visibility = View.GONE
                cell_content_view.visibility = View.VISIBLE
            } else {
                cell_content_view.visibility = View.GONE
                cell_title_view.visibility = View.VISIBLE
            }

            itemView.setOnClickListener({
                // toggle clicked cell state
                folding_cell.toggle(false)
                // register in adapter that state for selected cell is toggled
                registerToggle(position)
            })
        }

        private fun registerToggle(position: Int) {
            if (unfoldedIndexes.contains(position))
                registerFold(position)
            else
                registerUnfold(position)
        }

        private fun registerFold(position: Int) {
            unfoldedIndexes.remove(position)
        }

        private fun registerUnfold(position: Int) {
            unfoldedIndexes.add(position)
        }
    }
}