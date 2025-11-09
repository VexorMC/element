package com.element.configuration.game.impl

import com.element.configuration.game.GameProvider

class CustomGameProvider(
    override val identifier: String,
    override val mainClass: String
) : GameProvider
