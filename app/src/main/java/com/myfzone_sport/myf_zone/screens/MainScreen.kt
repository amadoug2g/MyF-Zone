package com.myfzone_sport.myf_zone.screens

import android.app.Activity
import android.content.*
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.ActivityMainScreenBinding
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth.activeCoachClub
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth.isAffiliated
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth.isConnected
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.model.chat.Chat
import com.myfzone_sport.myf_zone.screens.MainService.getImageReference
import com.myfzone_sport.myf_zone.setupWithNavController
import com.myfzone_sport.myf_zone.util.Constants.TRACKING
import com.myfzone_sport.myf_zone.util.Tracking
import kotlinx.android.synthetic.main.activity_main_screen.*
import kotlinx.android.synthetic.main.activity_main_screen.view.*
import kotlinx.android.synthetic.main.card_event_item.*
import kotlinx.android.synthetic.main.on_boarding_card.view.*
import kotlinx.android.synthetic.main.on_boarding_choice.view.*
import org.jetbrains.anko.toast
import java.util.*
import kotlin.concurrent.schedule

private const val PREFS_NAME = "onBoarding"
private const val INFO_MSG_AGENDA = "onBoarding_agenda"
private const val INFO_MSG_MAP = "onBoarding_map"
private const val INFO_MSG_CHAT = "onBoarding_chat"

class MainScreen : AppCompatActivity(), NavController.OnDestinationChangedListener {


    companion object {
        private val TAG = MainScreen::class.java.simpleName

        private var currentNavController: LiveData<NavController>? = null
        private var doubleBackToExitPressedOnce = false

        lateinit var navController: NavController
        lateinit var binding: ActivityMainScreenBinding
        private lateinit var viewModel: MainViewModel
        private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                Log.i(TAG, "Intent?1 ${intent.extras?.getString("elementId")}")
            }
        }
    }

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ManagerAuth.checkUserStatus()
//        versionCode.text = "1.0"
        setTheme(R.style.AppTheme)
//        versionCode.visibility = View.GONE
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_screen)

        Log.d(TAG, "onCREATE")

        binding.apply {
            lifecycleOwner = this@MainScreen
            executePendingBindings()
        }

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

