package net.coolshell.poite.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
