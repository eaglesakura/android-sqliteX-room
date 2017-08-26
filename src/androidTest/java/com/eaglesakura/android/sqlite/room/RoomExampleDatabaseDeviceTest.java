package com.eaglesakura.android.sqlite.room;

import com.eaglesakura.android.devicetest.DeviceTestCase;

import org.junit.Test;

import org.sqlite.database.SQLiteX;

import android.arch.persistence.room.Room;

import java.util.List;
import java.util.concurrent.CancellationException;

public class RoomExampleDatabaseDeviceTest extends DeviceTestCase {

    @Override
    public void onSetup() {
        super.onSetup();
        SQLiteX.install(getContext());
        getContext().getDatabasePath("example.db").delete();
    }

    @Test
    public void Builderごとに異なるインスタンスが取得できる() throws Throwable {
        RoomExampleDatabaseDevice database0 = Room.databaseBuilder(getContext(), RoomExampleDatabaseDevice.class, "example.db")
                .allowMainThreadQueries()
                .openHelperFactory(new RoomSQLiteOpenHelperFactory())
                .build();

        RoomExampleDatabaseDevice database1 = Room.databaseBuilder(getContext(), RoomExampleDatabaseDevice.class, "example.db")
                .allowMainThreadQueries()
                .openHelperFactory(new RoomSQLiteOpenHelperFactory(() -> false))
                .build();

        assertNotEquals(database0, database1);
        assertNotEquals(database0.getOpenHelper(), database1.getOpenHelper());
        assertNotEquals(database0.getDao(), database1.getDao());
    }

    @Test
    public void 動作がキャンセルされても別インスタンスは正常に動作する() throws Throwable {
        try {
            動作がキャンセルされたら例外を投げる();
        } catch (Exception e) {

        }
        RoomExampleDatabaseDevice database = Room.databaseBuilder(getContext(), RoomExampleDatabaseDevice.class, "example.db")
                .allowMainThreadQueries()
                .openHelperFactory(new RoomSQLiteOpenHelperFactory(() -> false))
                .build();

        assertNotNull(database);

        RoomExampleDatabaseDevice.DataAccess dao = database.getDao();
        assertNotNull(dao);

        try {
            database.runInTransaction(() -> {
                {
                    RoomExampleDatabaseDevice.User user = new RoomExampleDatabaseDevice.User();
                    user.name = "user1";
                    long[] keys = dao.insert(user);

                    assertEquals(keys.length, 1);
                    assertEquals(keys[0], 1);

                    RoomExampleDatabaseDevice.ToDo todo = new RoomExampleDatabaseDevice.ToDo();
                    todo.title = "example";
                    todo.userId = keys[0];

                    dao.insert(todo);
                }
                {
                    RoomExampleDatabaseDevice.User user = new RoomExampleDatabaseDevice.User();
                    user.name = "user2";
                    long[] keys = dao.insert(user);

                    assertEquals(keys.length, 1);
                    assertEquals(keys[0], 2);
                }
            });
        } finally {
            database.close();
        }

    }

    @Test(expected = CancellationException.class)
    public void 動作がキャンセルされたら例外を投げる() throws Throwable {
        RoomExampleDatabaseDevice database = Room.databaseBuilder(getContext(), RoomExampleDatabaseDevice.class, "example.db")
                .allowMainThreadQueries()
                .openHelperFactory(new RoomSQLiteOpenHelperFactory(() -> true))
                .build();

        assertNotNull(database);

        RoomExampleDatabaseDevice.DataAccess dao = database.getDao();
        assertNotNull(dao);

        try {
            database.runInTransaction(() -> {
                {
                    RoomExampleDatabaseDevice.User user = new RoomExampleDatabaseDevice.User();
                    user.name = "user1";
                    long[] keys = dao.insert(user);

                    assertEquals(keys.length, 1);
                    assertEquals(keys[0], 1);

                    RoomExampleDatabaseDevice.ToDo todo = new RoomExampleDatabaseDevice.ToDo();
                    todo.title = "example";
                    todo.userId = keys[0];

                    dao.insert(todo);
                }
                {
                    RoomExampleDatabaseDevice.User user = new RoomExampleDatabaseDevice.User();
                    user.name = "user2";
                    long[] keys = dao.insert(user);

                    assertEquals(keys.length, 1);
                    assertEquals(keys[0], 2);
                }
            });

            fail();
        } finally {
            database.close();
        }

    }

    @Test
    public void Databaseインスタンスを生成できる() throws Throwable {
        RoomExampleDatabaseDevice database = Room.databaseBuilder(getContext(), RoomExampleDatabaseDevice.class, "example.db")
                .allowMainThreadQueries()
                .openHelperFactory(new RoomSQLiteOpenHelperFactory(() -> false))
                .build();

        assertNotNull(database);

        RoomExampleDatabaseDevice.DataAccess dao = database.getDao();
        assertNotNull(dao);

        try {
            database.runInTransaction(() -> {
                {
                    RoomExampleDatabaseDevice.User user = new RoomExampleDatabaseDevice.User();
                    user.name = "user1";
                    long[] keys = dao.insert(user);

                    assertEquals(keys.length, 1);
                    assertEquals(keys[0], 1);

                    RoomExampleDatabaseDevice.ToDo todo = new RoomExampleDatabaseDevice.ToDo();
                    todo.title = "example";
                    todo.userId = keys[0];

                    dao.insert(todo);
                }
                {
                    RoomExampleDatabaseDevice.User user = new RoomExampleDatabaseDevice.User();
                    user.name = "user2";
                    long[] keys = dao.insert(user);

                    assertEquals(keys.length, 1);
                    assertEquals(keys[0], 2);
                }
            });
        } finally {
            database.close();
        }

        try {
            List<RoomExampleDatabaseDevice.TodoCard> todoCards = dao.listTodoCards();
            assertEquals(todoCards.size(), 1);
            RoomExampleDatabaseDevice.TodoCard card = todoCards.get(0);

            assertEquals(card.title, "example");
            assertEquals(card.NAME, "user1");
        } finally {
            database.close();
        }


        try {
            List<RoomExampleDatabaseDevice.TodoCard> todoCards = dao.listTodoCardsEx();
            validate(todoCards)
                    .notEmpty()
                    .checkAt(0, card -> {
                        assertEquals(card.title, "example");
                        assertEquals(card.NAME, "user1");
                    });
        } finally {
            database.close();
        }
    }
}
