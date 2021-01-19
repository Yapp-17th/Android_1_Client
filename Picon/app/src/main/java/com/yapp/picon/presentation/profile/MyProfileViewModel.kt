package com.yapp.picon.presentation.profile

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.yapp.picon.domain.usecase.GetUserInfoUseCase
import com.yapp.picon.domain.usecase.RequestPostsUseCase
import com.yapp.picon.domain.usecase.UploadImageUseCase
import com.yapp.picon.domain.usecase.UploadProfileUseCase
import com.yapp.picon.presentation.base.BaseViewModel
import com.yapp.picon.presentation.model.Address
import com.yapp.picon.presentation.model.Coordinate
import com.yapp.picon.presentation.model.Emotion
import com.yapp.picon.presentation.model.Post
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class MyProfileViewModel(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getPostsUseCase: RequestPostsUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
    private val uploadProfileUseCase: UploadProfileUseCase
) : BaseViewModel() {

    private val _backButton = MutableLiveData<Boolean>()
    private val _profileImageUrl = MutableLiveData<String>()
    private val _postList = MutableLiveData<List<Post>>()
    private val _myProfileTitle = MutableLiveData<String>()
    private val _following = MutableLiveData<Int>()
    private val _follower = MutableLiveData<Int>()
    private val _profileNewImageUri = MutableLiveData<Uri>()
    private val _userInfoLoadYN = MutableLiveData<Boolean>()
    private val _toastMsg = MutableLiveData<String>()

    private val _contentResolver = MutableLiveData<ContentResolver>()

    init {
        initFlags()

        _myProfileTitle.value = "닉네임"
        _following.value = 0
        _follower.value = 0
    }

    fun initFlags() {
        _backButton.value = false
        _userInfoLoadYN.value = false
    }

    val backButton: LiveData<Boolean> = _backButton
    val profileImageUrl: LiveData<String> = _profileImageUrl
    val postList: LiveData<List<Post>> = _postList
    val myProfileTitle: LiveData<String> = _myProfileTitle
    val following: LiveData<Int> = _following
    val follower: LiveData<Int> = _follower
    val profileNewImageUri: LiveData<Uri> = _profileNewImageUri
    val userInfoLoadYN: LiveData<Boolean> = _userInfoLoadYN
    val toastMsg: LiveData<String> get() = _toastMsg

    private fun showToast(msg: String) {
        _toastMsg.value = msg
    }

    fun requestUserInfo() {
        viewModelScope.launch {
            try {
                //HTTP 504 발생 Gateway Time Out
                getUserInfoUseCase().memberDetailDto.let {
                    _myProfileTitle.value = it.member.nickName
                    _profileImageUrl.value = it.member.profileImageUrl ?: ""
                    _userInfoLoadYN.value = true
                    _following.value = it.followInfo.followings
                    _follower.value = it.followInfo.followers
                }
            } catch (e: Exception) {
                Log.e("MyProfileViewModel", "requestUserInfo Error - ${e.message}")
            }
        }
    }

    fun requestPosts() {
        viewModelScope.launch {
            try {
                getPostsUseCase().let { post ->
                    post.posts.map {
                        Post(
                            it.id,
                            Coordinate(it.coordinate.lat, it.coordinate.lng),
                            it.imageUrls,
                            Address(
                                it.address.address,
                                it.address.addrCity,
                                it.address.addrDo,
                                it.address.addrGu
                            ),
                            getEmotion(it.emotion!!.name),
                            it.memo,
                            it.createdDate
                        )
                    }.let { _postList.value = it }
                }
            } catch (e: Exception) {
                Log.e("MyProfileViewModel", "requestPosts Error - ${e.message}")
            }
        }
    }

    private fun getEmotion(value: String): Emotion {
        return when (value) {
            "SOFT_BLUE" -> Emotion.SOFT_BLUE
            "CORN_FLOWER" -> Emotion.CORN_FLOWER
            "BLUE_GRAY" -> Emotion.BLUE_GRAY
            "VERY_LIGHT_BROWN" -> Emotion.VERY_LIGHT_BROWN
            "WARM_GRAY" -> Emotion.WARM_GRAY
            else -> throw Exception("MyProfileViewModel - getEmotion - color type is wrong.")
        }
    }

    fun clickBackButton(view: View) {
        _backButton.value?.let {
            _backButton.value = !it
        }
    }

    private fun getPath(uri: Uri): String? {
        _contentResolver.value?.let {
            it.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)?.run {
                val columnIndex = getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                moveToFirst()
                val str = getString(columnIndex)
                close()
                return str
            }
            return null
        }
        return null
    }

    private fun uriToPart(uri: Uri): MultipartBody.Part? {
        return getPath(uri)?.let { strUri ->
            File(strUri).let { file ->
                val mapRequestBody = LinkedHashMap<String, RequestBody>()
                val requestBody =
                    file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

                mapRequestBody["images\"; filename=\"${file.name}"] = requestBody

                MultipartBody.Part.createFormData(
                    "images",
                    file.name,
                    requestBody
                )
            }
        }
    }

    fun setProfileNewImageUri(uri: Uri) {
        _profileNewImageUri.value = uri
        _profileNewImageUri.value?.let {
            uriToPart(it)?.let { part ->
                viewModelScope.launch {
                    val imageUrl = uploadImageUseCase(listOf(part))[0]

                    uploadProfileUseCase(imageUrl).let { userResponse ->
                        if (userResponse.status == 200) {
                            showToast("프로필이 변경되었습니다.")
                        } else {
                            showToast("프로필이 변경 중 오류가 발생했습니다.")
                        }
                    }
                }
            }
        }
    }

    fun setUserInfoLoadYN(userInfoLoadYN: Boolean) {
        _userInfoLoadYN.value = userInfoLoadYN
    }

    fun setContentResolver(contentResolver: ContentResolver) {
        _contentResolver.value = contentResolver
    }

}