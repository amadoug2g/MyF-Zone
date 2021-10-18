package com.myfzone_sport.myf_zone.app.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.*
import androidx.navigation.fragment.NavHostFragment
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.viewmodel.*
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.ActivityMainBinding
import com.myfzone_sport.myf_zone.usecases.event.*
import com.myfzone_sport.myf_zone.usecases.registration.*
import com.myfzone_sport.myf_zone.usecases.user.*

class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    //region Variables
    lateinit var navController: NavController

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private lateinit var binding: ActivityMainBinding
        private lateinit var viewModel: ActivityViewModel
        private lateinit var fragmentViewModel: FragmentViewModel
        private lateinit var registrationViewModel: RegistrationViewModel
        private lateinit var eventViewModel: EventViewModel

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

        setupViews()
        initViewModels()
    }

    override fun onStart() {
        super.onStart()
        viewModel.getUserStatus()
        viewModel.checkUserStatus()

        setupObservers()

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
        when (destination.id) {
            R.id.loginFragment2 -> {
                hideBar()
            }
            R.id.homeFragment -> {
                hideBar()
            }
            else -> {
                showBar()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.fragmentNavHostView).navigateUp() || super.onSupportNavigateUp()
    }
    //endregion

    //region Init
    private fun setupViews() {
        setTheme(R.style.AppTheme)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.apply {
            lifecycleOwner = this@MainActivity
            executePendingBindings()
        }

        //Check User Status UC
        //Check Intent UC
        //Check OnBoarding UC

        supportActionBar!!.apply {
            hide()
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentNavHostView) as NavHostFragment
        navController = navHostFragment.navController

//        setupActionBarWithNavController(findNavController(R.id.fragmentNavHostView))
    }

    private fun initViewModels() {
        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(remoteDataSource)

        //Activity View Model
        val getUserStatusUseCase = GetUserStatusUseCase(repository)

        viewModel = ViewModelProvider(this, ActivityViewModelFactory(getUserStatusUseCase)).get(ActivityViewModel::class.java)


//        val navHostFragment: NavHostFragment =
//            supportFragmentManager.findFragmentById(R.id.fragmentNavHost) as NavHostFragment
//        navHostFragment.childFragmentManager.fragments[0]
    }

    private fun setupObservers() {
        viewModel.isUserConnected.observe(this, {
            if (it) {
                navController.setGraph(R.navigation.home)
            } else {
                navController.setGraph(R.navigation.main_start)
            }
        })
    }
    //endregion

    //region View Methods
    private fun hideBar() {
        supportActionBar!!.apply {
            hide()
        }
    }

    private fun showBar() {
        supportActionBar!!.apply {
            show()
        }
    }
    //endregion
}