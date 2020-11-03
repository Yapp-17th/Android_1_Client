package com.yapp.picon.presentation.nav

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.yapp.picon.presentation.base.BaseViewModel
import com.yapp.picon.presentation.nav.repository.SettingRepository

class NavViewModel: BaseViewModel() {
    val settingRepository = SettingRepository()
    private val _finishFlag = MutableLiveData<Boolean>()

    val finishFlag: LiveData<Boolean> get() = _finishFlag
    val settingRemoveAllDataFlag: LiveData<Boolean> get() = settingRepository.removeAllDataFlag
    val settingReviewFlag: LiveData<Boolean> get() = settingRepository.reviewFlag
    val settingDialogDismissFlag: LiveData<Boolean> get() = settingRepository.dialogDismissFlag
    val settingDialogRemoveFlag: LiveData<Boolean> get() = settingRepository.dialogRemoveFlag

    init {
        _finishFlag.value = false
    }

    fun clickFinishButton(view: View) {
        _finishFlag.value = _finishFlag.value?.let {
            !it
        }
    }

    fun settingInitializeDialogFlag() {
        settingRepository.initializeSettingFlag()
    }
}