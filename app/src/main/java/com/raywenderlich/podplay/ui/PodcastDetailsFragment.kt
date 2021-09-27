package com.raywenderlich.podplay.ui

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.adapter.EpisodeListAdapter
import com.raywenderlich.podplay.databinding.FragmentPodcastDetailsBinding
import com.raywenderlich.podplay.viewmodel.PodcastViewModel

class PodcastDetailsFragment : Fragment() {

    private val podcastViewModel: PodcastViewModel by activityViewModels()
    private lateinit var databinding:FragmentPodcastDetailsBinding
    private lateinit var episodeListAdapter: EpisodeListAdapter

    companion object{
        fun newInstance(): PodcastDetailsFragment{
            return PodcastDetailsFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container:ViewGroup?,
                              savedInstanceState:Bundle?): View {
        databinding=FragmentPodcastDetailsBinding.inflate(inflater, container, false)
        return databinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        podcastViewModel.podcastLiveData.observe(viewLifecycleOwner, {viewData ->
            if (viewData != null){
                databinding.feedTitleTextView.text = viewData.feedTitle
                databinding.feedDescTextView.text = viewData.feedDesc
                activity?.let {activity -> Glide.with(activity).load(viewData.imageUrl)
                    .into(databinding.feedImageView)}
                databinding.feedDescTextView.movementMethod = ScrollingMovementMethod()
                databinding.episodeRecycleView.setHasFixedSize(true)

                val layoutManager = LinearLayoutManager(activity)
                databinding.episodeRecycleView.layoutManager = layoutManager

                val dividerItemDecoration = DividerItemDecoration(databinding.episodeRecycleView.context, layoutManager.orientation)
                databinding.episodeRecycleView.addItemDecoration(dividerItemDecoration)

                episodeListAdapter = EpisodeListAdapter(viewData.episodes)
                databinding.episodeRecycleView.adapter = episodeListAdapter
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_details, menu)
    }

    //private fun updateControls(){
        //val viewData = podcastViewModel.activePodcastViewData ?: return
        //databinding.feedTitleTextView.text=viewData.feedTitle
        //databinding.feedDescTextView.text = viewData.feedDesc
        //activity?.let {activity ->
                //Glide.with(activity).load(viewData.imageUrl).into(databinding.feedImageView)
           // }
   // }
}