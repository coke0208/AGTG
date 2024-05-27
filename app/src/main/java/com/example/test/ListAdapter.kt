package com.example.test

import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.lang.String
import kotlin.Int


class ListAdapter(private val arrayList: ArrayList<User>?, context: Context) :
    RecyclerView.Adapter<ListAdapter.CustomViewHolder>() {
    private val context: Context

    init {
        this.context = context
    }

    @NonNull
    override fun onCreateViewHolder(
        @NonNull parent: ViewGroup,
        viewType: Int
    ): CustomViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: CustomViewHolder, position: Int) {
        Glide.with(holder.itemView)
            .load(arrayList!![position].getProfile())
            .into(holder.iv_p)
        holder.tv_id.setText(arrayList!![position].getId())
        holder.tv_pw.setText(String.valueOf(arrayList!![position].getPw()))
        holder.tv_name.setText(arrayList!![position].getUserName())
    }

    override fun getItemCount(): Int {
        return arrayList?.size ?: 0
    }

    inner class CustomViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView
        var addres: ImageView

        init {
            name = itemView.findViewById(R.id.)
            addres = itemView.findViewById(R.id.tv_id)
        }
    }
}