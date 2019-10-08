package cn.com.bamboo.esay_file_manage.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.com.bamboo.esay_file_manage.R

class OptionMenuLayout : LinearLayout {
    interface OptionMenuCallback {
        fun create()
        fun paste()
        fun more()
        fun checkOk()
        fun checkCopy()
        fun checkMove()
        fun checkRemove()
        fun checkMore()
    }


    var callback: OptionMenuCallback? = null

    var showCheckOption = false
        set(value) {
            field = value
            updateLayout(value)
        }

    private val menuName: Array<Pair<String, Int>> = arrayOf(
        Pair("新建", R.mipmap.ic_add),
        Pair("粘贴", R.mipmap.ic_paste),
        Pair("更多", R.mipmap.ic_more)
    )
    private val checkMenuName: Array<Pair<String, Int>> = arrayOf(
        Pair("确认", R.mipmap.ic_ok),
        Pair("复制", R.mipmap.ic_copy),
        Pair("移动", R.mipmap.ic_move),
        Pair("删除", R.mipmap.ic_cancel),
        Pair("更多", R.mipmap.ic_more)
    )


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    init {
        updateLayout(showCheckOption)
    }

    private fun updateLayout(showCheckOption: Boolean) {
        removeAllViews()
        if (showCheckOption) {
            buildOption(checkMenuName)
        } else {
            buildOption(menuName)
        }
    }

    private fun buildOption(menuList: Array<Pair<String, Int>>) {
        for (temp in menuList) {
            var view = LayoutInflater.from(context)
                .inflate(R.layout.layout_option_menu, this, false)
            val image = view.findViewById<ImageView>(R.id.image_menu)
            image.setColorFilter(resources.getColor(R.color.colorPrimary))
            image.setImageResource(temp.second)
            view.findViewById<TextView>(R.id.text_menu).text = temp.first
            view.setOnClickListener {
                menuClick(temp.first)
            }
            addView(view)
        }
    }

    private fun menuClick(name: String) {
        if (callback == null) return

        when (name) {
            menuName[0].first -> callback!!.create()
            menuName[1].first -> callback!!.paste()
            menuName[2].first -> callback!!.more()
            checkMenuName[0].first -> callback!!.checkOk()
            checkMenuName[1].first -> callback!!.checkCopy()
            checkMenuName[2].first -> callback!!.checkMove()
            checkMenuName[3].first -> callback!!.checkRemove()
            checkMenuName[4].first -> callback!!.checkMore()
        }
    }
}

