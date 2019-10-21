package cn.com.bamboo.easy_file_manage.model

data class ItemFile(val mPath: String, val mName: String = "", var mIsDirectory: Boolean = false, var mChildren: Int = 0, var mSize: Long = 0L, var mModified: Long = 0L,
                    var isChecked: Boolean = false)
    : FileDirItem(mPath, mName, mIsDirectory, mChildren, mSize, mModified)