package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Resident
import kotlinx.coroutines.flow.Flow

@Dao
interface ResidentDao {
  @Query("SELECT * FROM residents ORDER BY rt ASC, noKk ASC, id ASC")
  fun getAllResidents(): Flow<List<Resident>>

  @Query("SELECT * FROM residents WHERE rt = :rt ORDER BY noKk ASC, id ASC")
  fun getResidentsByRt(rt: String): Flow<List<Resident>>

  @Query("SELECT * FROM residents WHERE id = :id LIMIT 1")
  fun getResidentById(id: Long): Flow<Resident?>

  @Query("SELECT * FROM residents WHERE id = :id LIMIT 1")
  suspend fun getResidentByIdOnce(id: Long): Resident?

  @Query("SELECT * FROM residents WHERE noKk = :noKk ORDER BY id ASC")
  fun getResidentsByKk(noKk: String): Flow<List<Resident>>

  @Query("SELECT DISTINCT rt FROM residents WHERE rt != '' ORDER BY rt ASC")
  fun getAllRtList(): Flow<List<String>>

  @Query("""
    SELECT * FROM residents 
    WHERE namaLengkap LIKE '%' || :query || '%' 
       OR nik LIKE '%' || :query || '%' 
       OR noKk LIKE '%' || :query || '%' 
       OR alamatLengkap LIKE '%' || :query || '%'
       OR pekerjaan LIKE '%' || :query || '%'
    ORDER BY namaLengkap ASC
  """)
  fun searchResidents(query: String): Flow<List<Resident>>

  @Query("SELECT COUNT(*) FROM residents")
  suspend fun getCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(resident: Resident): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(residents: List<Resident>)

  @Update
  suspend fun update(resident: Resident)

  @Delete
  suspend fun delete(resident: Resident)

  @Query("DELETE FROM residents WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM residents")
  suspend fun clearAll()

  @Query("SELECT * FROM residents WHERE nik = :nik LIMIT 1")
  suspend fun getByNik(nik: String): Resident?

  @Query("DELETE FROM residents WHERE nik IN (:niks)")
  suspend fun deleteByNiks(niks: List<String>): Int

  @Query("SELECT * FROM residents ORDER BY namaLengkap ASC")
  suspend fun getAllList(): List<Resident>
}
