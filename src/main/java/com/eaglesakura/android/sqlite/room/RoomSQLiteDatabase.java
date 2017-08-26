package com.eaglesakura.android.sqlite.room;

import com.eaglesakura.android.sqlite.CancelSignal;
import com.eaglesakura.android.sqlite.CancelableCursor;

import org.sqlite.database.sqlite.SQLiteCursor;
import org.sqlite.database.sqlite.SQLiteCursorDriver;
import org.sqlite.database.sqlite.SQLiteDatabase;
import org.sqlite.database.sqlite.SQLiteQuery;

import android.arch.persistence.db.SimpleSQLiteQuery;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.db.SupportSQLiteStatement;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteTransactionListener;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RoomSQLiteDatabase implements SupportSQLiteDatabase {
    @NonNull
    final SQLiteDatabase mDatabase;

    @NonNull
    final CancelSignal mCancelSignal;

    RoomSQLiteDatabase(@NonNull SQLiteDatabase database, @NonNull CancelSignal cancelSignal) {
        mDatabase = database;
        mCancelSignal = cancelSignal;
    }

    @Override
    public SupportSQLiteStatement compileStatement(String sql) {
        return new RoomSQLiteStatement(mDatabase.compileStatement(sql));
    }

    @Override
    public void beginTransaction() {
        mDatabase.beginTransaction();
    }

    @Override
    public void beginTransactionNonExclusive() {
        mDatabase.beginTransactionNonExclusive();
    }

    @Override
    public void beginTransactionWithListener(SQLiteTransactionListener transactionListener) {
        mDatabase.beginTransactionWithListener(new org.sqlite.database.sqlite.SQLiteTransactionListener() {
            @Override
            public void onBegin() {
                transactionListener.onBegin();
            }

            @Override
            public void onCommit() {
                transactionListener.onCommit();
            }

            @Override
            public void onRollback() {
                transactionListener.onRollback();
            }
        });
    }

    @Override
    public void beginTransactionWithListenerNonExclusive(SQLiteTransactionListener transactionListener) {
        mDatabase.beginTransactionWithListenerNonExclusive(new org.sqlite.database.sqlite.SQLiteTransactionListener() {
            @Override
            public void onBegin() {
                transactionListener.onBegin();
            }

            @Override
            public void onCommit() {
                transactionListener.onCommit();
            }

            @Override
            public void onRollback() {
                transactionListener.onRollback();
            }
        });
    }

    @Override
    public void endTransaction() {
        mDatabase.endTransaction();
    }

    @Override
    public void setTransactionSuccessful() {
        mDatabase.setTransactionSuccessful();
    }

    @Override
    public boolean inTransaction() {
        return mDatabase.inTransaction();
    }

    @Override
    public boolean isDbLockedByCurrentThread() {
        return mDatabase.isDbLockedByCurrentThread();
    }

    @Override
    public boolean yieldIfContendedSafely() {
        return mDatabase.yieldIfContendedSafely();
    }

    @Override
    public boolean yieldIfContendedSafely(long sleepAfterYieldDelay) {
        return mDatabase.yieldIfContendedSafely(sleepAfterYieldDelay);
    }

    @Override
    public int getVersion() {
        return mDatabase.getVersion();
    }

    @Override
    public void setVersion(int version) {
        mDatabase.setVersion(version);
    }

    @Override
    public long getMaximumSize() {
        return mDatabase.getMaximumSize();
    }

    @Override
    public long setMaximumSize(long numBytes) {
        return mDatabase.setMaximumSize(numBytes);
    }

    @Override
    public long getPageSize() {
        return mDatabase.getPageSize();
    }

    @Override
    public void setPageSize(long numBytes) {
        mDatabase.setPageSize(numBytes);
    }

    private static final String[] CONFLICT_VALUES = new String[]
            {"", " OR ROLLBACK ", " OR ABORT ", " OR FAIL ", " OR IGNORE ", " OR REPLACE "};
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    @Override
    public Cursor query(String query) {
        return query(new SimpleSQLiteQuery(query));
    }

    @Override
    public Cursor query(String query, Object[] bindArgs) {
        return query(new SimpleSQLiteQuery(query, bindArgs));
    }


    @Override
    public Cursor query(SupportSQLiteQuery query) {
        return query(query, null);
    }

    @Override
    public Cursor query(SupportSQLiteQuery supportQuery, CancellationSignal cancellationSignal) {
        return mDatabase.rawQueryWithFactory((SQLiteDatabase db, SQLiteCursorDriver masterQuery, String editTable, SQLiteQuery query) -> {
            RoomSQLiteProgram program = new RoomSQLiteProgram(query);
            supportQuery.bindTo(program);
            SQLiteCursor cursor = new SQLiteCursor(masterQuery, editTable, query);
            return new CancelableCursor(cursor, () -> {
                // 先行キャンセル
                if (mCancelSignal.isCanceled()) {
                    return true;
                }

                if (cancellationSignal != null) {
                    return cancellationSignal.isCanceled();
                } else {
                    return false;
                }
            });
        }, supportQuery.getSql(), EMPTY_STRING_ARRAY, null);
    }

    @Override
    public long insert(String table, int conflictAlgorithm, ContentValues values) throws SQLException {
        return mDatabase.insertWithOnConflict(table, null, values,
                conflictAlgorithm);
    }

    @Override
    public int delete(String table, String whereClause, Object[] whereArgs) {
        String query = "DELETE FROM " + table
                + (isEmpty(whereClause) ? "" : " WHERE " + whereClause);
        SupportSQLiteStatement statement = compileStatement(query);
        SimpleSQLiteQuery.bind(statement, whereArgs);
        return statement.executeUpdateDelete();
    }

    @Override
    public int update(String table, int conflictAlgorithm, ContentValues values, String whereClause, Object[] whereArgs) {
        // taken from SQLiteDatabase class.
        if (values == null || values.size() == 0) {
            throw new IllegalArgumentException("Empty values");
        }
        StringBuilder sql = new StringBuilder(120);
        sql.append("UPDATE ");
        sql.append(CONFLICT_VALUES[conflictAlgorithm]);
        sql.append(table);
        sql.append(" SET ");

        // move all bind args to one array
        int setValuesSize = values.size();
        int bindArgsSize = (whereArgs == null) ? setValuesSize : (setValuesSize + whereArgs.length);
        Object[] bindArgs = new Object[bindArgsSize];
        int i = 0;
        for (String colName : values.keySet()) {
            sql.append((i > 0) ? "," : "");
            sql.append(colName);
            bindArgs[i++] = values.get(colName);
            sql.append("=?");
        }
        if (whereArgs != null) {
            for (i = setValuesSize; i < bindArgsSize; i++) {
                bindArgs[i] = whereArgs[i - setValuesSize];
            }
        }
        if (!isEmpty(whereClause)) {
            sql.append(" WHERE ");
            sql.append(whereClause);
        }
        SupportSQLiteStatement stmt = compileStatement(sql.toString());
        SimpleSQLiteQuery.bind(stmt, bindArgs);
        return stmt.executeUpdateDelete();
    }

    @Override
    public void execSQL(String sql) throws SQLException {
        mDatabase.execSQL(sql);
    }

    @Override
    public void execSQL(String sql, Object[] bindArgs) throws SQLException {
        mDatabase.execSQL(sql, bindArgs);
    }

    @Override
    public boolean isReadOnly() {
        return mDatabase.isReadOnly();
    }

    @Override
    public boolean isOpen() {
        return mDatabase.isOpen();
    }

    @Override
    public boolean needUpgrade(int newVersion) {
        return mDatabase.needUpgrade(newVersion);
    }

    @Override
    public String getPath() {
        return mDatabase.getPath();
    }

    @Override
    public void setLocale(Locale locale) {
        mDatabase.setLocale(locale);
    }

    @Override
    public void setMaxSqlCacheSize(int cacheSize) {
        mDatabase.setMaxSqlCacheSize(cacheSize);
    }

    @Override
    public void setForeignKeyConstraintsEnabled(boolean enable) {
        mDatabase.setForeignKeyConstraintsEnabled(enable);
    }

    @Override
    public boolean enableWriteAheadLogging() {
        return mDatabase.enableWriteAheadLogging();
    }

    @Override
    public void disableWriteAheadLogging() {
        mDatabase.disableWriteAheadLogging();
    }

    @Override
    public boolean isWriteAheadLoggingEnabled() {
        return mDatabase.isWriteAheadLoggingEnabled();
    }

    @Override
    public List<Pair<String, String>> getAttachedDbs() {
        return mDatabase.getAttachedDbs();
    }

    @Override
    public boolean isDatabaseIntegrityOk() {
        return mDatabase.isDatabaseIntegrityOk();
    }

    @Override
    public void close() throws IOException {
        mDatabase.close();
    }

    private static boolean isEmpty(String input) {
        return input == null || input.length() == 0;
    }
}
