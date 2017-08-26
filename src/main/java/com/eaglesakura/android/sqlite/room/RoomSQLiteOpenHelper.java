/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eaglesakura.android.sqlite.room;

import com.eaglesakura.android.sqlite.CancelSignal;

import org.sqlite.database.DatabaseErrorHandler;
import org.sqlite.database.sqlite.SQLiteDatabase;
import org.sqlite.database.sqlite.SQLiteOpenHelper;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import java.io.File;

class RoomSQLiteOpenHelper implements SupportSQLiteOpenHelper {
    private final OpenHelper mDelegate;

    private final CancelSignal mCancelSignal;

    RoomSQLiteOpenHelper(Context context, String name, int version,
                         DatabaseErrorHandler errorHandler,
                         Callback callback,
                         @NonNull CancelSignal signal) {
        mCancelSignal = signal;
        mDelegate = createDelegate(context, name, version, errorHandler, callback);
    }

    private OpenHelper createDelegate(Context context, String name,
                                      int version, DatabaseErrorHandler errorHandler,
                                      final Callback callback) {

        // 絶対パスから始まらず、ドライブレターも含まれていないならば相対パスである
        // 相対パスの場合はContextからパスを取得する
        if (!name.startsWith("/") && !name.contains(":")) {
            name = context.getDatabasePath(name).getAbsolutePath();
        }

        // ディレクトリを生成する
        new File(name).getAbsoluteFile().getParentFile().mkdirs();

        return new OpenHelper(context, name, null, version, errorHandler) {
            @Override
            public void onCreate(SQLiteDatabase sqLiteDatabase) {
                mWrappedDb = new RoomSQLiteDatabase(sqLiteDatabase, mCancelSignal);
                callback.onCreate(mWrappedDb);
            }

            @Override
            public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
                callback.onUpgrade(getWrappedDb(sqLiteDatabase), oldVersion, newVersion);
            }

            @Override
            public void onConfigure(SQLiteDatabase db) {
                callback.onConfigure(getWrappedDb(db));
            }

            @Override
            public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                callback.onDowngrade(getWrappedDb(db), oldVersion, newVersion);
            }

            @Override
            public void onOpen(SQLiteDatabase db) {
                callback.onOpen(getWrappedDb(db));
            }
        };
    }

    @Override
    public String getDatabaseName() {
        return mDelegate.getDatabaseName();
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setWriteAheadLoggingEnabled(boolean enabled) {
        mDelegate.setWriteAheadLoggingEnabled(enabled);
    }

    @Override
    public SupportSQLiteDatabase getWritableDatabase() {
        return mDelegate.getWritableSupportDatabase();
    }

    @Override
    public SupportSQLiteDatabase getReadableDatabase() {
        return mDelegate.getReadableSupportDatabase();
    }

    @Override
    public void close() {
        mDelegate.close();
    }

    abstract class OpenHelper extends SQLiteOpenHelper {

        RoomSQLiteDatabase mWrappedDb;

        OpenHelper(Context context, String name,
                   SQLiteDatabase.CursorFactory factory, int version,
                   DatabaseErrorHandler errorHandler) {
            super(context, name, factory, version, errorHandler);
        }

        SupportSQLiteDatabase getWritableSupportDatabase() {
            SQLiteDatabase db = super.getWritableDatabase();
            return getWrappedDb(db);
        }

        SupportSQLiteDatabase getReadableSupportDatabase() {
            SQLiteDatabase db = super.getReadableDatabase();
            return getWrappedDb(db);
        }

        RoomSQLiteDatabase getWrappedDb(SQLiteDatabase sqLiteDatabase) {
            if (mWrappedDb == null) {
                mWrappedDb = new RoomSQLiteDatabase(sqLiteDatabase, mCancelSignal);
            }
            return mWrappedDb;
        }

        @Override
        public synchronized void close() {
            super.close();
            mWrappedDb = null;
        }
    }
}
