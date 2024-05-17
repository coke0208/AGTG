package com.example.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test.databinding.ActivityFrozenBinding

class FrozenActivity: Fragment() {

    private lateinit var viewModel: TodoViewModel
    private lateinit var binding: ActivityFrozenBinding
    private lateinit var adapter : TodoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 1. View Model 설정
        viewModel = ViewModelProvider(requireActivity(), ViewModelProvider.NewInstanceFactory()) .get(
            TodoViewModel::class.java)

        // 2. View Binding 설정
        binding = ActivityFrozenBinding.inflate(inflater, container, false)

        // 3. adapter 설정
        var FrozenList = viewModel.frozenList.value
        adapter = TodoAdapter(
            FrozenList?: emptyList<Todo>(),
            onClickDeleteButton={
                viewModel.deleteTask(it) },
            onCheckedChange ={ it:Todo, check:Boolean ->
                viewModel.updateToggle(it, check)
            }
        )
        adapter.setHasStableIds(true)
        binding.frozenlist.adapter = adapter

        // 4. recyclerView에 Layout 꼭 설정하기 (안그러면 화면에 표시 안되고 skip됨)
        binding.frozenlist.layoutManager = LinearLayoutManager(activity)

        // 5. return Fragment Layout View
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.todoList.observe(viewLifecycleOwner, Observer{
            binding.frozenlist.post(Runnable { adapter.setFrozenData(it.filter { x->x.isDone }) })
        })
    }
}