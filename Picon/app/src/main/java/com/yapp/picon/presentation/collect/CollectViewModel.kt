package com.yapp.picon.presentation.collect

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.yapp.picon.domain.usecase.RequestPostsUseCase
import com.yapp.picon.presentation.base.BaseViewModel
import com.yapp.picon.presentation.model.EmotionEntity
import com.yapp.picon.presentation.model.Pin
import com.yapp.picon.presentation.model.Post
import com.yapp.picon.presentation.util.TextViewCode
import com.yapp.picon.presentation.util.toPresentation
import kotlinx.coroutines.launch

class CollectViewModel(
    private val getPostsUseCase: RequestPostsUseCase
) : BaseViewModel() {

    private val _items = MutableLiveData<List<Pin>>()
    val items: LiveData<List<Pin>> get() = _items

    private val _posts = MutableLiveData<List<Post>>()

    private val _locationCount = MutableLiveData<String>()
    val locationCount: LiveData<String> get() = _locationCount

    private val _emotionCount = MutableLiveData<String>()
    val emotionCount: LiveData<String> get() = _emotionCount

    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String> get() = _toastMsg

    private val _textViewCodeEmotion = MutableLiveData<Int>()
    val textViewCodeEmotion: LiveData<Int> get() = _textViewCodeEmotion

    private val _textViewCodeLocation = MutableLiveData<Int>()
    val textViewCodeLocation: LiveData<Int> get() = _textViewCodeLocation

    private val _emotions = MutableLiveData<List<MutableMap<String, String>>>()
    val emotions: LiveData<List<MutableMap<String, String>>> get() = _emotions

    private val _showFilterYN = MutableLiveData<Boolean>()
    val showFilterYN: LiveData<Boolean> get() = _showFilterYN

    private val _emotion1ClickedYN = MutableLiveData<Boolean>()
    val emotion1ClickedYN: LiveData<Boolean> get() = _emotion1ClickedYN

    private val _emotion2ClickedYN = MutableLiveData<Boolean>()
    val emotion2ClickedYN: LiveData<Boolean> get() = _emotion2ClickedYN

    private val _emotion3ClickedYN = MutableLiveData<Boolean>()
    val emotion3ClickedYN: LiveData<Boolean> get() = _emotion3ClickedYN

    private val _emotion4ClickedYN = MutableLiveData<Boolean>()
    val emotion4ClickedYN: LiveData<Boolean> get() = _emotion4ClickedYN

    private val _emotion5ClickedYN = MutableLiveData<Boolean>()
    val emotion5ClickedYN: LiveData<Boolean> get() = _emotion5ClickedYN

    init {
        _items.value = mutableListOf()
        _posts.value = mutableListOf()
        _textViewCodeEmotion.value = TextViewCode.COLLECT_MBS_EMOTION.code
        _textViewCodeLocation.value = TextViewCode.COLLECT_MBS_LOCATION.code
        _emotions.value = mutableListOf()
        _showFilterYN.value = false

        _emotion1ClickedYN.value = false
        _emotion2ClickedYN.value = false
        _emotion3ClickedYN.value = false
        _emotion4ClickedYN.value = false
        _emotion5ClickedYN.value = false
    }

    private fun showToast(msg: String) {
        _toastMsg.value = msg
    }

    fun setPosts() {
        viewModelScope.launch {
            getPostsUseCase().posts.let { postList ->
                _posts.value = postList.map {
                    toPresentation(it)
                }

                _items.value = _posts.value?.let { posts ->
                    _locationCount.value = "위치(${posts.groupBy { it.address.addrGu }.keys.size})"
                    _emotionCount.value = "감정(${posts.groupBy { it.emotion }.keys.size})"

                    posts.map {
                        val size = it.imageUrls?.size ?: 0
                        val showManyYN = size > 1

                        Pin(
                            it.id,
                            it.imageUrls?.get(0),
                            it.emotion?.rgb,
                            showManyYN = showManyYN,
                            showCbYN = false,
                            checkYN = false
                        )
                    }
                }
            }
        }
    }

    fun getPost(id: Int): Post? {
        return _posts.value?.first {
            it.id == id
        }
    }

    fun toBoolean(value: String?): Boolean = value.toBoolean()

    fun setEmotions(list: List<EmotionEntity>) {
        _emotions.value = list.map {
            mutableMapOf(
                "color" to it.color,
                "text" to it.emotion,
                "clickedYN" to "false"
            )
        }
    }

    fun toggleShowFilterYN() {
        _showFilterYN.value = _showFilterYN.value?.let { !it }
    }

    fun toggleEmotion1ClickedYN() {
        _emotion1ClickedYN.value = _emotion1ClickedYN.value?.let { !it }
    }

    fun toggleEmotion2ClickedYN() {
        _emotion2ClickedYN.value = _emotion2ClickedYN.value?.let { !it }
    }

    fun toggleEmotion3ClickedYN() {
        _emotion3ClickedYN.value = _emotion3ClickedYN.value?.let { !it }
    }

    fun toggleEmotion4ClickedYN() {
        _emotion4ClickedYN.value = _emotion4ClickedYN.value?.let { !it }
    }

    fun toggleEmotion5ClickedYN() {
        _emotion5ClickedYN.value = _emotion5ClickedYN.value?.let { !it }
    }

    fun applyFilter() {
        val filterEmotionList = arrayListOf<Boolean>()

        filterEmotionList.add(_emotion1ClickedYN.value ?: false)
        filterEmotionList.add(_emotion2ClickedYN.value ?: false)
        filterEmotionList.add(_emotion3ClickedYN.value ?: false)
        filterEmotionList.add(_emotion4ClickedYN.value ?: false)
        filterEmotionList.add(_emotion5ClickedYN.value ?: false)

        filterEmotionList.let {
            val lastIndex = it.size - 1
            _emotions.value = _emotions.value?.apply {
                for (i in 0..lastIndex) {
                    this[i]["clickedYN"] = it[i].toString()
                }
            }
            Log.e(TAG, "Emotions Value : ${_emotions.value.toString()}")
        }

        val emotionFilter = _emotions.value?.filter {
            it["clickedYN"] == "true"
        }?.mapNotNull { it["color"] }

        emotionFilter?.let {
            _items.value = _posts.value?.filter { post ->
                post.emotion?.name in emotionFilter
            }?.let { posts ->
                _locationCount.value = "위치(${posts.groupBy { it.address.addrGu }.keys.size})"
                _emotionCount.value = "감정(${posts.groupBy { it.emotion }.keys.size})"

                posts.map {
                    val size = it.imageUrls?.size ?: 0
                    val showManyYN = size > 1

                    Pin(
                        it.id,
                        it.imageUrls?.get(0),
                        it.emotion?.rgb,
                        showManyYN = showManyYN,
                        showCbYN = false,
                        checkYN = false
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "CollectViewModel"
    }

}