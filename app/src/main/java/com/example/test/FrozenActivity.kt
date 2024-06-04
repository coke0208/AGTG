package com.example.test

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test.databinding.ActivityFrozenBinding
import com.example.test.productinfo.ProductDB
import com.example.test.productutils.ProductAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class FrozenActivity : Fragment() {
    private var _binding: ActivityFrozenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ActivityFrozenBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val databaseReference = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
            .getReference("FrozenStorage")

        val productList = ArrayList<ProductDB>()
        val adapter = ProductAdapter(requireContext(), productList, "FrozenStorage")

        databaseReference.addValueEventListener(object : ValueEventListener {
            //@SuppressLint("NotifyDataSetChanged")
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isAdded) { // Check if fragment is still attached to activity
                    productList.clear()
                    for (productSnapshot in snapshot.children) {
                        val product = productSnapshot.getValue(ProductDB::class.java)
                        if (product != null) {
                            product.id =
                                productSnapshot.key.toString() // Assign the key to the product's id
                            productList.add(product)
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




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


/*private var _binding: ActivityFrozenBinding? = null
private val binding get() = _binding!!
private lateinit var productList: MutableList<ProductDB>
private lateinit var adapter: ProductAdapter
//private lateinit var filterProductDBList: MutableList<ProductDB>

override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    _binding = ActivityFrozenBinding.inflate(inflater, container, false)
    return binding.root
}


override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupRecyclerView()
    fetchProductsFromFirebase()


    val databaseReference = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
        .getReference("FrozenStorage")
    val productList = ArrayList<ProductDB>()

    binding.frozenlist.layoutManager = LinearLayoutManager(requireContext())
    binding.frozenlist.adapter = adapter
    //val adapter = ProductAdapter(requireContext(), productList, "FrozenStorage")

    databaseReference.addValueEventListener(object : ValueEventListener {
        //@SuppressLint("NotifyDataSetChanged")
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
                filter("")
            }
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle possible errors.
        }
    })
}

fun filter(query: String?){
    filterProductDBList.clear()
    if (query.isNullOrEmpty()){
        filterProductDBList.addAll(productList)
    } else {
        for(product in productList){
            if(product.name?.contains(query,ignoreCase = true)==true){
                filterProductDBList.add(product)
            }
        }
    }

}

override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
}
}*/