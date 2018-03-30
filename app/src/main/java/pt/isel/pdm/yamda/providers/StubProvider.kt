package pt.isel.pdm.yamda.providers

import com.android.volley.VolleyError
import pt.isel.pdm.yamda.model.BaseModel
import pt.isel.pdm.yamda.model.Movie
import pt.isel.pdm.yamda.model.MoviesList
import java.lang.reflect.Type

class StubProvider : MovieProvider() {
    private val genresMovie1 = ArrayList<Movie.Genre>()
    private val genresMovie2 = ArrayList<Movie.Genre>()

    init {
        genresMovie1.add(Movie.Genre("1", "Comedy"))
        genresMovie1.add(Movie.Genre("2", "Horror"))
        genresMovie2.add(Movie.Genre("3", "Documentary"))
        genresMovie2.add(Movie.Genre("1", "Biography"))
    }

    override fun provide(pathName: String, parameters: HashMap<String, String>, cb: (BaseModel) -> Unit, cbError: (VolleyError) -> Unit, clazz: Type) {
        if(clazz == Movie::class.java)
            cb(movie(pathName.split("/")[1]))
        else
            cb(movieList())
    }

    fun movie(id:String): Movie = movieList().results[Integer.parseInt(id)]


    fun movieList(): MoviesList {
        val movie1 = Movie("Benfica", "/on9JlbGEccLsYkjeEph2Whm1DIp.jpg",
                7.5f, genresMovie1, 1, "13/10/2017", "Ganda filme. Viva o 37", "0")
        val movie2 = Movie("Sporting", "/bKPtXn9n4M4s8vvZrbw40mYsefB.jpg",
                9.5f, genresMovie2, 180, "13/10/2017", "Ganda filme.", "1")
        val arr = ArrayList<Movie>()
        arr.add(movie1)
        arr.add(movie2)
        return MoviesList(arr,1,1)
    }
}