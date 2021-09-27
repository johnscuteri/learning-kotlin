package com.example.qrcodereaderapp.ui.main

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.qrcodereaderapp.R
import com.google.zxing.integration.android.IntentIntegrator
import org.json.JSONObject
import android.widget.TextView
import android.widget.ArrayAdapter

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var tv: TextView
    private lateinit var totView: TextView
    private lateinit var list: List<JSONObject>
    private lateinit var listView: ListView
    private var inCart: MutableList<Pair<Double, String>> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel

        var number = 0
        val btn: Button = view!!.findViewById(R.id.buttonForScan)
        tv = view!!.findViewById(R.id.message)
        totView = view!!.findViewById(R.id.totalNum)
        Log.d("MainFragment", "Set up buttons")
        btn.setOnClickListener {
            Log.d("MainFragment", "scan button clicked")
            number++
            tv.text = number.toString()
            val intentIntegrator = IntentIntegrator.forSupportFragment(this)
            intentIntegrator.setBeepEnabled(true)
            intentIntegrator.setCameraId(0)
            intentIntegrator.setPrompt("SCAN")
            intentIntegrator.setBarcodeImageEnabled(false)
            intentIntegrator.initiateScan()
        }
        list = viewModel.loadJSONFromAssetToArray(this.context!!)
        val btn2: Button = view!!.findViewById(R.id.buttonForManualEntry)
        btn2.setOnClickListener {
            val editText = EditText(this.context!!)
            val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
            editText.layoutParams = layoutParams
            AlertDialog.Builder(this.context!!)
                .setTitle("Add Object")
                .setMessage("Type in the code of the object to add")
                .setView(editText)
                .setPositiveButton("OK") { _, _ ->
                    var tuple: Pair<Double, String>? = viewModel.matchInput(editText.text.toString(), list)
                    Log.d("MainFragment", tuple.toString())
                    if (tuple != null)
                    {
                        inCart.add(tuple)
                        updateTotal()
                    }
                }
                .setNegativeButton("Cancel") { _, _ ->
                }
                .show()
        }
        listView = view!!.findViewById(R.id.listview)
        val adapter = CustomAdapter(inCart, this.context!!)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            inCart.removeAt(position)
            Log.d("MainFragment", "Removed " + position)
            updateTotal()
        }
    }
    fun updateTotal(){
        var total: Double = 0.0
        for (item: Pair<Number, String> in inCart)
        {
            total+=item.first.toDouble()
        }
        totView.text = "$" + total.toString()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Log.d("MainFragment", "cancelled")
                Toast.makeText(this.activity, "cancelled", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("MainFragment", "Scanned")
                Toast.makeText(this.activity, "Scanned -> " + result.contents, Toast.LENGTH_SHORT)
                    .show()
                tv.text = String.format("Scanned Result: %s", result)
                Log.d("MainFragment", "Scanned Result: " + result.toString())
                var tuple: Pair<Double, String>? = viewModel.getMatches(result, list)
                Log.d("MainFragment", tuple.toString())
                if (tuple != null)
                {
                    inCart.add(tuple)
                    updateTotal()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
            Log.d("MainFragment", "something else")
        }
    }


    class CustomAdapter(data: MutableList<Pair<Double, String>>, context: Context) :
        ArrayAdapter<Pair<Double, String>>(context, R.layout.list_view_custom, data), View.OnClickListener {
        private val dataSet: MutableList<Pair<Double, String>> = data

        // View lookup cache
        private class ViewHolder {
            var txtName: TextView? = null
            var txtPrice: TextView? = null
        }

        private var lastPosition = -1
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            // Get the data item for this position
            var convertView = convertView
            val dataModel: Pair<Double, String>? = getItem(position)
            // Check if an existing view is being reused, otherwise inflate the view
            val viewHolder: ViewHolder // view lookup cache stored in tag
            val result: View?
            if (convertView == null) {
                viewHolder = ViewHolder()
                val inflater = LayoutInflater.from(context)
                convertView = inflater.inflate(R.layout.list_view_custom, parent, false)
                viewHolder.txtName = convertView!!.findViewById<View>(R.id.name) as TextView?
                viewHolder.txtPrice = convertView.findViewById<View>(R.id.price) as TextView?
                result = convertView
                convertView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as ViewHolder
                result = convertView
            }
            lastPosition = position
            viewHolder.txtName!!.text = dataSet[position].second
            viewHolder.txtPrice!!.text = "$" + dataSet[position].first.toString()
            // Return the completed view to render on screen
            return convertView!!
        }

        override fun onClick(p0: View?) {
        }
    }
}