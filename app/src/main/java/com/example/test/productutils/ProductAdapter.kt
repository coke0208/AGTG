package com.example.test.productutils

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.FoodActivity
import com.example.test.ProductActivity
import com.example.test.R
import com.example.test.productinfo.ProductDB
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class ProductAdapter(private val context: Context, private var productList: ArrayList<ProductDB>, private val storageType: String,
                     private val targetUserUid: String ) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.tvTitle)
        val productImage: ImageView = view.findViewById(R.id.tvImage)
        val progressBar: ProgressBar = view.findViewById(R.id.progress)
        val deleteButton: ImageButton = view.findViewById(R.id.btnDelete)
        val D_day: TextView = view.findViewById(R.id.d_day)
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
        val startDate: Date? = dateFormat.parse(product.PROD ?: "")
        val endDate: Date? = dateFormat.parse(product.Usebydate ?: "")

        if (startDate != null && endDate != null) {
            val totalDuration = endDate.time - startDate.time
            val elapsedTime = currentDate.time - startDate.time
            val remainingDays = TimeUnit.MILLISECONDS.toDays(endDate.time - currentDate.time).toInt()

            holder.D_day.text="D-${remainingDays + 1}"

            if (totalDuration > 0) {
                holder.progressBar.max = totalDuration.toInt()
                holder.progressBar.progress = elapsedTime.toInt()

                when {
                    elapsedTime.toDouble() / totalDuration >= 1 -> {
                        holder.progressBar.progressDrawable.setColorFilter(
                            ContextCompat.getColor(context, R.color.black),
                            PorterDuff.Mode.SRC_IN
                        )
                        holder.D_day.text="X"
                    }
                    elapsedTime.toDouble() / totalDuration >= 0.9 -> {
                        holder.progressBar.progressDrawable.setColorFilter(
                            ContextCompat.getColor(context, R.color.red),
                            PorterDuff.Mode.SRC_IN
                        )
                    }
                    elapsedTime.toDouble() / totalDuration >= 0.5 -> {
                        holder.progressBar.progressDrawable.setColorFilter(
                            ContextCompat.getColor(context, R.color.orange),
                            PorterDuff.Mode.SRC_IN
                        )
                    }
                    else -> {
                        holder.progressBar.progressDrawable.setColorFilter(
                            ContextCompat.getColor(context, R.color.green),
                            PorterDuff.Mode.SRC_IN
                        )
                    }
                }
            } else {
                holder.progressBar.max = 1
                holder.progressBar.progress = 1
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, FoodActivity::class.java).apply {
                putExtra("name", product.name)
                putExtra("address", product.address)
                putExtra("PROD", product.PROD)
                putExtra("UsebyDate", product.Usebydate)
                putExtra("info", product.info)
                //putExtra("nutrition",product.nutrition)
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
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance()
            .getReference("users").child(targetUserUid)
            .child("products").child(storageType).child(productId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                databaseReference.removeValue().await()
                withContext(Dispatchers.Main) {
                    synchronized(productList) {
                        if (position < productList.size) {
                            productList.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, productList.size)
                            Toast.makeText(context, "삭제 성공", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "삭제 성공", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "삭제 실패: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    fun updateList(newList: ArrayList<ProductDB>) {
        productList=newList
        notifyDataSetChanged()
    }

}
