package com.iptv.androidtv.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import com.iptv.androidtv.R
import com.iptv.androidtv.data.Channel
import com.iptv.androidtv.data.MediaCategory
import com.iptv.androidtv.data.Movie
import com.iptv.androidtv.integration.VLCIntegrationManager
import com.iptv.androidtv.security.CredentialManager
import com.iptv.androidtv.service.IPTVService
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private lateinit var credentialManager: CredentialManager
    private lateinit var iptvService: IPTVService
    private lateinit var vlcManager: VLCIntegrationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        credentialManager = CredentialManager(this)
        iptvService = IPTVService()
        vlcManager = VLCIntegrationManager(this)

        // Check if credentials exist
        if (!credentialManager.hasCredentials()) {
            startSetupActivity()
            return
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, MainFragment())
                .commit()
        }
    }

    private fun startSetupActivity() {
        val intent = Intent(this, SetupActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        iptvService.close()
    }

    class MainFragment : BrowseSupportFragment() {
        
        private lateinit var rowsAdapter: ArrayObjectAdapter
        private lateinit var credentialManager: CredentialManager
        private lateinit var iptvService: IPTVService
        private lateinit var vlcManager: VLCIntegrationManager

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            
            credentialManager = CredentialManager(requireContext())
            iptvService = IPTVService()
            vlcManager = VLCIntegrationManager(requireContext())

            setupUI()
            loadContent()
        }

        private fun setupUI() {
            title = getString(R.string.app_name)
            headersState = HEADERS_ENABLED
            isHeadersTransitionOnBackEnabled = true

            // Set up the adapter
            rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
            adapter = rowsAdapter

            // Set up click listeners
            onItemViewClickedListener = ItemViewClickedListener()
        }

        private fun loadContent() {
            val credentials = credentialManager.getCredentials()
            if (credentials == null) {
                // Navigate back to setup
                val intent = Intent(requireContext(), SetupActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
                return
            }

            lifecycleScope.launch {
                try {
                    when (val result = iptvService.getContent(credentials)) {
                        is IPTVService.ServiceResult.Success -> {
                            val content = result.data
                            setupRows(content.channels, content.movies)
                        }
                        is IPTVService.ServiceResult.Error -> {
                            // Handle error - show error message
                            showError(result.message)
                        }
                    }
                } catch (e: Exception) {
                    showError("Failed to load content: ${e.message}")
                }
            }
        }

        private fun setupRows(channels: List<Channel>, movies: List<Movie>) {
            rowsAdapter.clear()

            // Add TV Channels row
            if (channels.isNotEmpty()) {
                val channelsAdapter = ArrayObjectAdapter(MediaItemPresenter())
                channels.forEach { channelsAdapter.add(it) }
                
                val channelsHeader = HeaderItem(0, getString(R.string.tv_channels))
                val channelsRow = ListRow(channelsHeader, channelsAdapter)
                rowsAdapter.add(channelsRow)
            }

            // Add Movies row
            if (movies.isNotEmpty()) {
                val moviesAdapter = ArrayObjectAdapter(MediaItemPresenter())
                movies.forEach { moviesAdapter.add(it) }
                
                val moviesHeader = HeaderItem(1, getString(R.string.movies))
                val moviesRow = ListRow(moviesHeader, moviesAdapter)
                rowsAdapter.add(moviesRow)
            }
        }

        private fun showError(message: String) {
            // In a real implementation, show proper error UI
            // For now, we'll just add an error row
            val errorAdapter = ArrayObjectAdapter(MediaItemPresenter())
            val errorHeader = HeaderItem(999, "Error")
            val errorRow = ListRow(errorHeader, errorAdapter)
            rowsAdapter.add(errorRow)
        }

        private inner class ItemViewClickedListener : OnItemViewClickedListener {
            override fun onItemClicked(
                itemViewHolder: Presenter.ViewHolder?,
                item: Any?,
                rowViewHolder: RowPresenter.ViewHolder?,
                row: Row?
            ) {
                when (item) {
                    is Channel -> playChannel(item)
                    is Movie -> playMovie(item)
                }
            }
        }

        private fun playChannel(channel: Channel) {
            val credentials = credentialManager.getCredentials() ?: return
            val authenticatedUrl = iptvService.buildAuthenticatedStreamUrl(credentials, channel.streamUrl)
            
            vlcManager.launchStreamWithFeedback(authenticatedUrl, channel.name)
        }

        private fun playMovie(movie: Movie) {
            val credentials = credentialManager.getCredentials() ?: return
            val authenticatedUrl = iptvService.buildAuthenticatedStreamUrl(credentials, movie.streamUrl)
            
            vlcManager.launchStreamWithFeedback(authenticatedUrl, movie.title)
        }
    }
}