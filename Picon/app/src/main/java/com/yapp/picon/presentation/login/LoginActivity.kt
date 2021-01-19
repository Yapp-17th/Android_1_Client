package com.yapp.picon.presentation.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.yapp.picon.BR
import com.yapp.picon.R
import com.yapp.picon.databinding.LoginActivityBinding
import com.yapp.picon.presentation.base.BaseActivity
import com.yapp.picon.presentation.map.MapActivity
import com.yapp.picon.presentation.simplejoin.SimpleJoinActivity
import com.yapp.picon.presentation.util.ActivityCode
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : BaseActivity<LoginActivityBinding, LoginViewModel>(
    R.layout.login_activity
) {

    override val vm: LoginViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setListeners()
    }

    private fun setListeners() {
        binding.loginBtnLogin.setOnClickListener { vm.login() }
        binding.loginTvSimpleJoin.setOnClickListener { startSimpleJoinActivity() }
        binding.loginIbNaver.setOnClickListener { showToast(getString(R.string.update_plan_guide_text)) }
        binding.loginIbKakao.setOnClickListener { showToast(getString(R.string.update_plan_guide_text)) }
    }

    private fun startSimpleJoinActivity() {
        startActivityForResult(
            Intent(this, SimpleJoinActivity::class.java),
            ActivityCode.SIMPLE_JOIN.code
        )
    }

    private fun startMapActivity() {
        startActivity(Intent(this, MapActivity::class.java))
    }

    override fun initViewModel() {
        binding.setVariable(BR.loginVM, vm)

        val toastMsgObserver = Observer<String> {
            showToast(it)
        }
        vm.toastMsg.observe(this, toastMsgObserver)

        val loginYNObserver = Observer<Boolean> {
            if (it) {
                startMapActivity()
                finish()
            }
        }
        vm.loginYN.observe(this, loginYNObserver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ActivityCode.SIMPLE_JOIN.code) {
                data?.run {
                    getStringExtra("id")?.let {
                        vm.id.value = it
                    }
                    getStringExtra("pw").let {
                        vm.pw.value = it
                    }
                    vm.login()
                }
            }
        }
    }

}