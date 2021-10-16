package com.myfzone_sport.myf_zone.app.ui.fragment

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.TRACKING
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.viewmodel.SettingsViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.SettingsViewModelViewModelFactory
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.FragmentSettings2Binding
import com.myfzone_sport.myf_zone.screens.MainScreen
import com.myfzone_sport.myf_zone.usecases.user.SignOutUseCase
import com.myfzone_sport.myf_zone.util.Constants
import com.myfzone_sport.myf_zone.util.Tracking
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.newTask
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast

class SettingsFragment : Fragment() {

    //region Variables
    companion object {
        private lateinit var binding: FragmentSettings2Binding
        private lateinit var viewModel: SettingsViewModel
        private lateinit var viewModelFactory: SettingsViewModelViewModelFactory
    }
    //endregion

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(remoteDataSource)

        val signOutUseCase = SignOutUseCase(repository)

        viewModelFactory = SettingsViewModelViewModelFactory(signOutUseCase)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(SettingsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_settings2,
            container,
            false
        )

        setupViews()

        return binding.root
    }
    //endregion

    //region Setups
    private fun setupViews() {
        binding.exitSettings.setOnClickListener { requireActivity().onBackPressed() }

        binding.logout.setOnClickListener { signOut() }

        binding.privacyPolicy.apply {
            TRACKING.logEvent(Tracking.POLICY, null)
            movementMethod = LinkMovementMethod.getInstance()
            binding.privacyPolicy.removeLinksUnderline()
        }

        binding.cgu.apply {
            TRACKING.logEvent(Tracking.CGU, null)
            movementMethod = LinkMovementMethod.getInstance()
            binding.cgu.removeLinksUnderline()
        }

        binding.osSettings.setOnClickListener {
            TRACKING.logEvent(Tracking.OS_SETTINGS, null)
//            val intent = Intent(Settings.ACTION_SETTINGS)
//            startActivityForResult(intent, 0)
            val dialogIntent = Intent(Settings.ACTION_SETTINGS)
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(dialogIntent)
        }
    }
    //endregion

    //region View Methods
    private fun signOut() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_message)
            .setPositiveButton(R.string.confirm_message) { _: DialogInterface, _: Int ->
                Constants.TRACKING.logEvent(Tracking.LOGOUT, null)
//                viewModel.signOut()
                toast(R.string.logout_success)
//                ManagerAuth.checkUserStatus()
                startActivity(intentFor<MainScreen>().newTask().clearTask())
            }
            .setNegativeButton(R.string.cancel_message) { _: DialogInterface, _: Int ->
            }
            .show()
    }

    private fun String.showToast() {
        toast(this)
    }

    private fun MaterialTextView.removeLinksUnderline() {
        val spannable = SpannableString(text)
        for (u in spannable.getSpans(0, spannable.length, URLSpan::class.java)) {
            spannable.setSpan(object : URLSpan(u.url) {
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            }, spannable.getSpanStart(u), spannable.getSpanEnd(u), 0)
        }
        text = spannable
    }
    //endregion

    //region Navigation
    private fun navigate(destination: Int, extra: Bundle? = null) {
        Navigation
            .findNavController(this.requireView())
            .navigate(destination, extra)
    }
    //endregion
}