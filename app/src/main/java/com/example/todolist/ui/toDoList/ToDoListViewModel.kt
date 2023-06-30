package com.example.todolist.ui.toDoList

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.model.TodoItem
import com.example.todolist.data.repository.ToDoRepository
import com.example.todolist.ui.toDoList.model.TodoListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ToDoListViewModel @Inject constructor(
    private val repository: ToDoRepository
) : ViewModel() {

    private val _todoItems = MutableStateFlow(TodoListState.empty)
    val todoItems = _todoItems.asStateFlow()

    init {
        loadTodoItems()
    }

    fun loadTodoItems() {
        viewModelScope.launch {
            repository.getItems().collect { items ->
                _todoItems.update { previousState ->
                    val shouldShowCompleted = previousState.isDone

                    val filteredItems = items.listItems.filter { item ->
                        if (shouldShowCompleted) {
                            true
                        } else {
                            item.isDone.not()
                        }
                    }

                    val completedCount = items.listItems.count { it.isDone }

                    TodoListState(
                        listItems = filteredItems,
                        completed = completedCount,
                        isDone = previousState.isDone,
                        error = items.error
                    )
                }
            }
        }
    }

    fun checkTodoItem(todoItem: TodoItem) {
        val checkedItem = todoItem.copy(isDone = todoItem.isDone.not())
        viewModelScope.launch {
            repository.saveItem(checkedItem)
            loadTodoItems()
        }
    }

    fun changeCompletedTodosVisibility() {
        Log.d("ayash", "clock see1 ${_todoItems.value.listItems}")
        _todoItems.update {
            TodoListState(
                listItems = it.listItems,
                completed = it.completed,
                isDone = it.isDone.not(),
                error = ""
            )
        }
        loadTodoItems()
    }

    fun deleteItem(item: TodoItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
            loadTodoItems()
        }
    }

}