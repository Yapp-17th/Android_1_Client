package com.yapp.picon.presentation.postedit

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.yapp.picon.data.model.Address
import com.yapp.picon.data.model.Coordinate
import com.yapp.picon.data.network.NetworkModule
import com.yapp.picon.domain.usecase.LoadAccessTokenUseCase
import com.yapp.picon.domain.usecase.UpdatePostUseCase
import com.yapp.picon.presentation.base.BaseViewModel
import com.yapp.picon.presentation.model.Emotion
import com.yapp.picon.presentation.model.Post
import kotlinx.coroutines.launch

class EditPostViewModel(
    private val loadAccessTokenUseCase: LoadAccessTokenUseCase,
    private val updatePostUseCase: UpdatePostUseCase
): BaseViewModel() {
    private val _backButton = MutableLiveData<Boolean>()
    val backButton: LiveData<Boolean> get() =  _backButton

    private val _saveButton = MutableLiveData<Boolean>()
    val saveButton: LiveData<Boolean> get() =  _saveButton

    private val _removePinButton = MutableLiveData<Boolean>()
    val removePinButton: LiveData<Boolean> get() =  _removePinButton

    private val _finishDialogConfirmButton = MutableLiveData<Boolean>()
    val finishDialogConfirmButton: LiveData<Boolean> get() =  _finishDialogConfirmButton

    private val _finishDialogCancelButton = MutableLiveData<Boolean>()
    val finishDialogCancelButton: LiveData<Boolean> get() =  _finishDialogCancelButton

    private val _removeDialogDeleteButton = MutableLiveData<Boolean>()
    val removeDialogDeleteButton: LiveData<Boolean> get() =  _removeDialogDeleteButton

    private val _removeDialogCancelButton = MutableLiveData<Boolean>()
    val removeDialogCancelButton: LiveData<Boolean> get() =  _removeDialogCancelButton

    private val _postImageList = MutableLiveData<List<String>>()
    val postImageList: LiveData<List<String>> get() =  _postImageList

    var postAddress = MutableLiveData<String>()

    private val _postEmotion = MutableLiveData<Emotion>()
    val postEmotion: LiveData<Emotion> get() =  _postEmotion

    var postMemo = MutableLiveData<String>()

    init {
        initialize()
    }

    fun initialize() {
        _backButton.value = false
        _saveButton.value = false
        _removePinButton.value = false
        _finishDialogConfirmButton.value = false
        _finishDialogCancelButton.value = false
        _removeDialogDeleteButton.value = false
        _removeDialogCancelButton.value = false
    }

    fun setPostContents(post: Post) {
        _postImageList.value = post.imageUrls ?: listOf()
        _postEmotion.value = post.emotion ?: throw Exception("EditPostViewModel - setPostContents - this post has not emotion.")
        postAddress.value = post.address.address
        postMemo.value = post.memo ?: ""
    }

    fun savePost(post: Post) {
        viewModelScope.launch {
            try {
                updatePostUseCase(
                    post.id,
                    Coordinate(
                        post.coordinate.lat,
                        post.coordinate.lng
                    ),
                    post.imageUrls,
                    Address(
                        post.address.address,
                        post.address.addrCity,
                        post.address.addrDo,
                        post.address.addrGu
                    ),
                    getEmotion(post.emotion?.name),
                    post.memo
                ).let {
                    if (it.status == 200) {
                        Log.d("EditPostViewModel","savePost - 게시물이 수정되었습니다.")
                    } else {
                        Log.e("EditPostViewModel","savePost - 게시물 수정에 실패했습니다.")
                    }
                }
            } catch (e: Exception) {
                Log.e("EditPostViewModel", "savePost error - ${e.message}")
            }
        }
    }

    fun removePost(id: Int) {
        viewModelScope.launch {
            try {
                loadAccessTokenUseCase().let { token ->
                    if (token.isNotEmpty()) {
                        NetworkModule.yappApi.removePost(token, id)
                    }
                }
            } catch (e: Exception) {
                Log.e("EditPostViewModel", "removePost error - ${e.message}")
            }
        }
    }

    private fun getEmotion(emotion: String?): com.yapp.picon.data.model.Emotion {
        emotion?.let {
            return when(it) {
                "SOFT_BLUE" -> com.yapp.picon.data.model.Emotion.SOFT_BLUE
                "CORN_FLOWER" -> com.yapp.picon.data.model.Emotion.CORN_FLOWER
                "BLUE_GRAY" -> com.yapp.picon.data.model.Emotion.BLUE_GRAY
                "VERY_LIGHT_BROWN" -> com.yapp.picon.data.model.Emotion.VERY_LIGHT_BROWN
                "WARM_GRAY" -> com.yapp.picon.data.model.Emotion.WARM_GRAY
                else -> throw Exception("EditPostViewModel - getEmotion - emotion string is wrong.")
            }
        } ?: throw Exception("EditPostViewModel - getEmotion - emotion is null.")
    }

    fun clickBackButton(view: View) {
        _backButton.value?.let {
            _backButton.value = !it
        }
    }

    fun clickSaveButton(view: View) {
        _saveButton.value?.let {
            _saveButton.value = !it
        }
    }

    fun clickRemovePinButton(view: View) {
        _removePinButton.value?.let {
            _removePinButton.value = !it
        }
    }

    fun clickFinishDialogConfirmButton(view: View) {
        _finishDialogConfirmButton.value?.let {
            _finishDialogConfirmButton.value = !it
        }
    }

    fun clickFinishDialogCancelButton(view: View) {
        _finishDialogCancelButton.value?.let {
            _finishDialogCancelButton.value = !it
        }
    }

    fun clickRemoveDialogDeleteButton(view: View) {
        _removeDialogDeleteButton.value?.let {
            _removeDialogDeleteButton.value = !it
        }
    }

    fun clickRemoveDialogCancelButton(view: View) {
        _removeDialogCancelButton.value?.let {
            _removeDialogCancelButton.value = !it
        }
    }
}