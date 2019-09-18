package cn.com.bamboo.esay_file_manage.extensions

/**
 * String扩展
 */

fun String.getFilenameFromPath() = substring(lastIndexOf("/") + 1)

fun String.getFilenameExtension() = substring(lastIndexOf(".") + 1)

fun String.getParentPath() = removeSuffix("/${getFilenameFromPath()}")


