package com.example.test.productutils

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.ProductActivity
import com.example.test.R
import com.example.test.productinfo.ProductDB
import com.google.firebase.database.FirebaseDatabase

class ProductAdapter(private val context: Context, private val productList: ArrayList<ProductDB>,
                     private val storagePath: String) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.tvTitle)
        val productEdate: TextView = view.findViewById(R.id.ex_date)
        val productImage: ImageView = view.findViewById(R.id.tvImage)
        val progressBar: ProgressBar = view.findViewById(R.id.progress)
        val deleteButton: Button = view.findViewById(R.id.btnDelete)
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
            val databaseReference = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
                .getReference(storagePath)
            databaseReference.child(product.id).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    productList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, itemCount)
                } else {
                    // Handle error
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}
