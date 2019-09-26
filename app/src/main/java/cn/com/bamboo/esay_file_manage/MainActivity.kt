package cn.com.bamboo.esay_file_manage

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.view.View
import cn.com.bamboo.esay_common.help.Permission4MultipleHelp
import cn.com.bamboo.esay_file_manage.extensions.formatDate
import cn.com.bamboo.esay_file_manage.extensions.formatSize
import cn.com.bamboo.esay_file_manage.model.ItemFile
import cn.com.bamboo.esay_file_manage.util.FileUtil
import cn.com.bamboo.esay_file_manage.view.OptionMenulayout
import cn.com.edu.hnzikao.kotlin.base.BaseKotlinActivity
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.util.*

class MainActivity : BaseKotlinActivity(), OptionMenulayout.OptionMenuCallback {
    private data class ParentPathInfo(var path: String, var parcelable: Parcelable)

    private var currentPath: String =
        Environment.getExternalStorageDirectory().absolutePath.trimEnd('/')
    private var pathHistory: Stack<ParentPathInfo> = Stack()
    private var showCheckbox = false
    private var checkAll = false

    private lateinit var adapter: FileAdapter

    override fun create() {
        toast("新建文件夹")
    }

    override fun paste() {
        toast("粘贴")
    }

    override fun more() {
        toast("更多")
    }

    override fun checkOk() {
        toast("确认")
    }

    override fun checkCancel() {
        showCheckbox()
    }

    override fun checkCopy() {
        toast("复制")
    }

    override fun checkMove() {
        toast("移动")
    }

    override fun checkMore() {
        toast("更多")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitleAndBackspace("文件管理") {
            backEvent()
        }
        //权限申请
        Permission4MultipleHelp.request(this, arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ), success = {
            adapter = FileAdapter(FileUtil.openDir(currentPath))
            recycler_view.adapter = adapter
        }, fail = { toast("请开启读写权限") })

        toolbar!!.inflateMenu(R.menu.checkbox)
        option_menu.callback = this
    }

    override fun onResume() {
        super.onResume()
        text_current_file_name.text = currentPath
    }

    override fun onBackPressedSupport() {
        backEvent()
    }

    /**
     * 返回事件处理
     */
    private fun backEvent() {
        if (showCheckbox) {
            showCheckbox()
            return
        }

        if (pathHistory.empty()) {
            finish()
        } else {
            var parentPathInfo = pathHistory.pop()
            openPath(parentPathInfo.path)
            recycler_view.layoutManager!!.onRestoreInstanceState(parentPathInfo.parcelable)
        }
    }

    /**
     * 打开文件夹
     */
    private fun openPath(path: String) {
        //更新当前路径
        currentPath = path
        text_current_file_name.text = currentPath
        (recycler_view.adapter as FileAdapter).replaceData(FileUtil.openDir(currentPath))
    }

    private fun showCheckbox() {
        if (!showCheckbox) {
            option_menu.showCheckOption = true
            showCheckbox = true
            adapter.notifyDataSetChanged()
            toolbar!!.menu.getItem(0).isVisible = true
            toolbar!!.menu.getItem(0).setOnMenuItemClickListener {
                checkAll = !checkAll
                if (checkAll) {
                    for (item in adapter.data) {
                        item.isChecked = true
                    }
                } else {
                    for (item in adapter.data) {
                        item.isChecked = false
                    }
                }
                adapter.notifyDataSetChanged()
                return@setOnMenuItemClickListener true
            }
        } else {
            showCheckbox = false
            option_menu.showCheckOption = false
            toolbar!!.menu.getItem(0).isVisible = false
            adapter.notifyDataSetChanged()
        }
    }

    private fun convertFolder(helper: BaseViewHolder?, data: ItemFile?) {
            if (helper == null || data == null) {
                return
            }

        helper!!.setText(R.id.text_name, data!!.name)
            .setImageResource(R.id.image_icon, R.mipmap.ic_folder)
            .setText(R.id.text_details, "${data!!.children}-项")
                .setText(R.id.text_date, data!!.modified.formatDate())
    }

    private fun convertFile(helper: BaseViewHolder?, data: ItemFile?) {
        if (helper == null || data == null) {
            return
        }
        helper!!.setText(R.id.text_name, data!!.name)
            .setImageResource(R.id.image_icon, R.mipmap.ic_file)
            .setText(R.id.text_date, data!!.modified.formatDate())
            .setText(R.id.text_details, data!!.size.formatSize())
    }

    private fun onClickFolder(data: ItemFile?) {
        if (showCheckbox) {
            return
        }

        if (data!!.children > 0) {
            //先保存之前路径
            pathHistory.push(
                ParentPathInfo(
                    currentPath,
                    recycler_view.layoutManager!!.onSaveInstanceState()!!
                )
            )
            openPath(data!!.path)
        }
    }


    inner class FileAdapter(data: List<ItemFile>) :
        BaseQuickAdapter<ItemFile, BaseViewHolder>(R.layout.item_folder, data) {

        init {
            setOnItemClickListener { adapter, view, position ->
                val item = adapter.data[position] as ItemFile
                if (item.isDirectory) {
                    onClickFolder(item)
                }
            }

            setOnItemLongClickListener { adapter, view, position ->
                showCheckbox()
                return@setOnItemLongClickListener true
            }
        }

        override fun convert(helper: BaseViewHolder, item: ItemFile?) {
            if (item!!.path.isEmpty()) {
                helper.itemView.visibility = View.INVISIBLE
            } else {
                helper.itemView.visibility = View.VISIBLE
                if (item!!.isDirectory) {
                    convertFolder(helper, item)
                } else {
                    convertFile(helper, item)
                }

                if (showCheckbox) {
                    helper.setVisible(R.id.checkbox, true)
                    helper.setChecked(R.id.checkbox, item.isChecked)
                    helper.setOnCheckedChangeListener(R.id.checkbox) { buttonView, isChecked ->
                        adapter.data[helper.adapterPosition].isChecked = isChecked
                    }
                } else {
                    helper.setVisible(R.id.checkbox, false)
                }
            }
        }
    }
}
