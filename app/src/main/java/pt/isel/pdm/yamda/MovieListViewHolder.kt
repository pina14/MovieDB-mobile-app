package pt.isel.pdm.yamda

import android.widget.ImageView
import android.widget.TextView

/**
 * Used to store the views in order to optimize the user experience and view access
 */
data class MovieListViewHolder(val nameTextView:TextView, val rateTextView:TextView, val imageImageView:ImageView, val favoriteImageView: ImageView){
    var poster_path:String? = null
}