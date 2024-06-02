package com.example.test.productutils

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.ProductActivity
import com.example.test.R
import com.example.test.productinfo.ProductDB
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.logging.Filter


class ProductAdapter(private val context: Context, private val productList: ArrayList<ProductDB>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.tvTitle)
        val productImage: ImageView = view.findViewById(R.id.tvImage)
        val progressBar: ProgressBar = view.findViewById(R.id.progress)
        val deleteButton: ImageButton = view.findViewById(R.id.btnDelete)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.productName.text = product.name

        Glide.with(context)
            .load(product.addres) // 여기에 Firebase Realtime Database에서 가져온 이미지 URL을 넣어줍니다.
            .into(holder.productImage)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = Date()
        val startDate: Date? = dateFormat.parse(product.cdate)
        val endDate: Date? = dateFormat.parse(product.edate)

        if (startDate != null && endDate != null) {
            val totalDuration = endDate.time - startDate.time
            val elapsedTime = currentDate.time - startDate.time

            if (totalDuration > 0) {
                holder.progressBar.max = totalDuration.toInt()
                holder.progressBar.progress = elapsedTime.toInt()
            } else {
                holder.progressBar.max = 1
                holder.progressBar.progress = 1
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProductActivity::class.java).apply {
                putExtra("name", product.name)
                putExtra("addres", product.addres)
                putExtra("edate", product.edate)
                putExtra("cdate", product.cdate)
                putExtra("info", product.info)
            }
            context.startActivity(intent)
        }


        holder.deleteButton.setOnClickListener {
            // Handle delete button click
            productList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount)
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}
