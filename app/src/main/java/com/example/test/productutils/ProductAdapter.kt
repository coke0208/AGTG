package com.example.test.productutils

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.ProductActivity
import com.example.test.R
import com.example.test.productinfo.ProductDB
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductAdapter(private val context: Context, private val productList: ArrayList<ProductDB>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.tvTitle)
        val productEdate: TextView = view.findViewById(R.id.ex_date)
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
        holder.productEdate.text = product.edate
        // Set image resource if needed
        // holder.progressBar.progress = ... // Set progress if needed

        /*val date = product.edate
        val d_day = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val end: Date =d_day.parse(date)!!
        val time = System.currentTimeMillis()

        val total = end.time - time
        holder.progressBar.max = total.toInt()*/

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProductActivity::class.java).apply {
                putExtra("name", product.name)
                putExtra("address", product.addres)
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
