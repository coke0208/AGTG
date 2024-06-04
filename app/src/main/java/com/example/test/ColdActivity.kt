package com.example.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test.databinding.ActivityColdBinding
import com.example.test.productinfo.ProductDB
import com.example.test.productutils.ProductAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ColdActivity : Fragment() {
    private var _binding: ActivityColdBinding? = null
    private lateinit var adapter: ProductAdapter
    private var productList = arrayListOf<ProductDB>()
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ActivityColdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val databaseReference = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
            .getReference("ColdStorage")
        val productList = ArrayList<ProductDB>()
        val adapter = ProductAdapter(requireContext(), productList, "ColdStorage")

        binding.coldlist.layoutManager = LinearLayoutManager(requireContext())
        binding.coldlist.adapter = adapter

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

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

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    override fun updateSearchQuery(query: String) {
        val filteredList = productList.filter { it.name.contains(query, true) }
        adapter.updateList(ArrayList(filteredList))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
