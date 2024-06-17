package com.example.test

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.test.Fregment_Pages.ColdActivity
import com.example.test.Fregment_Pages.FrozenActivity
import com.example.test.Fregment_Pages.RoomActivity

class ViewPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {

    private val fragments: ArrayList<Fragment> = arrayListOf(ColdActivity(), FrozenActivity(), RoomActivity())

    override fun getItemCount(): Int {
        return fragments.size
    }
    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}