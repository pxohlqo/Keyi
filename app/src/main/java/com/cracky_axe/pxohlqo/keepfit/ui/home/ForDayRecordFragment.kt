package com.cracky_axe.pxohlqo.keepfit.ui.home


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import com.cracky_axe.pxohlqo.keepfit.R
import com.cracky_axe.pxohlqo.keepfit.model.FoodRecordEntity
import com.cracky_axe.pxohlqo.keepfit.util.FitDateUtils
import com.wang.avi.AVLoadingIndicatorView
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.util.*

class ForDayRecordFragment : Fragment(), AnkoLogger, ForDayRecordAdapter.OnItemLongClickListener, PopupMenu.OnMenuItemClickListener {

    lateinit var mContext: Context
    lateinit var dataSet: List<FoodRecordEntity>
    var index: Int = 0

    lateinit var mRecyclerView: RecyclerView
    lateinit var mThermalTextView: TextView
    lateinit var loadingIndicator: AVLoadingIndicatorView
    lateinit var stateTextView: TextView

    lateinit var onRecordLongClickHandler: ForDayRecordAdapter.OnItemLongClickListener
    lateinit var recordFragmentCallBackHandler: RecordFragmentCallBackHandler

    var thermalSum: Int = 0

    var longClickedRecordIndex: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        //TODO implement longClick popup menu. [https://developer.android.com/guide/topics/ui/menus#PopupMenu]
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_forday, container, false)
        //val adapter = ForDayRecordAdapter(TestUtils.generateDataSet())
        val adapter = ForDayRecordAdapter(dataSet, this)

        countThermal()

        loadingIndicator = view.findViewById(R.id.forDay_loadingIndicator)
        loadingIndicator.hide()

        stateTextView = view.findViewById(R.id.forDay_state_textView)

        mRecyclerView = view.findViewById<RecyclerView>(R.id.forDay_recyclerView)
        mThermalTextView = view.findViewById(R.id.thermal_forDay_TextView)
        mRecyclerView.adapter = adapter
        mRecyclerView.layoutManager = LinearLayoutManager(mContext)

        mThermalTextView.text = thermalSum.toString()

        if (index > FitDateUtils.getTodayOfWeek()) {
            val foodList = mContext.resources.getStringArray(R.array.food_list)
            val stateText = mContext.resources.getString(R.string.for_day_frag_state_future) + foodList[Random().nextInt(foodList.size)]
            stateTextView.text = stateText

            mRecyclerView.visibility = View.INVISIBLE
            stateTextView.visibility = View.VISIBLE
        } else if (index <= FitDateUtils.getTodayOfWeek() && dataSet.isNotEmpty()) {
            mRecyclerView.visibility = View.VISIBLE
            stateTextView.visibility = View.INVISIBLE
        } else if (index <= FitDateUtils.getTodayOfWeek() && dataSet.isEmpty()) {
            stateTextView.apply {
                text = mContext.resources.getString(R.string.for_day_frag_state_no_record)
                visibility = View.VISIBLE
            }
            mRecyclerView.visibility = View.VISIBLE
        }

        return view
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onItemLongClick(view: View, position: Int) {
        PopupMenu(mContext, view).apply {
            inflate(R.menu.item_longclick_menu)
            setOnMenuItemClickListener(this@ForDayRecordFragment)
            show()
        }
        longClickedRecordIndex = position
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_longClick_delete -> {
                removeRecord(dataSet[longClickedRecordIndex])
                return true
            }

            R.id.menu_item_longClick_change -> {
                changeRecord(dataSet[longClickedRecordIndex])
                return true
            }

            else -> return false
        }
    }

    private fun countThermal() {
        if (dataSet.isNotEmpty()) {
            dataSet.forEach {
                thermalSum += it.food.target.thermal * it.quantity
            }
        }
    }

    /**
     * Not work. Somebody tell me why please!
     */
    fun refreshList() {
        mRecyclerView.adapter.notifyDataSetChanged()
    }

    fun showLoading() {
        loadingIndicator.smoothToShow()
        info { "showLoading() called on Thread: ${Thread.currentThread().name}" }
    }

    fun hideLoading() {
        loadingIndicator.smoothToHide()
    }

    companion object {
        fun newInstance(context: Context, foodRecordForDay: List<FoodRecordEntity>?,recordFragmentCallBackHandler: RecordFragmentCallBackHandler, index: Int): ForDayRecordFragment {
            val instance = ForDayRecordFragment()
            instance.mContext = context
            instance.dataSet = foodRecordForDay!!
            instance.index = index
            instance.recordFragmentCallBackHandler = recordFragmentCallBackHandler
            return instance
        }
    }

    interface RecordFragmentCallBackHandler {
        fun removeRecordItem(item: FoodRecordEntity)
        fun changeRecordItem(item: FoodRecordEntity)
    }

    fun removeRecord(item: FoodRecordEntity) {
        recordFragmentCallBackHandler.removeRecordItem(item)
    }

    fun changeRecord(item: FoodRecordEntity) {
        recordFragmentCallBackHandler.changeRecordItem(item)
    }


}
