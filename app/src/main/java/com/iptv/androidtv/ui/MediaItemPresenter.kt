package com.iptv.androidtv.ui

import android.graphics.Color
import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.iptv.androidtv.data.Channel
import com.iptv.androidtv.data.MediaItem
import com.iptv.androidtv.data.Movie
import com.iptv.androidtv.R

class MediaItemPresenter : Presenter() {
    
    companion object {
        private const val CARD_WIDTH = 313
        private const val CARD_HEIGHT = 176
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = object : ImageCardView(parent.context) {
            override fun setSelected(selected: Boolean) {
                updateCardBackgroundColor(this, selected)
                super.setSelected(selected)
            }
        }

        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        updateCardBackgroundColor(cardView, false)
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val mediaItem = item as MediaItem
        val cardView = viewHolder.view as ImageCardView

        cardView.titleText = mediaItem.getDisplayTitle()
        cardView.contentText = mediaItem.getDisplaySubtitle()
        cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)

        val imageUrl = mediaItem.getImageUrl()
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(viewHolder.view.context)
                .load(imageUrl)
                .centerCrop()
                .error(getDefaultImage(mediaItem))
                .into(cardView.mainImageView)
        } else {
            cardView.mainImage = viewHolder.view.context.getDrawable(getDefaultImage(mediaItem))
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        
        // Remove references to drawables and bitmaps to allow GC
        cardView.badgeImage = null
        cardView.mainImage = null
    }

    private fun updateCardBackgroundColor(view: ImageCardView, selected: Boolean) {
        val color = if (selected) {
            view.context.getColor(R.color.accent_color)
        } else {
            view.context.getColor(R.color.card_background)
        }
        
        // Set the background color
        view.setBackgroundColor(color)
        view.findViewById<ViewGroup>(androidx.leanback.R.id.info_field)?.setBackgroundColor(color)
    }

    private fun getDefaultImage(mediaItem: MediaItem): Int {
        return when (mediaItem) {
            is Channel -> R.drawable.ic_tv_default
            is Movie -> R.drawable.ic_movie_default
            else -> R.drawable.ic_media_default
        }
    }
}