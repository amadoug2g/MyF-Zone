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
import androidx.navigation.*
import androidx.navigation.fragment.NavHostFragment
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClubAffiliationLive
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClubLive
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachEventsLive
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachLive
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.affiliationNbrLive
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.checkUserStatus
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.isAffiliatedLive
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.isConnectedLive
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.viewmodel.*
import com.myfzone_sport.myf_zone.data.LocalDataSourceImpl
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.ActivityMainBinding
import com.myfzone_sport.myf_zone.usecases.detailevent.GetEventFromIdUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.GetOwnerFromEventUseCase
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

        checkUserStatus()
        initViews()
        initViewModels()
    }

    override fun onStart() {
        super.onStart()
        viewModel.getUserStatus()

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
                viewModel.getUserStatus()
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
        val localDataSource = LocalDataSourceImpl()
        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(/*localDataSource,*/ remoteDataSource)

        //Activity View Model
        val getUserStatusUseCase = GetUserStatusUseCase(repository)

        //Fragment View Model
        val getAllEventsUseCase = GetAllEventsUseCase(repository)
        val getCloseEventsUseCase = GetCloseEventsUseCase(repository)
        val getFriendlyEventsUseCase = GetFriendlyEventsUseCase(repository)
        val getTourneyEventsUseCase = GetTourneyEventsUseCase(repository)
        val getPlateauEventsUseCase = GetPlateauEventsUseCase(repository)
        val getUserEventsUseCase = GetUserEventsUseCase(repository)
        val getImageReferenceUseCase = GetImageReferenceUseCase(repository)
        val getUserUseCase = GetUserUseCase(repository)
        val getUserClubUseCase = GetUserClubUseCase(repository)
        val getUserAffiliationUseCase = GetUserClubAffiliationUseCase(repository)
        val getUserEventListUseCase = GetUserEventListUseCase(repository)

        //Registration View Model
        val addUserToDatabaseUseCase = AddUserToDatabaseUseCase(repository)
        val assignDisplayNameUseCase = AssignDisplayNameUseCase(repository)
        val assignProfileImageUseCase = AssignProfileImageUseCase(repository)
        val signInUserUseCase = SignInUserUseCase(repository)
        val signUpUserUseCase = SignUpUserUseCase(repository)
        val signOutUseCase = SignOutUseCase(repository)

        //Event View Model
        val getEventFromIdUseCase = GetEventFromIdUseCase(repository)
        val getOwnerFromEventUseCase = GetOwnerFromEventUseCase(repository)

        viewModel = ViewModelProvider(
            this,
            ActivityViewModelFactory(
                getUserStatusUseCase
            )
        ).get(
            ActivityViewModel::class.java
        )


//        val navHostFragment: NavHostFragment =
//            supportFragmentManager.findFragmentById(R.id.fragmentNavHost) as NavHostFragment
//        navHostFragment.childFragmentManager.fragments[0]

        fragmentViewModel = ViewModelProvider(
            this,
            FragmentViewModelFactory(
                getCloseEventsUseCase,
                getAllEventsUseCase,
                getFriendlyEventsUseCase,
                getTourneyEventsUseCase,
                getPlateauEventsUseCase,
                getUserEventsUseCase,
                getImageReferenceUseCase,
                getUserUseCase,
                getUserClubUseCase,
                getUserAffiliationUseCase,
                getUserEventListUseCase,
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

        eventViewModel = ViewModelProvider(
            this,
            EventViewModelFactory(
                getEventFromIdUseCase,
                getOwnerFromEventUseCase
            )
        ).get(EventViewModel::class.java)
    }

    private fun setupObservers() {
        viewModel.isUserConnected.observe(this, {
            if (it) {
                fragmentViewModel.getUser()
//                fragmentViewModel.getQuery()
                fragmentViewModel.getUserClub()
                fragmentViewModel.getUserAffiliation()
                fragmentViewModel.userConnected()
                navController.setGraph(R.navigation.home)
                fragmentViewModel.getUserEvents()
                fragmentViewModel.getCloseEvents()
                fragmentViewModel.getUserEventList()
            } else {
                fragmentViewModel.userNotConnected()
                navController.setGraph(R.navigation.main_start)
            }
        })

//        isAffiliatedLive.observe(this, {
//            if (it) {
//                fragmentViewModel.getUserEvents()
//                fragmentViewModel.getCloseEvents()
//            }
//        })
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