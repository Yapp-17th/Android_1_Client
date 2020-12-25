package com.yapp.picon.presentation.nav

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface.BOLD
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.yapp.picon.BR
import com.yapp.picon.R
import com.yapp.picon.databinding.DialogSettingBinding
import com.yapp.picon.databinding.NavSettingFragmentBinding
import com.yapp.picon.presentation.base.BaseFragment
import com.yapp.picon.presentation.nav.repository.SettingRepository
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SettingFragment: BaseFragment<NavSettingFragmentBinding, NavViewModel>(
    R.layout.nav_setting_fragment
) {
    private lateinit var dialog: AlertDialog
    private lateinit var repo: SettingRepository
    private lateinit var dialogBinding: DialogSettingBinding

    override val vm: NavViewModel by sharedViewModel()

    override fun initBinding() {
        binding.setVariable(BR.navVM, vm)
        binding.setVariable(BR.settingRepo, vm.settingRepository)
    }

    override fun onStart() {
        super.onStart()
        repo = vm.settingRepository

        val spannableString = SpannableString(getString(R.string.nav_setting_about))
        spannableString.setSpan(
            StyleSpan(BOLD),
            6,
            11,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.aboutTv.text = spannableString

        setReviewButton()
        setWithdrawalButton()
        setLogoutButton()
    }

    private fun setReviewButton() {
        repo.reviewFlag.observe(this, {
            if (it) {
                 startAppStore()
            }
        })
    }

    private fun startAppStore() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(
                "https://play.google.com/store/apps/details?id=com.yapp.picon"
            )
            setPackage("com.android.vending")
        }
        startActivity(intent)
    }

    private fun setWithdrawalButton() {
        repo.withdrawalFlag.observe(this, {
            if (it) {
                setDialog()
                setWithdrawalDialogView()
                dialog.show()
                setDialogSize()
            }
        })
    }

    @SuppressLint("ResourceType")
    private fun setWithdrawalDialogView() {
        dialogBinding.dialogSettingTitle.text = getString(R.string.nav_setting_dialog_withdrawal_title)
        dialogBinding.dialogSettingContent.text = getString(R.string.nav_setting_dialog_withdrawal_content)
        dialogBinding.dialogSettingLeftButton.apply {
            text = getString(R.string.withdrawal)
            setTextColor(Color.parseColor(getString(R.color.coral)))
            setOnClickListener {
                withdrawalTheAccount()
                Toast.makeText(context, "계정이 탈퇴되었습니다.", Toast.LENGTH_SHORT).show()
                dialogDismiss()
            }
        }
        dialogBinding.dialogSettingRightButton.apply {
            text = getString(R.string.cancellation)
            setTextColor(Color.parseColor(getString(R.color.very_light_pink)))
            setOnClickListener {
                dialogDismiss()
            }
        }
    }

    private fun withdrawalTheAccount() {
        // todo - 계정 탈퇴
    }

    private fun setLogoutButton() {
        repo.logoutFlag.observe(this, {
            if (it) {
                setDialog()
                setLogoutDialogView()
                dialog.show()
                setDialogSize()
            }
        })
    }

    @SuppressLint("ResourceType")
    private fun setLogoutDialogView() {
        dialogBinding.dialogSettingTitle.text = getString(R.string.nav_setting_dialog_logout_title)
        dialogBinding.dialogSettingContent.text = ""
        dialogBinding.dialogSettingLeftButton.apply {
            text = getString(R.string.cancellation)
            setTextColor(Color.parseColor(getString(R.color.very_light_pink)))
            setOnClickListener {
                dialogDismiss()
            }
        }
        dialogBinding.dialogSettingRightButton.apply {
            text = getString(R.string.nav_setting_logout)
            setTextColor(Color.parseColor(getString(R.color.coral)))
            setOnClickListener {
                logoutAccount()
                Toast.makeText(context, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                dialogDismiss()
            }
        }
    }

    private fun logoutAccount() {
        // todo - 계정 로그아웃
        vm.logout()
    }

    private fun setDialog() {
        dialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_setting,
            null,
            false
        )
        val builder = AlertDialog.Builder(context)
        builder.setView(dialogBinding.root)
        dialog = builder.create()
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun setDialogSize() {
        val layoutParams = WindowManager.LayoutParams().apply {
            copyFrom(dialog.window!!.attributes)
            width = (274 * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        dialog.window?.attributes = layoutParams
    }

    private fun dialogDismiss() {
        dialog.dismiss()
        vm.settingInitializeDialogFlag()
    }
}