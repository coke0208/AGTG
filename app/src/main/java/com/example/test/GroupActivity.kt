package com.example.test

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test.databinding.ActivityGroupBinding
import com.example.test.productutils.SubPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class GroupActivity : AppCompatActivity() {
    private val binding by lazy { ActivityGroupBinding.inflate(layoutInflater) }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var currentUserUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        currentUserUid = auth.currentUser!!.uid

        binding.back.setOnClickListener {
            finish()
        }

        binding.add.setOnClickListener {
            startActivity(Intent(this, ProductActivity::class.java))
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
                if (snapshot.exists()) {
                    setupViewPager(targetUserId)
                } else {
                    Toast.makeText(this@GroupActivity, "No products found for this user", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GroupActivity, "Failed to load products: ${error.message}", Toast.LENGTH_SHORT).show()
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

    private fun copyUidToClipboard() {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("UID", currentUserUid)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "UID 복사 완료", Toast.LENGTH_SHORT).show()
    }
}
