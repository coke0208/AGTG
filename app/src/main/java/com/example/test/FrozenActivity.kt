package com.example.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test.databinding.ActivityColdBinding
import com.example.test.databinding.ActivityFrozenBinding
import com.example.test.productinfo.ProductDB
import com.example.test.productutils.ProductAdapter
import com.google.firebase.database.FirebaseDatabase

class FrozenActivity : Fragment(), HomeFragment.SearchableFragment {
    private var _binding: ActivityFrozenBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ProductAdapter
    private var productList = ArrayList<ProductDB>()
    private var filteredList = ArrayList<ProductDB>()
    private var currentQuery: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ActivityFrozenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 어댑터 초기화
        adapter = ProductAdapter(requireContext(), productList, "FrozenStorage")
        binding.frozenlist.layoutManager = LinearLayoutManager(requireContext())
        binding.frozenlist.adapter = adapter

        // 데이터베이스에서 데이터 로드
        loadDataFromDatabase()
    }

    private fun loadDataFromDatabase() {
        val databaseReference = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
            .getReference("FrozenStorage")

        databaseReference.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
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

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
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
