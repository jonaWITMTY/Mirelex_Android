package com.example.jonathangalvan.mirelex.UI

import android.content.Context
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.View


class ViewPagerCustom(context: Context, attrs: AttributeSet?): androidx.viewpager.widget.ViewPager(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val child = getChildAt(currentItem)
        var newHeight = heightMeasureSpec
        if (child != null) {
            child.measure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
            val h = child.measuredHeight
            newHeight = View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, newHeight)
    }

    fun reMeasureCurrentPage() {
        requestLayout()
    }

}