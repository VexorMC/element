package com.element.configuration.game.impl

import com.element.configuration.game.GameProvider

class MinecraftGameProvider : GameProvider {
    override val identifier: String = "minecraft"
    override val mainClass: String = "net.minecraft.client.main.Main"
}