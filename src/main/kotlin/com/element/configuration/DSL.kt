package com.element.configuration

import com.element.configuration.model.GameEnvironment

fun onlyIn(vararg environments: GameEnvironment, block: () -> Unit) {
    if (Configuration.environment in environments) {
        block()
    }
}