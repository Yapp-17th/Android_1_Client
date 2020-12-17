package com.yapp.picon.presentation.postedit

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import androidx.annotation.LayoutRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.yapp.picon.R
import com.yapp.picon.databinding.PostEditImageItemBinding
import com.yapp.picon.presentation.base.BaseRecyclerView
import kotlinx.android.synthetic.main.post_edit_image_item.view.*


class EditPostImageAdapter(
    private val context: Context,
    @LayoutRes private val layoutRes: Int,
    bindingVariabledId: Int
): BaseRecyclerView.BaseAdapter<String, PostEditImageItemBinding>(
    layoutRes, bindingVariabledId
) {
    override fun onBindViewHolder(
        baseViewHolder: BaseRecyclerView.BaseViewHolder<PostEditImageItemBinding>,
        position: Int
    ) {
        super.onBindViewHolder(baseViewHolder, position)
        if (position == 0) {
            baseViewHolder.itemView.post_edit_image_item_bottom.setImageResource(R.drawable.bg_cornflower_radius_5dp)
            baseViewHolder.itemView.post_edit_image_item_top.visibility = View.VISIBLE
            setImage(baseViewHolder.itemView.post_edit_image_item_top, position)
        } else {
            baseViewHolder.itemView.post_edit_image_item_top.visibility = View.INVISIBLE
            setImage(baseViewHolder.itemView.post_edit_image_item_bottom, position)
        }

        // todo - baseViewHolder.itemView.post_edit_image_item_top에 클릭 이벤트 추가하기
    }

    private fun setImage(view: ImageView, position: Int) {
        var options: RequestOptions = RequestOptions()
        options = options.transform(CenterCrop(), RoundedCorners(dpToPx(context, 5f).toInt()))

        Glide.with(context)
            .load(items[position])
            .apply(options)
            .into(view)
    }

    private fun dpToPx(context: Context, dp: Float): Float {
        val displayMetrics = context.resources.displayMetrics

        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics)
    }
}