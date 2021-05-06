package com.myfzone_sport.myf_zone.fragments.affiliation.affiliation_club_list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.CardClubListBinding
import com.myfzone_sport.myf_zone.databinding.FragmentAffiliationClubListBinding
import com.myfzone_sport.myf_zone.fragments.affiliation.affiliation_club_list.AffiliationClubListService.getImageReference
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.model.club.Club

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AffiliationClubListFragment : Fragment() {
    companion object {
        private val TAG = AffiliationClubListFragment::class.java.simpleName
        private var adapter: FirestoreRecyclerAdapter<Club, ClubHolder>? = null

        private lateinit var binding: FragmentAffiliationClubListBinding
        private lateinit var viewModel: AffiliationClubListViewModel
    }

    class ClubHolder(val binding: CardClubListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(club: Club) {
            with(binding) {
                binding.club = club

                try {
                    GlideApp.with(itemView).apply {
                        load(getImageReference(club.logo))
                            .placeholder(R.drawable.ic_account)
                            .centerCrop()
                            .into(binding.clubImage)
                    }
                } catch (e: Exception) {
                    Log.e("Holder", "Image could not load: $e")
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ClubHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    CardClubListBinding.inflate(layoutInflater, parent, false)
                return ClubHolder(binding)
            }
        }
    }

    private var param1: String? = null
    private var param2: String? = null

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        viewModel = ViewModelProvider(this).get(AffiliationClubListViewModel::class.java)

        val recyclerOptions = FirestoreRecyclerOptions.Builder<Club>()
            .setQuery(viewModel.getQuery().orderBy("name"), Club::class.java)
            .setLifecycleOwner(this)
            .build()

        adapter = object :
            FirestoreRecyclerAdapter<Club, ClubHolder>(recyclerOptions) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClubHolder {
                return ClubHolder.from(parent)
            }

            override fun onBindViewHolder(
                holder: ClubHolder,
                position: Int,
                model: Club
            ) {
                if (model.name == "MFZ Guest" || model.name == "My F-Zone" || model.name == "Los Angeles Galaxy") {
//                    toast("01")
                } else {
                    holder.bind(model)
                }
            }

            override fun onDataChanged() {

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_affiliation_club_list,
            container,
            false
        )

        binding.apply {
            lifecycleOwner = this@AffiliationClubListFragment
            setupRecyclerParameters()
            executePendingBindings()
        }

        return binding.root
    }
    //endregion

    //region RecyclerView
    private fun setupRecyclerParameters() {
        binding.clubList.setHasFixedSize(false)
        binding.clubList.layoutManager = LinearLayoutManager(requireContext())
        binding.clubList.adapter = adapter
    }
    //endregion RecyclerView

    //region Loading
    private fun showProgressBar() {
        binding.affiliationProgressBar.apply {
            visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar() {
        binding.affiliationProgressBar.apply {
            visibility = View.GONE
        }
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