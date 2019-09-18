package cn.com.bamboo.esay_file_manage

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cn.com.bamboo.esay_file_manage.extensions.formatDate
import cn.com.bamboo.esay_file_manage.extensions.formatSize
import cn.com.bamboo.esay_file_manage.model.ItemFile
import cn.com.bamboo.esay_file_manage.util.FileUtil
import cn.com.bamboo.esay_file_manage.util.TYPE_FILE
import cn.com.bamboo.esay_file_manage.util.TYPE_FOLDER
import cn.com.edu.hnzikao.kotlin.base.BaseKotlinActivity
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.MultipleItemRvAdapter
import com.chad.library.adapter.base.provider.BaseItemProvider
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.util.*

class MainActivity : BaseKotlinActivity() {

    var currentPath: String = Environment.getExternalStorageDirectory().absolutePath.trimEnd('/')
    var pathHistory: Stack<String> = Stack()
    val MY_PERMISSIONS_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitleAndBackspace("文件管理") {
            backEvent()
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                MY_PERMISSIONS_REQUEST
            )
        } else {
            recycler_view.adapter = FileAdapter(FileUtil.openDir(currentPath))
        }
    }

    override fun onResume() {
        super.onResume()
        text_current_file_name.text = currentPath
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    recycler_view.adapter = FileAdapter(FileUtil.openDir(currentPath))
                } else {
                    toast("请开启读写权限")
                }
                return
            }
            else -> {
                toast("请开启读写权限")
            }
        }
    }

    override fun onBackPressedSupport() {
        backEvent()
    }

    /**
     * 返回事件处理
     */
    private fun backEvent() {
        if (pathHistory.empty()) {
            finish()
        } else {
            openPath(pathHistory.pop())
        }
    }

    /**
     * 打开文件夹
     */
    fun openPath(path: String) {
        //更新当前路径
        currentPath = path
        text_current_file_name.text = currentPath
        (recycler_view.adapter as FileAdapter).replaceData(FileUtil.openDir(currentPath))
    }

    inner class FileAdapter(data: List<ItemFile>) :
        MultipleItemRvAdapter<ItemFile, BaseViewHolder>(data) {

        init {
            finishInitialize()
        }

        override fun registerItemProvider() {
            mProviderDelegate.registerProvider(FolderItemProvider())
            mProviderDelegate.registerProvider(FileItemProvider())
        }

        override fun getViewType(t: ItemFile?): Int {
            return if (t!!.isDirectory) {
                TYPE_FOLDER
            } else {
                TYPE_FILE
            }
        }
    }

    inner class FolderItemProvider : BaseItemProvider<ItemFile, BaseViewHolder>() {
        override fun layout(): Int {
            return R.layout.item_folder
        }

        override fun viewType(): Int {
            return TYPE_FOLDER
        }

        override fun convert(helper: BaseViewHolder?, data: ItemFile?, position: Int) {
            if (helper == null || data == null) {
                return
            }
            helper!!.setText(R.id.text_name, data!!.name)
                .setText(R.id.text_child_count, "${data!!.children}-项")
                .setText(R.id.text_date, data!!.modified.formatDate())
        }

        override fun onClick(helper: BaseViewHolder?, data: ItemFile?, position: Int) {
            if (data!!.children > 0) {
                //先保存之前路径
                pathHistory.push(currentPath)
                openPath(data!!.path)
            }
        }

        override fun onLongClick(helper: BaseViewHolder?, data: ItemFile?, position: Int): Boolean {
            return super.onLongClick(helper, data, position)
        }
    }

    inner class FileItemProvider : BaseItemProvider<ItemFile, BaseViewHolder>() {
        override fun layout(): Int {
            return R.layout.item_file
        }

        override fun viewType(): Int {
            return TYPE_FILE
        }

        override fun convert(helper: BaseViewHolder?, data: ItemFile?, position: Int) {
            if (helper == null || data == null) {
                return
            }
            helper!!.setText(R.id.text_name, data!!.name)
                .setText(R.id.text_date, data!!.modified.formatDate())
                .setText(R.id.text_details, data!!.size.formatSize())
        }

        override fun onClick(helper: BaseViewHolder?, data: ItemFile?, position: Int) {
            super.onClick(helper, data, position)
        }

        override fun onLongClick(helper: BaseViewHolder?, data: ItemFile?, position: Int): Boolean {
            return super.onLongClick(helper, data, position)
        }
    }
}
