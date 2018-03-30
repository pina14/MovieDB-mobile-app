package pt.isel.pdm.yamda.persistentData

import android.content.ContentValues
import android.database.Cursor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pt.isel.pdm.yamda.model.Movie
import pt.isel.pdm.yamda.model.MoviesList

/**
 * Extension function that maps a given [Movie] to the corresponding URI and
 * [ContentValues] pair.
 * @param [movie] The instance bearing the movie information
 * @return The newly created [ContentValues] instance
 */
fun Movie.toContentValues(): ContentValues {
    val result = ContentValues()

    //parse genres to string
    val genresText = Gson().toJson(genres)

    with (YAMDAContentProvider) {
        result.put(COLUMN_ID, id)
        result.put(COLUMN_OVERVIEW, overview)
        result.put(COLUMN_ORIGINAL_TITLE, original_title)
        result.put(COLUMN_POSTER, poster_path)
        result.put(COLUMN_VOTE_AVERAGE, vote_average)
        result.put(COLUMN_GENRES, genresText)
        result.put(COLUMN_RUNTIME, runtime)
        result.put(COLUMN_RELEASE_DATE, release_date)
    }

    return result
}

/**
 * Extension function that maps a given [GeneralList] to the corresponding iterable of
 * [ContentValues].
 * @param [movie] The instance bearing the list of movie information
 * @return The newly created iterable of [ContentValues]
 */
fun MoviesList.toContentValues() : Array<ContentValues> =
        results.map(Movie::toContentValues).toTypedArray()

/**
 * Extension function that builds a [Movie] instance from the given [Cursor]
 * @param [cursor] The cursor pointing to the movie item data
 * @return The newly created [Movie]
 */
fun Cursor.toMovie(): Movie {
    //parse genresText to ArrayList<Genre>
    val genresText = getString(YAMDAContentProvider.COLUMN_GENRES_IDX)
    val genres = Gson().fromJson<ArrayList<Movie.Genre>>(genresText,  object : TypeToken<ArrayList<Movie.Genre>>() {}.type)

    with (YAMDAContentProvider) {
        return Movie(
                original_title = getString(COLUMN_ORIGINAL_TITLE_IDX),
                poster_path = getString(COLUMN_POSTER_IDX),
                vote_average = getFloat(COLUMN_VOTE_AVERAGE_IDX),
                genres = genres,
                runtime = getInt(COLUMN_RUNTIME_IDX),
                release_date = getString(COLUMN_RELEASE_DATE_IDX),
                overview = getString(COLUMN_OVERVIEW_IDX),
                id = getString(COLUMN_ID_IDX)
        )
    }
}

/**
 * Extension function that builds a list of [Movie] from the given [Cursor]
 * @param [cursor] The cursor bearing the set of movie items
 * @return The newly created list of [Movie]
 */
fun Cursor.toMovieList(): ArrayList<Movie> {
    val list = arrayListOf<Movie>()

    moveToFirst()
    while (!isAfterLast) {
        // The Cursor is now set to the right position
        list.add(toMovie())
        moveToNext()
    }

    return list
}