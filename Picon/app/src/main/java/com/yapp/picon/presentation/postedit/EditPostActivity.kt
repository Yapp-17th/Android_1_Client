package com.yapp.picon.presentation.postedit

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.yapp.picon.BR
import com.yapp.picon.R
import com.yapp.picon.presentation.model.Address
import com.yapp.picon.databinding.DialogEditPostFinishBinding
import com.yapp.picon.databinding.DialogEditPostRemoveBinding
import com.yapp.picon.databinding.EditPostActivityBinding
import com.yapp.picon.presentation.base.BaseActivity
import com.yapp.picon.presentation.model.Emotion
import com.yapp.picon.presentation.model.Post
import com.yapp.picon.presentation.model.PostEditEmotion
import com.yapp.picon.presentation.nav.repository.EmotionDatabaseRepository
import com.yapp.picon.presentation.postdetail.PostDetailActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditPostActivity: BaseActivity<EditPostActivityBinding, EditPostViewModel>(
    R.layout.edit_post_activity
) {
    private lateinit var finishDialog: Dialog
    private lateinit var finishBuilder: AlertDialog.Builder
    private lateinit var removeDialog: Dialog
    private lateinit var removeBuilder: AlertDialog.Builder
    private lateinit var imageAdapter: EditPostImageAdapter
    private lateinit var emotionAdapter: EditPostEmotionAdapter
    private lateinit var emotionDatabaseRepository: EmotionDatabaseRepository
    private var post: Post? = null

    override val vm: EditPostViewModel by viewModel()

    override fun initViewModel() {
        binding.setVariable(BR.epVM, vm)

        vm.postImageList.observe(this) {
            imageAdapter.setItems(it)
            imageAdapter.notifyDataSetChanged()
        }

        vm.backButton.observe(this) {
            if (it) {
                setFinishDialog()
                finishDialog.show()
                setDialogSize(finishDialog)
            }
        }

        vm.saveButton.observe(this) { isClicked ->
            if (isClicked) {
                post?.let {
                    val address = vm.postAddress.value?.split(" ") ?: listOf()

                    Post(
                        it.id,
                        it.coordinate,
                        vm.postImageList.value,
                        Address(
                            vm.postAddress.value ?: "",
                            address[0],
                            address[1],
                            address[2]
                        ),
                        getSelectedEmotion(emotionAdapter.selectedIndex),
                        vm.postMemo.value,
                        it.createdDate
                    )
                }?.let {
                    vm.savePost(it)

                    val resultIntent = intent.apply {
                        putExtra("resultPost", it)
                    }
                    setResult(PostDetailActivity.SAVE_PIN, resultIntent)
                    finish()
                }
            }
        }

        vm.removePinButton.observe(this) {
            if (it) {
                setRemoveDialog()
                removeDialog.show()
                setDialogSize(removeDialog)
            }
        }

        vm.finishDialogConfirmButton.observe(this) {
            if (it) {
                finishDialog.dismiss()
                finish()
            }
        }

        vm.finishDialogCancelButton.observe(this) {
            if (it) {
                finishDialog.dismiss()
                vm.initialize()
            }
        }

        vm.removeDialogDeleteButton.observe(this) {
            if (it) {
                post?.id?.let { id -> vm.removePost(id) }
                removeDialog.dismiss()
                setResult(PostDetailActivity.DELETE_PIN)
                finish()
            }
        }

        vm.removeDialogCancelButton.observe(this) {
            if (it) {
                removeDialog.dismiss()
                vm.initialize()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setImageRecyclerView()
        setEmotionRecyclerView()
        getColorFromDatabase()
        vm.postEmotion.observe(this) {
            emotionAdapter.selectedIndex = getPostEmotionIndex(it.name)
        }

        post = intent.getParcelableExtra("post")
        post?.let { vm.setPostContents(it) }
    }

    private fun setImageRecyclerView() {
        imageAdapter = EditPostImageAdapter(
            this,
            R.layout.post_edit_image_item,
            BR.peitem
        )
        binding.editPostImageRv.apply {
            adapter = imageAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }
    }

    private fun setEmotionRecyclerView() {
        emotionAdapter = EditPostEmotionAdapter(
            R.layout.post_edit_emotion_item,
            BR.peeItem
        )
        binding.editPostEmotionRv.apply {
            adapter = emotionAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
    }

    private fun getColorFromDatabase() {
        emotionDatabaseRepository = EmotionDatabaseRepository(application)
        emotionDatabaseRepository.getAll().observe(this) { list ->
            list.map {
                PostEditEmotion(
                    it.color,
                    it.emotion
                )
            }.let {
                emotionAdapter.setItems(it)
                emotionAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun getSelectedEmotion(index: Int): Emotion {
        val eList: List<Emotion> = listOf(
            Emotion.SOFT_BLUE,
            Emotion.CORN_FLOWER,
            Emotion.BLUE_GRAY,
            Emotion.VERY_LIGHT_BROWN,
            Emotion.WARM_GRAY
        )

        return eList[index]
    }

    private fun getPostEmotionIndex(emotion: String): Int {
        return when (emotion) {
            "SOFT_BLUE" -> 0
            "CORN_FLOWER" -> 1
            "BLUE_GRAY" -> 2
            "VERY_LIGHT_BROWN" -> 3
            "WARM_GRAY" -> 4
            else -> throw Exception("EditPostActivity - getPostEmotionIndex - emotion string is wrong.")
        }
    }

    private fun setFinishDialog() {
        val dialogFinishView: DialogEditPostFinishBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this), R.layout.dialog_edit_post_finish, null, false
        )
        dialogFinishView.setVariable(BR.depVM, vm)
        finishBuilder = AlertDialog.Builder(this)
        finishBuilder.setView(dialogFinishView.root)

        finishDialog = finishBuilder.create()
        finishDialog.setCancelable(false)
        finishDialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun setRemoveDialog() {
        val dialogRemoveView: DialogEditPostRemoveBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this), R.layout.dialog_edit_post_remove, null, false
        )
        dialogRemoveView.setVariable(BR.depVM, vm)
        removeBuilder = AlertDialog.Builder(this)
        removeBuilder.setView(dialogRemoveView.root)

        removeDialog = removeBuilder.create()
        removeDialog.setCancelable(false)
        removeDialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun setDialogSize(dialog: Dialog) {
        val layoutParams = WindowManager.LayoutParams().apply {
            copyFrom(dialog.window!!.attributes)
            width = (274 * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        dialog.window?.attributes = layoutParams
    }
}