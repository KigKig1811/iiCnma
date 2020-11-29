package com.imn.iicnma.data.local.popular

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "popular_movies_keys")
data class PopularMovieKeysEntity(
    @PrimaryKey @field:SerializedName("movie_id") val movieId: Long,
    @field:SerializedName("prev_key") val prevKey: Int?,
    @field:SerializedName("cur_key") val curKey: Int?,
    @field:SerializedName("next_key") val nextKey: Int?,
)