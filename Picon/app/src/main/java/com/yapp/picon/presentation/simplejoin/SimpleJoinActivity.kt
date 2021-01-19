package com.yapp.picon.presentation.simplejoin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.lifecycle.Observer
import com.yapp.picon.BR
import com.yapp.picon.R
import com.yapp.picon.databinding.SimpleJoinActivityBinding
import com.yapp.picon.presentation.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class SimpleJoinActivity : BaseActivity<SimpleJoinActivityBinding, SimpleJoinViewModel>(
    R.layout.simple_join_activity
) {

    override val vm: SimpleJoinViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setListeners()
    }

    private fun setListeners() {
        binding.simpleJoinBtnJoin.setOnClickListener { joinMembership() }

        View.OnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                if ((view as EditText).text.isNullOrBlank()) {
                    showTextEssential(view.id)
                } else {
                    hideTextEssential(view.id)
                }
            }
        }.let {
            binding.run {
                simpleJoinEtId.onFocusChangeListener = it
                simpleJoinEtPw.onFocusChangeListener = it
                simpleJoinEtPwRe.onFocusChangeListener = it
                simpleJoinEtNic.onFocusChangeListener = it
            }
        }
    }

    private fun joinMembership() {
        startLoading()
        vm.joinMembership()
        stopLoading()
    }

    private fun startLoading() {
        binding.simpleJoinProgressBar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun stopLoading() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.simpleJoinProgressBar.visibility = View.GONE
    }

    private fun showTextEssential(viewId: Int) {
        when (viewId) {
            binding.simpleJoinEtId.id -> vm.showIdIsBlank()
            binding.simpleJoinEtPw.id -> vm.showPwIsBlank()
            binding.simpleJoinEtPwRe.id -> vm.showPwReIsBlank()
            binding.simpleJoinEtNic.id -> vm.showNicIsBlank()
        }
    }

    private fun hideTextEssential(viewId: Int) {
        when (viewId) {
            binding.simpleJoinEtId.id -> vm.hideIdIsBlank()
            binding.simpleJoinEtPw.id -> vm.hidePwIsBlank()
            binding.simpleJoinEtPwRe.id -> vm.hidePwReIsBlank()
            binding.simpleJoinEtNic.id -> vm.hideNicIsBlank()
        }
    }

    override fun initViewModel() {
        binding.setVariable(BR.simpleJoinVM, vm)

        val toastMsgObserver = Observer<String> {
            showToast(it)
        }
        vm.toastMsg.observe(this, toastMsgObserver)

        val joinYNObserver = Observer<Boolean> {
            if (it) {
                val intent = Intent()
                vm.id.value?.let { id ->
                    intent.putExtra("id", id)
                }
                vm.pw.value?.let { pw ->
                    intent.putExtra("pw", pw)
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
        vm.joinYN.observe(this, joinYNObserver)
    }
}