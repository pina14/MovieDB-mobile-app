package pt.isel.pdm.yamda.persistentData

import android.content.ContentResolver
import android.net.Uri
import needle.Needle
import needle.UiRelatedTask
import pt.isel.pdm.yamda.model.ComingSoonList
import pt.isel.pdm.yamda.model.Movie
import pt.isel.pdm.yamda.model.MoviesList

object AccessDB {

    fun getMovie(contentResolver: ContentResolver, uri: Uri,projection: Array<String>?, selection: String?,
                 selectionArgs: Array<String>?, sortOrder: String?, func: (Movie?) -> Unit){
        Needle.onBackgroundThread().execute(object : UiRelatedTask<Movie?>(){

            override fun doWork(): Movie? {
                val cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
                if(cursor != null && cursor.moveToFirst()) {
                    val movie = cursor.toMovie()
                    cursor.close()
                    return movie
                }
                cursor.close()
                return null
            }

            override fun thenDoUiRelatedWork(result: Movie?) {
                func.invoke(result)
            }
        })
    }

    fun getMovies(contentResolver: ContentResolver, uri: Uri, projection: Array<String>?, selection: String?,
                  selectionArgs: Array<String>?, sortOrder: String?, listClass: Class<*>?, func: (MoviesList) -> Unit){
        Needle.onBackgroundThread().execute(object : UiRelatedTask<MoviesList>(){

            override fun doWork(): MoviesList {
                val cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)

                if(cursor != null) {
                    val movieList = cursor.toMovieList()
                    cursor.close()
                    if(listClass == ComingSoonList::class.java)
                        return ComingSoonList(movieList,-1,1)
                    return MoviesList(movieList,-1,1)
                }
                return MoviesList(ArrayList(),-1,1)
            }

            override fun thenDoUiRelatedWork(result: MoviesList) {
                func.invoke(result)
            }
        })
    }

    fun insertMovie(contentResolver: ContentResolver, uri: Uri, movie: Movie?){
        Needle.onBackgroundThread().execute{
            if(movie != null)
                contentResolver.insert(uri, movie.toContentValues())
        }
    }

    fun deleteMovie(contentResolver: ContentResolver, uri: Uri, selection: String?, selectionArgs: Array<String>?){
        Needle.onBackgroundThread().execute{
            contentResolver.delete(uri, selection, selectionArgs)
        }
    }

    fun deleteAllMovies(contentResolver: ContentResolver, uri: Uri){
        Needle.onBackgroundThread().execute{
            contentResolver.delete(uri, null, null)
        }
    }
}