package com.chemscanner.omniscient.marrow.data.converters

import androidx.room.TypeConverter
import com.chemscanner.omniscient.marrow.data.models.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * THE SOVEREIGN CONVERTERS.
 * Unified logic for complex data serialization within the Marrow SDK.
 */
class MarrowConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun fromMap(value: String?): Map<String, String>? {
        if (value == null) return null
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toMap(map: Map<String, String>?): String? = gson.toJson(map)

    @TypeConverter
    fun fromList(value: String?): List<String>? {
        if (value == null) return null
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toList(list: List<String>?): String? = gson.toJson(list)

    @TypeConverter
    fun fromMoleculeList(value: String?): List<GeneratedMolecule>? {
        if (value == null) return null
        val type = object : TypeToken<List<GeneratedMolecule>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toMoleculeList(list: List<GeneratedMolecule>?): String? = gson.toJson(list)

    // FOOTBALL ENUMS & TYPES
    @TypeConverter
    fun fromPlayerRole(role: PlayerRole): String = role.name

    @TypeConverter
    fun toPlayerRole(value: String): PlayerRole = PlayerRole.valueOf(value)

    @TypeConverter
    fun fromCompetitionType(type: CompetitionType): String = type.name

    @TypeConverter
    fun toCompetitionType(value: String): CompetitionType = CompetitionType.valueOf(value)

    @TypeConverter
    fun fromExperimentType(type: ExperimentType): String = type.name

    @TypeConverter
    fun toExperimentType(value: String): ExperimentType = ExperimentType.valueOf(value)
}
