package cn.com.bamboo.esay_file_manage.util

import cn.com.bamboo.esay_file_manage.extensions.getDirectChildrenCount
import cn.com.bamboo.esay_file_manage.model.ItemFile
import java.io.File

class FileUtil {
    companion object {
        var showHidden = false

        /**
         *按指定路径打开文件夹
         */
        fun openDir(realPath: String): List<ItemFile> {
            val items = ArrayList<ItemFile>()
            val files = File(realPath).listFiles()?.filterNotNull()
            if (files != null) {
                for (file in files) {
                    val fileDirItem = getFileDirItemFromFile(file)
                    if (fileDirItem != null) {
                        items.add(fileDirItem)
                    }
                }
            }
            items.sort()
            items.add(ItemFile(""))
            return items
        }

        private fun getFileDirItemFromFile(file: File): ItemFile? {
            val curPath = file.absolutePath
            val curName = file.name
            if (!showHidden && curName.startsWith(".")) {
                return null
            }

            val isDirectory = file.isDirectory
            val children = if (isDirectory) file.getDirectChildrenCount(showHidden) else 0
            val size = if (isDirectory) {
//                if (isSortingBySize) {
//                    file.getProperSize(showHidden)
//                } else {
                    0L
//                }
            } else {
                file.length()
            }

            return ItemFile(curPath, curName, isDirectory, children, size, file.lastModified())
        }
    }
}