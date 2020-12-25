package com.yapp.picon.presentation.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.jraska.falcon.Falcon
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.geometry.Tm128
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.yapp.picon.BR
import com.yapp.picon.R
import com.yapp.picon.databinding.MapActivityBinding
import com.yapp.picon.presentation.base.BaseMapActivity
import com.yapp.picon.presentation.collect.CollectActivity
import com.yapp.picon.presentation.model.EmotionEntity
import com.yapp.picon.presentation.model.Post
import com.yapp.picon.presentation.model.PostMarker
import com.yapp.picon.presentation.nav.NavActivity
import com.yapp.picon.presentation.nav.NavTypeStringSet
import com.yapp.picon.presentation.nav.UserInfoViewModel
import com.yapp.picon.presentation.nav.adapter.NavHeaderEmotionAdapter
import com.yapp.picon.presentation.nav.manageFriend.ManageFriendActivity
import com.yapp.picon.presentation.nav.repository.EmotionDatabaseRepository
import com.yapp.picon.presentation.pingallery.PinGalleryActivity
import com.yapp.picon.presentation.post.PostActivity
import com.yapp.picon.presentation.profile.MyProfileActivity
import com.yapp.picon.presentation.search.SearchActivity
import com.yapp.picon.presentation.util.*
import kotlinx.android.synthetic.main.map_nav_head.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ted.gun0912.clustering.naver.TedNaverClustering
import java.io.File

