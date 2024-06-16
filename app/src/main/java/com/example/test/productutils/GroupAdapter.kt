package com.example.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.productinfo.GroupDB

class GroupAdapter(
    private val groupList: ArrayList<GroupDB>,
    private val onGroupClick: (String) -> Unit,
    private val onIdButtonClick: (String) -> Unit
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groupList[position]
        holder.groupName.text = group.name
        holder.itemView.setOnClickListener {
            onGroupClick(group.groupId)
        }
        holder.btnId.setOnClickListener {
            onIdButtonClick(group.groupId)
        }
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

    class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val groupName: TextView = view.findViewById(R.id.groupName)
        val btnId: ImageButton = view.findViewById(R.id.btnid)
    }
}
