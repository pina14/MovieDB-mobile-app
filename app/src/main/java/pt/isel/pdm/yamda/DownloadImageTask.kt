package pt.isel.pdm.yamda

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

/**
 * Downloads an image resource in background and sets it to a holder
 */
object DownloadImageTask{

    private val urlPrefix = "http://image.tmdb.org/t/p/w185"

    fun execute(holder: MovieListViewHolder?, poster_path:String?, bmImage: ImageView, ctx:Context) {
        val url = if(poster_path == null)
                            App.getDefaultImageURL()
                         else
                            urlPrefix+poster_path

        if(holder == null || holder.poster_path ==  poster_path) {
            var requestOptions = RequestOptions()
            requestOptions = requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)

            Glide.with(ctx)
                    .load(url)
                    .apply(requestOptions)
                    .into(bmImage)
        }
    }
}