class MapActivity : BaseMapActivity<MapActivityBinding, MapViewModel>(
    R.layout.map_activity,
    R.id.map_frame
), NavigationView.OnNavigationItemSelectedListener {

    private val tag = "MapActivity"
    override val vm: MapViewModel by viewModel()
    private val userVM: UserInfoViewModel by viewModel()

    private lateinit var naverMap: NaverMap
    private lateinit var emotionDatabaseRepository: EmotionDatabaseRepository
    private lateinit var headerEmotionAdapter: NavHeaderEmotionAdapter

    private val currentLocationMarker = Marker()

    private lateinit var cluster: TedNaverClustering<PostMarker>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userVM.requestUserInfo()
        emotionDatabaseRepository = EmotionDatabaseRepository(application)
        headerEmotionAdapter =
            NavHeaderEmotionAdapter(R.layout.map_nav_head_emotion_item, BR.headEmoItem)
        /*
        todo
            1. 전체 포스트 조회 및 마커로 뿌려주기 이미지 확인
            2. 다시 화면으로 돌아왔을 경우 전체 포스트 다시 조회
         */

        setToolBar()
        setOnListeners()
    }

    override fun onResume() {
        super.onResume()

        setNavHeader()
        setSharedButton()
    }

    override fun initViewModel() {
        binding.setVariable(BR.mapVM, vm)

        vm.toastMsg.observe(this, {
            showToast(it)
        })

        vm.postLoadYN.observe(this, {
            if (it) {
                Log.e(tag, "postLoadYN : showCluster")
                showCluster()
                vm.completeLoadPost()
            }
        })

        vm.sharedMapYN.observe(this) {
            if (it) {
                val locationLayoutParams = ConstraintLayout.LayoutParams(
                    dpToPx(this, 60f).toInt(),
                    dpToPx(this, 60f).toInt()
                )
                locationLayoutParams.setMargins(0,0,18,0)
                locationLayoutParams.endToEnd = R.id.map_constraint_layout
                locationLayoutParams.topToTop = R.id.map_shared_button
                locationLayoutParams.bottomToBottom = R.id.map_shared_button
                binding.mapIbCurrentLocation.layoutParams = locationLayoutParams

                binding.mapSharedButton.visibility = View.VISIBLE
                binding.mapDrawerLayout.closeDrawer(GravityCompat.START)
            } else {
                val locationLayoutParams = ConstraintLayout.LayoutParams(
                    dpToPx(this, 60f).toInt(),
                    dpToPx(this, 60f).toInt()
                )
                locationLayoutParams.startToStart = R.id.map_ib_add
                locationLayoutParams.endToEnd = R.id.map_ib_add
                locationLayoutParams.bottomToTop = R.id.map_ib_add
                binding.mapIbCurrentLocation.layoutParams = locationLayoutParams

                binding.mapSharedButton.visibility = View.GONE
            }
        }
    }

    private fun setToolBar() {
        setSupportActionBar(binding.mapToolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setOnListeners() {
        binding.navView.setNavigationItemSelectedListener(this)

        binding.mapIbMenu.setOnClickListener { binding.mapDrawerLayout.openDrawer(GravityCompat.START) }

        binding.mapIbSearch.setOnClickListener {
            vm.toggleShowBtnYN()
            startSearchActivity()
        }

        binding.mapIbAdd.setOnClickListener {
            vm.toggleShowBtnYN()
            vm.toggleShowPinYN()
        }

        binding.mapIbPostPin.setOnClickListener {
            checkStoragePermission()
        }

        binding.mapIbPinClose.setOnClickListener {
            vm.toggleShowPinYN()
            vm.toggleShowBtnYN()
        }

        binding.mapIbCurrentLocation.setOnClickListener {
            setCurrentLocation()
        }
    }

    private fun setSharedButton() {
        binding.mapSharedButton.setOnClickListener {
            vm.setToggleShowBtnYN(false)
            vm.setSharedMenuButton(false)

            saveBitmapToJPEG()
            getBitmapFromFile()?.let { imageFile ->
                shareTheScreenShot(imageFile)
            }
        }
    }

    private fun saveBitmapToJPEG() {
        val storage: File = cacheDir
        val fileName = "capture.jpg"

        val tempFile = File(storage, fileName) // 저장할 파일 인스턴스 생성

        Falcon.takeScreenshot(this, tempFile) // 스크린샷 파일 저장
    }

    private fun getBitmapFromFile(): File? {
        val files = File(cacheDir.toString()).listFiles()

        files?.let { list ->
            for (file in list) {
                if (file.name == "capture.jpg") return file
            }
        }
        return null
    }

    private fun shareTheScreenShot(file: File) {
        /* todo - 사진파일 공유하는 기능 띄우기
            아래처럼 구현했었는데 오류가 나는 것 같아요.
            혹시 몰라서 주석으로 처리해둔 거라 지우셔도 괜찮습니다.
        */
//        val imageUri = Uri.fromFile(file)
//        Intent(Intent.ACTION_SEND).apply {
//            type = "image/*"
//            putExtra(Intent.EXTRA_STREAM, imageUri)
//        }.let { sharingIntent ->
//            startActivity(Intent.createChooser(sharingIntent, "Share The ScreenShot"))
//        }
    }

    private fun setNavHeader() {
        observeUserToken()

        setUserCircleImage()
        setUserNameText()
        setEmotionActivityStart()
        setHeadEmotionAdapter()
    }

    private fun observeUserToken() {
        userVM.token.observe(this, {
            vm.requestUserInfo(it)
        })
    }

    private fun setUserCircleImage() {
        vm.profileImageUrl.observe(this, {
            if (it.isNotEmpty()) {
                Glide.with(this)
                    .load(it)
                    .circleCrop()
                    .into(binding.navView.getHeaderView(0).nav_head_circle_user_image)
            } else {
                Glide.with(this)
                    .load(R.drawable.profile_pic)
                    .circleCrop()
                    .into(binding.navView.getHeaderView(0).nav_head_circle_user_image)
            }
        })

        binding.navView.getHeaderView(0).nav_head_circle_user_image.setOnClickListener {
            startActivity(
                Intent(this, MyProfileActivity::class.java)
            )
        }
    }

    private fun setUserNameText() {
        vm.profileNickname.observe(this, {
            binding.navView.getHeaderView(0).nav_head_user_name_text.text = it
        })
    }

    private fun setEmotionActivityStart() {
        binding.navView.getHeaderView(0).nav_head_emotion_button.setOnClickListener {
            startNavActivity(NavTypeStringSet.CustomEmotion.type)
        }
    }

    private fun setHeadEmotionAdapter() {
        binding.navView.getHeaderView(0).nav_head_emotion_rv.apply {
            adapter = headerEmotionAdapter
            layoutManager = GridLayoutManager(context, 2)
            setHasFixedSize(true)
        }
        emotionDatabaseRepository.getAll().observe(this, { origin ->
            val list = mutableListOf<EmotionEntity>()
            origin.map { list.add(it) }
            headerEmotionAdapter.setItems(list)
            headerEmotionAdapter.notifyDataSetChanged()

            if (origin.isEmpty()) {
                initDatabase()
            }
        })
    }

    private fun initDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            emotionDatabaseRepository.insert(EmotionEntity(1, "SOFT_BLUE", "새벽 3시"))
            emotionDatabaseRepository.insert(EmotionEntity(2, "CORN_FLOWER", "구름없는 하늘"))
            emotionDatabaseRepository.insert(EmotionEntity(3, "BLUE_GRAY", "아침 이슬"))
            emotionDatabaseRepository.insert(EmotionEntity(4, "VERY_LIGHT_BROWN", "창문 너머 노을"))
            emotionDatabaseRepository.insert(EmotionEntity(5, "WARM_GRAY", "잔잔한 밤"))
        }
    }

    private fun setCurrentLocation() {
        LocationHelper(this).requestLocationPermissions()?.let {
            val cameraUpdate = CameraUpdate.scrollAndZoomTo(
                LatLng(it.latitude, it.longitude),
                NaverCamera.DEFAULT_ZOOM
            )
                .animate(CameraAnimation.Fly, NaverCamera.DEFAULT_ANIMATION_TIME)
            naverMap.moveCamera(cameraUpdate)

            currentLocationMarker.map = null
            currentLocationMarker.run {
                icon = OverlayImage.fromResource(R.drawable.ic_map_now)
                position = LatLng(it.latitude, it.longitude)
                this.map = naverMap
            }
        }
    }

    private fun checkStoragePermission() {
        val storagePermissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                startPostActivity()
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
                showToast("저장 권한을 거부하셨습니다. 해당 기능을 사용할 수 없습니다.")
            }
        }

        TedPermission.with(this)
            .setPermissionListener(storagePermissionListener)
            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .check()
    }

    private fun startSearchActivity() {
        startActivityForResult(
            Intent(this, SearchActivity::class.java),
            RequestCodeSet.SEARCH_REQUEST_CODE.code
        )
    }

    private fun startPostActivity() {
        val intent = Intent(this@MapActivity, PostActivity::class.java)
        intent.putExtra("lat", naverMap.cameraPosition.target.latitude)
        intent.putExtra("lng", naverMap.cameraPosition.target.longitude)
        intent.putExtra("address", vm.address.value)

        startActivityForResult(
            intent,
            ActivityCode.REFRESH_MAP_CODE.code
        )
    }

    private fun startNavActivity(type: String) {
        startActivity(
            Intent(this, NavActivity::class.java)
                .putExtra("type", type)
        )
    }

    private fun startCollectActivity() {
        startActivity(Intent(this, CollectActivity::class.java))
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.map_nav_collect_picture -> startCollectActivity()
            R.id.map_nav_manage_friend -> startActivity(
                Intent(
                    this,
                    ManageFriendActivity::class.java
                )
            )
            R.id.map_nav_view_travel_statistic -> startNavActivity(NavTypeStringSet.Statistic.type)
            R.id.map_nav_share_map -> vm.setSharedMenuButton(true)
            R.id.map_nav_setting -> startNavActivity(NavTypeStringSet.Setting.type)
        }
        return false
    }

    private fun settingOptionToMap() {
        naverMap.apply {
            mapType = NaverMap.MapType.Navi
            isNightModeEnabled = true
            uiSettings.run {
                isZoomControlEnabled = false
                isZoomGesturesEnabled = true
                isCompassEnabled = false
                isScaleBarEnabled = false
                isRotateGesturesEnabled = false
            }

            lightness = -0.5f

            minZoom = NaverCamera.START_POSITION.zoom
            extent = LatLngBounds(NaverCamera.LIMIT_SOUTH_WEST, NaverCamera.LIMIT_NORTH_EAST)

            val cameraUpdate = CameraUpdate.scrollAndZoomTo(
                NaverCamera.START_POSITION.target,
                NaverCamera.START_POSITION.zoom
            )
                .animate(CameraAnimation.Fly, NaverCamera.START_ANIMATION_TIME)
            moveCamera(cameraUpdate)
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        this.naverMap.setOnMapClickListener { _, _ ->
//            Log.e(tag, "click : $latLng")
            vm.showBtnYN.value?.let {
                vm.toggleShowBtnYN()
            }
        }

        /* 카메라 로깅
        this.naverMap.addOnCameraChangeListener { reason, animated ->
            Log.e("NaverMap", "카메라 변경 - reson: $reason, animated: $animated")
            Log.e("NaverMap", "카메라 : ${this.naverMap.cameraPosition}")
        }
         */

        this.naverMap.addOnCameraIdleListener {
            vm.showPinYN.value?.let {
                if (it) {
                    this.naverMap.cameraPosition.target.run {
                        vm.setAddress(latitude, longitude)
                    }
                }
            }
        }

        settingOptionToMap()
        requestPosts()
    }

    private fun requestPosts() {
        vm.requestPosts(this@MapActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RequestCodeSet.SEARCH_REQUEST_CODE.code -> {
                    vm.toggleShowBtnYN()
                    data?.let {
                        val mapX = it.getDoubleExtra("mapX", 0.0)
                        val mapY = it.getDoubleExtra("mapY", 0.0)
                        val tm128 = Tm128(mapX, mapY)
                        val latLng = tm128.toLatLng()
                        val cameraUpdate = CameraUpdate.scrollAndZoomTo(
                            latLng,
                            NaverCamera.DEFAULT_ZOOM
                        ).animate(CameraAnimation.Fly, NaverCamera.DEFAULT_ANIMATION_TIME)
                        naverMap.moveCamera(cameraUpdate)
                    }
                }

                ActivityCode.REFRESH_MAP_CODE.code -> {
                    Log.e(tag, "refresh posts")
                    vm.closePinYN()
                    clearCluster()
                    requestPosts()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (binding.mapDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mapDrawerLayout.closeDrawer(GravityCompat.START)
            return
        }
        if (binding.mapSharedButton.visibility == View.VISIBLE) {
            vm.setSharedMenuButton(false)
            return
        }
        vm.showPinYN.value?.let {
            if (it) {
                vm.toggleShowPinYN()
                vm.toggleShowBtnYN()
                return@onBackPressed
            }
        }
        super.onBackPressed()
    }

    //todo 디자인 수정 필요
    private fun showCluster() {
        vm.postMarkers.value?.let { postMarkers ->
            Log.e(tag, "initCluster")
            cluster = TedNaverClustering.with<PostMarker>(this, naverMap)
                .customMarker { pinMarker(it) }
                .markerClickListener {
                    val item = arrayListOf(toPost(it))

                    startActivityForResult(
                        Intent(this@MapActivity, PinGalleryActivity::class.java)
                            .putParcelableArrayListExtra("posts", item),
                        ActivityCode.REFRESH_MAP_CODE.code
                    )
                }
                .customCluster { vm.getCluster(it.items, this@MapActivity) }
                .clusterClickListener {
                    val items = arrayListOf<Post>()
                    it.items.forEach { postMarker ->
                        items.add(toPost(postMarker))
                    }

                    startActivityForResult(
                        Intent(this@MapActivity, PinGalleryActivity::class.java)
                            .putParcelableArrayListExtra("posts", items),
                        ActivityCode.REFRESH_MAP_CODE.code
                    )
                }
                .items(postMarkers)
                .make()

            refreshCamera()
        }
    }

    private fun clearCluster() {
        Log.e(tag, "clearCluster")
        cluster.clearItems()
    }

    private fun pinMarker(
        postMarker: PostMarker
    ): Marker {
        val latLng =
            LatLng(postMarker.coordinate.lat.toDouble(), postMarker.coordinate.lng.toDouble())

        return Marker(latLng).apply {
            vm.mapMarkerView.value?.get(postMarker.id)?.let {
                icon = OverlayImage.fromView(it)
            }
        }
    }

    private fun refreshCamera() {
        val cameraUpdate = CameraUpdate.scrollAndZoomTo(
            naverMap.cameraPosition.target,
            naverMap.cameraPosition.zoom
        ).animate(CameraAnimation.Fly, NaverCamera.REFRESH_ANIMATION_TIME)
        naverMap.moveCamera(cameraUpdate)
    }

    private fun dpToPx(context: Context, dp: Float): Float {
        val displayMetrics = context.resources.displayMetrics

        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics)
    }

}