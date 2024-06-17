package com.example.test

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test.databinding.ActivityGroupBinding
import com.example.test.productinfo.ProductDB
import com.example.test.productutils.SubPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class GroupActivity : AppCompatActivity() {
    private val binding by lazy { ActivityGroupBinding.inflate(layoutInflater) }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var currentUserUid: String
    private lateinit var productList: ArrayList<ProductDB>
    private lateinit var subPagerAdapter: SubPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        currentUserUid = auth.currentUser!!.uid
        productList = ArrayList()

        binding.back.setOnClickListener {
            finish()
        }

        binding.add.setOnClickListener {
            val targetUserId = binding.targetUserIdEditText.text.toString()
            if (targetUserId.isNotEmpty()) {
                val intent = Intent(this, ProductActivity::class.java)
                intent.putExtra("TARGET_USER_UID", targetUserId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "사용자 ID를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }



        binding.idcheck.setOnClickListener {
            binding.targetUserIdEditText.setText(currentUserUid)
            copyUidToClipboard()
        }

        binding.viewUserProductsButton.setOnClickListener {
            val targetUserId = binding.targetUserIdEditText.text.toString()
            if (targetUserId.isNotEmpty()) {
                viewTargetUserProducts(targetUserId)
            } else {
                Toast.makeText(this, "유효한 사용자 ID를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }

        viewTargetUserProducts(currentUserUid)
    }

    private fun viewTargetUserProducts(targetUserId: String) {
        val databaseReference = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
            .getReference("users").child(targetUserId).child("products")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (storageSnapshot in snapshot.children) {
                    for (productSnapshot in storageSnapshot.children) {
                        val product = productSnapshot.getValue(ProductDB::class.java)
                        if (product != null) {
                            product.id = productSnapshot.key.toString() // 제품 ID를 key로 설정
                            productList.add(product)
                        }
                    }
                }
                setupViewPager(targetUserId)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GroupActivity, "제품 로드 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun setupViewPager(userId: String) {
        val subPagerAdapter = SubPagerAdapter(this, userId)
        binding.viewPager2.adapter = subPagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            0 -> "냉장실"
            1 -> "냉동실"
            2 -> "실온"
            else -> null
        }
    }

    private fun deleteUserProducts(userId: String) {
        val userProductsReference = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
            .getReference("users").child(userId).child("products")

        userProductsReference.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "제품이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "제품 삭제에 실패했습니다: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun copyUidToClipboard() {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("UID", currentUserUid)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "UID 복사 완료", Toast.LENGTH_SHORT).show()
    }
}
