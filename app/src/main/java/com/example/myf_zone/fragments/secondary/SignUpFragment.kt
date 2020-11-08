package com.example.myf_zone.fragments.secondary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.transition.ChangeBounds
import com.example.myf_zone.R

class SignUpFragment : Fragment() {

    private val TAG = SignUpFragment::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.become_coach)
        setHasOptionsMenu(true)
    }

    override fun onDetach() {
        super.onDetach()
        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.login_coach)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedElementEnterTransition = ChangeBounds().apply {
            duration = 300
        }
        sharedElementReturnTransition = ChangeBounds().apply {
            duration = 300
        }

        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}