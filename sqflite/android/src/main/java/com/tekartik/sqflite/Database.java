package com.tekartik.sqflite;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

import static com.tekartik.sqflite.Constant.TAG;

class Database {
    final boolean singleInstance;
    final String path;
    final String password;
    final int id;
    final int logLevel;
    SQLiteDatabase sqliteDatabase;

    Database(String path, String password, int id, boolean singleInstance, int logLevel) {
        this.path = path;
        this.password = (password != null) ? password : "";

        this.singleInstance = singleInstance;
        this.id = id;
        this.logLevel = logLevel;
    }

    public void open() {
        openWithFlags(SQLiteDatabase.CREATE_IF_NECESSARY);

    }

    public void openReadOnly() {
        openWithFlags(SQLiteDatabase.OPEN_READONLY);
    }

    private void openWithFlags(int flags){
        try {

            sqliteDatabase = SQLiteDatabase.openDatabase(path, password, null, flags, null);

        }catch (Exception e) {
            Log.d(TAG, "Opening db in " + path + " with PRAGMA cipher_migrate");
            SQLiteDatabaseHook hook = new SQLiteDatabaseHook() {
                @Override
                public void preKey(net.sqlcipher.database.SQLiteDatabase database) {
                }

                @Override
                public void postKey(net.sqlcipher.database.SQLiteDatabase database) {
                    database.rawExecSQL("PRAGMA cipher_migrate;");
                }
            };

            sqliteDatabase = net.sqlcipher.database.SQLiteDatabase.openDatabase(path, password, null, flags, hook);
        }
    }

    public void close() {
        sqliteDatabase.close();
    }

    public SQLiteDatabase getWritableDatabase() {
        return sqliteDatabase;
    }

    public SQLiteDatabase getReadableDatabase() {
        return sqliteDatabase;
    }

    public boolean enableWriteAheadLogging() {
        try {
            sqliteDatabase.rawExecSQL("PRAGMA journal_mode=WAL;");
        } catch (Exception e) {
            Log.e(TAG, "enable WAL error: " + e);
            return false;
        }
        return true;
    }

    String getThreadLogTag() {
        Thread thread = Thread.currentThread();

        return "" + id + "," + thread.getName() + "(" + thread.getId() + ")";
    }
}
