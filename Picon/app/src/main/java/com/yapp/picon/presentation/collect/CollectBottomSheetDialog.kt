package com.yapp.picon.presentation.collect

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yapp.picon.BR
import com.yapp.picon.R
import com.yapp.picon.databinding.CollectBottomSheetBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CollectBottomSheetDialog : BottomSheetDialogFragment() {

    private lateinit var binding: CollectBottomSheetBinding
    private val vm: CollectViewModel by sharedViewModel()

    private fun initViewModel() {
        binding.setVariable(BR.collectVM, vm)

        val emotion1ClickedYNObserver = Observer<Boolean> {
            if (it) {
                selectedEmotion(binding.collectMbsTvEmotion1, binding.collectMbsIvEmotion1)
            } else {
                unselectedEmotion(binding.collectMbsTvEmotion1, binding.collectMbsIvEmotion1)
            }
        }
        vm.emotion1ClickedYN.observe(this, emotion1ClickedYNObserver)

        val emotion2ClickedYNObserver = Observer<Boolean> {
            if (it) {
                selectedEmotion(binding.collectMbsTvEmotion2, binding.collectMbsIvEmotion2)
            } else {
                unselectedEmotion(binding.collectMbsTvEmotion2, binding.collectMbsIvEmotion2)
            }
        }
        vm.emotion2ClickedYN.observe(this, emotion2ClickedYNObserver)

        val emotion3ClickedYNObserver = Observer<Boolean> {
            if (it) {
                selectedEmotion(binding.collectMbsTvEmotion3, binding.collectMbsIvEmotion3)
            } else {
                unselectedEmotion(binding.collectMbsTvEmotion3, binding.collectMbsIvEmotion3)
            }
        }
        vm.emotion3ClickedYN.observe(this, emotion3ClickedYNObserver)

        val emotion4ClickedYNObserver = Observer<Boolean> {
            if (it) {
                selectedEmotion(binding.collectMbsTvEmotion4, binding.collectMbsIvEmotion4)
            } else {
                unselectedEmotion(binding.collectMbsTvEmotion4, binding.collectMbsIvEmotion4)
            }
        }
        vm.emotion4ClickedYN.observe(this, emotion4ClickedYNObserver)

        val emotion5ClickedYNObserver = Observer<Boolean> {
            if (it) {
                selectedEmotion(binding.collectMbsTvEmotion5, binding.collectMbsIvEmotion5)
            } else {
                unselectedEmotion(binding.collectMbsTvEmotion5, binding.collectMbsIvEmotion5)
            }
        }
        vm.emotion5ClickedYN.observe(this, emotion5ClickedYNObserver)
    }

    private fun selectedEmotion(textView: TextView, imageView: ImageView) {
        textView.apply {
            setTypeface(null, Typeface.BOLD)
            setTextColor(ResourcesCompat.getColor(resources, R.color.pale_grey, null))
        }
        imageView.visibility = View.VISIBLE
    }

    private fun unselectedEmotion(textView: TextView, imageView: ImageView) {
        textView.apply {
            setTypeface(null, Typeface.NORMAL)
            setTextColor(ResourcesCompat.getColor(resources, R.color.brown_grey, null))
        }
        imageView.visibility = View.GONE
    }

    private fun setListeners() {
        binding.collectMbsLiEmotion1.setOnClickListener { vm.toggleEmotion1ClickedYN() }
        binding.collectMbsLiEmotion2.setOnClickListener { vm.toggleEmotion2ClickedYN() }
        binding.collectMbsLiEmotion3.setOnClickListener { vm.toggleEmotion3ClickedYN() }
        binding.collectMbsLiEmotion4.setOnClickListener { vm.toggleEmotion4ClickedYN() }
        binding.collectMbsLiEmotion5.setOnClickListener { vm.toggleEmotion5ClickedYN() }

        binding.collectMbsTvApply.setOnClickListener {
            vm.applyFilter()
            vm.toggleShowFilterYN()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.collect_bottom_sheet, container, false)

        initViewModel()
        setListeners()

        return binding.root
    }

}