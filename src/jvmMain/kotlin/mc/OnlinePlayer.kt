package mc

import java.util.UUID

class OnlinePlayer(
    uuid: UUID,
    profileName: String,
    skinTextureUrl: String,
    capeTextureUrl: String?,
    val isOp: Boolean,
): Player(uuid, profileName, skinTextureUrl, capeTextureUrl) {
    constructor(player: Player, isOp: Boolean): this(
        player.uuid,
        player.profileName,
        player.skinTextureUrl,
        player.capeTextureUrl,
        isOp,
    )
}
