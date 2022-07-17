package data.server

data class NetworkProperties(
    val port: Int = 25565,
    val rconEnable: Boolean = false,
    val rconPort: Int = 25575,
    val rconPassword: String = "",
    val queryEnable: Boolean = false,
    val queryPort: Int = 25565,
    val statusEnable: Boolean = true,
    val networkCompressionThreshold: Int = 256,
    val preventProxyConnections: Boolean = false,
    val hideOnlinePlayers: Boolean = false,
) {
    constructor(properties: Map<String, String>) : this(
        properties.getValue("server-port").toInt(),
        properties.getValue("enable-rcon").toBoolean(),
        properties.getValue("rcon.port").toInt(),
        properties.getValue("rcon.password"),
        properties.getValue("enable-query").toBoolean(),
        properties.getValue("query.port").toInt(),
        properties.getValue("enable-status").toBoolean(),
        properties.getValue("network-compression-threshold").toInt(),
        properties.getValue("prevent-proxy-connections").toBoolean(),
        properties.getValue("hide-online-players").toBoolean(),
    )

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + port
        result = 31 * result + rconEnable.hashCode()
        result = 31 * result + rconPort
        result = 31 * result + rconPassword.hashCode()
        result = 31 * result + queryEnable.hashCode()
        result = 31 * result + queryPort
        result = 31 * result + statusEnable.hashCode()
        result = 31 * result + networkCompressionThreshold
        result = 31 * result + preventProxyConnections.hashCode()
        result = 31 * result + hideOnlinePlayers.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NetworkProperties

        if (port != other.port) return false
        if (rconEnable != other.rconEnable) return false
        if (rconPort != other.rconPort) return false
        if (rconPassword != other.rconPassword) return false
        if (queryEnable != other.queryEnable) return false
        if (queryPort != other.queryPort) return false
        if (statusEnable != other.statusEnable) return false
        if (networkCompressionThreshold != other.networkCompressionThreshold) return false
        if (preventProxyConnections != other.preventProxyConnections) return false
        if (hideOnlinePlayers != other.hideOnlinePlayers) return false

        return true
    }
}
