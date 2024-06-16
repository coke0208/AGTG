package com.example.test

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.databinding.ActivityRoomBinding
import com.example.test.productinfo.ProductDB
import com.example.test.productutils.ProductAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RoomActivity : Fragment(), HomeFragment.SearchableFragment {
    private var _binding: ActivityRoomBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ProductAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var userUid: String
    private var productList = ArrayList<ProductDB>()
    private var filteredList = ArrayList<ProductDB>()
    private var currentQuery: String = ""

    companion object {
        private const val ARG_PRODUCT_LIST = "product_list"
        private const val ARG_USER_ID = "user_id"
        fun newInstance(productList: ArrayList<ProductDB>, userId: String): RoomActivity {
            val fragment = RoomActivity()
            val args = Bundle()
            args.putParcelableArrayList(ARG_PRODUCT_LIST, productList)
            args.putString(ColdActivity.ARG_USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ActivityRoomBinding.inflate(inflater, container, false)
        val view = binding.root

        adapter = ProductAdapter(requireContext(), ArrayList(), "냉장실")
        val recyclerView: RecyclerView = binding.roomlist
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return view
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
        val targetUserId = arguments?.getString("TARGET_USER_ID", userUid) ?: userUid

        val databaseReference = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
            .getReference("users").child(targetUserId).child("products").child("RoomStorage")

        //val productList = ArrayList<ProductDB>()
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
                            product.let { productList.add(it) }
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    override fun updateSearchQuery(query: String) {
        currentQuery = query
        if (this::adapter.isInitialized) {
            filteredList.clear()
            filteredList.addAll(productList.filter { it.name!!.contains(query, true) })
            adapter.updateList(filteredList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
