package mfz.myfzone_sport.myf_zone.fragments.settings

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.databinding.FragmentSettingsBinding
import mfz.myfzone_sport.myf_zone.screens.MainScreen
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.newTask
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SettingsFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingsFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        private lateinit var binding: FragmentSettingsBinding
        private lateinit var viewModel: SettingsViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_settings,
            container,
            false
        )

        binding.logout.setOnClickListener { signOut() }

        binding.osSettings.setOnClickListener {
//            val intent = Intent(Settings.ACTION_SETTINGS)
//            startActivityForResult(intent, 0)
            val dialogIntent = Intent(Settings.ACTION_SETTINGS)
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(dialogIntent)
        }

        return binding.root
    }

    //region View Methods
    private fun signOut() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_message)
            .setPositiveButton(R.string.confirm_message) { _: DialogInterface, _: Int ->
                viewModel.signOut()
                toast(R.string.logout_success)
                startActivity(intentFor<MainScreen>().newTask().clearTask())
            }
            .setNegativeButton(R.string.cancel_message) { _: DialogInterface, _: Int ->
            }
            .show()
    }

    private fun showToast(string: String) {
        toast(string)
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