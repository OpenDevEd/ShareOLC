package com.android.sharepluscode.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.sharepluscode.R
import com.android.sharepluscode.ui.MainActivity
import com.android.sharepluscode.utils.PrefUtil
import java.util.*


class MenuAdapter(private var mContext: Activity) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    private val stringMenus: List<String> = listOf("English", "French", "Spanish", "Portuguese", "Swahili", "Hindi")
    private val stringCode: List<String> = listOf("en", "fr", "es", "pt", "sw", "hi")

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var menuName: TextView = view.findViewById(R.id.txtMenuNameRow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_item_menu, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.menuName.text = stringMenus[position]
        holder.itemView.setOnClickListener {
            if (mContext is MainActivity) {
                PrefUtil.putStringPref(PrefUtil.PRF_LANGUAGE, stringCode[position], mContext)
                val mainActivity = mContext as MainActivity
                mainActivity.updateLocale(Locale(stringCode[position]))
                mainActivity.hideMenu()
            }
        }
    }

    override fun getItemCount(): Int {
        return stringMenus.size
    }
}