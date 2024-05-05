package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.test.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val group = Intent(this, GroupActivity::class.java)
        binding.group.setOnClickListener { startActivity(group) }

        val mypage = Intent(this, MypageActivity::class.java)
        binding.mypage.setOnClickListener { startActivity(mypage) }

        val upload = Intent(this, UploadActivity::class.java)
        binding.upload.setOnClickListener { startActivity(upload) }

        initView()
    }

    private fun initView() {
        val viewPager = binding.swipe
        val tabLayout = binding.tapview
        val tabtitle = arrayOf("냉장고", "냉동고", "실온")

        val fragmentList = ArrayList<Fragment>()
        fragmentList.add(ColdActivity())
        fragmentList.add(FrozenActivity())
        fragmentList.add(RoomActivity())

        viewPager.adapter = ViewPagerAdapter(fragmentList, this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabtitle[position]
        }.attach()
    }


    private fun initSearchView() {
        // init SearchView
        binding.search.isSubmitButtonEnabled = true
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // @TODO
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // @TODO
                return true
            }
        })
    }
}