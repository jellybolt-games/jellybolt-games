package com.jellybolt.handwriting;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jellybolt.handwriting.core.Alphabet;
import com.jellybolt.handwriting.core.HandwritingRecognizer;
import com.jellybolt.handwriting.core.Ink;

import java.util.ArrayList;
import java.util.List;

public final class ProfileStore extends SQLiteOpenHelper {
    public static final int MAX_EXAMPLES_PER_LABEL = 200;
    public static final int MAX_PROFILES = 20;

    public static final class Profile {
        public final long id;
        public final String name;

        private Profile(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override public String toString() { return name; }
    }

    public ProfileStore(Context context) {
        super(context, "personal-handwriting.db", null, 1);
    }

    @Override public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
        try (Cursor cursor = db.rawQuery("PRAGMA secure_delete=ON", null)) {
            if (!cursor.moveToFirst() || cursor.getInt(0) != 1) {
                throw new IllegalStateException("Could not enable secure example deletion");
            }
        }
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE profiles (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL UNIQUE CHECK(length(name) BETWEEN 1 AND 40))");
        db.execSQL("CREATE TABLE examples (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "profile_id INTEGER NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,"
                + "alphabet TEXT NOT NULL,label TEXT NOT NULL,ink BLOB NOT NULL)");
        db.execSQL("CREATE INDEX examples_lookup ON examples(profile_id,alphabet,label)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new IllegalStateException("An explicit profile migration is required");
    }

    public List<Profile> profiles() {
        List<Profile> result = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query("profiles",
                new String[]{"id", "name"}, null, null, null, null, "id")) {
            while (cursor.moveToNext()) result.add(new Profile(cursor.getLong(0), cursor.getString(1)));
        }
        return result;
    }

    public Profile createProfile(String name) {
        if (name == null || name.trim().isEmpty() || name.trim().length() > 40) {
            throw new IllegalArgumentException("Use an alias of 1 to 40 characters");
        }
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            if (profiles().size() >= MAX_PROFILES) throw new IllegalStateException("Profile limit reached");
            ContentValues values = new ContentValues();
            values.put("name", name.trim());
            long id = db.insertOrThrow("profiles", null, values);
            db.setTransactionSuccessful();
            return new Profile(id, name.trim());
        } finally {
            db.endTransaction();
        }
    }

    public void deleteProfile(long profileId) {
        int deleted = getWritableDatabase().delete("profiles", "id=?", new String[]{Long.toString(profileId)});
        if (deleted != 1) throw new IllegalArgumentException("Profile does not exist");
    }

    public List<HandwritingRecognizer.Example> examples(long profileId, String group) {
        Alphabet.labels(group);
        List<HandwritingRecognizer.Example> result = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query("examples", new String[]{"id", "label", "ink"},
                "profile_id=? AND alphabet=?", new String[]{Long.toString(profileId), group},
                null, null, "id")) {
            while (cursor.moveToNext()) {
                result.add(new HandwritingRecognizer.Example(cursor.getLong(0), cursor.getString(1),
                        Ink.decode(cursor.getBlob(2))));
            }
        }
        return result;
    }

    public void addExample(long profileId, String group, String label, Ink ink) {
        validateLabel(group, label);
        if (ink == null || ink.isEmpty()) throw new IllegalArgumentException("Draw a character first");
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            try (Cursor cursor = db.rawQuery("SELECT count(*) FROM examples"
                    + " WHERE profile_id=? AND alphabet=? AND label=?", args(profileId, group, label))) {
                cursor.moveToFirst();
                if (cursor.getInt(0) >= MAX_EXAMPLES_PER_LABEL) {
                    throw new IllegalStateException("Example limit reached for this character");
                }
            }
            ContentValues values = new ContentValues();
            values.put("profile_id", profileId);
            values.put("alphabet", group);
            values.put("label", label);
            values.put("ink", ink.encode());
            db.insertOrThrow("examples", null, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public boolean deleteLastExample(long profileId, String group, String label) {
        validateLabel(group, label);
        return getWritableDatabase().delete("examples", "id=(SELECT max(id) FROM examples"
                + " WHERE profile_id=? AND alphabet=? AND label=?)", args(profileId, group, label)) == 1;
    }

    private static void validateLabel(String group, String label) {
        if (!Alphabet.labels(group).contains(label)) throw new IllegalArgumentException("Invalid character label");
    }

    private static String[] args(long profileId, String group, String label) {
        return new String[]{Long.toString(profileId), group, label};
    }
}
