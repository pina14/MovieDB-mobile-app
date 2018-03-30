package pt.isel.pdm.yamda.persistentData

import android.content.*
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.support.annotation.MainThread

/**
 * Class that implements the authority for movie information.
 *
 * The content provider exposes two tables: one that holds the upcoming movies; the other that
 * holds the movies currently in exhibition. The columns of both tables are identical.
 */
class YAMDAContentProvider : ContentProvider() {

    /**
     * The public contract of the provided data model.
     */
    companion object {
        const val AUTHORITY = "pt.isel.pdm.yamda"
        const val COMING_SOON_TABLE_PATH = "coming_soon"
        const val NOW_PLAYING_TABLE_PATH = "now_playing"
        const val FOLLOWING_TABLE_PATH = "following"

        const val COMING_SOON_CONTENT = "content://${AUTHORITY}/${COMING_SOON_TABLE_PATH}"
        val COMING_SOON_CONTENT_URI: Uri = Uri.parse(COMING_SOON_CONTENT)
        const val NOW_PLAYING_CONTENT = "content://${AUTHORITY}/${NOW_PLAYING_TABLE_PATH}"
        val NOW_PLAYING_CONTENT_URI: Uri = Uri.parse(NOW_PLAYING_CONTENT)
        const val FOLLOWING_CONTENT = "content://${AUTHORITY}/${FOLLOWING_TABLE_PATH}"
        val FOLLOWING_CONTENT_URI: Uri = Uri.parse(FOLLOWING_CONTENT)

        val MOVIE_LIST_CONTENT_TYPE = "${ContentResolver.CURSOR_DIR_BASE_TYPE}/movies"
        val MOVIE_ITEM_CONTENT_TYPE = "${ContentResolver.CURSOR_ITEM_BASE_TYPE}/movie"

        const val COLUMN_ID = "id"
        const val COLUMN_ORIGINAL_TITLE = "original_title"
        const val COLUMN_POSTER = "poster_path"
        const val COLUMN_VOTE_AVERAGE = "vote_average"
        const val COLUMN_GENRES = "genres"
        const val COLUMN_RUNTIME = "runtime"
        const val COLUMN_RELEASE_DATE = "release_date"
        const val COLUMN_OVERVIEW = "overview"

        const val COLUMN_ID_IDX = 0
        const val COLUMN_ORIGINAL_TITLE_IDX = 1
        const val COLUMN_POSTER_IDX = 2
        const val COLUMN_VOTE_AVERAGE_IDX = 3
        const val COLUMN_GENRES_IDX = 4
        const val COLUMN_RUNTIME_IDX = 5
        const val COLUMN_RELEASE_DATE_IDX = 6
        const val COLUMN_OVERVIEW_IDX = 7

        // Private constants to be used by the implementation
        private const val COMING_SOON_TABLE_NAME = "coming_soon"
        private const val NOW_PLAYING_TABLE_NAME = "now_playing"
        private const val FOLLOWING_TABLE_NAME = "following"

        private const val COMING_SOON_LIST_CODE = 1010
        private const val COMING_SOON_ITEM_CODE = 1011
        private const val NOW_PLAYING_LIST_CODE = 1020
        private const val NOW_PLAYING_ITEM_CODE = 1021
        private const val FOLLOWING_LIST_CODE = 1030
        private const val FOLLOWING_ITEM_CODE = 1031
    }

    /**
     * The associated helper for DB accesses and migration.
     */
    private inner class MovieInfoDbHelper(version: Int = 1, dbName: String = "YAMDA_DB") :
            SQLiteOpenHelper(this@YAMDAContentProvider.context, dbName, null, version) {

        private fun createTable(db: SQLiteDatabase?, tableName: String) {
            val CREATE_CMD = "CREATE TABLE $tableName ( " +
                    "${COLUMN_ID} INTEGER PRIMARY KEY , " +
                    "${COLUMN_ORIGINAL_TITLE} TEXT , " +
                    "${COLUMN_POSTER} TEXT , " +
                    "${COLUMN_VOTE_AVERAGE} REAL , " +
                    "${COLUMN_GENRES} TEXT , " +
                    "${COLUMN_RUNTIME} INTEGER , " +
                    "${COLUMN_RELEASE_DATE} TEXT , " +
                    "${COLUMN_OVERVIEW} TEXT)"
            db?.execSQL(CREATE_CMD)
        }

        private fun dropTable(db: SQLiteDatabase?, tableName: String) {
            val DROP_CMD = "DROP TABLE IF EXISTS $tableName"
            db?.execSQL(DROP_CMD)
        }

        override fun onCreate(db: SQLiteDatabase?) {
            createTable(db, COMING_SOON_TABLE_NAME)
            createTable(db, NOW_PLAYING_TABLE_NAME)
            createTable(db, FOLLOWING_TABLE_NAME)
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            // Implementation note: This approach is usually unacceptable in production code.
            // Instead of dropping all data and creating it a new, in most cases we should migrate it instead
            dropTable(db, COMING_SOON_TABLE_NAME)
            dropTable(db, NOW_PLAYING_TABLE_NAME)
            dropTable(db, FOLLOWING_TABLE_NAME)
            createTable(db, COMING_SOON_TABLE_NAME)
            createTable(db, NOW_PLAYING_TABLE_NAME)
            createTable(db, FOLLOWING_TABLE_NAME)
        }
    }

    /**
     * @property dbHelper The DB helper instance to be used for DB accesses.
     */
    @Volatile private lateinit var dbHelper: MovieInfoDbHelper

