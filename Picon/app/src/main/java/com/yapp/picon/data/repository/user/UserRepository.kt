package com.yapp.picon.data.repository.user

import com.yapp.picon.data.model.LoginRequest
import com.yapp.picon.data.model.LoginResponse
import com.yapp.picon.data.model.SimpleJoinRequest
import com.yapp.picon.data.model.SimpleJoinResponse

interface UserRepository {
    suspend fun saveFirst(firstYN: Boolean)
    suspend fun loadFirst(): Boolean
    suspend fun simpleJoin(simpleJoinRequest: SimpleJoinRequest): SimpleJoinResponse
    suspend fun login(loginRequest: LoginRequest): LoginResponse
    suspend fun saveAccessToken(accessToken: String)
    suspend fun loadAccessToken(): String
}