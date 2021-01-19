package com.yapp.picon.presentation.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter
import com.sangcomz.fishbun.define.Define
import com.yapp.picon.BR
import com.yapp.picon.R
import com.yapp.picon.databinding.MyProfileActivityBinding
import com.yapp.picon.presentation.base.BaseActivity
import com.yapp.picon.presentation.model.Post
import com.yapp.picon.presentation.postdetail.PostDetailActivity
import org.koin.androidx.viewmodel.ext.android.viewModel


class MyProfileActivity : BaseActivity<MyProfileActivityBinding, MyProfileViewModel>(
    R.layout.my_profile_activity
) {
    private lateinit var postAdapter: MyProfilePostAdapter

    override val vm: MyProfileViewModel by viewModel()

    override fun initViewModel() {
        binding.setVariable(BR.profileVM, vm)

        vm.backButton.observe(this, {
            if (it) onBackPressed()
        })
        vm.userInfoLoadYN.observe(this, {
            if (it) {
                setProfileImage()
                vm.setUserInfoLoadYN(false)
            }
        })
        vm.postList.observe(this, {
            postAdapter.setItems(it)
            postAdapter.notifyDataSetChanged()

            if (it.isEmpty()) binding.myProfileEmptyPostText.visibility = View.VISIBLE
            else binding.myProfileEmptyPostText.visibility = View.GONE
        })
        vm.profileNewImageUri.observe(this, {
            setProfileImage(it)
        })
        vm.toastMsg.observe(this, {
            showToast(it)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setAdapter()
        setListener()
        vm.requestUserInfo()
        vm.setContentResolver(contentResolver)
    }

    override fun onResume() {
        super.onResume()
        vm.initFlags()
        vm.requestPosts()
    }

    private fun setAdapter() {
        postAdapter = MyProfilePostAdapter(
            this,
            { view, post -> startPostDetailActivity(view, post) },
            R.layout.my_profile_post_item,
            BR.profilePost
        )

        binding.myProfilePostRv.apply {
            adapter = postAdapter
            layoutManager = GridLayoutManager(context, 3)
            setHasFixedSize(true)
        }
    }

    private fun setListener() {
        binding.myProfileUserImageEdit.setOnClickListener { checkStoragePermission() }
    }

    private fun setProfileImage() {
        vm.profileImageUrl.value?.let {
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(binding.myProfileUserImage)
        }
    }

    private fun setProfileImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .into(binding.myProfileUserImage)
    }

    private fun startPostDetailActivity(view: View, post: Post) {
        Intent(this, PostDetailActivity::class.java).apply {
            putExtra("post", post)
            putExtra("isFriend", false)
        }.let {
            startActivity(it)
        }
    }

    private fun checkStoragePermission() {
        val storagePermissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                startAlbum()
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

    private fun startAlbum() {
        FishBun.with(this@MyProfileActivity)
            .setImageAdapter(GlideAdapter())
            .setActionBarColor(
                ResourcesCompat.getColor(resources, R.color.dark_grey, null),
                ResourcesCompat.getColor(resources, R.color.dark_grey, null),
                false
            ).setActionBarTitleColor(Color.parseColor("#ffffff"))
            .setPickerSpanCount(3)
            .setMaxCount(1)
            .isStartInAllView(true)
            .startAlbum()
    }

    private fun changeProfileImage(uri: Uri) {
        startLoading()
        vm.setProfileNewImageUri(uri)
        stopLoading()
    }

    private fun startLoading() {
        binding.myProfileProgressBar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun stopLoading() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.myProfileProgressBar.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            Define.ALBUM_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.run {
                        getParcelableArrayListExtra<Uri>(Define.INTENT_PATH)?.let {
                            changeProfileImage(it[0])
                        }
                    }
                }
            }
        }
    }

}