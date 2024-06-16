package com.example.test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test.databinding.ActivityGroupBinding
import com.example.test.productinfo.GroupDB
import com.example.test.productinfo.ProductDB
import com.example.test.productutils.ProductAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Suppress("DEPRECATION")
class GroupActivity : AppCompatActivity() {
    private val binding by lazy { ActivityGroupBinding.inflate(layoutInflater) }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var groupAdapter: GroupAdapter
    private val groupList = ArrayList<GroupDB>()
    private lateinit var currentUserUid: String
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: ArrayList<ProductDB>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        currentUserUid = auth.currentUser!!.uid
        productList = ArrayList()
        productAdapter = ProductAdapter(this, productList, "products")


        binding.back.setOnClickListener {
            finish()
        }

        groupAdapter = GroupAdapter(groupList, { groupId ->
            loadGroupProducts(groupId)
        }, { groupId ->
            binding.groupIdTextView.text = groupId
        })

        val receivedProductList = intent.getParcelableArrayListExtra<ProductDB>("PRODUCT_LIST")
        receivedProductList?.let {
            productList.clear()
            productList.addAll(it)
            productAdapter.notifyDataSetChanged()
        }


        binding.groupRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.groupRecyclerView.adapter = groupAdapter

        binding.createGroupButton.setOnClickListener {
            createNewGroup()
        }

        binding.joinGroupButton.setOnClickListener {
            val groupId = binding.groupIdEditText.text.toString().trim()
            if (groupId.isNotEmpty()) {
                joinGroup(groupId)
            } else {
                Toast.makeText(this, "그룹 ID를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }

        loadUserGroups()
    }

    private fun joinGroup(groupId: String) {
        val userId = auth.currentUser?.uid ?: return
        database.child("groups").child(groupId).child("members").child(userId).setValue(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    database.child("users").child(userId).child("groupId").child(groupId).setValue(true)
                    Toast.makeText(this, "그룹에 가입되었습니다.", Toast.LENGTH_SHORT).show()
                    loadUserGroups()
                } else {
                    Toast.makeText(this, "그룹 가입에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createNewGroup() {
        val userId = auth.currentUser?.uid ?: return
        val groupId = database.child("groups").push().key ?: return
        val groupName = "New Group"
        val group = GroupDB(groupId, groupName, mutableMapOf(userId to true))

        database.child("groups").child(groupId).setValue(group).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                database.child("users").child(userId).child("groupId").child(groupId).setValue(true)
                Toast.makeText(this, "새 그룹이 생성되었습니다.", Toast.LENGTH_SHORT).show()
                loadUserGroups()
            } else {
                Log.e("GroupActivity", "그룹 생성 실패: ${task.exception}")
                Toast.makeText(this, "그룹 생성에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserGroups() {
        val userId = auth.currentUser?.uid ?: return
        database.child("users").child(userId).child("groupId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                groupList.clear()
                for (groupSnapshot in snapshot.children) {
                    val groupId = groupSnapshot.key ?: continue
                    database.child("groups").child(groupId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(groupSnap: DataSnapshot) {
                            val group = groupSnap.getValue(GroupDB::class.java)
                            group?.let { groupList.add(it) }
                            groupAdapter.notifyDataSetChanged()
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadGroupProducts(groupId: String) {
        database.child("groups").child(groupId).child("products").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = ArrayList<ProductDB>()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(ProductDB::class.java)
                    if (product != null) {
                        productList.add(product)
                    }
                }
                showProducts(productList)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showProducts(productList: List<ProductDB>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putParcelableArrayListExtra("PRODUCT_LIST", ArrayList(productList))
        }
        startActivity(intent)
    }
}
