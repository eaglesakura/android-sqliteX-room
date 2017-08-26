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

import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.support.annotation.NonNull;

/**
 * Implements {@link SupportSQLiteOpenHelper.Factory} using the SQLite implementation in the
 * framework.
 */
@SuppressWarnings("unused")
public class RoomSQLiteOpenHelperFactory implements SupportSQLiteOpenHelper.Factory {

    @NonNull
    CancelSignal mCancelSignal;

    public RoomSQLiteOpenHelperFactory() {
        this(null);
    }

    public RoomSQLiteOpenHelperFactory(CancelSignal cancelSignal) {
        mCancelSignal = cancelSignal;
        if (mCancelSignal == null) {
            mCancelSignal = () -> false;
        }
    }

    @Override
    public SupportSQLiteOpenHelper create(SupportSQLiteOpenHelper.Configuration configuration) {
        return new RoomSQLiteOpenHelper(
                configuration.context, configuration.name,
                configuration.version, db -> {
        }, configuration.callback, mCancelSignal
        );
    }
}
