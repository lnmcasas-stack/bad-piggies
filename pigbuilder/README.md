# Side-Scroll Smasher (NeoForge 1.21.11)

Original mod. All code and textures are new; nothing comes from any third-party game.
Targets Minecraft 1.21.11 and NeoForge 21.11.x. Java 21.

## Build the jar
1. Download the official MDK for 1.21.11 (github.com/NeoForgeMDKs/MDK-1.21.11-ModDevGradle) and copy its
   `gradlew`, `gradlew.bat` and `gradle/` folder into this project (so you get a matching Gradle version).
   Or install Gradle yourself and run `gradle wrapper`.
2. Run `./gradlew build` (Windows: `gradlew.bat build`).
3. The jar is `build/libs/pigbuilder-1.0.0.jar`.
4. If it can't resolve NeoForge, set `neo_version` in gradle.properties to a version from projects.neoforged.net.

## Prism Launcher
New instance: Minecraft 1.21.11, NeoForge 21.11.x. Edit, Mods, Add file, pick the jar.

## First time
A new world shows a short tutorial (7 chat messages). `/sidescroll tutorial` replays it, `/sidescroll tutorial skip` stops it.

## Commands: /sidescroll ...
help, start (kit + demo cart + side view), kit, tutorial [skip], side on|off,
spawn cart|plane|dozer, disassemble, clear, debug.
Turn them off with `commandsEnabled = false` in the server config.

## Controls
- G: toggle 2.5D side view (rebindable). A/D move, Space jump, Attack smashes/pushes. W/S + Attack = up/down.
- Right-click a contraption part with the Assembler to turn the connected blocks into a vehicle.
- Right-click the vehicle to ride (side view turns on). A/D drive, Space fires propellers, Shift dismounts.
- Shift + right-click it with the Assembler (or hit it in creative) to turn it back into blocks.
- Drive = wheels*4 / blocks. Lift = propellers*2 / blocks. Hovering needs lift of about 0.25 or more.

## Config (serverconfig/pigbuilder-server.toml)
maxHardness, smashCooldownTicks, pushGravityBlocks, contraptionBulldoze, contraptionMaxHardness (0 = push only),
contraptionBulldozeCooldown, maxContraptionBlocks, tutorialEnabled, commandsEnabled.
Tags: pigbuilder:pushable, pigbuilder:contraption_parts.

## Known limits
- Camera is vanilla third-person-back; blocks behind you push it in close. Use a flat world or a carved corridor.
- Pressing W/S in side view tries to walk along Z and gets snapped back, so you may see a small jitter.
- Contraptions can't contain block entities (chests, furnaces) and only move along X/Y.
- Rider sits on top of the structure.
