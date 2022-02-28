package com.tokyonth.txphook.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tokyonth.txphook.R
import com.tokyonth.txphook.databinding.ItemHookConfigBinding
import com.tokyonth.txphook.db.HookRule
import com.tokyonth.txphook.utils.ktx.dp2px
import com.tokyonth.txphook.utils.ktx.visibleOrGone

class HookConfigAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var dataArr: MutableList<HookRule> = ArrayList()

    private var btnClick: ((HookRule?, Int, Int) -> Unit)? = null

    private var arrayAdapter: ArrayAdapter<*>

    init {
        val items = context.resources.getStringArray(R.array.HookDataType)
        arrayAdapter = ArrayAdapter(context, R.layout.item_list_array, items)
    }

    fun setBtnClick(btnClick: (HookRule?, Int, Int) -> Unit) {
        this.btnClick = btnClick
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(dataArr: MutableList<HookRule>) {
        this.dataArr.addAll(dataArr)
        notifyDataSetChanged()
    }

    fun getData(): MutableList<HookRule> {
        return dataArr
    }

    fun addSimpleData(pkgName: String, name: String) {
        val rule = HookRule(
            enableHook = false,
            pkgName = pkgName,
            hookName = name,
            methodName = "",
            classPath = "",
            resultVale = "",
            valueType = ""
        )
        dataArr.add(rule)
        notifyItemInserted(dataArr.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            val view = View(parent.context).apply {
                layoutParams =
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100.dp2px().toInt())
            }
            BlockViewHolder(view)
        } else {
            ViewHolder(ItemHookConfigBinding.inflate(LayoutInflater.from(parent.context)))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val data = dataArr[position]
            holder.bind(data, btnClick!!)
            holder.initExposedDrop(arrayAdapter)
        }
    }

    override fun getItemCount(): Int {
        return dataArr.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == dataArr.size) {
            1
        } else {
            0
        }
    }

    class BlockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    class ViewHolder(private val binding: ItemHookConfigBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(hookRule: HookRule, btnClick: (HookRule?, Int, Int) -> Unit) {
            binding.run {
                itemEnableHook.isChecked = hookRule.enableHook
                itemHookName.text = hookRule.hookName
                etHookPath.setText(hookRule.classPath)
                etHookResult.setText(hookRule.resultVale)
                etHookMethodName.setText(hookRule.methodName)
            }

            binding.itemEnableHook.setOnCheckedChangeListener { _, b ->
                /*HookConfigManager.getInstance()
                    .removeAndModifyHookConfig(hookInfoEntry.packageName, adapterPosition, 1, b)*/
            }

            binding.btnSaveHook.setOnClickListener {
                if (fillConfigAndSave(hookRule)) {
                    btnClick.invoke(hookRule, adapterPosition, 1)
                } else {
                    btnClick.invoke(null, adapterPosition, 1)
                }
            }

            binding.btnDelHook.setOnClickListener {
                btnClick.invoke(null, adapterPosition, 0)
            }

            binding.llHookMsg.setOnClickListener {
                binding.llHookConfig.visibility.let { vis ->
                    (vis == View.GONE).let {
                        binding.itemIvArrow.animate().rotation(if (it) 0F else -90F).start()
                        binding.llHookConfig.visibleOrGone(it)
                    }
                }
            }
        }

        fun initExposedDrop(adapter: ArrayAdapter<*>) {
            binding.itemExposedDrop.apply {
                setAdapter(adapter)
                keyListener = null
                ellipsize = TextUtils.TruncateAt.END
            }
        }

        private fun fillConfigAndSave(hookRule: HookRule): Boolean {
            val methodName = binding.etHookMethodName.text.toString()
            val classPath = binding.etHookPath.text.toString()
            val resultVale = binding.etHookResult.text.toString()
            val isEnableHook = binding.itemEnableHook.isChecked

            // binding.itemExposedDrop.get

            return if (methodName.isEmpty()
                && classPath.isEmpty()
                && resultVale.isEmpty()
            ) {
                false
            } else {
                hookRule.enableHook = isEnableHook
                hookRule.methodName = methodName
                hookRule.classPath = classPath
                hookRule.resultVale = resultVale
                true
            }
        }

    }

}
