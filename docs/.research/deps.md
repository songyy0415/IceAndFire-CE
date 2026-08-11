# Dependency & Build Migration Analysis — IceAndFire-CE (MC 1.21.1 → MC 26.2)

Research file for the Fabric-only migration of IceAndFire-CE (Architectury common/fabric/neoforge) from MC 1.21.1 Mojmap to MC 26.2 Mojmap.
Feeds `docs/dependency-migration.md` and `docs/migration-plan.md`.

All paths absolute. All versions below were verified against on-disk reference sources, not guessed (exceptions are marked 待确认).

---

## 1. Current platform state (MC 1.21.1)

| Item | Current value | Evidence |
|---|---|---|
| enabled_platforms | `fabric` | `D:/IceAndFire-CE/gradle.properties:10` |
| settings.gradle includes | `common`, `fabric` only | `D:/IceAndFire-CE/settings.gradle:12-13` |
| neoforge module | exists on disk but NOT included in build | `D:/IceAndFire-CE/neoforge/` present; not in settings.gradle |
| Loom plugin | `dev.architectury.loom` `1.11-SNAPSHOT` | `D:/IceAndFire-CE/build.gradle:2` |
| architectury-plugin | `3.4-SNAPSHOT` | `D:/IceAndFire-CE/build.gradle:3` |
| Shadow plugin | `com.github.johnrengelman.shadow` `8.1.1` | `D:/IceAndFire-CE/build.gradle:4` |
| Gradle wrapper | `8.14` | `D:/IceAndFire-CE/gradle/wrapper/gradle-wrapper.properties:3` |
| Java | Zulu `21` pinned, release 21 | `D:/IceAndFire-CE/gradle.properties:5`, `build.gradle:62-68` |
| Mappings | `loom.layered { officialMojangMappings() }` | `D:/IceAndFire-CE/build.gradle:51-53` |
| Minecraft | `1.21.1` | `D:/IceAndFire-CE/gradle.properties:12` |
| mod_version | `2.0-beta.16` | `D:/IceAndFire-CE/gradle.properties:7` |
| mappings-patch flatDir | `flatDir { dirs '../mappings-patch' }` — declared but never referenced by any `mappings` config | `D:/IceAndFire-CE/build.gradle:46`; jar contains one `mappings.tiny` mapping `class_1263.method_5438 -> getStackInSlot` (Inventory.getStackInSlot), header `tiny 2 0 intermediary named` |

Note: `neoforge/build.gradle:24` references `$rootProject.neoforge_version` but **no `neoforge_version` is defined in any gradle.properties**. The neoforge module currently cannot evaluate; it is excluded via settings.gradle so it is never loaded.

---

## 2. Full dependency inventory (MC 1.21.1)

### 2.1 `common/build.gradle` (`D:/IceAndFire-CE/common/build.gradle`)

| group:artifact:version | Config | Used for |
|---|---|---|
| `net.fabricmc:fabric-loader:0.16.7` (root prop `fabric_loader_version`) | `modImplementation` (line 13) | `@Environment` annotations in common only (per comment) |
| `dev.architectury:architectury:13.0.8` (`architectury_api_version`) | `modImplementation` (line 16) | Architectury common API (events/networking/platform) |
| `com.github.IAFEnvoy.Integration:integration-common:0.2` (`integration_version`, jitpack coordinate) | `modImplementation` (line 18) | `com.iafenvoy.integration.IntegrationExecutor` — used in `common/.../entity/DragonBaseEntity.java:28` and `common/.../IceAndFireClient.java:13` (loader-independent integration executor) |
| `maven.modrinth:uranus:FH0tB0dy` | `modImplementation` (line 20) | Animation engine / entity & AI helpers. Used in ~40 files: `data/DragonColor.java`, `data/SeaSerpentType.java`, `data/TrollType.java`, `entity/ai/DragonAIAttackMeleeGoal.java`, `entity/DragonBaseEntity.java`, etc. |
| `maven.modrinth:jupiter:5pNXzmee` | `modImplementation` (line 21) | Config library. Used in `config/IafClientConfig.java`, `config/IafCommonConfig.java`, `IceAndFire.java`, `IceAndFireClient.java` |
| `dev.emi:emi-fabric:1.1.19+1.21.1:api` (`emi_version`) | `modCompileOnly` (line 23) | EMI recipe compat — `compat/emi/IceAndFireEmiPlugin.java`, `compat/emi/ForgeRecipeHolder.java` |
| `mezz.jei:jei-1.21.1-fabric-api:19.21.0.247` (`jei_version`) | `modCompileOnly` (line 24) | JEI recipe compat — `compat/jei/*.java` (5 files) |
| `maven.modrinth:jade:pA0xvozk` | `modImplementation` (line 26) | Jade/WTHIT tooltip compat — `compat/jade/*.java` (6 files) |
| `net.createmod.ponder:Ponder-Fabric-1.21.1:1.0.61` (`ponder_version`) | `modImplementation` (line 28) | Create Ponder scenes — `compat/ponder/DragonForgeStoryBoard.java`, `compat/ponder/IceAndFirePonderPlugin.java` |

