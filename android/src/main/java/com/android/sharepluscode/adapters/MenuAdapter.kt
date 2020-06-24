package com.android.sharepluscode.adapters

import android.app.Activity
import android.content.Intent
import android.content.Intent.getIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.android.sharepluscode.R
import com.android.sharepluscode.model.LanguageModel
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
                //mainActivity.recreate()
                restartActivity(mainActivity)
            }
        }
    }

    private fun restartActivity(activity: Activity) {
        val intent = activity.intent
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.finish()
        activity.startActivity(intent)
        activity.overridePendingTransition(R.anim.fade_in_activity, R.anim.fade_out_activity);
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    private fun createLanguageData(): MutableList<LanguageModel> {
        val dataList: MutableList<LanguageModel> = mutableListOf()
        dataList.add(LanguageModel("English", "en"))
        dataList.add(LanguageModel("French", "fr"))
        dataList.add(LanguageModel("Spanish", "es"))
        dataList.add(LanguageModel("Portuguese", "pt"))
        dataList.add(LanguageModel("Swahili", "sw"))
        dataList.add(LanguageModel("Hindi", "hi"))
        dataList.add(LanguageModel("Arabic", "ar"))
        dataList.add(LanguageModel("Amharic", "am"))
        dataList.add(LanguageModel("Bengali", "bn"))
        dataList.add(LanguageModel("Hausa", "ha"))
        dataList.add(LanguageModel("Igbo", "ig"))
        dataList.add(LanguageModel("Shona", "sn"))
        dataList.add(LanguageModel("Telugu", "te"))
        dataList.add(LanguageModel("Urdu", "ur"))
        dataList.add(LanguageModel("Xhosa", "xh"))
        dataList.add(LanguageModel("Zulu", "zu"))
        dataList.add(LanguageModel("Kinyarwanda", "rw"))
        dataList.add(LanguageModel("Bemba", "bem"))

        Collections.sort(dataList, object : Comparator<LanguageModel?> {
            override fun compare(s1: LanguageModel?, s2: LanguageModel?): Int {
                return s1!!.languageName.compareTo(s2!!.languageName, ignoreCase = true)
            }
        })
        return dataList
    }
}