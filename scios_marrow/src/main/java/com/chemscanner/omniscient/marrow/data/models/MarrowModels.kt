package com.chemscanner.omniscient.marrow.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Ignore
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.IgnoredOnParcel

/**
 * SOVEREIGN MODELS HUB v4.3.
 * INTEGRITY GUARD: Removed unused imports (Offset, Anchor, RawValue).
 */

@Parcelize
@Entity(tableName = "scan_history")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chemicalName: String = "",
    val chemicalFormula: String = "",
    val smilesNotation: String? = null,
    val chemicalCid: String? = null,
    val confidenceScore: Float = 1.0f,
    val imagePath: String? = null,
    val scanDate: Long = System.currentTimeMillis(),
    val resultDescription: String? = null,
    val sustainabilityScore: Double? = null,
    val scanType: String = "CHEMICAL",
    val isFavorite: Boolean = false,
    val blockchainHash: String? = null 
) : Parcelable

@Entity(tableName = "blockchain_ledger")
data class BlockchainEntry(
    @PrimaryKey val blockHash: String,
    val previousHash: String,
    val module: String,
    val dataPayload: String,
    val timestamp: Long,
    val merkleRoot: String
)

@Entity(tableName = "toxicity_data")
data class ToxicityData(
    @PrimaryKey val casNumber: String,
    val chemicalName: String,
    val toxicityScore: Double,
    val safetyLevel: String,
    val precautions: String
)

@Entity(tableName = "chemicals")
data class ChemicalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val formula: String? = null,
    val smiles: String? = null,
    val molecularWeight: Double = 0.0,
    val description: String? = null,
    val cid: String? = null,
    val spectralHex: String? = null
)

@Entity(tableName = "cached_reactions")
data class CachedReaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reactant1: String,
    val reactant2: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val content: String,
    val role: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "discovered_planets")
data class DiscoveredPlanetEntity(
    @PrimaryKey val name: String,
    val type: String,
    val gravity: Float,
    val temperature: Float,
    val radius: Float = 1.0f,
    val distanceLy: Float = 0f,
    val hostStar: String = "Unknown",
    val ra: Double = 0.0,
    val dec: Double = 0.0,
    val telescopeSource: String = "Sovereign Network",
    val blockchainHash: String? = null,
    val resources: List<String> = emptyList(),
    val oxygenLevel: Float = 0f,
    val methaneLevel: Float = 0f,
    val bioIndex: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "match_results")
data class MatchResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamHomeId: String = "HOME",
    val teamAwayId: String = "AWAY",
    val scoreHome: Int = 0,
    val scoreAway: Int = 0,
    val userScore: Int = 0,
    val opponentScore: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chemical_properties")
data class ChemicalProperty(
    @PrimaryKey val propertyName: String,
    val propertyValue: String
)

@Entity(tableName = "genomic_organisms")
data class GenomicOrganismEntity(
    @PrimaryKey val id: String,
    val name: String,
    val geneticSequence: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "material_blueprints")
data class MaterialBlueprintEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val materialName: String,
    val atomicComposition: String,
    val stabilityIndex: Float,
    val maxGForceResisted: Float,
    val notarizationHash: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class GeneratedMolecule(
    val smiles: String,
    val sustainabilityScore: Double,
    val justification: String
)

enum class ExperimentType { HANDS_ON, AR_DEMO }

@Parcelize
data class Experiment(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val materials: List<String>,
    val steps: List<String>,
    val type: ExperimentType = ExperimentType.HANDS_ON,
    val videoUrl: String? = null
) : Parcelable

@Parcelize
data class PeriodicElement(
    val atomicNumber: Int,
    val symbol: String,
    val name: String,
    val atomicMass: Double,
    val category: String,
    val group: Int,
    val period: Int,
    val electronConfiguration: String?,
    val electronegativity: Double?,
    val discoveryYear: String?,
    val discoveredBy: String?,
    val summary: String?,
    val density: Double?,
    val melt: Double?,
    val boil: Double?,
    val isotopes: List<String> = emptyList()
) : Parcelable

@Parcelize
data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
) : Parcelable

@Parcelize
@Entity(tableName = "football_leagues")
data class League(
    @PrimaryKey val id: String, 
    val name: String, 
    val country: String
) : Parcelable

@Parcelize
@Entity(tableName = "football_teams")
data class Team(
    @PrimaryKey val id: String,
    val name: String,
    val leagueId: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val budget: Long = 50_000_000L,
    val prestige: Int = 50,
    @Ignore var players: List<FootballPlayer> = emptyList()
) : Parcelable {
    constructor(id: String, name: String, leagueId: String, primaryColor: Long, secondaryColor: Long, budget: Long, prestige: Int) : 
        this(id, name, leagueId, primaryColor, secondaryColor, budget, prestige, emptyList())
}

@Parcelize
@Entity(tableName = "football_players")
data class FootballPlayer(
    @PrimaryKey val id: String,
    val name: String,
    val role: PlayerRole,
    @Embedded val stats: PlayerStats,
    val marketValue: Long,
    val teamId: String,
    val experience: Int = 0,
    val level: Int = 1,
    val potential: Int = 85,
    val age: Int = 22,
    val isStartingXI: Boolean = true
) : Parcelable {
    @Ignore @IgnoredOnParcel var isSelected: Boolean = false
}

@Parcelize
data class PlayerStats(
    val speed: Float,
    val shooting: Float,
    val passing: Float,
    val dribbling: Float,
    val defending: Float,
    val physical: Float,
    val power: Float,
    val reaction: Float
) : Parcelable {
    fun getOverall(): Int {
        return ((speed + shooting + passing + dribbling + defending + physical) / 6).toInt()
    }
}

enum class PlayerRole { GOALKEEPER, DEFENDER, MIDFIELDER, STRIKER }

@Parcelize
@Entity(tableName = "football_stadiums")
data class Stadium(
    @PrimaryKey val id: String, 
    val name: String, 
    val city: String, 
    val capacity: Int, 
    val surface: String = "NATURAL", 
    val color: Long = 0L
) : Parcelable

@Parcelize
data class Competition(
    val id: String, val name: String, val type: CompetitionType, val teamIds: List<String>
) : Parcelable

enum class CompetitionType { DOMESTIC_LEAGUE, DOMESTIC_CUP, INTERNATIONAL_ELITE, INTERNATIONAL_SECONDARY }
