package cn.com.bamboo.esay_file_manage.extensions

import java.io.File

fun File.getDirectChildrenCount(countHiddenItems: Boolean) = listFiles()?.filter { if (countHiddenItems) true else !it.isHidden }?.size ?: 0