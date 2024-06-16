package com.example.test

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test.databinding.ActivityGroupBinding
import com.example.test.productinfo.ProductDB
import com.example.test.productutils.SubPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class GroupActivity : AppCompatActivity() {
    private val binding by lazy { ActivityGroupBinding.inflate(layoutInflater) }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var currentUserUid: String
    private lateinit var productList: ArrayList<ProductDB>
    private lateinit var subPagerAdapter: SubPagerAdapter

    private var targetUserIdEditText: EditText? = null
    private var viewUserProductsButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        currentUserUid = auth.currentUser!!.uid
        productList = ArrayList()


        targetUserIdEditText = findViewById(R.id.targetUserIdEditText)
        viewUserProductsButton = findViewById(R.id.viewUserProductsButton)

        binding.back.setOnClickListener {
            finish()
        }

        binding.idcheck.setOnClickListener {
            binding.targetUserIdEditText.setText(currentUserUid)
            copyUidToClipboard()
        }

        viewUserProductsButton?.setOnClickListener {
            val targetUserId = targetUserIdEditText?.text.toString()
            if (targetUserId.isNotEmpty()) {
                viewTargetUserProducts(targetUserId)
            } else {
                Toast.makeText(this, "Please enter a valid user ID", Toast.LENGTH_SHORT).show()
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
                            product.id = productSnapshot.key.toString() // Assign the key to the product's id
                            product.let { productList.add(it) }
                        }
                    }
                }
                setupViewPager(targetUserId)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GroupActivity, "Failed to load products: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupViewPager(userId: String) {
        subPagerAdapter = SubPagerAdapter(this, productList, userId)
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

    private fun copyUidToClipboard() {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("UID", currentUserUid)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "UID 복사 완료", Toast.LENGTH_SHORT).show()
    }
}
