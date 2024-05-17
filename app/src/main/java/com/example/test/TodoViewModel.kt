package com.example.test

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TodoViewModel : ViewModel() {

    val todoList = MediatorLiveData<List<Todo>>()
    private var datas = arrayListOf<Todo>()
    val coldList = MutableLiveData<List<Todo>>()
    val frozenList = MutableLiveData<List<Todo>>()
    val roomList = MutableLiveData<List<Todo>>()

    init{
        todoList.addSource(coldList){
                value -> todoList.value = value
        }
        todoList.addSource(frozenList){
                value -> todoList.value = value
        }
        todoList.addSource(roomList){
                value -> todoList.value = value
        }
    }

    fun addTask(todo: Todo){
        datas.add(todo)
        setData(datas)
    }

    fun deleteTask(todo:Todo){
        datas.remove(todo)
        setData(datas)
    }

    fun updateToggle(todo:Todo, isCheck: Boolean) {
        if (todo.isDone != isCheck) {
            todo.isDone = isCheck
        }
        setData(datas)
    }

    private fun setData(data: ArrayList<Todo>){
        coldList.value = data.filter { x-> !x.isDone }.toList()
        frozenList.value = data.filter { x->x.isDone }.toList()
        roomList.value = data.filter { x->x.isDone }.toList()
        todoList.value = data
    }
}