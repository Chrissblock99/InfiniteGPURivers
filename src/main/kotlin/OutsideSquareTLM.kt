package me.chriss99

import glm_.vec2.Vec2i

open class OutsideSquareTLM<T>(radius: Int, center: Vec2i) : SquareTLM<T>(radius, center) {
    override fun loadCommander() = listOf<Vec2i>()
}