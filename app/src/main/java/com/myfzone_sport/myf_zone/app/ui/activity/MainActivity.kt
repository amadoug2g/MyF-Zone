package com.myfzone_sport.myf_zone.app.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.viewmodel.*
import com.myfzone_sport.myf_zone.data.LocalDataSourceImpl
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.ActivityMainBinding
import com.myfzone_sport.myf_zone.usecases.event.*
import com.myfzone_sport.myf_zone.usecases.registration.*
import com.myfzone_sport.myf_zone.usecases.user.GetUserUseCase
import com.myfzone_sport.myf_zone.usecases.user.SignOutUseCase

class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    //region Variables
    lateinit var navController: NavController

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private lateinit var binding: ActivityMainBinding
        private lateinit var viewModel: ActivityViewModel
        private lateinit var fragmentViewModel: FragmentViewModel
        private lateinit var registrationViewModel: RegistrationViewModel

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
        checkStatus()
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        //Check Show Image Profile if connected

        return super.onCreateView(name, context, attrs)
    }

    override fun onStart() {
        super.onStart()
        viewModel.getUser()
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
                viewModel.getUser()
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
        val localDataSource = LocalDataSourceImpl()
        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(/*localDataSource,*/ remoteDataSource)

        //Activity View Model
        val getUserUseCase = GetUserUseCase(repository)

        //Fragment View Model
        val getAllEventsUseCase = GetAllEventsUseCase(repository)
        val getCloseEventsUseCase = GetCloseEventsUseCase(repository)
        val getFriendlyEventsUseCase = GetFriendlyEventsUseCase(repository)
        val getTourneyEventsUseCase = GetTourneyEventsUseCase(repository)
        val getPlateauEventsUseCase = GetPlateauEventsUseCase(repository)
        val getUserEventsUseCase = GetUserEventsUseCase(repository)


        //Registration View Model
        val addUserToDatabaseUseCase = AddUserToDatabaseUseCase(repository)
        val assignDisplayNameUseCase = AssignDisplayNameUseCase(repository)
        val assignProfileImageUseCase = AssignProfileImageUseCase(repository)
        val signInUserUseCase = SignInUserUseCase(repository)
        val signUpUserUseCase = SignUpUserUseCase(repository)
        val signOutUseCase = SignOutUseCase(repository)

        viewModel = ViewModelProvider(
            this,
            ActivityViewModelFactory(
                getUserUseCase
            )
        ).get(
            ActivityViewModel::class.java
        )

        fragmentViewModel = ViewModelProvider(
            this,
            FragmentViewModelFactory(
                getCloseEventsUseCase,
                getAllEventsUseCase,
                getFriendlyEventsUseCase,
                getTourneyEventsUseCase,
                getPlateauEventsUseCase,
                getUserEventsUseCase,
                signOutUseCase
            )
        ).get(
            FragmentViewModel::class.java
        )

        registrationViewModel = ViewModelProvider(
            this,
            RegistrationViewModelFactory(
                addUserToDatabaseUseCase,
                assignDisplayNameUseCase,
                assignProfileImageUseCase,
                signInUserUseCase,
                signUpUserUseCase
            )
        ).get(
            RegistrationViewModel::class.java
        )
    }

    private fun checkStatus() {
        viewModel.isUserConnected.observe(this, {
            if (it) fragmentViewModel.userConnected() else fragmentViewModel.userNotConnected()
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