### 2.2 `fabric/build.gradle` (`D:/IceAndFire-CE/fabric/build.gradle`)

| group:artifact:version | Config | Used for |
|---|---|---|
| `net.fabricmc:fabric-loader:0.16.7` | `modImplementation` (line 32) | Fabric platform runtime |
| `net.fabricmc.fabric-api:fabric-api:0.116.5+1.21.1` (`fabric_api_version`) | `modImplementation` (line 35) | Fabric API modules (entity registry, brewing, events, networking) |
| `dev.architectury:architectury-fabric:13.0.8` | `modImplementation` (line 38) | Architectury Fabric platform impl (NOT bundled; separate mod in production) |
| `:common` namedElements | `common(...)` (line 40) | common classes on classpath |
| `:common` transformProductionFabric | `shadowBundle` (line 41) | bundled into dev-shadow jar by Shadow |
| `maven.modrinth:uranus:FH0tB0dy` | `modImplementation` (line 43) | same as common |
| `maven.modrinth:jupiter:5pNXzmee` | `modImplementation` (line 44) | same as common |
| `com.github.IAFEnvoy.Integration:integration-fabric:0.2` | `modImplementation(include(...))` (line 45) | bundled into the mod jar; `IntegrationExecutor` in `fabric/.../IceAndFireFabric.java:6` |
| `com.terraformersmc:modmenu:11.0.3` (`modmenu_version`) | `modApi` (line 49) | Config screen; entrypoint `com.iafenvoy.iceandfire.fabric.ModMenu` |
| `dev.emi:emi-fabric:1.1.19+1.21.1` | `modLocalRuntime` (line 52) | runtime for dev (EMI API already compileOnly in common) |
| `maven.modrinth:jade:pA0xvozk` | `modImplementation` (line 55) | same as common |
| `maven.modrinth:trinkets:JagCscwi` | `modApi` (line 58) | Trinket slots — `fabric/.../compat/trinkets/SimpleTickItemWrapper.java`, `TrinketsRegistry.java` |
| `dev.onyxstudios.cardinal-components-api:cardinal-components-base:6.1.1` (`cca_version`) | `modApi` (line 59) | **No direct source usage found** (grep of common+fabric for `cardinal|onyxstudios` = 0 hits). Likely needed by Trinkets at runtime or vestigial → 待确认 |
| `dev.onyxstudios.cardinal-components-api:cardinal-components-entity:6.1.1` | `modApi` (line 60) | ditto → 待确认 |
| `net.createmod.ponder:Ponder-Fabric-1.21.1:1.0.61` | `modCompileOnly` (line 62) | Ponder (compile only on fabric side) |

### 2.3 `neoforge/build.gradle` (not built — informational) (`D:/IceAndFire-CE/neoforge/build.gradle`)

| group:artifact:version | Config | Notes |
|---|---|---|
| `net.neoforged:neoforge:$rootProject.neoforge_version` | `neoForge` (line 24) | **`neoforge_version` undefined** — module would not evaluate |
| `dev.architectury:architectury-neoforge:13.0.8` | `modImplementation` (line 27) | |
| `com.github.IAFEnvoy.Integration:integration-neoforge:0.2` | `modImplementation(include(...))` (line 30) | |
| `maven.modrinth:uranus:BBb3HOQ5` | `modImplementation` (line 32) | NeoForge-specific modrinth file |
| `maven.modrinth:jupiter:m2itNS7Z` | `modImplementation` (line 33) | NeoForge-specific modrinth file |
| `dev.emi:emi-neoforge:1.1.19+1.21.1` | `modLocalRuntime` (line 35) | |
| `maven.modrinth:jade:JkFFfEao` | `modImplementation` (line 37) | |
| `top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1` | `modApi` (line 39) | |
| `curse.maven:projecte-226410:6611984` | `modCompileOnly` (line 41) | ProjectE |
| `net.createmod.ponder:Ponder-NeoForge-1.21.1:1.0.61` | `modCompileOnly` (line 43) | |
| `com.hollingsworth.ars_nouveau:ars_nouveau-1.21.1:5.10.3.1204` (`ars_version`) | `modCompileOnly` (line 45) | |
| `software.bernie.geckolib:geckolib-neoforge-1.21.1:4.7.7` (`geckolib_version`) | `modCompileOnly` (line 46) | |
| `top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1` | `modCompileOnly` (line 47) | duplicate of line 39 |

