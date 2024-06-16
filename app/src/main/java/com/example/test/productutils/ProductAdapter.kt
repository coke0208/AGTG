package com.example.test.productutils

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.CheckBox
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
import com.google.android.material.animation.AnimationUtils
import com.google.firebase.auth.FirebaseAuth
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
class ProductAdapter(private val context: Context, private var productList: ArrayList<ProductDB>, private val storageType: String) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // RecyclerView 내 각 항목에 대한 뷰 홀더
    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.tvTitle) ?: throw NullPointerException("TextView tvTitle not found")
        val productImage: ImageView = view.findViewById(R.id.tvImage) ?: throw NullPointerException("ImageView tvImage not found")
        val progressBar: ProgressBar = view.findViewById(R.id.progress) ?: throw NullPointerException("ProgressBar progress not found")
        val deleteButton: ImageButton = view.findViewById(R.id.btnDelete) ?: throw NullPointerException("ImageButton btnDelete not found")
        val D_day: TextView = view.findViewById(R.id.d_day) ?: throw NullPointerException("TextView d_day not found")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false)
        return ProductViewHolder(view)
    }
    // RecyclerView의 각 항목에 데이터를 바인딩
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

// 아이템을 클릭하면 제품 상세 정보를 표시하는 ProductActivity로 이동
        holder.itemView.setOnClickListener {
            val intent = Intent(context, FoodActivity::class.java).apply {
                putExtra("name", product.name)
                putExtra("address", product.address)
                putExtra("PROD", product.PROD)
                putExtra("UsebyDate", product.Usebydate)
                putExtra("info", product.info)
            }
            context.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            // Handle delete button click
            deleteProduct(product.id, position)
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }
    // Firebase에서 제품을 삭제하고 UI 업데이트
    private fun deleteProduct(productId: String, position: Int) {
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
            .getReference("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("products").child(storageType).child(productId)

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
        productList = newList
        notifyDataSetChanged()
    }
}
