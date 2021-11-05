package com.myfzone_sport.myf_zone.app.ui.fragment

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.adapter.AffiliationCategoryAdapter
import com.myfzone_sport.myf_zone.app.ui.adapter.AffiliationClubAdapter
import com.myfzone_sport.myf_zone.app.ui.adapter.AffiliationSportAdapter
import com.myfzone_sport.myf_zone.app.ui.adapter.AffiliationSubCategoryAdapter
import com.myfzone_sport.myf_zone.app.ui.viewmodel.AffiliationRequestListViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.AffiliationRequestListViewModelFactory
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.FragmentAffiliationRequestListBinding
import com.myfzone_sport.myf_zone.usecases.affiliation.GetCategoryListUseCase
import com.myfzone_sport.myf_zone.usecases.affiliation.GetClubListUseCase
import com.myfzone_sport.myf_zone.usecases.affiliation.GetSportListUseCase
import com.myfzone_sport.myf_zone.usecases.affiliation.GetSubCategoryListUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase
import com.myfzone_sport.myf_zone.domain.club.Club
import com.myfzone_sport.myf_zone.domain.sport.Category
import com.myfzone_sport.myf_zone.domain.sport.Sport
import com.myfzone_sport.myf_zone.domain.sport.SubCategory


class AffiliationRequestListFragment : Fragment() {

    //region Variables
    companion object {
        private lateinit var binding: FragmentAffiliationRequestListBinding
        private lateinit var viewModel: AffiliationRequestListViewModel
        private lateinit var viewModelFactory: AffiliationRequestListViewModelFactory

        private var clubFirstPass = true
        private var sportFirstPass = true
        private var categoryFirstPass = true
        private var subCategoryFirstPass = true
    }
    //endregion

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_affiliation_request_list,
            container,
            false
        )

        setupViews()
        setupObservers()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    //endregion

    //region Setups
    private fun setupViewModel() {
        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(remoteDataSource)

        val getSportListUseCase = GetSportListUseCase(repository)
        val getClubListUseCase = GetClubListUseCase(repository)
        val getCategoryListUseCase = GetCategoryListUseCase(repository)
        val getSubCategoryListUseCase = GetSubCategoryListUseCase(repository)

        viewModelFactory = AffiliationRequestListViewModelFactory(
            getSportListUseCase,
            getClubListUseCase,
            getCategoryListUseCase,
            getSubCategoryListUseCase
        )

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(AffiliationRequestListViewModel::class.java)
    }

    private fun setupViews() {
        binding.codeAffiliationLink.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        binding.exitAffiliationRequest.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.settings.setOnClickListener {
            navigate(R.id.affiliationRequestToSettings)
        }

        binding.codeAffiliationLink.setOnClickListener {
            navigate(R.id.affiliationRequestToAffiliationCode)
        }

        viewModel.club.observe(viewLifecycleOwner, { club ->
            binding.affiliationBtn.setOnClickListener {
                val bundle = bundleOf("clubId" to club.id)
                navigate(R.id.affiliationRequestToAffiliationSuccess, bundle)
            }
        })

        binding.subCategorySpinner.visibility = View.GONE

        clubSpinner()
        sportSpinner()
        categorySpinner()
//        subCategorySpinner()
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

    //region Spinners
    private fun clubSpinner() {
        viewModel.clubList.observe(viewLifecycleOwner, { clubList ->
            if (clubList.isEmpty()) {
                binding.clubSpinner.visibility = View.GONE
            } else {
                val remoteDataSource = RemoteDataSourceImpl()
                val repository = RepositoryImpl(remoteDataSource)

                val getImageReferenceUseCase = GetImageReferenceUseCase(repository)

                val adapter = AffiliationClubAdapter(requireContext(), clubList, getImageReferenceUseCase)
                binding.clubSpinner.adapter = adapter

                binding.clubSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(p0: AdapterView<*>?) {
                        }

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            val club = parent?.getItemAtPosition(position) as Club
//                            viewModel.getClubItem(club.name, clubList)
                            viewModel.assignClub(club)
                        }
                    }
            }
        })
    }

    private fun sportSpinner() {
        viewModel.sportList.observe(viewLifecycleOwner, { sportList ->
            if (sportList.isEmpty()) {
                binding.sportSpinner.visibility = View.GONE
            } else {
                val adapter = AffiliationSportAdapter(requireContext(), sportList)
                binding.sportSpinner.adapter = adapter

                binding.sportSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(p0: AdapterView<*>?) {
                        }

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            val sport = parent?.getItemAtPosition(position) as Sport
//                            viewModel.getSportItem(sport.name, sportList)
                            viewModel.assignSport(sport)
                        }
                    }
            }
        })
    }

    private fun categorySpinner() {
        viewModel.categoryList.observe(viewLifecycleOwner, { categoryList ->
            if (categoryList.isEmpty()) {
                binding.categorySpinner.visibility = View.GONE
            } else {
                val adapter = AffiliationCategoryAdapter(requireContext(), categoryList)
                binding.categorySpinner.adapter = adapter

                binding.categorySpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(p0: AdapterView<*>?) {
                        }

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            val category = parent?.getItemAtPosition(position) as Category
//                            viewModel.getCategoryItem(category.name, categoryList)
                            viewModel.assignCategory(category)
                        }
                    }
            }
        })
    }

    private fun subCategorySpinner() {
        viewModel.subCategoryList.observe(viewLifecycleOwner, { subCategoryList ->
            if (subCategoryList.isEmpty()) {
                binding.subCategorySpinner.visibility = View.GONE
            } else {
                val adapter = AffiliationSubCategoryAdapter(requireContext(), subCategoryList)
                binding.subCategorySpinner.adapter = adapter

                binding.subCategorySpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(p0: AdapterView<*>?) {
                        }

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            val subCategory = parent?.getItemAtPosition(position) as SubCategory
                            viewModel.assignSubCategory(subCategory)
                        }
                    }
            }
        })
    }
    //endregion

    //region Affiliation
    //endregion

    //region Navigation
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
            binding.exitAffiliationRequest,
            if (!message.isNullOrEmpty()) message else viewModel.errorMessage.value.toString(),
            Snackbar.LENGTH_LONG
        )
            .setTextColor(Color.WHITE)
            .setActionTextColor(Color.CYAN)
            .show()
    }
    //endregion
}