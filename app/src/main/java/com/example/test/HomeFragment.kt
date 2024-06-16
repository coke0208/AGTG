package com.example.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.test.databinding.FragmentHomeBinding
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPagerAdapter: ViewPagerAdapter

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

        //binding.vpTodo.adapter = activity?.let { ViewPagerAdapter(it) }
        //binding.vpTodo.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        viewPagerAdapter = ViewPagerAdapter(this)
        binding.vpTodo.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.vpTodo) { tab, position ->
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

    interface SearchableFragment {
        fun updateSearchQuery(query: String)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
