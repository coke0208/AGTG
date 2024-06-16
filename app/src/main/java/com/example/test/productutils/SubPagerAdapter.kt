package com.example.test.productutils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.test.ColdActivity
import com.example.test.FrozenActivity
import com.example.test.RoomActivity
import com.example.test.productinfo.ProductDB

class SubPagerAdapter(fragmentActivity: FragmentActivity, private val productList: ArrayList<ProductDB>, private val userId: String) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ColdActivity.newInstance(productList, userId)
            1 -> FrozenActivity.newInstance(productList, userId)
            2 -> RoomActivity.newInstance(productList, userId)
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}
