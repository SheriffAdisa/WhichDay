package uk.ac.aston.cs3mdd.whichdayapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FavoritesDatabaseHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "favorites.db";
  private static final int DATABASE_VERSION = 1;

  private static final String TABLE_NAME = "bookmarks";
  private static final String COLUMN_NAME = "name";

  public FavoritesDatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_NAME + " TEXT PRIMARY KEY)";
    db.execSQL(createTable);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    onCreate(db);
  }

  public boolean addBookmark(String name) {
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(COLUMN_NAME, name);

    long result = db.insert(TABLE_NAME, null, values);
    return result != -1; // Return true if insertion was successful
  }

  public boolean removeBookmark(String name) {
    SQLiteDatabase db = this.getWritableDatabase();
    int result = db.delete(TABLE_NAME, COLUMN_NAME + " = ?", new String[]{name});
    return result > 0; // Return true if deletion was successful
  }

  public boolean isBookmarked(String name) {
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME + " = ?", new String[]{name}, null, null, null);
    boolean exists = cursor.getCount() > 0;
    cursor.close();
    return exists;
  }

  public Cursor getAllBookmarks() {
    SQLiteDatabase db = this.getReadableDatabase();
    return db.query(TABLE_NAME, null, null, null, null, null, null);
  }
}
