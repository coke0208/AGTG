package com.example.test

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.test.databinding.FragmentHomeBinding
import com.example.test.productinfo.ProductDB
import com.example.test.productutils.ProductAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.vpTodo.adapter = activity?.let { ViewPagerAdapter(it) }
        binding.vpTodo.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        TabLayoutMediator(binding.tabLayout, binding.vpTodo) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()

        val databaseReference = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
            .getReference("ColdStorage")

        val productList = ArrayList<ProductDB>()
        productAdapter = ProductAdapter(requireContext(), productList, "ColdStorage")

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
                    productAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
            }
        })

        // RecyclerView 설정
        binding.recyclerView.adapter = productAdapter

        // MainActivity에 ProductAdapter 전달
        (activity as MainActivity).setProductAdapter(productAdapter)
    }

}

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            0 -> "냉장실"
            1 -> "냉동실"
            2 -> "실온"
            else -> null
        }
    }




    /*override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}*/

