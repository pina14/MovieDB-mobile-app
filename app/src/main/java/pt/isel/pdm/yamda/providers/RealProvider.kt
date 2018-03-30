package pt.isel.pdm.yamda.providers

import com.android.volley.VolleyError
import pt.isel.pdm.yamda.App
import pt.isel.pdm.yamda.model.BaseModel
import pt.isel.pdm.yamda.request.Request
import java.lang.reflect.Type

/**
 * Provides a DTO from a API online
 */
class RealProvider : MovieProvider() {
    private lateinit var query:String
    private lateinit var params:String
    private lateinit var url:String

    override fun provide(pathName: String, parameters: HashMap<String, String>,
                             cb: (BaseModel) -> Unit,cbError: (VolleyError) -> Unit, clazz: Type){
        query = pathName
        params = buildStringFromMap(parameters)
        url = "https://api.themoviedb.org/3/$query?api_key=d6bb450594da87f1ca90611dd16d969c$params"

        App.getQueue().add(Request(url, clazz, cb, cbError))
    }

    /**
     * Builds a URL query String from a Map
     */
    private fun buildStringFromMap(parameters: HashMap<String, String>): String {
        val str = StringBuilder()
        parameters.forEach {
            str.append("&${it.key}=${it.value}")
        }

        return str.toString()
    }
}