package com.example.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test.databinding.ActivityColdBinding

class ColdActivity: Fragment() {

    private lateinit var viewModel: TodoViewModel
    private lateinit var binding: ActivityColdBinding
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
        binding = ActivityColdBinding.inflate(inflater, container, false)

        // 3. adapter 설정 (list를 인자로)
        var coldList = viewModel.coldList.value
        adapter = TodoAdapter(
            coldList?: emptyList<Todo>(),
            onClickDeleteButton={
                viewModel.deleteTask(it) },
            onCheckedChange ={ it:Todo, check:Boolean ->
                viewModel.updateToggle(it, check)
            }
        )
        adapter.setHasStableIds(true)
        binding.coldlist.adapter = adapter

        // 4. recyclerView에 Layout 꼭 설정하기 (안그러면 화면에 표시 안되고 skip됨)
        binding.coldlist.layoutManager = LinearLayoutManager(activity)

        // 5. return Fragment Layout View
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.todoList.observe(viewLifecycleOwner, Observer{
            binding.coldlist.post(Runnable { adapter.setColdData(it.filter { x -> !x.isDone }) })
        })
    }

    override fun onStart() {
        super.onStart()
        binding.btnAddTask.setOnClickListener{
            val transaction = (activity as MainActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frameLayout, AddFragment())
            transaction.commit()
        }
    }
}