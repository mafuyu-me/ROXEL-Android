package setsunai.roxel.network.model.payload

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class FastRequestPayload(
    @SerializedName("e")
    val event: String = "f_req",
    @SerializedName("n")
    val name: String,
    @SerializedName("d")
    val data: Serializable?
) : Serializable