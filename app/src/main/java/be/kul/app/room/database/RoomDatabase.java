package be.kul.app.room.database;


import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import be.kul.app.Dashboard;
import be.kul.app.room.dao.AnswerEntityDAO;
import be.kul.app.room.dao.QuestionEntityDAO;
import be.kul.app.room.dao.UserEntityDAO;
import be.kul.app.room.model.AnswerEntity;
import be.kul.app.room.model.QuestionEntity;
import be.kul.app.room.model.UserEntity;

@Database(entities = {UserEntity.class, QuestionEntity.class, AnswerEntity.class}, version = 1)
public abstract class RoomDatabase extends android.arch.persistence.room.RoomDatabase {

    public abstract UserEntityDAO userEntityDAO();
    public abstract QuestionEntityDAO questionEntityDAO();
    public abstract AnswerEntityDAO answerEntityDAO();

    private static volatile RoomDatabase INSTANCE;

    public static RoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RoomDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            RoomDatabase.class, "database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Room database stuff
    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){

                @Override
                public void onOpen (@NonNull SupportSQLiteDatabase db){
                    super.onOpen(db);
                    new PopulateDbAsync(INSTANCE).execute();
                }
            };

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

       //private final WordDao mDao;

        PopulateDbAsync(RoomDatabase db) {
            //mDao = db.wordDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            /*mDao.deleteAll();
            Word word = new Word("Hello");
            mDao.insert(word);
            word = new Word("World");
            mDao.insert(word);*/
            return null;
        }
    }
}
