package be.kul.app.room.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import be.kul.app.room.model.UserEntity;

import java.util.List;

@Dao
public interface UserEntityDAO {

    @Insert
    void insert(UserEntity userEntity);

    @Query("DELETE FROM user")
    void deleteAll();

    @Query("SELECT * from user ORDER BY userId ASC")
    LiveData<List<UserEntity>> getAllUsers();

    @Query("SELECT * FROM user WHERE userId = :userId")
    UserEntity getUserById(int userId);

    @Query("SELECT * FROM user WHERE username = :username")
    UserEntity getUserByName(String username);

    @Query("SELECT * FROM user ORDER BY userId ASC")
    List<UserEntity> getAllUsersAsList();
}
