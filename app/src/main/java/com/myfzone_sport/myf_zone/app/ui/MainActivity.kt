package com.myfzone_sport.myf_zone.app.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.ActivityMainBinding
import com.myfzone_sport.myf_zone.screens.MainScreen

class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    //region Variables
    lateinit var navController: NavController

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private lateinit var binding: ActivityMainBinding
        private lateinit var viewModel: ActivityViewModel

        private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
//                Log.i(TAG, "Intent?1 ${intent.extras?.getString("elementId")}")
            }
        }
    }
    //endregion

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
        initViewModel()
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        //Check Show Image Profile if connected

        return super.onCreateView(name, context, attrs)
    }

    override fun onStart() {
        super.onStart()
//        LocalBroadcastManager.getInstance(this).registerReceiver(
//            messageReceiver,
//            IntentFilter("MyData")
//        )
    }

    override fun onStop() {
        super.onStop()
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver)
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        TODO("Not yet implemented")
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.fragmentNavHostView).navigateUp() || super.onSupportNavigateUp()
    }
    //endregion

    //region Init
    private fun initViews() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.apply {
            lifecycleOwner = this@MainActivity
            executePendingBindings()
        }

        //Check User Status UC
        //Check Intent UC
        //Check OnBoarding UC

//        setTheme(R.style.AppTheme)

        supportActionBar!!.apply {
            hide()
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentNavHostView) as NavHostFragment
        navController = navHostFragment.navController

//        setupActionBarWithNavController(findNavController(R.id.fragmentNavHostView))
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)}
    //endregion
}