    /**
     * @property uriMatcher The instance used to match an URI to its corresponding content type
     */
    @Volatile private lateinit var uriMatcher: UriMatcher

    /**
     * Callback method that signals instance creation.
     * @return A boolean value indicating whether the instance was correctly initialized or not
     */
    @MainThread
    override fun onCreate(): Boolean {
        dbHelper = MovieInfoDbHelper()
        uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        with (uriMatcher) {
            addURI(AUTHORITY, COMING_SOON_TABLE_PATH, COMING_SOON_LIST_CODE)
            addURI(AUTHORITY, "${COMING_SOON_TABLE_PATH}/#", COMING_SOON_ITEM_CODE)
            addURI(AUTHORITY, NOW_PLAYING_TABLE_PATH, NOW_PLAYING_LIST_CODE)
            addURI(AUTHORITY, "${NOW_PLAYING_TABLE_PATH}/#", NOW_PLAYING_ITEM_CODE)
            addURI(AUTHORITY, FOLLOWING_TABLE_PATH, FOLLOWING_LIST_CODE)
            addURI(AUTHORITY, "${FOLLOWING_TABLE_PATH}/#", FOLLOWING_ITEM_CODE)
        }
        return true
    }

    /** @see ContentProvider.getType */
    override fun getType(uri: Uri): String? = when (uriMatcher.match(uri)) {
        COMING_SOON_LIST_CODE, NOW_PLAYING_LIST_CODE, FOLLOWING_LIST_CODE -> MOVIE_LIST_CONTENT_TYPE
        COMING_SOON_ITEM_CODE, NOW_PLAYING_ITEM_CODE, FOLLOWING_ITEM_CODE -> MOVIE_ITEM_CONTENT_TYPE
        else -> throw IllegalArgumentException("Uri $uri not supported")
    }

    /**
     * Helper function used to obtain the table information (i.e. table name and path) based on the
     * given [uri]
     * @param [uri] The table URI
     * @return A [Pair] instance bearing the table name (the pair's first) and the table path
     * part (the pair's second).
     * @throws IllegalArgumentException if the received [uri] does not refer to an existing table
     */
    private fun resolveTableInfoFromUri(uri: Uri): Pair<String, String> = when (uriMatcher.match(uri)) {
        COMING_SOON_LIST_CODE -> Pair(COMING_SOON_TABLE_NAME, COMING_SOON_TABLE_PATH)
        NOW_PLAYING_LIST_CODE -> Pair(NOW_PLAYING_TABLE_NAME, NOW_PLAYING_TABLE_PATH)
        FOLLOWING_LIST_CODE -> Pair(FOLLOWING_TABLE_NAME, NOW_PLAYING_TABLE_PATH)
        else -> null
    } ?: throw IllegalArgumentException("Uri $uri not supported")

    /**
     * Helper function used to obtain the table name and selection arguments based on the
     * given [uri]
     * @param [uri] The received URI, which may refer to a table or to an individual entry
     * @return A [Triple] instance bearing the table name (the triple's first), the selection
     * string (the triple's second) and the selection string parameters (the triple's third).
     * @throws IllegalArgumentException if the received [uri] does not refer to a valid data set
     */
    private fun resolveTableAndSelectionInfoFromUri(uri: Uri, selection: String?, selectionArgs: Array<String>?)
            : Triple<String, String?, Array<String>?> {
        val itemSelection = "${COLUMN_ID} = ${uri.pathSegments.last()}"
        return when (uriMatcher.match(uri)) {
            COMING_SOON_ITEM_CODE -> Triple(COMING_SOON_TABLE_NAME, itemSelection, null)
            NOW_PLAYING_ITEM_CODE -> Triple(NOW_PLAYING_TABLE_NAME, itemSelection, null)
            else -> resolveTableInfoFromUri(uri).let { Triple(it.first, selection, selectionArgs) }
        }
    }

    /** @see ContentProvider.delete */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val params = resolveTableAndSelectionInfoFromUri(uri, selection, selectionArgs)
        val db = dbHelper.writableDatabase
        val deletedCount = db.delete(params.first, params.second, params.third)
        if (deletedCount != 0)
            context.contentResolver.notifyChange(uri, null)
        return deletedCount

    }

    /** @see ContentProvider.insert */
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val tableInfo = resolveTableInfoFromUri(uri)
        val db = dbHelper.writableDatabase
        val id = db.insert(tableInfo.first, null, values)
        return if (id < 0) null
        else {
            context.contentResolver.notifyChange(uri, null)
            Uri.parse("content://$AUTHORITY/${tableInfo.second}/$id")
        }
    }

    override fun bulkInsert(uri: Uri, values: Array<out ContentValues>): Int {
        val tableInfo = resolveTableInfoFromUri(uri)
        val db = dbHelper.writableDatabase
        var count = 0
        values.forEach {
            val id = db.insert(tableInfo.first, null, it)
            if(id >= 0){
                context.contentResolver.notifyChange(uri, null)
                Uri.parse("content://$AUTHORITY/${tableInfo.second}/$id")
                ++count
            }
        }
        return count
    }

    /** @see ContentProvider.query */
    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {

        val params = resolveTableAndSelectionInfoFromUri(uri, selection, selectionArgs)
        val db = dbHelper.readableDatabase
        return db.query(params.first, projection, params.second, params.third, null, null, sortOrder)
    }

    /** @see ContentProvider.update */
    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        val params = resolveTableAndSelectionInfoFromUri(uri, selection, selectionArgs)
        val db = dbHelper.writableDatabase
        return db.update(params.first,values,params.second,params.third)
    }
}