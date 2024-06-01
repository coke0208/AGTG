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
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.test.ProductActivity
import com.example.test.R
import com.example.test.productinfo.ProductDB
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProductAdapter(private val context: Context, private val productList: ArrayList<ProductDB>, private val storageType: String) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // RecyclerView 내 각 항목에 대한 뷰 홀더
        class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.tvTitle)
        val productEdate: TextView = view.findViewById(R.id.ex_date)
        val productImage: ImageView = view.findViewById(R.id.tvImage)
        val progressBar: ProgressBar = view.findViewById(R.id.progress)
        val deleteButton: Button = view.findViewById(R.id.btnDelete)
    }
    //

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false)
        return ProductViewHolder(view)
    }
    // RecyclerView의 각 항목에 데이터를 바인딩
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]// 해당 위치의 제품 가져오기
        holder.productName.text = product.name
        holder.productEdate.text = product.edate

// 아이템을 클릭하면 제품 상세 정보를 표시하는 ProductActivity로 이동
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
            deleteProduct(product.id, position)
        }
    }
    // RecyclerView에 표시되는 아이템의 수 반환
    override fun getItemCount(): Int {
        return productList.size
    }
    // Firebase에서 제품을 삭제하고 UI 업데이트
    private fun deleteProduct(productId: String, position: Int) {
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
            .getReference(storageType).child(productId)
// 위치가 유효한지 확인
        if (position >= productList.size) {
            Toast.makeText(context, "삭제 실패: Index out of bounds", Toast.LENGTH_SHORT).show()
            return
        }
// 비동기 작업(특정 코드를 수행하는 도중에도 아래로 계속 내려가며 수행함. 순서대로 진행하는 것이 아니라 한번에 여러개가 진행)을 위해 코루틴 사용
        CoroutineScope(Dispatchers.IO).launch {
            try {
                databaseReference.removeValue().await()
                withContext(Dispatchers.Main) {// 메인 스레드에서 UI 업데이트
                    synchronized(productList) {
                        if (position < productList.size) {// 위치가 여전히 유효한지 확인
                            productList.removeAt(position)
                            notifyItemRemoved(position)// 어댑터에 항목 제거 알림
                            notifyItemRangeChanged(position, productList.size) // 데이터 세트가 변경되었음을 어댑터에 알림
                            Toast.makeText(context, "삭제 성공", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "삭제 성공", Toast.LENGTH_SHORT).show()
                            //삭제 되는데 왜 실패라 뜨는지 모르겠음/삭제 성공에 대한 토스트 메시지 표시 (인덱스가 범위를 벗어날 경우)
                            //Toast.makeText(context, "삭제 실패: Index out of bounds", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {  // 예외 처리 및 삭제 실패에 대한 토스트 메시지 표시
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "삭제 실패: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}