### 2.4 Repositories currently declared (`D:/IceAndFire-CE/build.gradle:26-47`)
terraformersmc, ladysnake, neoforged, theillusivec4 (curios), curse maven, modrinth maven, jitpack, nucleoid, blamejared, modmaven.dev, Fuzss modresources, createmod, geckolib cloudsmith, forge maven, `flatDir ../mappings-patch`.

---

## 3. TARGET versions for MC 26.2 (verified from reference sources)

| Dependency | Target | Evidence |
|---|---|---|
| Minecraft | `26.2` | `D:/aiminecraftdev/minecraft26.2/version.json` (`id: 26.2`, `java_version: 25`) |
| fabric-loader | `0.19.3` | AGENTS.md:78; `architectury-api26.2/gradle.properties`; `Uranus26.2/gradle.properties` |
| fabric-api | `0.156.0+26.2` | AGENTS.md:81 (`fabric-api-0.156.0-26.2`); `Uranus26.2/gradle.properties`; `Jupiter new/versions/26.2/gradle.properties` (note: architectury 26.2 itself builds against `0.154.2+26.2`, and EMI/Jade/Trinkets against 0.152-0.154 — but Uranus & Jupiter 26.2, the closest peers, use 0.156.0) |
| Architectury | `21.0.7` (`dev.architectury:architectury` / `-fabric`) | `architectury-api26.2/gradle.properties` `base_version=21.0`; `Uranus26.2/gradle.properties` `architectury_api_version=21.0.7`; confirmed present in local Gradle cache (`~/.gradle/.../dev.architectury/architectury/21.0.7` and `architectury-fabric/21.0.7`) |
| architectury-plugin | `3.5-SNAPSHOT` | `architectury-api26.2/build.gradle:2`; `Uranus26.2/build.gradle:3` |
| Loom | `dev.architectury.loom-no-remap` `1.17-SNAPSHOT` | `architectury-api26.2/build.gradle:3`; `Uranus26.2/build.gradle:2`. Both plugin IDs (`loom` and `loom-no-remap`) have `1.17-SNAPSHOT` markers in the local cache; release `dev.architectury:architectury-loom:1.17.491` also cached. Uranus (same-structure 26.2 mod) is the authoritative blueprint and uses `loom-no-remap`. |
| Gradle | `9.5.1` | `architectury-api26.2/gradle/wrapper/gradle-wrapper.properties`; `fabric-api-0.156.0-26.2/gradle/wrapper/gradle-wrapper.properties`; `Uranus26.2` wrapper |
| Java | **25** (was 21) | MC 26.2 `version.json` `java_version: 25`; `architectury-api26.2/settings.gradle:9-11` throws unless Java 25+; `architectury-api26.2/build.gradle` `release = 25`; `Uranus26.2/gradle.properties` pins `org.gradle.java.home=C:/Program Files/Zulu/zulu-25` |
| Shadow | `com.gradleup.shadow` `8.3.10` (Uranus) / `9.4.3` (architectury) | `Uranus26.2/build.gradle:4`; `architectury-api26.2/build.gradle:4`. The old `com.github.johnrengelman.shadow` is deprecated/moved to `com.gradleup.shadow`. |
| modmenu | `20.0.1` | `architectury-api26.2/gradle.properties`; `Jupiter new/versions/26.2/gradle.properties` |
| EMI | `1.1.24` (coordinate likely `dev.emi:emi-fabric:1.1.24+26.2` — exact suffix 待确认) | `D:/aiminecraftdev/emi26.2/gradle.properties` (`mod_version=1.1.24`) |
| JEI | `mezz.jei:jei-26.2-fabric-api:30.7.0.39` | `D:/aiminecraftdev/emi26.2/gradle.properties` (`jei_version=jei-26.2-fabric:30.7.0.39`) |
| Jade | `26.2.10` | `D:/aiminecraftdev/Jade26.2/gradle.properties` (`mod_version=26.2.10`) |
| Trinkets | `4.1.0-beta.3+26.2` | `D:/aiminecraftdev/trinkets26.2/gradle.properties` (`mod_version = 4.1.0-beta.3+26.2`) |
| Jupiter | `2.4.2` | `D:/aiminecraftdev/Jupiter new/gradle.properties` (`mod.version=2.4.2`); 26.2 sub-config: `fabric-api 0.156.0+26.2`, `fabric-loader 0.19.2`, `mod_menu 20.0.1`, `minecraft_version_range_fabric =>=26.2` (`versions/26.2/gradle.properties`) |
| Uranus | `2.4.1-bugfix` | `D:/aiminecraftdev/Uranus26.2/gradle.properties` (`mod_version=2.4.1-bugfix`) |
| Ponder | 待确认 — no 26.2 reference source on disk; current `net.createmod.ponder:Ponder-Fabric-1.21.1:1.0.61`. New coord would be `Ponder-Fabric-26.2:<v>` |
| Ars Nouveau / GeckoLib / Curios (neoforge-only) | 待确认 for 26.2 | no reference sources on disk |
| CCA (cardinal-components) | 待确认 whether still needed at all (no source usage found); Trinkets 26.2 (`trinkets26.2`) uses `yumi_version=1.1.1+26.2` (Ladysnake data API), not CCA — may be droppable |

