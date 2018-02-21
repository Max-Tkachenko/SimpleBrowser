package max.example.com.quickurl.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static int DATABASE_VERSION = 1;
    public static String DATABASE_NAME = "ReferencesDB";

    public static String TABLE_LINKS = "links";
    public static String TABLE_GROUPS = "groups";

    public static final String KEY_OWNER_ID = "groupId";
    public static final String KEY_LINK_NAME = "name";
    public static final String KEY_URL = "link";

    public static final String KEY_GROUP_ID = "_id";
    public static final String KEY_GROUP_NAME = "name";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_LINKS + " (" +
                KEY_OWNER_ID + " int," +
                KEY_LINK_NAME + "  text," +
                KEY_URL + " text" +")");
        db.execSQL("create table " + TABLE_GROUPS + " (" +
                KEY_GROUP_ID + " integer primary key," +
                KEY_GROUP_NAME + " text" +")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists " + TABLE_LINKS);
        sqLiteDatabase.execSQL("drop table if exists " + TABLE_GROUPS);
        onCreate(sqLiteDatabase);
    }
}