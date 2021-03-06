package com.yapp.picon.domain.usecase

import com.yapp.picon.data.repository.user.UserRepository

class LoadAccessTokenUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(): String =
        repository.loadAccessToken()
}