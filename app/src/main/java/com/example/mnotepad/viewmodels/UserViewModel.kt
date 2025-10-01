package com.example.mnotepad.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mnotepad.entities.models.User
import com.example.mnotepad.entities.models.UserDetail
import com.example.mnotepad.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val repository: UserRepository) : ViewModel() {

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _userDetail = MutableLiveData<UserDetail>()
    val userDetail: LiveData<UserDetail> = _userDetail

    init {
        fetchUsers()
    }

    private fun fetchUsers() = viewModelScope.launch(Dispatchers.IO) {
        val response = repository.getUsers()
        _users.postValue(response.users)
    }

    fun fetchUserById(userId: Int) = viewModelScope.launch(Dispatchers.IO) {
        val response = repository.getUserById(userId)
        _userDetail.postValue(response)
    }
}
