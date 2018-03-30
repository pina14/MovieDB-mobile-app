package pt.isel.pdm.yamda.request

import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonRequest
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import pt.isel.pdm.yamda.model.BaseModel
import java.io.UnsupportedEncodingException
import java.lang.reflect.Type

/**
 * Requests in background resources from the internet
 */
class Request<BaseModel>(url:String, val clazz: Type, success: (BaseModel) -> Unit, error : (VolleyError) -> Unit):
                        JsonRequest<BaseModel>(Method.GET,url,null,success,error){

    companion object {
        val gson = Gson()
    }

    override fun parseNetworkResponse(response: NetworkResponse?): Response<BaseModel> {
        try {
            val json = String(response!!.data)
            return Response.success(
                    gson.fromJson<BaseModel>(json, clazz),
                    HttpHeaderParser.parseCacheHeaders(response))
        } catch (e: UnsupportedEncodingException) {
            return Response.error<BaseModel>(ParseError(e))
        } catch (e: JsonSyntaxException) {
            return Response.error<BaseModel>(ParseError(e))
        }
    }
}