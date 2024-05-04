package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener

class ElectionsFragment : Fragment() {
    private val viewModel: ElectionsViewModel by lazy {
        val application = requireNotNull(this.activity).application
        val viewModelFactory = ElectionsViewModelFactory(application)
        ViewModelProvider(this, viewModelFactory)[ElectionsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentElectionBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_election, container, false
        )

        binding.electionViewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.getSavedElection()

        val upcomingAdapter = ElectionListAdapter(ElectionListener { election ->
            viewModel.onElectionClicked(election)
        })
        binding.upcomingElectionRecycler.adapter = upcomingAdapter
        viewModel.upcomingElections.observe(viewLifecycleOwner) {
            it?.let { upcomingAdapter.submitList(it) }
        }

        val savedAdapter = ElectionListAdapter(ElectionListener {
            viewModel.onElectionClicked(it)
        })
        binding.saveElectionRecycler.adapter = savedAdapter
        viewModel.savedElections.observe(viewLifecycleOwner) {
            it?.let { savedAdapter.submitList(it) }
        }

        viewModel.navigateVoterInfo.observe(viewLifecycleOwner) {
            it?.let {
                Navigation.findNavController(requireView()).navigate(
                    ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(
                        it.id,
                        it.division
                    )
                )
                viewModel.onNavigated()
            }
        }

        return binding.root
    }
}