package pt.isel.pdm.yamda

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.movie_list_item.view.*
import pt.isel.pdm.yamda.model.ComingSoonList
import pt.isel.pdm.yamda.model.Movie
import pt.isel.pdm.yamda.model.MoviesList

/**
 * Adapter used to represent a Custom View (Custom Layouts of Movie ) in each ListItem of a ListView
 */
class MovieListAdapter(private val act:Activity, private val resource:Int, val movies:MoviesList?): ArrayAdapter<Movie>(act as Context, resource) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: MovieListViewHolder
        val view:View

        if (convertView == null) {
            view = act.layoutInflater.inflate(resource, parent, false) //false because we don't want to create the view right now
            holder = MovieListViewHolder(view.movie_name_value, view.rate_value, view.movie_image, view.favorite_image_view)
            view.tag = holder
        } else{
            holder = convertView.tag as MovieListViewHolder
            view = convertView
        }

        //get movie in position [position]
        val movie = movies!!.results[position]

        //configure values
        holder.nameTextView.text = movie.original_title
        holder.rateTextView.text = movie.vote_average.toString()

        if(movies is ComingSoonList)
            movies.switchFavouriteVisibility(holder, movie, act.contentResolver)

        //define poster_path value
        holder.poster_path = movie.poster_path
        DownloadImageTask.execute(holder, movie.poster_path, holder.imageImageView, act)

        return view
    }

    override fun getItem(position: Int): Movie = movies!!.results[position]

    override fun getItemId(position: Int): Long = 0

    override fun getCount(): Int = movies!!.results.size
}