---

## 4. Mapping approach for MC 26.2

**Big change**: MC 26.2 ships official (mojmap) class names directly — **no separate mappings artifact exists**, so there is no `mappings` line.

- `Uranus26.2/build.gradle` subprojects `dependencies` block, verbatim comment: *"MC 26.2 ships official (mojmap) class names directly — no separate mappings artifact exists, so loom-no-remap mode needs no `mappings` line."* — `minecraft "com.mojang:minecraft:$rootProject.minecraft_version"` only.
- Confirmed in `architectury-api26.2`: `grep mappings` over all its `*.gradle` returns **zero** hits; subprojects declare `minecraft "com.mojang:minecraft:..."` with no mappings layer.
- Consequence: the current `loom.layered { officialMojangMappings() }` block (`D:/IceAndFire-CE/build.gradle:51-53`) is **removed** on migration.
- `mappings-patch/yarn-mappings-patch-neoforge-mods.jar` (tiny mapping for `Inventory.getStackInSlot`) is currently unreferenced by any build config and becomes irrelevant under loom-no-remap.

### loom-no-remap changes the Gradle DSL (from `Uranus26.2`):
- **No `modImplementation` / `modApi` / `modCompileOnly` / `modLocalRuntime` / `modRuntimeOnly` configurations.** Use plain Gradle `implementation` / `api` / `compileOnly` / `runtimeOnly`. (`Uranus26.2/common/build.gradle` comment: "loom-no-remap (MC 26.2) has no `modImplementation` config — plain `implementation` is used."; `Uranus26.2/fabric/build.gradle` uses `api` for loader/fabric-api.)
- **No `remapJar` / no `include(...)`.** Bundled mods go into `META-INF/jars/` verbatim and must be listed in `fabric.mod.json` → `"jars": [ {"file": "META-INF/jars/<name>.jar"} ]`. (`Uranus26.2/fabric/build.gradle` `nestedJars` config; `Uranus26.2/fabric/src/main/resources/fabric.mod.json`).
- **The standard `jar` task IS the final artifact** (not shadowJar). Access widener injected via `loom { injectAccessWidener(tasks.named('jar')) }`. Shadow's `from(zipTree(...))` must be avoided because it re-expands nested `.jar` blobs. (`Uranus26.2/fabric/build.gradle`).
- AccessWidener + mixin JSON must be copied from `:common` resources into the fabric module resources (dev launcher requirement). (`Uranus26.2/fabric/build.gradle` processResources `from(project(':common').file(...))`).

**Implication for IceAndFire-CE**: currently `Integration` is `include(...)`ed (`fabric/build.gradle:45`) and architectury-fabric is `modImplementation` (not bundled). Under loom-no-remap:
- Integration must move to a `nestedJars`-style config → `META-INF/jars/` + a `"jars"` entry in `fabric.mod.json`.
- Architectury-fabric: currently NOT bundled in production (users install it separately); may stay unbundled or be nested like Uranus does — decision point.
- All `modImplementation`/`modApi`/`modCompileOnly`/`modLocalRuntime` keywords throughout common+fabric build files must be rewritten.

