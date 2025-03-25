package com.meter_alc_rgb.gpstrecker.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.meter_alc_rgb.gpstrecker.R
import com.meter_alc_rgb.gpstrecker.database.TrackAdapter
import com.meter_alc_rgb.gpstrecker.database.TrackItem
import com.meter_alc_rgb.gpstrecker.databinding.FragmentTrackListBinding
import com.meter_alc_rgb.gpstrecker.main.MainApp
import com.meter_alc_rgb.gpstrecker.main.MyViewModel
import com.meter_alc_rgb.gpstrecker.utils.TestTrackList

class TrackListFragment : BaseFragment("track"), TrackAdapter.Listener {
   private lateinit var binding: FragmentTrackListBinding
   private lateinit var adapter: TrackAdapter
   private val model: MyViewModel by activityViewModels{
       MyViewModel.MainViewModelFactory((context?.applicationContext as MainApp).database)
   }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrackListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
        trackUpdate()
    }

    private fun trackUpdate(){
        model.liveDataTracks.observe(viewLifecycleOwner){
            binding.tvEmpty.visibility = if(it.isEmpty()) View.VISIBLE else View.GONE
            adapter.submitList(it)
        }
    }

    private fun initRcView() = with(binding){
        adapter = TrackAdapter(this@TrackListFragment)
        rcView.layoutManager = LinearLayoutManager(context)
        rcView.adapter = adapter
    }

    companion object {
        @JvmStatic
        fun newInstance() = TrackListFragment()
    }

    override fun onClick(item: TrackItem, type: TrackAdapter.ClickType) {
        if(type == TrackAdapter.ClickType.DELETE){
            model.deleteTrack(item)
        } else if(type == TrackAdapter.ClickType.OPEN){
            model.liveDataTrackItem.value = item
            FragmentManager.setFragment(
                ViewTrackFragment.newInstance(),
                activity as AppCompatActivity)
        }
    }
}