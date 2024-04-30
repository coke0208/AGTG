package com.example.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.test.databinding.ActivityRoomBinding

class RoomActivity: Fragment() {
    private lateinit var binding: ActivityRoomBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityRoomBinding.inflate(inflater, container, false)
        return binding.root
    }
}