---

## 5. Modrinth version-file URLs — MUST be updated

These coordinates use **modrinth version-file hashes** (the third segment is a per-file hash), not semantic versions:

| Dependency | Current hash (1.21.1) | Location | 26.2 status |
|---|---|---|---|
| `maven.modrinth:uranus:FH0tB0dy` | FH0tB0dy | `common/build.gradle:20`, `fabric/build.gradle:43` | needs new 26.2 file hash (known new version: 2.4.1-bugfix) → 待确认 exact hash |
| `maven.modrinth:jupiter:5pNXzmee` | 5pNXzmee | `common/build.gradle:21`, `fabric/build.gradle:44` | needs new 26.2 file hash (known new version: 2.4.2) → 待确认 exact hash |
| `maven.modrinth:jade:pA0xvozk` | pA0xvozk | `common/build.gradle:26`, `fabric/build.gradle:55` | needs new 26.2 file hash (known new version: 26.2.10) → 待确认 exact hash |
| `maven.modrinth:trinkets:JagCscwi` | JagCscwi | `fabric/build.gradle:58` | needs new 26.2 file hash (known new version: 4.1.0-beta.3+26.2) → 待确认 exact hash |
| (neoforge) `maven.modrinth:uranus:BBb3HOQ5` | BBb3HOQ5 | `neoforge/build.gradle:32` | ditto |
| (neoforge) `maven.modrinth:jupiter:m2itNS7Z` | m2itNS7Z | `neoforge/build.gradle:33` | ditto |
| (neoforge) `maven.modrinth:jade:JkFFfEao` | JkFFfEao | `neoforge/build.gradle:37` | ditto |

New hashes must be resolved from the Modrinth API (network access required) — marked 待确认 until then.

---

## 6. Version constraints in metadata files

### `fabric/src/main/resources/fabric.mod.json` (`D:/IceAndFire-CE/fabric/src/main/resources/fabric.mod.json`)
- `depends.minecraft`: `"1.21.x"` (line 43) → must become `"26.x"` (Uranus 26.2 uses `"26.x"`; Jupiter 26.2 uses `">=26.2"`)
- `depends.jupiter`: `">=2.3"` (line 44) → bump floor to new version (2.4.2) or keep loose `>=2.3` — 待确认 policy
- `depends.uranus`: `">=2.3.2"` (line 45) → bump floor to new version (2.4.1) or keep loose — 待确认 policy
- `entrypoints` (lines 21-37): main/client/emi/jade/modmenu — unchanged shape, but check EMI & Jade 26.2 plugin entrypoint contracts in their migration docs.
- `jars` array: **does not exist yet** — required once Integration (and optionally architectury-fabric) are nested under META-INF/jars.

### `neoforge/src/main/resources/META-INF/neoforge.mods.toml` (not built, informational)
- `minecraft` range `[1.21,1.21.2)` → `[26.2,26.3)` 待确认
- `neoforge` range `[21.1.197,)` → 26.2.x (architectury 26.2 neoforge_version = 26.2.0.43-beta; trinkets = 26.2.0.28-beta) 待确认
- `uranus` `[2.3.2,)`, `jupiter` `[2.3,)` → bump floors 待确认

---

## 7. Toolchain / Java

- MC 26.2 **requires Java 25** (verified: `minecraft26.2/version.json` `"java_version": 25`; architectury 26.2 hard-fails below Java 25; all 26.2 reference mods use `release = 25`; `Uranus26.2/gradle.properties` pins `org.gradle.java.home=C:/Program Files/Zulu/zulu-25`).
- Current project pins Zulu **21** (`D:/IceAndFire-CE/gradle.properties:5`) and `release 21` (`build.gradle:67`) → must become Zulu 25 / `release 25`.
- Gradle 8.14 → 9.5.1 (needed for Java 25 support).
- CI (`D:/IceAndFire-CE/.github/workflows/build.yml`): currently `actions/setup-java` Java `'21'` temurin (line 17-19) + `gradle/actions/setup-gradle` `gradle-version: '8.14'` (line 23-24) → must become Java 25 + Gradle 9.5.1.
- fabric-loader 0.19.3 ships asm 9.10.1 / mixin 0.17.3 (`fabric-loader-0.19.3/gradle.properties`); loader itself still targets Java 8 bytecode (`fabric-loader build.gradle:143-144`) — runs on Java 25 fine.

---

