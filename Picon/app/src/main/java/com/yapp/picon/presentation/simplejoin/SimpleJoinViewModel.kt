package com.yapp.picon.presentation.simplejoin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.yapp.picon.domain.usecase.SimpleJoinUseCase
import com.yapp.picon.presentation.base.BaseViewModel
import kotlinx.coroutines.launch

class SimpleJoinViewModel(
    private val simpleJoinUseCase: SimpleJoinUseCase
) : BaseViewModel() {

    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String> get() = _toastMsg

    private val _joinYN = MutableLiveData<Boolean>()
    val joinYN: LiveData<Boolean> get() = _joinYN

    var id = MutableLiveData<String>()
    var pw = MutableLiveData<String>()
    var pwRe = MutableLiveData<String>()
    var nic = MutableLiveData<String>()

    private val _idIsBlank = MutableLiveData<Boolean>()
    val idIsBlank: LiveData<Boolean> get() = _idIsBlank
    private val _idIsError = MutableLiveData<Boolean>()
    val idIsError: LiveData<Boolean> get() = _idIsError

    private val _pwIsBlank = MutableLiveData<Boolean>()
    val pwIsBlank: LiveData<Boolean> get() = _pwIsBlank
    private val _pwIsError = MutableLiveData<Boolean>()
    val pwIsError: LiveData<Boolean> get() = _pwIsError

    private val _pwReIsBlank = MutableLiveData<Boolean>()
    val pwReIsBlank: LiveData<Boolean> get() = _pwReIsBlank
    private val _pwReIsError = MutableLiveData<Boolean>()
    val pwReIsError: LiveData<Boolean> get() = _pwReIsError

    private val _nicIsBlank = MutableLiveData<Boolean>()
    val nicIsBlank: LiveData<Boolean> get() = _nicIsBlank
    private val _nicIsError = MutableLiveData<Boolean>()
    val nicIsError: LiveData<Boolean> get() = _nicIsError

    init {
        _joinYN.value = false

        _idIsBlank.value = false
        _idIsError.value = false
        _pwIsBlank.value = false
        _pwIsError.value = false
        _pwReIsBlank.value = false
        _pwReIsError.value = false
        _nicIsBlank.value = false
        _nicIsError.value = false
    }

    private fun showToast(msg: String) {
        _toastMsg.value = msg
    }

    private fun showError(error: String) {
        when (error) {
            "nickName" -> {
                hideNicIsBlank()
                _nicIsError.value = true
            }
            "password" -> {
                hidePwIsBlank()
                _pwIsError.value = true
            }
        }
    }

    private fun hideErrors() {
        _idIsError.value = false
        _pwIsError.value = false
        _pwReIsError.value = false
        _nicIsError.value = false
    }

    //todo 유효성 검사 가입 완료 후
    fun joinMembership() {
        viewModelScope.launch {
            hideErrors()
            val loginId = id.value
            if (loginId.isNullOrEmpty()) {
                showIdIsBlank()
                return@launch
            } else {
                hideIdIsBlank()
            }

            val loginPw = pw.value
            if (loginPw.isNullOrEmpty()) {
                showPwIsBlank()
                return@launch
            } else {
                hidePwIsBlank()
            }

            val loginPwRe = pwRe.value
            if (loginPwRe.isNullOrEmpty()) {
                showPwReIsBlank()
                return@launch
            } else {
                hidePwReIsBlank()
            }

            if (loginPw != loginPwRe) {
                hidePwReIsBlank()
                _pwReIsError.value = true
                return@launch
            } else {
                _pwReIsError.value = false
            }

            val loginNic = nic.value
            if (loginNic.isNullOrEmpty()) {
                showNicIsBlank()
                return@launch
            } else {
                hideNicIsBlank()
            }

            simpleJoinUseCase(loginId, loginPw, loginNic).let {
                Log.e(TAG, "joinMembership : $it")

                if (it.status == 200) {
                    showToast("회원가입이 완료되었습니다!")
                    _joinYN.value = true
                } else {
                    if (it.status == 409) {
                        _idIsError.value = true
                    } else {
                        it.errors.split(",").let { errors ->
                            Log.e(TAG, errors.size.toString())
                            Log.e(TAG, errors.toString())

                            for (error in errors) {
                                val startIndex = error.indexOf("error field:") + 12
                                val endIndex = error.indexOf("message:")
                                if ((startIndex == -1) or (endIndex == -1)) {
                                    break
                                }
                                Log.e(TAG, "startIndex : $startIndex , endIndex : $endIndex")
                                val errorField = error.substring(startIndex, endIndex).trim()
                                showError(errorField)
                            }
                        }
                    }
                }
            }
        }
    }

    fun showIdIsBlank() {
        _idIsBlank.value = true
    }

    fun hideIdIsBlank() {
        _idIsBlank.value = false
    }

    fun showPwIsBlank() {
        _pwIsBlank.value = true
    }

    fun hidePwIsBlank() {
        _pwIsBlank.value = false
    }

    fun showPwReIsBlank() {
        _pwReIsBlank.value = true
    }

    fun hidePwReIsBlank() {
        _pwReIsBlank.value = false
    }

    fun showNicIsBlank() {
        _nicIsBlank.value = true
    }

    fun hideNicIsBlank() {
        _nicIsBlank.value = false
    }

    companion object {
        private const val TAG = "SimpleJoinViewModel"
    }

}