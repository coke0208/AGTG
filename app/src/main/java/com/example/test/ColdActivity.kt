package com.example.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.test.databinding.ActivityColdBinding

class ColdActivity: Fragment() {
    private lateinit var binding: ActivityColdBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityColdBinding.inflate(inflater, container, false)
        return binding.root
    }
}
