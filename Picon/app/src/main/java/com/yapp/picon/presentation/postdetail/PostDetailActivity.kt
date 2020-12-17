package com.yapp.picon.presentation.postdetail

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.yapp.picon.BR
import com.yapp.picon.R
import com.yapp.picon.databinding.DialogCustomRemovePostBinding
import com.yapp.picon.databinding.PostDetailActivityBinding
import com.yapp.picon.presentation.base.BaseActivity
import com.yapp.picon.presentation.model.Emotion
import com.yapp.picon.presentation.model.Post
import com.yapp.picon.presentation.nav.repository.EmotionDatabaseRepository
import com.yapp.picon.presentation.postedit.EditPostActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class PostDetailActivity: BaseActivity<PostDetailActivityBinding, PostDetailViewModel>(
    R.layout.post_detail_activity
) {
    companion object {
        const val DELETE_PIN: Int = 400
        const val SAVE_PIN: Int = 300
    }

    private lateinit var imagePagerAdapter: ImagePagerAdapter
    private lateinit var emotionDatabaseRepository: EmotionDatabaseRepository
    private lateinit var removeDialog: Dialog
    private lateinit var removeBuilder: AlertDialog.Builder
    private var post: Post? = null
    private var totalImage = 0
    private var id = -1
    private var emotionColor = ""

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100) {
            if (resultCode == DELETE_PIN) finish()
            else if (resultCode == SAVE_PIN) {
                data?.let {
                    it.getParcelableExtra<Post>("resultPost")?.let { rPost ->
                        post = rPost
                        totalImage = rPost.imageUrls?.size ?: 0
                        id = rPost.id ?: -1

                        setViewModel(rPost)
                        emotionColor = rPost.emotion?.name ?: ""
                        setEmotionCircleImage(rPost.emotion?.name ?: "")
                        setBackgroundColor(rPost.emotion?.name ?: "")
                    }
                }
            }
        }
    }

    override val vm: PostDetailViewModel by viewModel()

    override fun initViewModel() {
        binding.setVariable(BR.postDetailVM, vm)

        vm.imageList.observe(this, {
            imagePagerAdapter.setItems(it)
        })
        vm.content.observe(this, {
            if (it.isEmpty()) binding.postDetailScrollView.visibility = View.GONE
            else binding.postDetailScrollView.visibility = View.VISIBLE
        })
        vm.editIconFlag.observe(this, {
            if (it) {
                binding.postDetailEditLinear.visibility = View.VISIBLE
            } else {
                binding.postDetailEditLinear.visibility = View.GONE
            }
        })
        vm.editButtonFlag.observe(this, {
            if (it) {
                vm.clickEditIcon(binding.postDetailEditIconButton)
                startEditPostActivity()
            }
        })
        vm.removeButtonFlag.observe(this, {
            if (it) {
                removeDialog.show()
                setDialogSize()
                vm.clickEditIcon(binding.postDetailEditIconButton)
            }
        })
        vm.dialogRemoveButtonFlag.observe(this, {
            if (it) {
                removeDialog.dismiss()
                vm.removePost(id)
                finish()
            }
        })
        vm.dialogCancelButtonFlag.observe(this, {
            if (it) {
                removeDialog.dismiss()
                vm.initFlag()
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        emotionDatabaseRepository = EmotionDatabaseRepository(application)

        val isFriend = intent.getBooleanExtra("isFriend", true)
        if (isFriend) binding.postDetailEditIconButton.visibility = View.GONE
        else binding.postDetailEditIconButton.visibility = View.VISIBLE

        window.run {
            sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
            sharedElementExitTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        }

        setImagePager()
        setFinishDialog()
        getPostFromIntent()
    }

    override fun onResume() {
        super.onResume()

        vm.initFlag()
    }

    private fun startEditPostActivity() {
        post?.let {
            Intent(this, EditPostActivity::class.java).apply {
                putExtra("post", it)
            }.let { startActivityForResult(it, 100) }
        } ?: showToast("게시물이 존재하지 않습니다.")

        /** Warning
        원래 편집 화면을 띄우고 다시 돌아오는 과정에서 게시물 내용이 갱신되도록 해야 하는데,
        서버에 게시글 조회 요청할 때, id로 조회하는 게 없어서 갱신이 불가합니다.
        */
    }

    private fun setImagePager() {
        imagePagerAdapter = ImagePagerAdapter(this) { img ->
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                binding.postDetailImagePager,
                "image"
            )

            Intent(this, PostDetailImageActivity::class.java).apply {
                putExtra("image", img)
                putExtra("color", emotionColor)
            }.let { startActivity(it, options.toBundle()) }
        }

        binding.postDetailImagePager.apply {
            adapter = imagePagerAdapter
            registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    vm.setImageNumber(position+1, totalImage)
                }
            })
        }
    }

    private fun getPostFromIntent() {
        post = intent.getParcelableExtra<Post>("post")
        post?.let {
            totalImage = it.imageUrls?.size ?: 0
            id = it.id ?: -1

            setViewModel(it)
            emotionColor = it.emotion?.name ?: ""
            setEmotionCircleImage(it.emotion?.name ?: "")
            setBackgroundColor(it.emotion?.name ?: "")
        }
    }

    private fun setViewModel(post: Post) {
        val dateList = post.createdDate?.split('.')
            ?: listOf("2020", "00", "00")

        vm.setImageList(post.imageUrls ?: listOf())
        vm.setImageNumber(1, totalImage)
        vm.setAddress(post.address.address)
        vm.setDate(dateList[0].toInt(), dateList[1].toInt(), dateList[2].toInt())
        vm.setContent(post.memo ?: "")

        emotionDatabaseRepository.getAll().observe(this, { list ->
            vm.setEmotion(
                list[getColorIndex(post.emotion)].emotion
            )
        })
    }

    private fun setFinishDialog() {
        val dialogRemoveView: DialogCustomRemovePostBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this), R.layout.dialog_custom_remove_post, null, false
        )
        dialogRemoveView.setVariable(BR.dialogRemove, vm)
        removeBuilder = AlertDialog.Builder(this)
        removeBuilder.setView(dialogRemoveView.root)

        removeDialog = removeBuilder.create()
        removeDialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun setDialogSize() {
        val layoutParams = WindowManager.LayoutParams().apply {
            copyFrom(removeDialog.window!!.attributes)
            width = (274 * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        removeDialog.window?.attributes = layoutParams
    }

    private fun setEmotionCircleImage(color: String) {
        binding.postDetailColorCircleImage.setImageResource(
            when (color) {
                "SOFT_BLUE" -> R.drawable.ic_custom_circle_soft_blue
                "CORN_FLOWER" -> R.drawable.ic_custom_circle_cornflower
                "BLUE_GRAY" -> R.drawable.ic_custom_circle_bluegrey
                "VERY_LIGHT_BROWN" -> R.drawable.ic_custom_circle_very_light_brown
                "WARM_GRAY" -> R.drawable.ic_custom_circle_warm_grey
                else -> throw Exception("PostDetailActivity - setEmotionCircleImage - color type is wrong.")
            }
        )
    }

    private fun setBackgroundColor(color: String) {
        binding.postDetailLayout.setBackgroundResource(
            when (color) {
                "SOFT_BLUE" -> R.color.soft_blue_30
                "CORN_FLOWER" -> R.color.cornflower_30
                "BLUE_GRAY" -> R.color.blue_gray_30
                "VERY_LIGHT_BROWN" -> R.color.very_light_brown_30
                "WARM_GRAY" -> R.color.warm_gray_30
                else -> throw Exception("PostDetailActivity - setBackgroundColor - color type is wrong.")
            }
        )
    }

    private fun getColorIndex(value: Emotion?): Int {
        return when(value) {
            Emotion.SOFT_BLUE -> 0
            Emotion.CORN_FLOWER -> 1
            Emotion.BLUE_GRAY -> 2
            Emotion.VERY_LIGHT_BROWN -> 3
            Emotion.WARM_GRAY -> 4
            else -> throw Exception("PostDetailActivity - getColorIndex - emotion is null.")
        }
    }

    override fun onBackPressed() {
        binding.postDetailImagePager.currentItem.let { cur ->
            if (cur == 0) super.onBackPressed()
            else binding.postDetailImagePager.currentItem = cur - 1
        }
    }
}