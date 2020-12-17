package com.yapp.picon.presentation.postedit

import android.view.View
import androidx.annotation.LayoutRes
import com.yapp.picon.R
import com.yapp.picon.databinding.PostEditEmotionItemBinding
import com.yapp.picon.presentation.base.BaseRecyclerView
import com.yapp.picon.presentation.model.PostEditEmotion
import kotlinx.android.synthetic.main.post_edit_emotion_item.view.*


class EditPostEmotionAdapter(
    @LayoutRes private val layoutRes: Int,
    bindingVariabledId: Int
): BaseRecyclerView.BaseAdapter<PostEditEmotion, PostEditEmotionItemBinding>(
    layoutRes, bindingVariabledId
) {
    var selectedIndex = 0

    override fun onBindViewHolder(
        baseViewHolder: BaseRecyclerView.BaseViewHolder<PostEditEmotionItemBinding>,
        position: Int
    ) {
        super.onBindViewHolder(baseViewHolder, position)
        if (selectedIndex == position) {
            baseViewHolder.itemView.post_edit_emotion_circle.visibility = View.INVISIBLE
            baseViewHolder.itemView.post_edit_emotion_double_circle.visibility = View.VISIBLE
            baseViewHolder.itemView.post_edit_emotion_double_circle.setImageResource(
                getDoubleCircleResource(items[position].colorName)
            )
        } else {
            baseViewHolder.itemView.post_edit_emotion_double_circle.visibility = View.INVISIBLE
            baseViewHolder.itemView.post_edit_emotion_circle.visibility = View.VISIBLE
            baseViewHolder.itemView.post_edit_emotion_circle.setImageResource(
                getCircleResource(items[position].colorName)
            )
        }

        baseViewHolder.itemView.setOnClickListener {
            selectedIndex = position
            notifyDataSetChanged()
        }
    }

    private fun getCircleResource(color: String): Int {
        return when (color) {
            "SOFT_BLUE" -> R.drawable.ic_custom_circle_soft_blue
            "CORN_FLOWER" -> R.drawable.ic_custom_circle_cornflower
            "BLUE_GRAY" -> R.drawable.ic_custom_circle_bluegrey
            "VERY_LIGHT_BROWN" -> R.drawable.ic_custom_circle_very_light_brown
            "WARM_GRAY" -> R.drawable.ic_custom_circle_warm_grey
            else -> throw Exception("CustomEmotionAdapter - getResource - color type is wrong.")
        }
    }

    private fun getDoubleCircleResource(color: String): Int {
        return when (color) {
            "SOFT_BLUE" -> R.drawable.ic_two_circle_soft_blue
            "CORN_FLOWER" -> R.drawable.ic_two_circle_cornflower
            "BLUE_GRAY" -> R.drawable.ic_two_circle_bluegrey
            "VERY_LIGHT_BROWN" -> R.drawable.ic_two_circle_very_light_brown
            "WARM_GRAY" -> R.drawable.ic_two_circle_warm_grey
            else -> throw Exception("CustomEmotionAdapter - getResource - color type is wrong.")
        }
    }
}