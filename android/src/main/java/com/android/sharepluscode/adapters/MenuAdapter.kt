package com.android.sharepluscode.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.sharepluscode.R
import com.android.sharepluscode.localeHelper.LocaleHelper
import com.android.sharepluscode.model.LanguegeModel
import com.android.sharepluscode.ui.MainActivity
import com.android.sharepluscode.utils.PrefUtil
import java.util.*


class MenuAdapter(private var mContext: Activity) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    private val dataList = createLanguageData()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var menuName: TextView = view.findViewById(R.id.txtMenuNameRow)
        var imgCheck: ImageView = view.findViewById(R.id.imgCheckRow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_item_menu, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val languageModel = dataList[position]
        holder.menuName.text = languageModel.languageName
        val getLastCode = PrefUtil.getStringPref(PrefUtil.PRF_LANGUAGE, mContext)
        if (getLastCode == languageModel.languageCode) {
            holder.imgCheck.visibility = View.VISIBLE
        } else {
            holder.imgCheck.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (mContext is MainActivity) {
                PrefUtil.putStringPref(PrefUtil.PRF_LANGUAGE, languageModel.languageCode, mContext)
                //LocaleHelper.setLocale(mContext, Locale(languageModel.languageCode))
                val mainActivity = mContext as MainActivity
                mainActivity.updateLocale(Locale(languageModel.languageCode))
                notifyDataSetChanged()
                mainActivity.hideMenu()
                mainActivity.recreate()
            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    private fun createLanguageData(): MutableList<LanguegeModel> {
        val dataList: MutableList<LanguegeModel> = mutableListOf()
        dataList.add(LanguegeModel("English", "en"))
        dataList.add(LanguegeModel("French", "fr"))
        dataList.add(LanguegeModel("Spanish", "es"))
        dataList.add(LanguegeModel("Portuguese", "pt"))
        dataList.add(LanguegeModel("Swahili", "sw"))
        dataList.add(LanguegeModel("Hindi", "hi"))
        dataList.add(LanguegeModel("Arabic", "ar"))
        dataList.add(LanguegeModel("Amharic", "am"))
        dataList.add(LanguegeModel("Bengali", "bn"))
        dataList.add(LanguegeModel("Hausa", "ha"))
        dataList.add(LanguegeModel("Igbo", "ig"))
        dataList.add(LanguegeModel("Shona", "sn"))
        dataList.add(LanguegeModel("Telugu", "te"))
        dataList.add(LanguegeModel("Urdu", "ur"))
        dataList.add(LanguegeModel("Xhosa", "xh"))
        dataList.add(LanguegeModel("Zulu", "zu"))

        dataList.add(LanguegeModel("Kinyarwanda", "rw"))
        dataList.add(LanguegeModel("Bemba", "bem"))

        Collections.sort(dataList, object : Comparator<LanguegeModel?> {
            override fun compare(s1: LanguegeModel?, s2: LanguegeModel?): Int {
                return s1!!.languageName.compareTo(s2!!.languageName, ignoreCase = true)
            }
        })
        return dataList
    }
}