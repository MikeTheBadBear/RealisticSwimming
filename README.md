# RealisticSwimming

A Bukkit/Paper plugin that adds configurable swimming, falling, stamina, and armor-weight mechanics.

## Supported server lines

- Paper 1.21.x using Java 21
- Paper 26.x using Java 25

The distributed JAR is compiled for Java 21 against the Paper 1.21 API. It uses only stable Bukkit/Paper API calls, so the same artifact can load on the newer Java 25 runtime used by Paper 26.x. Continuous integration also compiles the source directly against the current Paper 26.x API to catch removed or changed API calls.

## Build

Use Java 21 for the distributable artifact:

    mvn clean verify

Use Java 25 to run the forward-compatibility compile:

    mvn -Ppaper-26 clean verify

The normal JAR is written to target/RealisticSwimming-2.0.0.jar.

## Optional integrations

PlaceholderAPI, Glide, and NoCheatPlus remain optional. PlaceholderAPI support is compiled as a provided dependency and is not bundled into the plugin.
