package net.coolshell.poite.shared.core.storage

import platform.Foundation.NSBundle
import platform.Foundation.NSHomeDirectory

actual fun kvStoreFileBytes(): Long {
    val bundleId = NSBundle.mainBundle.bundleIdentifier ?: return 0
    return fileSizeBytes(NSHomeDirectory() + "/Library/Preferences/$bundleId.plist")
}
