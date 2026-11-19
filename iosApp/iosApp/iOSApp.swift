import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        IosKoinKt.startKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
