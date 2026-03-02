# Copilot Instructions — SwordofMagic7

## Build & Lint

```bash
./gradlew shadowJar        # Build the plugin JAR (output: build/libs/)
./gradlew lintKotlin       # Lint Kotlin code (runs in CI on push to master)
./gradlew formatKotlin     # Auto-format Kotlin code
./gradlew deploy           # Build + deploy JAR to dev server via SFTP (requires gradle.properties credentials)
```

No test suite exists in this project.

## Architecture

This is a **PaperMC 1.21.11** plugin (Java 21, Kotlin 2.x) that implements an MMORPG game system.

### Dual-language codebase (actively migrating Java → Kotlin)
- **Legacy Java** lives in `src/main/java/swordofmagic7/` (package `swordofmagic7.*`)
- **New Kotlin** lives in `src/main/kotlin/net/somrpg/swordofmagic7/` (package `net.somrpg.swordofmagic7.*`)
- The goal is to migrate all Java code to Kotlin. New code should be written in Kotlin under the `net.somrpg.swordofmagic7` package.

### Entry point
`net.somrpg.swordofmagic7.SomCore` — extends `SuspendingJavaPlugin` (MCCoroutine) for coroutine support.

### Central data store
`swordofmagic7.Data.DataBase` — static class holding all game data (items, mobs, maps, classes, shops, etc.) loaded from YAML files at `plugins/SwordofMagic7/Database/`.

### Per-player state
`swordofmagic7.Data.PlayerData` — loaded on join, unloaded on quit. Access via the extension function:
```kotlin
val data = player.getPlayerData()  // net.somrpg.swordofmagic7.extensions
```

### Command system
Commands use **ACF (Annotation Command Framework)** via `PaperCommandManager`. Any class extending `BaseCommand` placed under the `net.somrpg.swordofmagic7.commands` package is **auto-registered** at startup via `PackageClassFinder` — no manual registration needed.

```kotlin
@CommandAlias("mycommand")
@CommandPermission("som7.user")
class MyCommand : BaseCommand() {
    @Default
    fun default(sender: Player) {
        sender.sendMessage("You ran /mycommand!")
    }
}
```

Subpackages by audience: `commands/user/`, `commands/developer/`, `commands/builder/`, `commands/user/settings/`.

### Async / threading
- Use `NewMultiThread.runTaskAsync { }` for fire-and-forget async work.
- Use `asyncDispatcher` / `minecraftDispatcher` (from `net.somrpg.swordofmagic7.extensions`) with `SomCore.instance.launch { }` for coroutine-based async.
- Bukkit API calls must be dispatched on `minecraftDispatcher`.

### Key libraries bundled (shaded)
| Library | Purpose |
|---|---|
| ACF Paper (`co.aikar:acf-paper`) | Command framework |
| Triumph GUI (`dev.triumphteam:triumph-gui`) | Inventory GUIs |
| Item NBT API (`de.tr7zw:item-nbt-api`) | NBT access |
| EvalEx (`com.ezylang:EvalEx`) | Formula evaluation |
| MCCoroutine | Kotlin coroutines on Bukkit |
| kotlinx.serialization + kaml | JSON/YAML serialization |

### Key soft/hard dependencies (not shaded)
- **ForestRedisAPI** — cross-server data sync
- **DecentHolograms** — holograms above mobs/players
- **LibsDisguises** — mob disguises (used for NPCs)
- **PacketEvents** — low-level packet handling
- **Floodgate** (soft) — Bedrock crossplay via `BedrockAdapter`
- **BlueMap** (soft) — map markers

## Key Conventions

- **Permissions**: `som7.developer` (OP default), `som7.builder` (OP default), `som7.user` (TRUE default), `som7.chat` (TRUE default).
- **All user-facing strings are in Japanese.** ACF locale is set to `Locale.JAPANESE`. ACF message overrides live in `src/main/resources/acf-SwordofMagic7_ja.properties`.
- **Kotlin commands must be annotated with `@file:Suppress("unused")`** because ACF instantiates them reflectively.
- **Linter**: kotlinter (ktlint rules). Run `./gradlew formatKotlin` before committing Kotlin changes.
- **Version metadata**: version string appends the git commit abbreviation (or `-Dev` suffix on dirty working tree) via `versionMetadata()` in `build.gradle.kts`.
- **Shading**: relocated packages live under `net.somrpg.swordofmagic7.libs.*` (see `tasks.shadowJar` in `build.gradle.kts`).
- **Data files**: all persistent game data (items, mobs, dungeons, etc.) is YAML, stored at the path configured as `databasePath` in `config.yml` (default: `plugins/SwordofMagic7/Database`).
