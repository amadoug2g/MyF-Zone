package mfz.myfzone_sport.myf_zone.fragments.user_sign.login

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.databinding.FragmentLoginBinding
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.screens.MainScreen
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.newTask
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast


class LoginFragment : Fragment() {
    companion object {
        private val TAG = LoginFragment::class.java.simpleName
        private lateinit var viewModel: LoginModelView
        private lateinit var binding: FragmentLoginBinding
    }

    //region Override Methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_login,
            container,
            false
        )

        viewModel = ViewModelProvider(this).get(LoginModelView::class.java)

        binding.apply {
            lifecycleOwner = this@LoginFragment
            executePendingBindings()
        }

        binding.loginRegister.setOnClickListener {

            val extras = FragmentNavigatorExtras(
                binding.loginEmailLayout to "email_transition_field",
                binding.loginPasswordLayout to "password_transition_field",
                binding.loginButton to "button_transition"
            )

            navigate(R.id.loginToSignUp, extras)
            resetFields()

            sharedElementEnterTransition = TransitionInflater.from(requireContext())
                .inflateTransition(R.transition.change_image_transform)
        }

        binding.loginButton.setOnClickListener {
            signInProcess()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FragmentNavigator.Extras.Builder()
            .addSharedElement(sign_up_email_layout, "email_transition_field")
            .addSharedElement(sign_up_password_layout, "password_transition_field")
            .addSharedElement(sign_up_button, "button_transition")
            .build()
    }

    override fun onStop() {
        super.onStop()
        binding.loginEmailInput.hideKeyboard()
    }
    //endregion

    private suspend fun signInUser() {
        viewModel.signInUser(
            binding.loginEmailInput.text.toString(),
            binding.loginPasswordInput.text.toString()
        ).collect { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
                    hideProgressBar()
                    Log.i(TAG, "${state.data}")
                    toast(getString(R.string.login_message))
                    startActivity(intentFor<MainScreen>().newTask().clearTask())
                }
                is State.Failed -> {
                    hideProgressBar()
                    Log.d(TAG, "signInUserWithEmail:failed: " + state.message)
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.sign_in_error)
                        .setMessage(state.message)
                        .setPositiveButton(getString(R.string.confirm_message)) { _: DialogInterface, _: Int ->
                        }
                        .show()
                }
            }
        }
    }

    private fun signInProcess() {
        if (validateForm())
            lifecycleScope.launch {
                signInUser()
            }
    }

    private fun resetFields() {
        binding.loginEmailLayout.error = null
        binding.loginPasswordLayout.error = null
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = binding.loginEmailInput.text.toString()
        val password = binding.loginPasswordInput.text.toString()

        Log.i(TAG, "Credentials:\nemail is $email\npassword is $password")

        val emailLayout = binding.loginEmailLayout
        val passwordLayout = binding.loginPasswordLayout

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

        return valid
    }

    //region Loading
    private fun showProgressBar() {
        binding.loginProgressBar.apply {
            visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar() {
        binding.loginProgressBar.apply {
            visibility = View.GONE
        }
    }
    //endregion

    //region View Methods
    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
    //endregion

    //region Navigation
    private fun navigate(destination: Int, extra: FragmentNavigator.Extras? = null) {
        findNavController().navigate(destination, null, null, extra)
    }
    //endregion
}