package com.yt.apps.Widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.yt.apps.R
import com.yt.apps.Utils.PermissionUtils
import com.yt.apps.Utils.SystemProperties
import com.yt.apps.model.CustomCallback
import java.util.*
import kotlin.collections.ArrayList

class DetailAdapter(var activity: Activity, var context: Context) : BaseAdapter(), CustomCallback {
    val VIEW_TYPE_HEAD = 0
    val VIEW_TYPE_DETAIL = 1
    val VIEW_TYPE_ACC = 2
    val VIEW_TYPE_COUNT = 3

    var position = 0
    var viewType = VIEW_TYPE_HEAD
    var contentView: MutableList<Int> = ArrayList()
    var inflater: LayoutInflater? = LayoutInflater.from(context)

    fun setContent() {
        contentView.add(VIEW_TYPE_HEAD)
        contentView.add(VIEW_TYPE_DETAIL)
        contentView.add(VIEW_TYPE_ACC)
    }

    override fun getCount(): Int {
        return contentView.size
    }

    override fun getItem(position: Int): Int? {
        return if (contentView.isEmpty() || position < 0 || position > contentView.size) null else contentView[position]
    }

    override fun getItemId(position: Int): Long {
        return if (contentView.isEmpty() || position < 0 || position > contentView.size) -1 else contentView[position].toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View
        val type = getItemViewType(position)
        if (convertView == null) {
            when (type) {
                VIEW_TYPE_HEAD -> {
                    view = inflater!!.inflate(R.layout.header_layout, parent, false)
                    bindViewHolder(view, type)
                }
                VIEW_TYPE_DETAIL -> {
                    view = inflater!!.inflate(R.layout.detail_item_layout, parent, false)
                    bindViewHolder(view, type)

                }
                VIEW_TYPE_ACC -> {
                    view = inflater!!.inflate(R.layout.accelerate_layout, parent, false)
                    bindViewHolder(view, type)
                }
                else -> {
                    throw IllegalStateException("Unexpected value: $type")
                }
            }
        } else {
            view = convertView
            bindViewHolder(view, type)
        }
        return view
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return VIEW_TYPE_HEAD
        } else if (position % 2 == 0)
            return VIEW_TYPE_ACC
        else
            return VIEW_TYPE_DETAIL
    }

    override fun getViewTypeCount(): Int {
        return VIEW_TYPE_COUNT
    }

    fun bindViewHolder(view: View, type: Int) {
        when (type) {
            VIEW_TYPE_HEAD -> initHolderHeader(view)
            VIEW_TYPE_DETAIL -> initHolderDetail(view)
            VIEW_TYPE_ACC -> initHolderAcc(view)
        }
    }

    @SuppressLint("SetTextI18n", "ResourceAsColor")
    private fun initHolderAcc(view: View) {
        val holderAcc = ViewHolderAcc()
        holderAcc.accMem = view.findViewById(R.id.acc_mem) as TextView
        holderAcc.accPls = view.findViewById(R.id.acc_pls) as TextView
        holderAcc.accBtn = view.findViewById(R.id.btn_acc) as Button

        val mem = SystemProperties.getUsedMemory(activity)
        val isLow = SystemProperties.isLowMemory(activity)
        holderAcc.accMem!!.setText(mem.toString() + "MB")
        if (isLow) {
            holderAcc.accMem!!.setTextColor(R.color.colorAccent)
            holderAcc.accPls!!.visibility = VISIBLE
        } else {
            holderAcc.accMem!!.setTextColor(R.color.colorGreen)
            holderAcc.accPls!!.visibility = INVISIBLE
        }
        holderAcc.accBtn!!.setOnClickListener(recycleListener)

        view.tag = holderAcc
    }

    private fun initHolderDetail(view: View) {
        val holderDetail = ViewHolderDetail()
        holderDetail.recMem0 = view.findViewById(R.id.recy_mem0) as TextView
        holderDetail.recMem1 = view.findViewById(R.id.recy_mem1) as TextView
        holderDetail.recMem2 = view.findViewById(R.id.recy_mem2) as TextView
        holderDetail.recMem3 = view.findViewById(R.id.recy_mem3) as TextView
        holderDetail.recycleBtn = view.findViewById(R.id.btn_recycle) as Button

        val total = SystemProperties.getTotalCacheSize(context)
        val file = SystemProperties.getInnerCacheSize(context)
        val ext = SystemProperties.getExternalCacheSize(context)
        holderDetail.recMem0!!.setText(total)
        holderDetail.recMem1!!.setText(total)
        holderDetail.recMem2!!.setText(file)
        holderDetail.recMem3!!.setText(ext)
        holderDetail.recycleBtn!!.setOnClickListener(accListener)
        view.tag = holderDetail
    }

    @SuppressLint("NewApi", "SetTextI18n")
    private fun initHolderHeader(view: View) {
        val holderHeader = ViewHolderHeader()
        holderHeader.Usage = view.findViewById(R.id.mem_used0) as TextView
        holderHeader.memPpogress = view.findViewById(R.id.mem_progress) as ProgressBar
        val used = SystemProperties.getUsedMemory(activity)
        val total = SystemProperties.getTotalMemory(activity)
        val usagePercent = (used.toDouble() / total.toDouble() * 100).toInt()
        holderHeader.Usage!!.setText(usagePercent.toString() + "%")
        holderHeader.memPpogress!!.setProgress(usagePercent.toInt(), true)
        view.tag = holderHeader
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    val recycleListener: View.OnClickListener = View.OnClickListener {
        SystemProperties.clean(activity, customCallback = notice(null))
           if(PermissionUtils.needPermissionForBlocking(context)){
               PermissionUtils.startActivitySafely(
                   Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                   activity.applicationContext
               )
        } else {
            Log.println(Log.DEBUG, "detail", SystemProperties.printForegroundTask(activity))
        }
    }

    val accListener: View.OnClickListener = View.OnClickListener {
        SystemProperties.clearAllCache(context)
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
    }

    fun refreshAdapter(contentView: MutableList<Int>) {
        this.contentView = contentView
        notifyDataSetChanged()
    }

    internal class ViewHolderHeader {
        var Usage: TextView? = null
        var memPpogress: ProgressBar? = null
    }

    internal class ViewHolderDetail {
        var recMem0: TextView? = null
        var recMem1: TextView? = null
        var recMem2: TextView? = null
        var recMem3: TextView? = null
        var recycleBtn: Button? = null
    }

    internal class ViewHolderAcc {
        var accMem: TextView? = null
        var accPls: TextView? = null
        var accBtn: Button? = null
    }

    override fun notice(datamap: Map<String, Objects>?) {
        val contentView: MutableList<Int> = ArrayList()
        contentView.add(0)
        contentView.add(1)
        contentView.add(2)
        refreshAdapter(contentView)
    }
}