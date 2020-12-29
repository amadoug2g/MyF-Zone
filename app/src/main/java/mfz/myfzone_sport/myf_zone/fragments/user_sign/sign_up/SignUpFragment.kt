package mfz.myfzone_sport.myf_zone.fragments.user_sign.sign_up

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.transition.ChangeBounds
import androidx.transition.TransitionInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.databinding.FragmentSignUpBinding
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.coach.Coach
import org.jetbrains.anko.support.v4.toast
import java.util.*

class SignUpFragment : Fragment() {
    companion object {
        private val TAG = SignUpFragment::class.java.simpleName

        private lateinit var viewModel: SignUpViewModel
        private lateinit var binding: FragmentSignUpBinding
    }

    //region Override Methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_sign_up,
            container,
            false
        )

        viewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)

        binding.apply {
            lifecycleOwner = this@SignUpFragment
            executePendingBindings()
        }

        binding.signUpButton.setOnClickListener {
            signUpProcess()
        }

        binding.loginRegister.setOnClickListener {

            val extras = FragmentNavigatorExtras(
                binding.signUpEmailLayout to "email_transition_field",
                binding.signUpPasswordLayout to "password_transition_field",
                binding.signUpButton to "button_transition"
            )

            navigate(R.id.signUpToLogin, extras)
            resetFields()

            sharedElementEnterTransition = TransitionInflater.from(requireContext())
                .inflateTransition(R.transition.change_image_transform)
        }

        sharedElementEnterTransition = ChangeBounds().apply {
            duration = 300
        }
        sharedElementReturnTransition = ChangeBounds().apply {
            duration = 300
        }

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                resetFields()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        binding.signUpFirstNameInput.hideKeyboard()
    }
    //endregion

    //region Sign Up
    private fun signUpProcess() {
        if (validateForm())
            lifecycleScope.launch {
                signUpUser()
            }
    }

    private suspend fun signUpUser() {
        viewModel.signUpUser(
            binding.signUpEmailInput.text.toString(),
            binding.signUpPasswordInput.text.toString()
        ).collect { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
                    hideProgressBar()
                    Log.i(TAG, "${state.data}")

                    val time = Calendar.getInstance().time
                    val user = FirebaseAuth.getInstance().currentUser

                    val coach = Coach(
                        user!!.uid,
                        binding.signUpEmailInput.text.toString(),
                        binding.signUpFirstNameInput.text.toString(),
                        binding.signUpLastNameInput.text.toString(), mutableListOf(), time
                    )

                    addUserToDB(coach)
                }
                is State.Failed -> {
                    hideProgressBar()
                    (state.message).errorDialog()
                }
            }
        }
    }

    private suspend fun addUserToDB(coach: Coach) {
        viewModel.addUserToDB(coach).collect { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
                    hideProgressBar()
                    assignDisplayName(state.data)
                }
                is State.Failed -> {
                    hideProgressBar()
                    (state.message).errorDialog()
                }
            }
        }
    }

    private suspend fun assignDisplayName(coach: Coach) {
        viewModel.assignDisplayName(coach).collect { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
                    hideProgressBar()
                    toast(getString(R.string.account_creation_msg))
//                    navigate(R.id.signUpToAffiliationRequest)
                    val bundle = bundleOf("page" to R.id.signUpFragment)
                    navigate(R.id.globalToAffiliation, bundle)
                }
                is State.Failed -> {
                    hideProgressBar()
                    (state.message).errorDialog()
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = binding.signUpEmailInput.text.toString()
        val password = binding.signUpPasswordInput.text.toString()
        val firstName = binding.signUpFirstNameInput.text.toString()
        val lastName = binding.signUpLastNameInput.text.toString()

        val emailLayout = binding.signUpEmailLayout
        val passwordLayout = binding.signUpPasswordLayout
        val firstNameLayout = binding.signUpFirstNameLayout
        val lastNameLayout = binding.signUpLastNameLayout

        if (TextUtils.isEmpty(email)) {
            emailLayout.error = getString(R.string.hint_required)
            valid = false
        } else {
            emailLayout.error = null
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.error = getString(R.string.hint_required)
            valid = false
        } else {
            passwordLayout.error = null
        }

        if (TextUtils.isEmpty(firstName)) {
            firstNameLayout.error = getString(R.string.hint_required)
            valid = false
        } else {
            firstNameLayout.error = null
        }

        if (TextUtils.isEmpty(lastName)) {
            lastNameLayout.error = getString(R.string.hint_required)
            valid = false
        } else {
            lastNameLayout.error = null
        }

        return valid
    }
    //endregion

    //region Navigation
    private fun navigate(destination: Int, extra: Bundle? = null) {
        Navigation
            .findNavController(this.requireView())
            .navigate(destination, extra)
    }

    private fun navigate(destination: Int, extra: FragmentNavigator.Extras? = null) {
        findNavController().navigate(destination, null, null, extra)
    }
    //endregion

    //region Loading
    private fun showProgressBar() {
        binding.signUpProgressBar.apply {
            visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar() {
        binding.signUpProgressBar.apply {
            visibility = View.GONE
        }
    }
    //endregion

    //region View Methods
    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun String.errorDialog() {
        Log.d(TAG, "signUpUserWithEmail:failed: $this")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.sign_up_error)
            .setMessage(this)
            .setPositiveButton(getString(R.string.confirm_message)) { _: DialogInterface, _: Int ->
            }
            .show()
    }

    private fun resetFields() {
        binding.signUpEmailLayout.error = null
        binding.signUpPasswordLayout.error = null
        binding.signUpFirstNameLayout.error = null
        binding.signUpLastNameLayout.error = null
    }
    //endregion
}