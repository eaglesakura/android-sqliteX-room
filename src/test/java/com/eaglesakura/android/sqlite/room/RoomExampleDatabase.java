package com.eaglesakura.android.sqlite.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RoomDatabase;

import java.util.List;

@Database(entities = {RoomExampleDatabase.ToDo.class, RoomExampleDatabase.User.class}, version = 1)
public abstract class RoomExampleDatabase extends RoomDatabase {

    public abstract DataAccess getDao();

    @Dao
    public interface DataAccess {
        /**
         * MEMO: ALPHA1ではAS文を利用するとビルドが止まらなくなるため注意が必要
         */
        @Query("SELECT\n" +
                "\tTODO.TITLE,\n" +
                "\tUSER.NAME\n" +
                "FROM\n" +
                "\tTODO\n" +
                "JOIN\n" +
                "\tUSER ON TODO.USER_ID = USER.USER_ID\n")
        List<TodoCard> listTodoCards();

        /**
         * MEMO: ALPHA1ではAS文を利用するとビルドが止まらなくなるため注意が必要
         */
        @Query("SELECT\n" +
                "\tTODO.TITLE,\n" +
                "\tUSER.NAME\n" +
                "FROM\n" +
                "\tTODO\n" +
                "JOIN\n" +
                "\tUSER ON TODO.USER_ID = USER.USER_ID\n")
        List<TodoCard> listTodoCardsEx();

        @Insert
        void insert(ToDo... todo);

        @Insert
        long[] insert(User... user);
    }

    public static class TodoCard {
        @ColumnInfo(name = "TITLE")
        public String title;

        public String NAME;
    }

    /**
     * TODOリストの１アイテム
     */
    @Entity(tableName = "TODO")
    public static class ToDo {
        /**
         * アイテムごとに割り振られるUID
         */
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "UNIQUE_ID", index = true)
        public long uniqueId;

        /**
         * TODO名
         */
        @ColumnInfo(name = "TITLE")
        public String title;

        /**
         * ユーザーID
         */
        @ColumnInfo(name = "USER_ID", index = true)
        public long userId;
    }

    @Entity(tableName = "USER")
    public static class User {
        /**
         * 一意に割り当てられるユーザーID
         */
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "USER_ID", index = true)
        public long userId;

        /**
         * ユーザー名
         */
        @ColumnInfo(name = "NAME")
        public String name;
    }

}