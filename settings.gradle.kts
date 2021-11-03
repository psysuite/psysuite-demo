include(":nativeaudio")
include(":psysuitecore")
project(":psysuitecore").projectDir = File(settingsDir, "../modules/psysuitecore/psysuitecore")

include(":core")
project(":core").projectDir = File(settingsDir, "../modules/core/core")

include(":app")
