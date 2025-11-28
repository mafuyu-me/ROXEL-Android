package setsunai.roxel.ext.request

import setsunai.roxel.Roxel
import setsunai.roxel.ext.impl.Hook
import setsunai.roxel.utils.RoxelUtils.toCRC32
import java.io.Serializable

open class FastRequest(instance: Roxel.Instance, val id: String) : Hook {
    private var updateTransmitter: ((FastRequest, Serializable?) -> Unit)? =
        null
    open var hash: Long = id.toCRC32()

    init {
        instance.registerFastRequestImpl(this)
    }

    fun registerUpdateTransmitter(transmitter: ((FastRequest, Serializable?) -> Unit)?) {
        updateTransmitter = transmitter
    }

    fun send(payload: Serializable? = null) {
        updateTransmitter?.invoke(this@FastRequest, payload)
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is FastRequest -> hash == other.hash
            is Long -> hash == other
            else -> other?.hashCode() == hashCode()
        }
    }

    override fun hashCode(): Int {
        return hash.hashCode()
    }

    override fun dispose() {}
}