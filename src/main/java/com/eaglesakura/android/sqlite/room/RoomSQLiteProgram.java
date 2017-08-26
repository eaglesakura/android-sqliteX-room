package com.eaglesakura.android.sqlite.room;

import org.sqlite.database.sqlite.SQLiteProgram;

import android.arch.persistence.db.SupportSQLiteProgram;
import android.support.annotation.NonNull;

public class RoomSQLiteProgram implements SupportSQLiteProgram {
    @NonNull
    final SQLiteProgram mProgram;

    public RoomSQLiteProgram(@NonNull SQLiteProgram program) {
        mProgram = program;
    }

    @Override
    public void bindNull(int index) {
        mProgram.bindNull(index);
    }

    @Override
    public void bindLong(int index, long value) {
        mProgram.bindLong(index, value);
    }

    @Override
    public void bindDouble(int index, double value) {
        mProgram.bindDouble(index, value);
    }

    @Override
    public void bindString(int index, String value) {
        mProgram.bindString(index, value);
    }

    @Override
    public void bindBlob(int index, byte[] value) {
        mProgram.bindBlob(index, value);
    }

    @Override
    public void clearBindings() {
        mProgram.clearBindings();
    }

    @Override
    public void close() throws Exception {
        mProgram.close();
    }
}
