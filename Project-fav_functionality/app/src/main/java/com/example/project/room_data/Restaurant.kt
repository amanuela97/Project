package com.example.project.room_data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["name","address"], unique = true)])
data class Restaurant(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String?,
    val business_status: List<String>?,
    val photo: ByteArray?,
    val open: Boolean?,
    val rating: Double?,
    val phoneNumber: String?,
    val address: String?,
    @Embedded
    val location: Location,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Restaurant

        if (id != other.id) return false
        if (name != other.name) return false
        if (business_status != other.business_status) return false
        if (photo != null) {
            if (other.photo == null) return false
            if (!photo.contentEquals(other.photo)) return false
        } else if (other.photo != null) return false
        if (open != other.open) return false
        if (rating != other.rating) return false
        if (phoneNumber != other.phoneNumber) return false
        if (address != other.address) return false
        if (location != other.location) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (business_status?.hashCode() ?: 0)
        result = 31 * result + (photo?.contentHashCode() ?: 0)
        result = 31 * result + (open?.hashCode() ?: 0)
        result = 31 * result + (rating?.hashCode() ?: 0)
        result = 31 * result + (phoneNumber?.hashCode() ?: 0)
        result = 31 * result + (address?.hashCode() ?: 0)
        result = 31 * result + location.hashCode()
        return result
    }
}

data class Location(
    val lat: Double?,
    val lng: Double?
)