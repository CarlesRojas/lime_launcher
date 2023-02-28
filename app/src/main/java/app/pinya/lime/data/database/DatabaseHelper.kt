package app.pinya.lime.data.database

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

import android.database.sqlite.SQLiteOpenHelper


class DatabaseHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_APP_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        // If you have to change the database look at this: https://thebhwgroup.com/blog/how-android-sqlite-onupgrade
    }

    fun appExistsInDb(packageName: String): Boolean {
        val sql =
            "SELECT EXISTS (SELECT * FROM $APP_TABLE_NAME WHERE $PACKAGE_NAME_COL='$packageName' LIMIT 1)"

        val db = this.readableDatabase

        val cursor: Cursor = db.rawQuery(sql, null)
        cursor.moveToFirst()

        val exists = cursor.getInt(0) == 1
        cursor.close()
        return exists
    }


    fun addOrUpdateApp(
        packageName: String,
        isHome: Boolean,
        isHidden: Boolean,
        isRenamed: Boolean,
        name: String
    ) {
        // TODO try this if the bug still deletes the home apps randomly
        //val values = ContentValues()

        // we are inserting our values
        // in the form of key-value pair
        //values.put(NAME_COl, name)
        //values.put(AGE_COL, age)

        // here we are creating a
        // writable variable of
        // our database as we want to
        // insert value in our database
        //val db = this.writableDatabase

        // all values are inserted into database
        //db.insert(TABLE_NAME, null, values)

        // at last we are
        // closing our database
        //db.close()
    }

    fun getHomeApps(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $APP_TABLE_NAME WHERE $HOME_COL > 0", null)
    }

    fun getHiddenApps(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $APP_TABLE_NAME WHERE $HIDDEN_COL > 0", null)
    }

    fun getRenamedApps(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $APP_TABLE_NAME WHERE $IS_RENAMED_COL > 0", null)
    }

    companion object {
        private const val DATABASE_NAME = "LimeLauncherDB"
        private const val DATABASE_VERSION = 1

        const val APP_TABLE_NAME = "app"

        const val PACKAGE_NAME_COL = "packageName"
        const val HOME_COL = "home"
        const val HIDDEN_COL = "hidden"
        const val IS_RENAMED_COL = "isRenamed"
        const val NAME_COL = "name"

        const val CREATE_APP_TABLE = ("CREATE TABLE " + APP_TABLE_NAME +
                " (" +
                PACKAGE_NAME_COL + " TEXT PRIMARY KEY, " +
                HOME_COL + " INTEGER," +
                HIDDEN_COL + " INTEGER," +
                IS_RENAMED_COL + " INTEGER," +
                NAME_COL + " TEXT" +
                ");")
    }
}