//        lifecycleScope.launch {
//            viewModel.checkUserAffiliationStatus()
//        }

        binding.accountButton.background = null
        binding.infoButton.background = null

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }

        binding.infoWindowCard.continueInfoCard.setOnClickListener {
            binding.infoWindowCardLayout.visibility = View.GONE
            binding.mainLayout.isEnabled = true
        }

        binding.infoWindowCardLayout.setOnClickListener {
            binding.infoWindowCardLayout.visibility = View.GONE
            binding.mainLayout.isEnabled = true
        }

        imageProfile()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentNavHost) as NavHostFragment
        navController = navHostFragment.navController

        //bottomNavBar.selectedItemId = R.id.map
        checkIntent()
        checkOnBoarding()
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        imageProfile()
        return super.onCreateView(name, context, attrs)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("MainScreen", "created result")
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            messageReceiver,
            IntentFilter("MyData")
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        binding.bottomNavBar.apply {
            background = null
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onBackPressed() {
        try {
            if (navController.currentDestination!!.id == R.id.calendarFragment) {
                if (doubleBackToExitPressedOnce || supportFragmentManager.backStackEntryCount != 0) {
                    super.onBackPressed()
                    return
                }

                doubleBackToExitPressedOnce = true
                toast(getString(R.string.exit_message))

                val delay: Long = 2000
                Timer().schedule(delay) {
                    doubleBackToExitPressedOnce = false
                }
            } else {
                super.onBackPressed()
            }
        } catch (e: Exception) {
            val bundleTracking =
                bundleOf("MainScreen ${getString(R.string.error_msg)}" to e.localizedMessage)
            TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

            toast("Error: $e")
            Log.e(TAG, "Error in onBackPressed: ${e.localizedMessage}")
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        when (destination.id) {
            R.id.calendarFragment -> {
//                checkOnBoardingAgenda()
                TRACKING.logEvent(Tracking.AGENDA, null)
                mainNavBarAppearance()
                binding.bottomBar.hideOnScroll = true

                binding.fabMain.setOnClickListener {
                    fabButton(R.id.calendarFragment)
                }

                binding.accountButton.setOnClickListener {
                    profileButton(R.id.calendarFragment)
                }

                binding.infoButton.setOnClickListener {
                    infoButton(R.id.calendarFragment)
                }
            }
            R.id.mapsFragment -> {
                checkOnBoardingMap()
                TRACKING.logEvent(Tracking.MAP, null)
                mainNavBarAppearance()
                binding.bottomBar.hideOnScroll = false

                binding.fabMain.setOnClickListener {
                    fabButton(R.id.mapsFragment)
                }

                binding.accountButton.setOnClickListener {
                    profileButton(R.id.mapsFragment)
                }

                binding.infoButton.setOnClickListener {
                    infoButton(R.id.mapsFragment)
                }
            }
            R.id.messageFragment -> {
                checkOnBoardingChat()
                TRACKING.logEvent(Tracking.CHAT, null)
                mainNavBarAppearance()
                binding.bottomBar.hideOnScroll = true

                binding.fabMain.setOnClickListener {
                    fabButton(R.id.messageFragment)
                }

                binding.accountButton.setOnClickListener {
                    profileButton(R.id.messageFragment)
                }

                binding.infoButton.setOnClickListener {
                    infoButton(R.id.messageFragment)
                }
            }
            R.id.loginFragment2 -> {
                binding.bottomBar.visibility = View.GONE
                binding.fabMain.visibility = View.GONE
                binding.accountButton.visibility = View.GONE
                binding.infoButton.visibility = View.GONE

                supportActionBar!!.apply {
                    hide()
                }
            }
            R.id.homeFragment -> {
                binding.bottomBar.visibility = View.GONE
                binding.fabMain.visibility = View.GONE
                binding.accountButton.visibility = View.GONE
                binding.infoButton.visibility = View.GONE

                supportActionBar!!.apply {
                    hide()
                }
            }
            else -> {
                supportActionBar!!.apply {
                    show()
                }

                binding.bottomBar.visibility = View.GONE
                binding.fabMain.visibility = View.GONE
                binding.accountButton.visibility = View.GONE
                binding.infoButton.visibility = View.GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }
    //endregion

    //region Bottom Navigation
    private fun setupBottomNavigationBar() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavBar)

        val navGraphIds = listOf(R.navigation.calendar, R.navigation.map, R.navigation.message)

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.fragmentNavHost,
            intent = intent
        )

        // Whenever the selected controller changes, setup the action bar.
        controller.observe(this, { navController ->
            setupActionBarWithNavController(navController)
            navController.addOnDestinationChangedListener(this)
        })
        currentNavController = controller
    }

    private fun mainNavBarAppearance() {
        binding.bottomBar.visibility = View.VISIBLE
        binding.fabMain.visibility = View.VISIBLE
        binding.accountButton.visibility = View.VISIBLE
        binding.infoButton.visibility = View.VISIBLE

        if (isConnected) {
            if (isAffiliated) {
                try {
                    viewModel.addChatListener {
                        countMessages(it)
                    }
                } catch (e: Exception) {
                    val bundleTracking =
                        bundleOf("MainScreen ${getString(R.string.error_msg)}" to e.localizedMessage)
                    TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                    Log.e(TAG, "Error in eventListener: $e")
                    toast("Error in eventListener: $e")
                }
            }
        }

        supportActionBar!!.apply {
            hide()
        }

        binding.fabMain.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                applicationContext,
                R.color.colorAccent
            )
        )
    }
    //endregion

    //region View Methods

    /*
    //    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
    //        if (ev!!.action == MotionEvent.ACTION_DOWN) {
    //            val v: View? = currentFocus
    //            if (v is TextInputEditText) {
    //                val outRect = Rect()
    //                v.getGlobalVisibleRect(outRect)
    //                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
    //                    if (currentFocus != null) {
    //                        val imm =
    //                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    //                        imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
    //                    }
    //                    hideKeyboard()
    //                    v.clearFocus()
    //                }
    //            }
    //        }
    //        return super.dispatchTouchEvent(ev)
    //    }

     */
    private fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun imageProfile() {
        if (isConnected) {
            if (isAffiliated) {
                try {
                    GlideApp.with(this).apply {
                        load(getImageReference(activeCoachClub!!.logo))
                            .placeholder(R.drawable.ic_account)
                            .centerCrop()
                            .into(binding.accountButton)
                    }
                } catch (e: Exception) {
                    Log.e("Holder", "Image could not load: $e")
                }
            }
        }
    }
    //endregion

    //region onBoarding
    private fun checkOnBoarding() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val firstStart = prefs.getBoolean("firstStart", true)

        if (firstStart) {
            showOnBoarding()
        } else {
            binding.mainLayout.visibility = View.VISIBLE
            binding.onBoardingCardLayout.visibility = View.GONE
        }
    }

    private fun checkOnBoardingAgenda() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val firstStart = prefs.getBoolean("firstStart", true)

        val prefsAgenda = getSharedPreferences(INFO_MSG_AGENDA, MODE_PRIVATE)
        val firstStartAgenda = prefsAgenda.getBoolean("firstStartAgenda", true)

