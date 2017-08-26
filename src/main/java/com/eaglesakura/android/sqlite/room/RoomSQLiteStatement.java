package com.eaglesakura.android.sqlite.room;

import org.sqlite.database.sqlite.SQLiteStatement;

import android.arch.persistence.db.SupportSQLiteStatement;
import android.support.annotation.NonNull;

public class RoomSQLiteStatement implements SupportSQLiteStatement {
    @NonNull
    SQLiteStatement mStatement;

    RoomSQLiteStatement(@NonNull SQLiteStatement statement) {
        mStatement = statement;
    }

    @Override
    public void execute() {
        mStatement.execute();
    }

    @Override
    public int executeUpdateDelete() {
        return mStatement.executeUpdateDelete();
    }

    @Override
    public long executeInsert() {
        return mStatement.executeInsert();
    }

    @Override
    public long simpleQueryForLong() {
        return mStatement.simpleQueryForLong();
    }

    @Override
    public String simpleQueryForString() {
        return mStatement.simpleQueryForString();
    }

    @Override
    public void bindNull(int index) {
        mStatement.bindNull(index);
    }

    @Override
    public void bindLong(int index, long value) {
        mStatement.bindLong(index, value);
    }

    @Override
    public void bindDouble(int index, double value) {
        mStatement.bindDouble(index, value);
    }

    @Override
    public void bindString(int index, String value) {
        mStatement.bindString(index, value);
    }

    @Override
    public void bindBlob(int index, byte[] value) {
        mStatement.bindBlob(index, value);
    }

    @Override
    public void clearBindings() {
        mStatement.clearBindings();
    }

    @Override
    public void close() throws Exception {
        mStatement.close();
    }
}
