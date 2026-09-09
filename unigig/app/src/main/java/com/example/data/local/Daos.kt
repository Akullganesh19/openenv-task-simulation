package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email)")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface GigDao {
    @Query("SELECT * FROM gigs ORDER BY createdAt DESC")
    fun getAllGigs(): Flow<List<GigEntity>>

    @Query("SELECT * FROM gigs WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getGigsByClient(clientId: String): Flow<List<GigEntity>>

    @Query("SELECT * FROM gigs WHERE id = :id")
    fun getGigById(id: String): Flow<GigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGig(gig: GigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGigs(gigs: List<GigEntity>)

    @Update
    suspend fun updateGig(gig: GigEntity)

    @Query("DELETE FROM gigs WHERE id = :id")
    suspend fun deleteGig(id: String)
}

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones WHERE gigId = :gigId")
    fun getMilestonesForGig(gigId: String): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestones")
    suspend fun getAllMilestones(): List<MilestoneEntity>

    @Query("SELECT * FROM milestones WHERE id = :id")
    suspend fun getMilestoneById(id: String): MilestoneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<MilestoneEntity>)

    @Update
    suspend fun updateMilestone(milestone: MilestoneEntity)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUser(userId: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(txs: List<TransactionEntity>)
}

@Dao
interface DisputeDao {
    @Query("SELECT * FROM disputes ORDER BY timestamp DESC")
    fun getAllDisputes(): Flow<List<DisputeEntity>>

    @Query("SELECT * FROM disputes WHERE id = :id")
    suspend fun getDisputeById(id: String): DisputeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispute(dispute: DisputeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisputes(disputes: List<DisputeEntity>)

    @Update
    suspend fun updateDispute(dispute: DisputeEntity)
}

@Dao
interface SkillBadgeDao {
    @Query("SELECT * FROM skill_badges")
    fun getAllBadges(): Flow<List<SkillBadgeEntity>>

    @Query("SELECT * FROM skill_badges WHERE userId = :userId")
    fun getBadgesForUser(userId: String): Flow<List<SkillBadgeEntity>>

    @Query("SELECT * FROM skill_badges WHERE id = :id")
    suspend fun getBadgeById(id: String): SkillBadgeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<SkillBadgeEntity>)

    @Update
    suspend fun updateBadge(badge: SkillBadgeEntity)
}

@Dao
interface ProposalBidDao {
    @Query("SELECT * FROM proposal_bids WHERE gigId = :gigId ORDER BY timestamp DESC")
    fun getBidsForGig(gigId: String): Flow<List<ProposalBidEntity>>

    @Query("SELECT * FROM proposal_bids WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getBidsForStudent(studentId: String): Flow<List<ProposalBidEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBid(bid: ProposalBidEntity)
}

@Dao
interface LegalBondDao {
    @Query("SELECT * FROM legal_bonds WHERE gigId = :gigId")
    fun getBondForGig(gigId: String): Flow<LegalBondEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBond(bond: LegalBondEntity)

    @Update
    suspend fun updateBond(bond: LegalBondEntity)
}

@Dao
interface AiAuditLogDao {
    @Query("SELECT * FROM ai_audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AiAuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AiAuditLogEntity)
}