//        if (firstStart) {
//            Log.d(TAG, "firstStart = $firstStart")
        if (firstStartAgenda) {
            Log.d(TAG, "firstStartAgenda = $firstStartAgenda")
            infoButton(R.id.calendarFragment)
        }
//        }
    }

    private fun checkOnBoardingMap() {
        val prefs = getSharedPreferences(INFO_MSG_MAP, MODE_PRIVATE)
        val firstStart = prefs.getBoolean("firstStartMap", true)

        if (firstStart) {
            infoButton(R.id.mapsFragment)
        }
    }

    private fun checkOnBoardingChat() {
        val prefs = getSharedPreferences(INFO_MSG_CHAT, MODE_PRIVATE)
        val firstStart = prefs.getBoolean("firstStartChat", true)

        if (firstStart) {
            infoButton(R.id.messageFragment)
        }
    }

    private fun showOnBoarding() {
        onBoarding()

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean("firstStart", false)
        editor.apply()
    }

    private fun onBoarding() {
        binding.onBoardingCardLayout.visibility = View.VISIBLE
        binding.mainLayout.visibility = View.GONE

        binding.onBoardingCardLayout.onBoardingCard.checkbox_on_boarding.setOnCheckedChangeListener { _, b ->
            binding.onBoardingCardLayout.onBoardingCard.continue_on_boarding.isEnabled = b
        }

        binding.onBoardingCardLayout.onBoardingCard.continue_on_boarding.setOnClickListener {
            binding.onBoardingCardLayout.onBoardingCard.visibility = View.GONE
            binding.onBoardingCardLayout.onBoardingCardChoice.visibility = View.VISIBLE
        }

        binding.onBoardingCardLayout.onBoardingCardChoice.choice_login.setOnClickListener {
            binding.onBoardingCardLayout.visibility = View.GONE
            binding.mainLayout.visibility = View.VISIBLE
            navigate(R.id.calendarToSignUp)

            val bundleChoice = bundleOf("onBoarding" to "Register")
            TRACKING.logEvent(Tracking.ALERT_CHOICE, bundleChoice)
        }

        binding.onBoardingCardLayout.onBoardingCardChoice.choice_later.setOnClickListener {
            binding.onBoardingCardLayout.visibility = View.GONE
            binding.mainLayout.visibility = View.VISIBLE

            val bundleChoice = bundleOf("onBoarding" to "Later")
            TRACKING.logEvent(Tracking.ALERT_CHOICE, bundleChoice)
        }
    }
    //endregion

    //region Navigation
    private fun navigate(destination: Int, extra: Bundle? = null) {
        findNavController(binding.fragmentNavHost.id).navigate(destination, extra)
    }

    private fun fabButton(destinationId: Int) {
        TRACKING.logEvent(Tracking.EVENT_CREATION, null)
        val bundle = bundleOf("page" to destinationId)
        when (destinationId) {
            R.id.calendarFragment -> {
                if (isConnected) {
                    if (isAffiliated) {
                        navigate(R.id.calendarToEventCreation)
                    } else {
                        toast(getString(R.string.user_not_affiliated))
                        navigate(R.id.calendarToAffiliationRequest, bundle)
                    }
                } else {
                    navigate(R.id.calendarToSignUp)
                }
            }
            R.id.mapsFragment -> {
                if (isConnected) {
                    if (isAffiliated) {
                        navigate(R.id.mapsToEventCreation)
                    } else {
                        toast(getString(R.string.user_not_affiliated))
                        navigate(R.id.mapsToAffiliationRequest, bundle)
                    }
                } else {
                    navigate(R.id.mapsToSignUp)
                }
            }
            R.id.messageFragment -> {
                if (isConnected) {
                    if (isAffiliated) {
                        navigate(R.id.messageToEventCreation)
                    } else {
                        toast(getString(R.string.user_not_affiliated))
                        navigate(R.id.messageToAffiliationRequest, bundle)
                    }
                } else {
                    navigate(R.id.messageToSignUp)
                }
            }
        }
    }

    private fun profileButton(destinationId: Int) {
        TRACKING.logEvent(Tracking.ACCOUNT, null)
        val bundle = bundleOf("page" to destinationId)
        when (destinationId) {
            R.id.calendarFragment -> {
                navigate(R.id.calendarToLogin2)

//                if (isConnected) {
//                    if (isAffiliated) {
//                        navigate(R.id.calendarToProfile)
//                    } else {
//                        toast(getString(R.string.user_not_affiliated))
//                        navigate(R.id.calendarToAffiliationRequest, bundle)
//                    }
//                } else {
//                    navigate(R.id.calendarToSignUp)
//                }
            }
            R.id.mapsFragment -> {
                if (isConnected) {
                    if (isAffiliated) {
                        navigate(R.id.mapsToProfile)
                    } else {
                        toast(getString(R.string.user_not_affiliated))
                        navigate(R.id.mapsToAffiliationRequest, bundle)
                    }
                } else {
                    navigate(R.id.mapsToSignUp)
                }
            }
            R.id.messageFragment -> {
                if (isConnected) {
                    if (isAffiliated) {
                        navigate(R.id.messageToProfile)
                    } else {
                        toast(getString(R.string.user_not_affiliated))
                        navigate(R.id.messageToAffiliationRequest, bundle)
                    }
                } else {
                    navigate(R.id.messageToSignUp)
                }
            }
        }
    }

    private fun infoButton(destinationId: Int) {
        binding.mainLayout.isEnabled = false
        when (destinationId) {
            R.id.calendarFragment -> {
                val prefs = getSharedPreferences(INFO_MSG_AGENDA, MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putBoolean("firstStartAgenda", false)
                editor.apply()

                binding.infoWindowCardLayout.visibility = View.VISIBLE
                binding.infoWindowCard.textViewIntroAgenda.visibility = View.VISIBLE
                binding.infoWindowCard.textViewIntroMap.visibility = View.GONE
                binding.infoWindowCard.textViewIntroMapClub.visibility = View.GONE
                binding.infoWindowCard.textViewIntroChat.visibility = View.GONE
            }
            R.id.mapsFragment -> {
                val prefs = getSharedPreferences(INFO_MSG_MAP, MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putBoolean("firstStartMap", false)
                editor.apply()

                binding.infoWindowCardLayout.visibility = View.VISIBLE
                binding.infoWindowCard.textViewIntroMap.visibility = View.VISIBLE
                binding.infoWindowCard.textViewIntroMapClub.visibility = View.VISIBLE
                binding.infoWindowCard.textViewIntroAgenda.visibility = View.GONE
                binding.infoWindowCard.textViewIntroChat.visibility = View.GONE
            }
            R.id.messageFragment -> {
                val prefs = getSharedPreferences(INFO_MSG_CHAT, MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putBoolean("firstStartChat", false)
                editor.apply()

                binding.infoWindowCardLayout.visibility = View.VISIBLE
                binding.infoWindowCard.textViewIntroAgenda.visibility = View.GONE
                binding.infoWindowCard.textViewIntroMap.visibility = View.GONE
                binding.infoWindowCard.textViewIntroMapClub.visibility = View.GONE
                binding.infoWindowCard.textViewIntroChat.visibility = View.VISIBLE
            }
        }
    }
    //endregion

    //region Intent Receiver
    private fun checkIntent() {
        val bundle = intent.extras
        if (bundle != null) {
            val bundleTracking = bundleOf("NotificationType" to bundle.getString("type"))
            TRACKING.logEvent(Tracking.ALERT_NOTIFICATION, bundleTracking)

            when (bundle.getString("type")) {
                "chatReceiveMessage" -> {
                    Log.i(TAG, "Intent type: new chat message")
                    val coachId = bundle.getString("elementId")
                    val bundleNavigation = bundleOf("coachId" to coachId)
//                    bottomNavBar.selectedItemId = R.id.message
                    try {
                        super.onPostResume()
                        navController.navigate(
                            R.id.notificationCalendarToDiscussion,
                            bundleNavigation
                        )
//                        navController.navigate(R.id.discussionFragment, bundleNavigation)
                    } catch (e: Exception) {
                        val bundleError =
                            bundleOf("NotificationType ${getString(R.string.error_msg)}" to e.localizedMessage)
                        TRACKING.logEvent(Tracking.ALERT_ERROR, bundleError)
                        Log.e(TAG, "Could not navigate to Discussion:  $e")
                    }
                }
                "eventModification", "eventAcceptParticipation", "eventParticipation", "eventRefuseParticipation" -> {
                    Log.i(TAG, "Intent type: event modified")
                    val eventId = bundle.getString("elementId")
                    val bundleNavigation = bundleOf("eventId" to eventId)
                    try {
                        super.onPostResume()
                        navController.navigate(R.id.calendarToEventDetail, bundleNavigation)
                    } catch (e: Exception) {
                        val bundleError =
                            bundleOf("NotificationType ${getString(R.string.error_msg)}" to e.localizedMessage)
                        TRACKING.logEvent(Tracking.ALERT_ERROR, bundleError)
                        Log.e(TAG, "Could not navigate to Event Detail:  $e")
                    }
                }
                else -> {
                    Log.i(TAG, "Intent? ${bundle.getString("type")}")
                }
            }
        }
    }
    //endregion

    //region Message Counter
    private fun countMessages(list: MutableList<Chat>) {
        var messageCount = 0

        list.forEach { chat -> if (chat.unread) messageCount++ }

        notifyMessages(messageCount)
    }

    private fun notifyMessages(count: Int) {
        when (count > 0) {
            true -> {
                binding.bottomNavBar.getOrCreateBadge(R.id.message).backgroundColor =
                    ContextCompat.getColor(this, R.color.colorCoral)
//                binding.bottomNavBar.getOrCreateBadge(R.id.message).number = count
            }

            false -> {
                binding.bottomNavBar.removeBadge(R.id.message)
            }
        }
    }
    //endregion
}