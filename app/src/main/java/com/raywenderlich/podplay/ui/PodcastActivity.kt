package com.raywenderlich.podplay.ui

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.repository.ItunesRepo
import com.raywenderlich.podplay.service.ItunesService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.raywenderlich.podplay.adapter.PodcastListAdapter
import com.raywenderlich.podplay.databinding.ActivityPodcastBinding
import com.raywenderlich.podplay.repository.PodcastRepo
import com.raywenderlich.podplay.viewmodel.PodcastViewModel
import com.raywenderlich.podplay.viewmodel.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PodcastActivity : AppCompatActivity(), PodcastListAdapter.PodcastListAdapterListener {

    val TAG=javaClass.simpleName
    private val searchViewModel by viewModels<SearchViewModel>()
    private val podcastViewModel by viewModels<PodcastViewModel>()
    private lateinit var podcastListAdapter: PodcastListAdapter
    private lateinit var searchMenuItem: MenuItem
    private lateinit var databinding:ActivityPodcastBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databinding= ActivityPodcastBinding.inflate(layoutInflater)
        setContentView(databinding.root)

        //val itunesService=ItunesService.instance
        //val itunesRepo=ItunesRepo(itunesService)

        //GlobalScope.launch {
        //    val results = itunesRepo.searchByTerm("Android Developer")
        //    Log.i(TAG, "Results=${results.body()}")
        //}
        setupToolbar()
        setupViewModels()
        updateControls()
        handleIntent(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater=menuInflater
        inflater.inflate(R.menu.menu_search, menu)

        searchMenuItem = menu!!.findItem(R.id.search_item)
        val searchView=searchMenuItem.actionView as SearchView

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        if(supportFragmentManager.backStackEntryCount>0){
            databinding.podcastRecyclerView.visibility=View.INVISIBLE
        }

        if(databinding.podcastRecyclerView.visibility == View.INVISIBLE){
            searchMenuItem.isVisible=false
        }
        return true
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onShowDetails(podcastSummaryViewData: SearchViewModel.PodcastSummaryViewData) {
        val feedUrl = podcastSummaryViewData.feedUrl ?: return

        showProgressBar()

        val podcast = podcastViewModel.getPodcast(podcastSummaryViewData)
        hideProgressBar()
        if(podcast != null){
            showDetailsFragment()
        }else{
            showError("Error loading feed $feedUrl")
        }
    }

    private fun performSearch(term:String){
        showProgressBar()
        GlobalScope.launch {
            val results = searchViewModel.searchPodcasts(term)
            withContext(Dispatchers.Main){
                hideProgressBar()
                databinding.toolbar.title=term
                podcastListAdapter.setSearchData(results)
            }
        }
    }

    private fun handleIntent(intent: Intent){
        if(Intent.ACTION_SEARCH == intent.action){
            val query = intent.getStringExtra(SearchManager.QUERY) ?:
        return
            performSearch(query)
        }
    }

    private fun setupToolbar(){
        setSupportActionBar(databinding.toolbar)
    }

    private fun setupViewModels(){
        val service = ItunesService.instance
        searchViewModel.itunesRepo= ItunesRepo(service)
        podcastViewModel.podcastRepo= PodcastRepo()
    }

    private fun addBackStackListner(){
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0){
                databinding.podcastRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun updateControls() {
        databinding.podcastRecyclerView.setHasFixedSize(true)

        val layoutManager=LinearLayoutManager(this)
        databinding.podcastRecyclerView.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(
            databinding.podcastRecyclerView.context, layoutManager.orientation)

        databinding.podcastRecyclerView.addItemDecoration(dividerItemDecoration)

        podcastListAdapter = PodcastListAdapter(emptyList(), this, this)
        databinding.podcastRecyclerView.adapter = podcastListAdapter
    }

    private fun showDetailsFragment(){
        val podcastDetailsFragment = createPodcastDetailsFragment()

        supportFragmentManager.beginTransaction().add(
            R.id.podcastDetailsContainer, podcastDetailsFragment, TAG_DETAILS_FRAGMENT)
            .addToBackStack("DetailsFragment").commit()

        databinding.podcastRecyclerView.visibility = View.INVISIBLE

        searchMenuItem.isVisible = false
    }

    private fun createPodcastDetailsFragment():PodcastDetailsFragment{
        var podcastDetailsFragment = supportFragmentManager
            .findFragmentByTag(TAG_DETAILS_FRAGMENT) as PodcastDetailsFragment?

        if(podcastDetailsFragment==null){
            podcastDetailsFragment = PodcastDetailsFragment.newInstance()
        }
        return podcastDetailsFragment
    }

    private fun showProgressBar(){
        databinding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar(){
        databinding.progressBar.visibility= View.INVISIBLE
    }

    private fun showError(message:String){
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok_button),null)
            .create()
            .show()
    }

    companion object{

        private const val TAG_DETAILS_FRAGMENT = "DetailsFragment"
    }
}