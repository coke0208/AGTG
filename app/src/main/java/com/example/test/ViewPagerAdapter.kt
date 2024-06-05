package com.example.test

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {

    // ViewPager에서 이동할 Fragment list
    private val fragments: ArrayList<Fragment> = arrayListOf(ColdActivity(), FrozenActivity(), RoomActivity())

    // FragmentStateAdapter 상속 시 무조건 override 해야하는 fun
    override fun getItemCount(): Int {
        return fragments.size
    }

    // FragmentStateAdapter 상속 시 무조건 override 해야하는 fun (View Pager의 position에 해당하는 fragment return)
    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}