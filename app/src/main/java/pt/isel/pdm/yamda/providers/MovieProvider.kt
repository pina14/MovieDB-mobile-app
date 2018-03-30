package pt.isel.pdm.yamda.providers

import com.android.volley.VolleyError
import pt.isel.pdm.yamda.model.BaseModel
import java.lang.reflect.Type

/**
 * Provides a DTO using a certain provider
 */
abstract class MovieProvider {
    abstract fun provide(pathName: String, parameters: HashMap<String, String>, cb: (BaseModel) -> Unit, cbError: (VolleyError) -> Unit, clazz: Type)

}