## 8. Other observations / risks

1. **No remapping layer** in 26.2 → source names are already mojmap; the migration is purely a *version/API* migration, not a name migration. This matches AGENTS.md's claim that sources on disk are mojmap.
2. **Architectury artifact jump**: 13.0.8 → 21.0.x (base_version 21.0). Several consumer-visible changes are possible; check `architectury-api26.2` for renamed packages when the code migration starts.
3. **Integration** reference source is still on MC 1.20.4 (`D:/aiminecraftdev/Integration/gradle.properties`: `minecraft_version=1.20.4`, `mod_version=0.2`, loom 1.10-SNAPSHOT). AGENTS.md states Integration is loader-based (version-agnostic). Whether `integration-common/fabric:0.2` actually runs on 26.2 → **待确认** (verify IntegrationExecutor compatibility at runtime).
4. **CCA (cardinal-components-base/entity 6.1.1)** declared as modApi in fabric but **no source imports it** anywhere → likely vestigial or a Trinkets transitive requirement. Trinkets 26.2 now uses `yumi` (Ladysnake) instead → likely droppable. 待确认.
5. **Jupiter 26.2 is a universal/stonecutter build** (`Jupiter new` uses Stonecutter, active version 26.2) → the modrinth artifact for 26.2 will be a universal jar; the `maven.modrinth:jupiter:<hash>` coordinate still applies but hash must change.
6. **Loom version answer**: MC 26.2 does not itself "require" a Loom; the ecosystem consensus is: Architectury projects → `dev.architectury.loom-no-remap` 1.17-SNAPSHOT (+ architectury-plugin 3.5-SNAPSHOT); plain fabric projects → fabric-loom 1.16.2 (fabric-api 26.2) / 1.16-SNAPSHOT (Jade) / 1.15-SNAPSHOT (Trinkets). Architectury 26.2 also sets `loom.ignoreDependencyLoomVersionValidation=true`.
7. **Shadow plugin switch**: `com.github.johnrengelman.shadow:8.1.1` → `com.gradleup.shadow` (8.3.10 in Uranus, 9.4.3 in architectury 26.2). For the fabric module, Uranus's pattern means shadowJar is NOT used for the final jar (plain `jar` + META-INF/jars nesting instead).
8. **repositories**: the flatDir `../mappings-patch` repo can be removed on migration; the many maven repos may be trimmed but keeping them is harmless.

---

## 9. Quick old → new version table (summary)

| Item | Old (1.21.1) | New (26.2) |
|---|---|---|
| minecraft | 1.21.1 | 26.2 |
| Java | 21 | 25 |
| Gradle | 8.14 | 9.5.1 |
| Loom plugin | `dev.architectury.loom` 1.11-SNAPSHOT | `dev.architectury.loom-no-remap` 1.17-SNAPSHOT |
| architectury-plugin | 3.4-SNAPSHOT | 3.5-SNAPSHOT |
| Shadow | com.github.johnrengelman.shadow 8.1.1 | com.gradleup.shadow 8.3.10 / 9.4.3 |
| mappings | `loom.layered{ officialMojangMappings() }` | none (MC ships mojmap) |
| fabric-loader | 0.16.7 | 0.19.3 |
| fabric-api | 0.116.5+1.21.1 | 0.156.0+26.2 |
| architectury | 13.0.8 | 21.0.7 |
| modmenu | 11.0.3 | 20.0.1 |
| emi | 1.1.19+1.21.1 | 1.1.24 (+26.2?) 待确认 |
| jei | jei-1.21.1-fabric-api 19.21.0.247 | jei-26.2-fabric-api 30.7.0.39 |
| jade (modrinth) | pA0xvozk | new hash; version 26.2.10 |
| trinkets (modrinth) | JagCscwi | new hash; version 4.1.0-beta.3+26.2 |
| uranus (modrinth) | FH0tB0dy | new hash; version 2.4.1-bugfix |
| jupiter (modrinth) | 5pNXzmee | new hash; version 2.4.2 |
| integration | 0.2 (jitpack, MC 1.20.4 build) | 待确认 (loader-based; runtime check) |
| ponder | Ponder-Fabric-1.21.1:1.0.61 | 待确认 (no 26.2 source on disk) |
| cca | 6.1.1 (modApi, unused) | likely droppable 待确认 |
| ars/geckolib/curios (neoforge-only) | 5.10.3.1204 / 4.7.7 / 9.5.1+1.21.1 | 待确认 |
