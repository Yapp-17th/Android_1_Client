package com.yapp.picon.presentation.map

import android.app.Activity
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.yapp.picon.R
import com.yapp.picon.data.model.PostsResponse
import com.yapp.picon.data.network.NetworkModule
import com.yapp.picon.databinding.MarkerViewBinding
import com.yapp.picon.domain.usecase.GetRevGeoUseCase
import com.yapp.picon.domain.usecase.RequestPostsUseCase
import com.yapp.picon.presentation.base.BaseViewModel
import com.yapp.picon.presentation.model.Emotion
import com.yapp.picon.presentation.model.PostMarker
import com.yapp.picon.presentation.util.toPostMarker
import com.yapp.picon.presentation.util.toPresentation
import jp.wasabeef.glide.transformations.MaskTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapViewModel(
    private val requestPostsUseCase: RequestPostsUseCase,
    private val getRevGeoUseCase: GetRevGeoUseCase
) : BaseViewModel() {
    private val tag = "MapViewModel"

    private val _showBtnYN = MutableLiveData<Boolean>()
    val showBtnYN: LiveData<Boolean> get() = _showBtnYN

    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String> get() = _toastMsg

    private val _showPinYN = MutableLiveData<Boolean>()
    val showPinYN: LiveData<Boolean> get() = _showPinYN

    private val _sharedMapYN = MutableLiveData<Boolean>()
    val sharedMapYN: LiveData<Boolean> get() = _sharedMapYN

    private val _postMarkers = MutableLiveData<List<PostMarker>>()
    val postMarkers: LiveData<List<PostMarker>> get() = _postMarkers

    private val _postLoadYN = MutableLiveData<Boolean>()
    val postLoadYN: LiveData<Boolean> get() = _postLoadYN

    private val _showAddressYN = MutableLiveData<Boolean>()
    val showAddressYN: LiveData<Boolean> get() = _showAddressYN

    private val _profileNickname = MutableLiveData<String>()
    val profileNickname: LiveData<String> get() = _profileNickname

    private val _profileImageUrl = MutableLiveData<String>()
    val profileImageUrl: LiveData<String> get() = _profileImageUrl

    var address = MutableLiveData<String>()

    private val _mapMarkerView = MutableLiveData<MutableMap<Int, View>>()
    val mapMarkerView: LiveData<MutableMap<Int, View>> get() = _mapMarkerView

    init {
        _showBtnYN.value = false
        _showPinYN.value = false
        _sharedMapYN.value = false
        _postMarkers.value = mutableListOf()
        _postLoadYN.value = false
        _showAddressYN.value = false
        _profileNickname.value = ""
        _profileImageUrl.value = ""
        _mapMarkerView.value = mutableMapOf()
    }

    private fun showToast(msg: String) {
        _toastMsg.value = msg
    }

    fun setToggleShowBtnYN(value: Boolean) {
        _showBtnYN.value = value
    }

    fun toggleShowBtnYN() {
        _showBtnYN.value = _showBtnYN.value?.let {
            !it
        }
    }

    fun toggleShowPinYN() {
        _showPinYN.value = _showPinYN.value?.let {
            !it
        }
    }

    fun closePinYN() {
        _showPinYN.value = false
    }

    fun requestUserInfo(token: String) {
        viewModelScope.launch {
            try {
                NetworkModule.yappApi.requestUserInfo(token).let {
                    _profileNickname.value = it.memberDetailDto.member.nickName
                    _profileImageUrl.value = it.memberDetailDto.member.profileImageUrl ?: ""
                }
            } catch (e: Exception) {
                Log.e("MyProfileViewModel", "requestUserInfo Error - ${e.message}")
            }
        }
    }

    private fun emotionToDrawable(emotion: Emotion): Int {
        return when (emotion) {
            Emotion.SOFT_BLUE -> R.drawable.pin_soft_blue
            Emotion.CORN_FLOWER -> R.drawable.pin_cornflower
            Emotion.BLUE_GRAY -> R.drawable.pin_blue_grey
            Emotion.VERY_LIGHT_BROWN -> R.drawable.pin_very_light_brown
            Emotion.WARM_GRAY -> R.drawable.pin_warm_grey
        }
    }

    private fun emotionToBackground(emotion: Emotion): Int {
        return when (emotion) {
            Emotion.SOFT_BLUE -> R.drawable.bg_soft_blue_radius_1dp
            Emotion.CORN_FLOWER -> R.drawable.bg_cornflower_radius_1dp
            Emotion.BLUE_GRAY -> R.drawable.bg_blue_grey_radius_1dp
            Emotion.VERY_LIGHT_BROWN -> R.drawable.bg_very_light_brown_radius_1dp
            Emotion.WARM_GRAY -> R.drawable.bg_warm_grey_radius_1dp
        }
    }

    fun requestPosts(activity: Activity) {
        viewModelScope.launch {
            val posts = requestPostsUseCase()

            setMarkers(posts)
            setImages(activity)
        }
    }

    private suspend fun setMarkers(postsResponse: PostsResponse) =
        withContext(Dispatchers.Main) {
            if (postsResponse.status == 200) {
                _postMarkers.value = postsResponse.posts.map { post ->
                    toPostMarker(toPresentation(post))
                }
            }
        }

    private suspend fun setImages(activity: Activity) = withContext(Dispatchers.IO) {
        _postMarkers.value?.forEach { postMarker ->
            postMarker.id?.let { id ->
                postMarker.imageUrls?.get(0)?.let { url ->
                    Glide.with(activity)
                        .load(url)
                        .override(64, 64)
                        .centerCrop()
                        .apply(
                            RequestOptions.bitmapTransform(
                                MaskTransformation(R.drawable.pin_image)
                            )
                        )
                        .error(R.drawable.pin_image)
                        .submit()
                        .get()?.let {
                            withContext(Dispatchers.Main) {
                                val markerView = MarkerViewBinding.inflate(activity.layoutInflater)
                                    .apply {
                                        postMarker.emotion?.let { emotion ->
                                            markerViewCl.setBackgroundResource(
                                                emotionToDrawable(emotion)
                                            )
                                        }

                                        markerViewIvImage.setImageDrawable(it)
                                    }.root

                                _mapMarkerView.value?.put(id, markerView)

                                Log.e(tag, "markerImages SIZE : ${_mapMarkerView.value?.size}")
                            }
                        }
                }
            }
        }

        _postLoadYN.postValue(true)
    }

    private fun dpToPxInt(activity: Activity, dp: Int): Int {
        val displayMetrics = activity.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), displayMetrics)
            .toInt()
    }

    //todo 포스트 개수 정확하지 않음
    fun getCluster(
        postMarkers: Collection<PostMarker>,
        activity: Activity
    ): View {
        val thisPostMarker = postMarkers
            .filter { it.imageUrls?.size ?: 0 > 0 }
            .random()

        val clusterConstraintLayout = ConstraintLayout(activity)

        val clusterLayoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            marginStart = dpToPxInt(activity, 8)
        }

        val clusterTextView = TextView(activity).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                topMargin = dpToPxInt(activity, 12)
                minWidth = dpToPxInt(activity, 16)
                setPadding(dpToPxInt(activity, 4), 0, dpToPxInt(activity, 4), 0)
            }

            text = postMarkers.count().toString()
            setTextColor(ResourcesCompat.getColor(activity.resources, R.color.pale_grey, null))
            textSize = 11F

            thisPostMarker.emotion?.let {
                setBackgroundResource(emotionToBackground(it))
            }
        }

        return clusterConstraintLayout.apply {
            _mapMarkerView.value?.get(thisPostMarker.id)?.apply {
                layoutParams = clusterLayoutParams
                try {
                    clusterConstraintLayout.addView(this)
                } catch (e: IllegalStateException) {
                    clusterConstraintLayout.removeAllViews()
                    clusterConstraintLayout.addView(this)
                }
            }
            clusterConstraintLayout.addView(clusterTextView)
        }
    }

    fun completeLoadPost() {
        _postLoadYN.value = false
    }

    fun setAddress(lat: Double, lng: Double) {
        viewModelScope.launch {
            val coords = "$lng,$lat"

            try {
                getRevGeoUseCase(coords).let {
                    // RevGeoResult 객체 사용 (RevGeoResult 객체가 반환됩니다.)
                    Log.e(tag, it.toString())

                    if (it.status.code == 0) {
                        val area1 = it.results[0].region.area1.name
                        val area2 = it.results[0].region.area2.name
                        val area3 = it.results[0].region.area3.name
                        val area4 = it.results[0].region.area4.name
                        val addressText = "$area1 $area2 $area3 $area4"
                        _showAddressYN.value = true
                        address.value = addressText
                    } else {
                        _showAddressYN.value = false
                        address.value = ""
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Reverse Geo Error - ${e.message}")
                _showAddressYN.value = false
                address.value = ""
            }
        }
    }

    fun setSharedMenuButton(value: Boolean) {
        _sharedMapYN.value = value
    }
}