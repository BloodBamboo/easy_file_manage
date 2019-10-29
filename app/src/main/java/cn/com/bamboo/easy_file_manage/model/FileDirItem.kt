package cn.com.bamboo.easy_file_manage.model

import cn.com.bamboo.easy_file_manage.extensions.getParentPath
import cn.com.bamboo.easy_file_manage.util.SORT_BY_DATE_MODIFIED
import cn.com.bamboo.easy_file_manage.util.SORT_BY_NAME
import cn.com.bamboo.easy_file_manage.util.SORT_BY_SIZE
import cn.com.bamboo.easy_file_manage.util.SORT_DESCENDING

open class FileDirItem(val path: String, val name: String = "", var isDirectory: Boolean = false, var children: Int = 0, var size: Long = 0L, var modified: Long = 0L) :
    Comparable<FileDirItem> {
    companion object {
        var sorting = 1
    }

    override fun toString() = "FileDirItem(path=$path, name=$name, isDirectory=$isDirectory, children=$children, size=$size, modified=$modified)"

    override fun compareTo(other: FileDirItem): Int {
        return if (isDirectory && !other.isDirectory) {
            -1
        } else if (!isDirectory && other.isDirectory) {
            1
        } else {
            var result: Int
            when (sorting){
                SORT_BY_NAME -> result = name.toLowerCase().compareTo(other.name.toLowerCase())
                SORT_BY_SIZE -> result = when {
                    size == other.size -> 0
                    size > other.size -> 1
                    else -> -1
                }
                SORT_BY_DATE_MODIFIED -> {
                    result = when {
                        modified == other.modified -> 0
                        modified > other.modified -> 1
                        else -> -1
                    }
                }
                else -> {
                    result = getExtension().toLowerCase().compareTo(other.getExtension().toLowerCase())
                }
            }

            if (sorting == SORT_DESCENDING) {
                result *= -1
            }
            result
        }
    }

    fun getExtension() = if (isDirectory) name else path.substringAfterLast('.', "")


//    fun getProperSize(countHidden: Boolean) = File(path).getProperSize(countHidden)
//
//    fun getProperFileCount(countHidden: Boolean) = File(path).getFileCount(countHidden)
//
//    fun getDirectChildrenCount(countHiddenItems: Boolean) = File(path).getDirectChildrenCount(countHiddenItems)
//
    fun getParentPath() = path.getParentPath()
//
//    fun getDuration() = path.getDuration()
//
//    fun getFileDurationSeconds() = path.getFileDurationSeconds()
//
//    fun getArtist() = path.getFileArtist()
//
//    fun getAlbum() = path.getFileAlbum()
//
//    fun getSongTitle() = path.getFileSongTitle()
//
//    fun getResolution(context: Context) = context.getResolution(path)
//
//    fun getVideoResolution(context: Context) = context.getVideoResolution(path)
//
//    fun getImageResolution() = path.getImageResolution()
}