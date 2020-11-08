package com.example.myf_zone.fragments.primary

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myf_zone.R
import kotlinx.android.synthetic.main.fragment_message.*

class MessageFragment : Fragment() {

    private val TAG = MessageFragment::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCREATE")
        if (savedInstanceState != null) {
            val input = savedInstanceState["input"]
            textViewMessage.text = input as Editable?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message, container, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val input = textViewMessage.text.toString()

        outState.putString("input", input)
    }
}