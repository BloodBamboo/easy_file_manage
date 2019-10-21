package cn.com.bamboo.esay_file_manage

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.view.View
import android.widget.EditText
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import cn.com.bamboo.esay_common.help.Permission4MultipleHelp
import cn.com.bamboo.esay_file_manage.extensions.formatDate
import cn.com.bamboo.esay_file_manage.extensions.formatSize
import cn.com.bamboo.esay_file_manage.model.ItemFile
import cn.com.bamboo.esay_file_manage.util.FileUtil
import cn.com.bamboo.esay_file_manage.view.OptionMenuLayout
import cn.com.edu.hnzikao.kotlin.base.BaseKotlinActivity
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kotlinx.android.synthetic.main.activity_file_manage.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import java.io.File
import java.util.*

class FileManageActivity : BaseKotlinActivity(), OptionMenuLayout.OptionMenuCallback {

    companion object {
        val GET_PATHS = "getPath"
    }

    private data class ParentPathInfo(var path: String, var parcelable: Parcelable)

    private var currentPath: String =
        Environment.getExternalStorageDirectory().absolutePath.trimEnd('/')
    private var pathHistory: Stack<ParentPathInfo> = Stack()
    private var showCheckbox = false
    private var checkAll = false

    private var getFilePaths = false

    private lateinit var adapter: FileAdapter

    override fun create() {
        this.alert {
            this.title = "新建文件夹"
            val nameEditText = EditText(this@FileManageActivity)
            nameEditText.hint = "文件夹名称"
//            nameEditText.textSize = this@FileManageActivity.dip(13).toFloat()
            nameEditText.setTextColor(this@FileManageActivity.resources.getColor(R.color.text_primary))
            this.customView = nameEditText
            this.positiveButton("确定") {
                val name = nameEditText.text.toString()
                if (name.isNotEmpty() &&
                    !name.contains(".") &&
                    FileUtil.createFileOrFolder(currentPath, name)
                ) {
                    openPath(currentPath)
                } else {
                    toast("创建失败，文件名不能为空或其他特殊字符")
                }

            }
            this.negativeButton("取消") {

            }
        }.show()
    }

    override fun paste() {
        toast("粘贴")
    }

    override fun more() {
        toast("更多")
    }

    override fun checkOk() {
        val list = adapter.checkData()
        if (list.size > 1000) {
            this.alert {
                message = "最多不能超过1000个文件和文件夹"
            }
        } else {
            val paths = list.map {
                FileProvider.getUriForFile(
                    this,
                    "${BuildConfig.APPLICATION_ID}.provider",
                    File(it)
                )
            } as ArrayList
            val clipData =
                ClipData("FilePath", arrayOf("text/plain"), ClipData.Item(paths.removeAt(0)))
            paths.forEach {
                clipData.addItem(ClipData.Item(it))
            }
            Intent().apply {
                this.clipData = clipData
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                setResult(Activity.RESULT_OK, this)
            }
            if (getFilePaths) {
                finish()
            } else {
                showCheckbox()
            }
        }
    }

    override fun checkCopy() {
        toast("复制")
    }

    override fun checkMove() {
        toast("移动")
    }

    override fun checkRemove() {
        if (FileUtil.remove(adapter.checkData())) {
            openPath(currentPath)
            toast("删除成功")
        } else {
            toast("删除失败")
        }
    }

    override fun checkMore() {
        toast("更多")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_manage)
        setTitleAndBackspace("文件管理") {
            backEvent()
        }
        //权限申请
        Permission4MultipleHelp.request(this, arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ), success = {
            getFilePaths = intent.getBooleanExtra(GET_PATHS, false)
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

        //先保存之前路径
        pathHistory.push(
            ParentPathInfo(
                currentPath,
                recycler_view.layoutManager!!.onSaveInstanceState()!!
            )
        )
        openPath(data!!.path)
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

        fun checkData(): List<String> {
            val list = ArrayList<String>()
            for (itemFile in data) {
                if (itemFile.isChecked) {
                    list.add(itemFile.path)
                }
            }
            return list
        }
    }
}
