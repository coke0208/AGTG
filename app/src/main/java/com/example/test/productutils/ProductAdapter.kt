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
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.ProductActivity
import com.example.test.R
import com.example.test.productinfo.ProductDB
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductAdapter(private val context: Context, private var productList: List<ProductDB>, private val storageType: String) :
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
            .load(product.address)
            .into(holder.productImage)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = Date()

        try {
            val startDate = product.PROD?.let { dateFormat.parse(it) }
            val endDate = product.Usebydate?.let { dateFormat.parse(it) }

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
            } else {
                holder.progressBar.max = 1
                holder.progressBar.progress = 0
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProductActivity::class.java).apply {
                putExtra("name", product.name)
                putExtra("address", product.address)
                putExtra("PROD", product.PROD)
                putExtra("UsebyDate", product.Usebydate)
                putExtra("info", product.info)
            }
            context.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            deleteProduct(product.id, position)
        }
    }


    override fun getItemCount(): Int {
        return productList.size
    }

    private fun deleteProduct(productId: String, position: Int) {
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
            .getReference("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("products").child(storageType).child(productId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                databaseReference.removeValue().await()
                withContext(Dispatchers.Main) {
                    productList = productList.toMutableList().apply {
                        removeAt(position)
                    }
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, productList.size)
                    Toast.makeText(context, "삭제 성공", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "삭제 실패: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
