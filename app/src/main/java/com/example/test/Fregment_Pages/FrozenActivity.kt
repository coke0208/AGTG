package com.example.test.Fregment_Pages

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test.HomeFragment
import com.example.test.databinding.ActivityFrozenBinding
import com.example.test.productinfo.ProductDB
import com.example.test.productutils.ProductAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FrozenActivity : Fragment(), HomeFragment.SearchableFragment {
    private var _binding: ActivityFrozenBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ProductAdapter
    private var productList = ArrayList<ProductDB>()
    private var filteredList = ArrayList<ProductDB>()
    private var currentQuery: String = ""
    private lateinit var targetUserId: String

    companion object {
        private const val ARG_USER_ID = "user_id"
        fun newInstance(userId: String): FrozenActivity {
            val fragment = FrozenActivity()
            val args = Bundle()
            args.putString(ARG_USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ActivityFrozenBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        targetUserId = arguments?.getString(ARG_USER_ID) ?: FirebaseAuth.getInstance().currentUser!!.uid

        val databaseReference = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
            .getReference("users").child(targetUserId).child("products").child("FrozenStorage")

        adapter = ProductAdapter(requireContext(), productList, "FrozenStorage",targetUserId)

        binding.frozenlist.layoutManager = LinearLayoutManager(requireContext())
        binding.frozenlist.adapter = adapter

        databaseReference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isAdded) {
                    productList.clear()
                    for (productSnapshot in snapshot.children) {
                        val product = productSnapshot.getValue(ProductDB::class.java)
                        if (product != null) {
                            product.id = productSnapshot.key.toString()
                            productList.add(product)
                        }
                    }
                    updateSearchQuery(currentQuery)
                }
            }

            override fun onCancelled(error: DatabaseError) {

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