package com.myfzone_sport.myf_zone.app.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.StorageReference
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.activity.MainActivity
import com.myfzone_sport.myf_zone.app.ui.viewmodel.AffiliationSuccessViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.AffiliationSuccessViewModelFactory
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.FragmentAffiliationSuccess2Binding
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.usecases.affiliation.GetClubIdUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.newTask
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast

private const val ARG_PARAM1 = "clubId"

class AffiliationSuccessFragment : Fragment() {

    //region Variables
    companion object {
        private lateinit var binding: FragmentAffiliationSuccess2Binding
        private lateinit var viewModel: AffiliationSuccessViewModel
        private lateinit var viewModelFactory: AffiliationSuccessViewModelFactory
        private var clubId: String? = null
    }
    //endregion

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            clubId = it.getString(ARG_PARAM1)
        }

        setupViewModel()

        viewModel.assignClubId(clubId!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_affiliation_success2,
            container,
            false
        )

        setupViews()
        setupObservers()

        return binding.root
    }
    //endregion

    //region Setups
    private fun setupViewModel() {
        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(remoteDataSource)

        val getClubIdUseCase = GetClubIdUseCase(repository)
        val getImageReferenceUseCase = GetImageReferenceUseCase(repository)

        viewModelFactory = AffiliationSuccessViewModelFactory(
            getClubIdUseCase,
            getImageReferenceUseCase
        )

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(AffiliationSuccessViewModel::class.java)
    }

    private fun setupViews() {
        binding.exit.setOnClickListener { requireActivity().onBackPressed() }

        binding.settings.setOnClickListener {
            navigate(R.id.affiliationSuccessToSettings)
        }

        binding.successNotification.setOnClickListener { }

        binding.successSkipBtn.setOnClickListener {
            startActivity(intentFor<MainActivity>().newTask().clearTask())
        }

        viewModel.clubImagePath.observe(viewLifecycleOwner, {
            toast("path: $it")
            displayClubImage(it)
        })
    }

    private fun setupObservers() {
        viewModel.errorMessage.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) showError()
        })

        viewModel.isLoading.observe(viewLifecycleOwner, { contentIsLoading ->
            if (contentIsLoading) loadingStart() else loadingStop()
        })
    }
    //endregion

    //region Club
    private fun displayClubImage(path: StorageReference) {
        GlideApp.with(this).apply {
            load(path)
                .centerCrop()
                .into(binding.clubImage)
        }
    }
    //endregion

    //region Navigation
    private fun navigateWithView(destination: Int, extra: Bundle? = null, view: View) {
        Navigation
            .findNavController(view)
            .navigate(destination, extra)
    }

    private fun navigate(destination: Int, extra: Bundle? = null) {
        Navigation
            .findNavController(this.requireView())
            .navigate(destination, extra)
    }
    //endregion

    //region View Methods
    private fun loadingStart() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun loadingStop() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun showError(message: String? = "") {

        Snackbar.make(
            binding.progressBar,
            if (!message.isNullOrEmpty()) message else viewModel.errorMessage.value.toString(),
            Snackbar.LENGTH_LONG
        )
            .setTextColor(Color.WHITE)
            .setActionTextColor(Color.CYAN)
            .show()
    }
    //endregion
}