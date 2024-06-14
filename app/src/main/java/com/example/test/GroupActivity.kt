package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test.databinding.ActivityGroupBinding
import com.example.test.productinfo.GroupDB
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class GroupActivity : AppCompatActivity() {
    private val binding by lazy { ActivityGroupBinding.inflate(layoutInflater) }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var groupAdapter: GroupAdapter
    private val groupList = ArrayList<GroupDB>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.back.setOnClickListener {
            finish()
        }

        groupAdapter = GroupAdapter(groupList) { groupId ->
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("groupId", groupId)
            startActivity(intent)
        }

        binding.groupRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.groupRecyclerView.adapter = groupAdapter

        binding.inviteButton.setOnClickListener {
            val inviteeId = binding.inviteIdEditText.text.toString().trim()
            if (inviteeId.isNotEmpty()) {
                sendGroupInvite(inviteeId)
            } else {
                Toast.makeText(this, "초대할 사용자 ID를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }

        binding.acceptInvitationButton.setOnClickListener {
            acceptGroupInvite()
        }

        loadUserGroups()
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

    private fun sendGroupInvite(inviteeId: String) {
        val userId = auth.currentUser?.uid ?: return
        val groupId = database.child("groups").push().key ?: return
        val groupName = "New Group"

        val group = GroupDB(groupId, groupName, mutableMapOf(userId to true, inviteeId to false))

        database.child("groups").child(groupId).setValue(group).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                database.child("users").child(userId).child("groupId").child(groupId).setValue(true)
                database.child("users").child(inviteeId).child("groupId").child(groupId).setValue(false)

                Toast.makeText(this, "초대가 전송되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "초대 전송에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun acceptGroupInvite() {
        val userId = auth.currentUser?.uid ?: return
        database.child("users").child(userId).child("groupId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (groupSnapshot in snapshot.children) {
                    val groupId = groupSnapshot.key ?: continue
                    if (groupSnapshot.getValue(Boolean::class.java) == false) {
                        database.child("groups").child(groupId).child("members").child(userId).setValue(true)
                        database.child("users").child(userId).child("groupId").child(groupId).setValue(true)
                    }
                }
                loadUserGroups()
                Toast.makeText(this@GroupActivity, "초대가 수락되었습니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
