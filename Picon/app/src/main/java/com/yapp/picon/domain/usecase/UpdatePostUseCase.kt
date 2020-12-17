package com.yapp.picon.domain.usecase

import com.yapp.picon.data.model.*
import com.yapp.picon.data.repository.post.PostRepository
import com.yapp.picon.data.repository.user.UserRepository

class UpdatePostUseCase(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        id: Int?,
        coordinate: Coordinate,
        imageUrls: List<String>?,
        address: Address,
        emotion: Emotion?,
        memo: String?
    ): PostResponse =
        userRepository.loadAccessToken().let {
            postRepository.createPost(
                it, PostRequest(
                    Post(
                        id = id,
                        coordinate = coordinate,
                        imageUrls = imageUrls,
                        address = address,
                        emotion = emotion,
                        memo = memo,
                        createdDate = null
                    )
                )
            )
        }
}