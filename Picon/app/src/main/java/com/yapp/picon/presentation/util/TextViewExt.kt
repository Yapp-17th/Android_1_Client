package com.yapp.picon.presentation.util

import android.graphics.Typeface
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.yapp.picon.R

@BindingAdapter("isClicked", "textViewCode")
fun TextView.isClicked(isClicked: Boolean, textViewCode: Int) {
    when (textViewCode) {
        TextViewCode.COLLECT_MBS_EMOTION.code -> {
            if (isClicked) {
                setTypeface(null, Typeface.BOLD)
                setTextColor(ResourcesCompat.getColor(resources, R.color.pale_grey, null))
            } else {
                setTypeface(null, Typeface.NORMAL)
                setTextColor(ResourcesCompat.getColor(resources, R.color.brown_grey, null))
            }
        }
        TextViewCode.COLLECT_MBS_LOCATION.code -> {
            if (isClicked) {
                setTypeface(null, Typeface.BOLD)
                setTextColor(ResourcesCompat.getColor(resources, R.color.pale_grey, null))
            } else {
                setTypeface(null, Typeface.NORMAL)
                setTextColor(ResourcesCompat.getColor(resources, R.color.brown_grey_two, null))
            }
        }
    }
}