package pt.isel.pdm.yamda.model

import android.os.Parcel
import android.os.Parcelable

data class Movie(val original_title:String, val poster_path: String?, val vote_average: Float, var genres: ArrayList<Genre>?, val runtime: Int,
                 val release_date: String, val overview: String, val id:String) : Parcelable,BaseModel {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readFloat(),
            parcel.createTypedArrayList(Genre.CREATOR),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel?, p1: Int) {
        String.javaClass.javaClass.javaClass.javaClass
        parcel?.writeString(original_title)
        parcel?.writeString(poster_path)
        parcel?.writeFloat(vote_average)
        parcel?.writeTypedList(genres)
        parcel?.writeInt(runtime)
        parcel?.writeString(release_date)
        parcel?.writeString(overview)
        parcel?.writeString(id)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Movie> {
        override fun createFromParcel(parcel: Parcel): Movie = Movie(parcel)

        override fun newArray(size: Int): Array<Movie?> = arrayOfNulls(size)
    }


    data class Genre(val id : String, val name : String) : Parcelable{
        override fun toString() = name

        fun toJsonObject() = "{id : $id, name : $name}"

        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(name)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Genre> {
            override fun createFromParcel(parcel: Parcel): Genre = Genre(parcel)

            override fun newArray(size: Int): Array<Genre> = arrayOf()//arrayOfNulls(size)
        }
    }

}

