package pt.isel.pdm.yamda.model

import android.content.ContentResolver
import android.view.View
import pt.isel.pdm.yamda.MovieListViewHolder
import pt.isel.pdm.yamda.R
import pt.isel.pdm.yamda.persistentData.AccessDB
import pt.isel.pdm.yamda.persistentData.YAMDAContentProvider

class ComingSoonList(results: ArrayList<Movie>, total_pages: Int, page: Int) : MoviesList(results, total_pages, page){
    fun switchFavouriteVisibility(holder: MovieListViewHolder, movie: Movie, contentResolver: ContentResolver) {
        //default visibility = gone, switch to visible
        holder.favoriteImageView.visibility = View.VISIBLE
        checkIfMovieInDB(holder, movie, contentResolver)

        holder.favoriteImageView.setOnClickListener {
            favouriteOnClickMethod(holder, movie, contentResolver)
        }
    }

    private fun checkIfMovieInDB(holder: MovieListViewHolder, movie: Movie, contentResolver: ContentResolver){
        AccessDB.getMovie(
                contentResolver,
                YAMDAContentProvider.FOLLOWING_CONTENT_URI,
                arrayOf("*"),
                "${YAMDAContentProvider.COLUMN_ID}=${movie.id}",
                null,
                null){
            val image = if (it == null)
                R.drawable.ic_not_favourite
            else
                R.drawable.ic_favourite_movie
            holder.favoriteImageView.setImageResource(image)
            holder.favoriteImageView.tag = image
        }
    }

    private fun favouriteOnClickMethod(holder: MovieListViewHolder, movie: Movie, contentResolver: ContentResolver){
        val lastImage: Int = holder.favoriteImageView.tag as Int
        val image: Int
        image = if (lastImage == R.drawable.ic_not_favourite) {
            AccessDB.insertMovie(contentResolver, YAMDAContentProvider.FOLLOWING_CONTENT_URI, movie)
            R.drawable.ic_favourite_movie
        } else {
            AccessDB.getMovie(contentResolver, YAMDAContentProvider.FOLLOWING_CONTENT_URI, arrayOf("*"),
                    "${YAMDAContentProvider.COLUMN_ID}=${movie.id}", null, null){
                AccessDB.deleteMovie(
                        contentResolver,
                        YAMDAContentProvider.FOLLOWING_CONTENT_URI,
                        "${YAMDAContentProvider.COLUMN_ID}=${it!!.id}",
                        null
                )
            }
            R.drawable.ic_not_favourite
        }
        holder.favoriteImageView.setImageResource(image)
        holder.favoriteImageView.tag = image
    }
}