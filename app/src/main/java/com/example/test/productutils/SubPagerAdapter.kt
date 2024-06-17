package com.example.test.productutils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.test.Fregment_Pages.ColdActivity
import com.example.test.Fregment_Pages.FrozenActivity
import com.example.test.Fregment_Pages.RoomActivity

class SubPagerAdapter(fragmentActivity: FragmentActivity, private val userId: String) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ColdActivity.newInstance(userId)
            1 -> FrozenActivity.newInstance(userId)
            2 -> RoomActivity.newInstance(userId)
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}
