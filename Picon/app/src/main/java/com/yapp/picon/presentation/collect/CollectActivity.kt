package com.yapp.picon.presentation.collect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.yapp.picon.BR
import com.yapp.picon.R
import com.yapp.picon.databinding.CollectActivityBinding
import com.yapp.picon.databinding.CollectItemBinding
import com.yapp.picon.presentation.base.BaseActivity
import com.yapp.picon.presentation.model.Pin
import com.yapp.picon.presentation.model.Post
import com.yapp.picon.presentation.nav.repository.EmotionDatabaseRepository
import com.yapp.picon.presentation.postdetail.PostDetailActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class CollectActivity : BaseActivity<CollectActivityBinding, CollectViewModel>(
    R.layout.collect_activity
) {

    override val vm: CollectViewModel by viewModel()

    private val collectAdapter =
        object :
            CollectClickAdapter<CollectItemBinding>(
                R.layout.collect_item,
                BR.collectItem,
                { item: Pin -> itemClicked(item) }
            ) {}

    private val collectBottomSheetDialog = CollectBottomSheetDialog()

    private fun itemClicked(item: Pin) {
        clickItem(item)
    }

    private fun startPostDetailActivity(post: Post) {
        Intent(this, PostDetailActivity::class.java).apply {
            putExtra("post", post)
        }.let {
            startActivity(it)
        }
    }

    private fun clickItem(item: Pin) {
        item.let {
            it.id?.let { id ->
                vm.getPost(id)?.let { post ->
                    startPostDetailActivity(post)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setAdapter()
        setListeners()
        setPosts()
        setEmotions()
    }

    private fun setAdapter() {
        binding.collectRecyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.collectRecyclerView.adapter = collectAdapter
    }

    private fun setListeners() {
        binding.collectIbBack.setOnClickListener { finish() }
        binding.collectIbFilter.setOnClickListener { vm.toggleShowFilterYN() }
    }

    private fun setPosts() {
        vm.setPosts()
    }

    private fun setEmotions() {
        EmotionDatabaseRepository(application).getAll().observe(this, { vm.setEmotions(it) })
    }

    override fun initViewModel() {
        binding.setVariable(BR.collectVM, vm)

        val toastMsgObserver = Observer<String> {
            showToast(it)
        }
        vm.toastMsg.observe(this, toastMsgObserver)

        val showFilterYNObserver = Observer<Boolean> {
            Log.e(TAG, "showFilterYNObserver : $it")

            if (it) {
                collectBottomSheetDialog.show(supportFragmentManager, "CollectBottomSheetDialog")
            } else {
                if (collectBottomSheetDialog.isVisible) {
                    collectBottomSheetDialog.dismiss()
                }
            }
        }
        vm.showFilterYN.observe(this, showFilterYNObserver)
    }

    companion object {
        private const val TAG = "CollectActivity"
    }

}