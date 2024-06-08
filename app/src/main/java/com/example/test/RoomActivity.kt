package com.example.test

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test.databinding.ActivityRoomBinding
import com.example.test.productinfo.ProductDB
import com.example.test.productutils.ProductAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RoomActivity : Fragment() {
    private var _binding: ActivityRoomBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ProductAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var userUid: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ActivityRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("RoomActivity", "로그인이 필요합니다.")
            activity?.finish()
            return
        }

        userUid = currentUser.uid

        val databaseReference = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
            .getReference("users").child(userUid).child("products").child("RoomStorage")

        val productList = ArrayList<ProductDB>()
        adapter = ProductAdapter(requireContext(), productList, "RoomStorage")

        binding.roomlist.layoutManager = LinearLayoutManager(requireContext())
        binding.roomlist.adapter = adapter

        databaseReference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isAdded) { // Check if fragment is still attached to activity
                    productList.clear()
                    for (productSnapshot in snapshot.children) {
                        val product = productSnapshot.getValue(ProductDB::class.java)
                        if (product != null) {
                            product.id = productSnapshot.key.toString() // Assign the key to the product's id
                            productList.add(product)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RoomActivity", "Database error: ${error.message}", error.toException())
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
