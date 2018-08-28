package example.com.booklog;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static example.com.booklog.BookContract.BookEntry.CODE_BOOK;
import static example.com.booklog.BookContract.BookEntry.CODE_BOOK_WITH_ID;
import static example.com.booklog.BookContract.BookEntry.CONTENT_ITEM_TYPE;
import static example.com.booklog.BookContract.BookEntry.CONTENT_LIST_TYPE;
import static example.com.booklog.BookContract.BookEntry.TABLE_NAME;
import static example.com.booklog.BookContract.BookEntry._ID;
import static example.com.booklog.BookContract.CONTENT_AUTHORITY;

public class BookProvider extends ContentProvider {

    BookDbHelper helper;
    private static UriMatcher uriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(CONTENT_AUTHORITY, TABLE_NAME, CODE_BOOK);
        uriMatcher.addURI(CONTENT_AUTHORITY, TABLE_NAME + "/#", CODE_BOOK_WITH_ID);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        helper = new BookDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor;

        switch (uriMatcher.match(uri)) {
            case CODE_BOOK:
                cursor = database.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case CODE_BOOK_WITH_ID:
                selection = _ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

                default:
                    throw new IllegalArgumentException("Cannot query unknown URI : " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case CODE_BOOK:
                return CONTENT_LIST_TYPE;
            case CODE_BOOK_WITH_ID:
                return CONTENT_ITEM_TYPE;
                default:
                    throw new IllegalArgumentException("");
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        SQLiteDatabase database = helper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case CODE_BOOK:
                long id = database.insert(TABLE_NAME, null, contentValues);
                if (id != -1) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return BookContract.BookEntry.buildUriWithId(id);
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = helper.getWritableDatabase();

        int rowsDeleted;

        switch (uriMatcher.match(uri)) {
            case CODE_BOOK:
                rowsDeleted = database.delete(TABLE_NAME, selection, selectionArgs);
                break;

            case CODE_BOOK_WITH_ID:
                selection = _ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(TABLE_NAME, selection, selectionArgs);
                break;

                default:
                    throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = helper.getWritableDatabase();
        int rowsUpdated = database.update(TABLE_NAME, contentValues, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
