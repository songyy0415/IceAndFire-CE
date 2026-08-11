# Jupiter API Migration Research — old (2.3.7) → new (2.4.2)

Research date: 2026-08-09
Project: IceAndFire-CE (MC 1.21.1 Mojmap, Architectury common/fabric/neoforge) → MC 26.2 Mojmap

Sources on disk:
- OLD: `D:/aiminecraftdev/Jupiter old`  (version **2.3.7**, MC 1.18.2–1.21.10, fabric+forge+neoforge)
- NEW: `D:/aiminecraftdev/Jupiter new`  (version **2.4.2**, MC **26.1.2 / 26.2 only**, neoforge-universal + fabric compileOnly)
- MC 26.2 mojmap: `D:/aiminecraftdev/minecraft26.2`

---

## 0. Headline findings (read first)

1. **Package root is UNCHANGED**: `com.iafenvoy.jupiter` in both versions. All imports stay valid.
2. **The single load-bearing change is MC-level, not Jupiter-level**: old Jupiter is compiled against MC 1.21.1 mojmap where the id type is `net.minecraft.resources.ResourceLocation`; new Jupiter is compiled against MC 26.2 mojmap where that class is renamed to **`net.minecraft.resources.Identifier`**. Every Jupiter container constructor that the project calls takes this id type, so the project's `ResourceLocation.fromNamespaceAndPath(...)` arguments must become `Identifier.fromNamespaceAndPath(...)`. (`Identifier.fromNamespaceAndPath` exists and has the same signature — verified `D:/aiminecraftdev/minecraft26.2/net/minecraft/resources/Identifier.java:40`.)
3. **Every other Jupiter API the project uses is unchanged** (same signatures, same behavior): `ConfigManager`, `ServerConfigManager` (+ `PermissionChecker.IS_OPERATOR`), `AutoInitConfigContainer` + nested `AutoInitConfigCategoryBase`, `FileConfigContainer`, and the `BooleanEntry`/`DoubleEntry`/`IntegerEntry`/`SeparatorEntry` builders, and `ConfigSelectScreen.builder(...)`.
4. **Version / coordinates**: 2.3.7 → 2.4.2. Project currently pins Modrinth `maven.modrinth:jupiter:5pNXzmee` (common+fabric) and `maven.modrinth:jupiter:m2itNS7Z` (neoforge) — see §6 for the "待确认" new coordinate.
5. New Jupiter dropped the **Forge** loader entirely (fabric + neoforge only), renamed `ResourceLocationEntry`→`IdentifierEntry`, removed the `com.iafenvoy.jupiter.interfaces` package, and internally switched to a `JupiterProxies` service-locator. None of these affect the project's current usage (see §5 for details).

---

## 1. All project usages of Jupiter (every file:line)

Grep `com.iafenvoy.jupiter` in `D:/IceAndFire-CE/common/src` and `D:/IceAndFire-CE/fabric/src`. Five source files; 10 distinct symbols.

| # | Project file | Line(s) | Jupiter symbol used |
|---|--------------|---------|---------------------|
| 1 | `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/IceAndFire.java` | 11, 42 | `com.iafenvoy.jupiter.ConfigManager` — `getInstance().registerConfigHandler(IafCommonConfig.INSTANCE)` |
| 2 | `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/IceAndFire.java` | 12, 43 | `com.iafenvoy.jupiter.ServerConfigManager` — `registerServerConfig(IafCommonConfig.INSTANCE, PermissionChecker.IS_OPERATOR)` |
| 3 | `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/IceAndFireClient.java` | 14, 22 | `com.iafenvoy.jupiter.ConfigManager` — `getInstance().registerConfigHandler(IafClientConfig.INSTANCE)` |
| 4 | `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/config/IafCommonConfig.java` | 4, 11 | `com.iafenvoy.jupiter.config.container.AutoInitConfigContainer` (extends) — ctor `(ResourceLocation, String, String)` |
| 5 | `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/config/IafCommonConfig.java` | 5 | `com.iafenvoy.jupiter.config.entry.BooleanEntry` — `builder(...).key(...).build()` |
| 6 | `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/config/IafCommonConfig.java` | 6 | `com.iafenvoy.jupiter.config.entry.DoubleEntry` — `builder(...).min(...).range(...).key(...).build()` |
| 7 | `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/config/IafCommonConfig.java` | 7 | `com.iafenvoy.jupiter.config.entry.IntegerEntry` — `builder(...).min(...).range(...).key(...).build()` |
| 8 | `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/config/IafCommonConfig.java` | 8 | `com.iafenvoy.jupiter.config.entry.SeparatorEntry` — `builder().build()` |
| 9 | `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/config/IafCommonConfig.java` | 40 (×20 subclasses) | `AutoInitConfigCategoryBase` (nested static class of `AutoInitConfigContainer`, referenced unqualified) |
| 10 | `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/config/IafClientConfig.java` | 4, 8 | `com.iafenvoy.jupiter.config.container.FileConfigContainer` (extends) — ctor `(ResourceLocation, String, String)`; uses inherited `createTab(...).addEntry(...)` |
| 11 | `D:/IceAndFire-CE/fabric/src/main/java/com/iafenvoy/iceandfire/fabric/ModMenu.java` | 5, 16 | `com.iafenvoy.jupiter.render.screen.ConfigSelectScreen` — `builder(Component, Screen).server(...).client(...).build()` |

Methods/fields invoked per symbol (verified in sources):

- `ConfigManager.getInstance()` → `ConfigManager` (static field `INSTANCE`) — unchanged
- `ConfigManager#registerConfigHandler(AbstractConfigContainer)` — unchanged
- `ServerConfigManager#registerServerConfig(AbstractConfigContainer, PermissionChecker)` — unchanged
- `ServerConfigManager.PermissionChecker.IS_OPERATOR` — unchanged
- `AutoInitConfigContainer#<init>(ResourceLocation/Identifier, String titleKey, String path)` — type-arg rename only
- `AutoInitConfigContainer.AutoInitConfigCategoryBase#<init>(String id, String translateKey)` — unchanged
- `FileConfigContainer#<init>(ResourceLocation/Identifier, String titleKey, String path)` — type-arg rename only
- `AbstractConfigContainer#createTab(String, String)` + `ConfigGroup#addEntry(ConfigEntry<?>)` — unchanged
- `BooleanEntry.builder(String, boolean)`, `Builder#key(String)`, `#build()` — unchanged
- `DoubleEntry.builder(String, double)`, `Builder#min(double)`, `#range(double,double)`, `#key(String)`, `#build()` — unchanged
- `IntegerEntry.builder(String, int)`, `Builder#min(int)`, `#range(int,int)`, `#key(String)`, `#build()` — unchanged
- `SeparatorEntry.builder()` → `Builder#build()` — unchanged
- `ConfigSelectScreen.builder(String, Screen)` / `(Component, Screen)`, `Builder#server(AbstractConfigContainer)`, `#client(AbstractConfigContainer)`, `#build()` — unchanged

---

## 2. OLD Jupiter API surface (2.3.7)

Package root: `com.iafenvoy.jupiter`. ~165 source files. Full file list below (only packages/classes the migration may touch are annotated).

```
com.iafenvoy.jupiter
├── ConfigManager.java                (used by project)
├── Jupiter.java
├── Platform.java                     (static util; was loader-switched via stonecutter)
├── ServerConfigManager.java          (used by project)
├── _loader/
│   ├── fabric/  JupiterFabric, JupiterFabricClient, compat/ModMenu,
│   │            network/ClientNetworkHelperImpl, ServerNetworkHelperImpl,
│   │            reloader/ClientConfigReloader, ServerConfigReloader
│   ├── forge/   JupiterForge, JupiterForgeClient, network/...(incl. packet/ByteBufC2S, ByteBufS2C)   [loader dropped in NEW]
│   └── neoforge/ JupiterNeoForge, JupiterNeoForgeClient, network/...(incl. packet/ByteBufC2S, ByteBufS2C)
├── api/          JupiterConfig, JupiterConfigEntry
├── compat/       ExtraConfigHolder, ExtraConfigManager, clothconfig/{ClothConfigHolder,ClothConfigLoader},
│                 forgeconfigspec/{ConfigSpecLoader,NightConfigHolder}
├── config/
│   ├── ConfigDataFixer, ConfigGroup (used: addEntry), ConfigSide, ConfigSource
│   ├── container/  AbstractConfigContainer, AutoInitConfigContainer (used), FileConfigContainer (used),
│   │               wrapper/{ExtraConfigWrapper, RemoteConfigWrapper}
│   ├── entry/      BaseEntry, BooleanEntry (used), ConfigGroupEntry, DoubleEntry (used), EnumEntry,
│   │               FloatEntry, IntegerEntry (used), LongEntry, StringEntry, SeparatorEntry (used),
│   │               ResourceLocationEntry, ListBaseEntry, ListBooleanEntry, ListDoubleEntry, ListEnumEntry,
│   │               ListIntegerEntry, ListLongEntry, ListStringEntry, MapBaseEntry, MapDoubleEntry,
│   │               MapIntegerEntry, MapStringEntry, EntryBaseEntry, EntryDoubleEntry, EntryIntegerEntry,
│   │               EntryStringEntry
│   ├── interfaces/  ConfigBuilder, ConfigEntry, ConfigMetaProvider, RangeConfigEntry, TextFieldConfigEntry,
│   │                ValueChangeCallback
│   └── type/        ConfigType, ConfigTypes, ListConfigType, MapConfigType, SingleConfigType
├── interfaces/     IConfigEntry, IConfigEnumEntry                  [package removed in NEW]
├── internal/       ConfigButtonReplaceStrategy, JupiterSettings
├── mixin/          AutoConfigAccessor, AutoConfigMixin             [AutoConfigMixin → AutoConfigClientMixin]
├── network/        ByteBufHelper, ClientConfigNetwork, ClientNetworkHelper, NetworkConstants,
│                   ServerConfigNetwork, ServerNetworkHelper, payload/{ConfigErrorPayload,ConfigRequestPayload,ConfigSyncPayload}
├── render/
│   ├── BadgeRenderer, JupiterRenderContext [removed], TitleStack
│   ├── internal/   JupiterConfigListScreen, JupiterConfigListWidget
│   ├── screen/     ClientConfigScreen [removed], ConfigContainerScreen, ConfigListScreen,
│   │               ConfigSelectScreen (used), JupiterScreen, ServerConfigScreen [removed],
│   │               SingleConfigScreen, WidgetBuilderManager, dialog/{AbstractListDialog,Dialog,EnumSelectDialog,
│   │               EnumSelectWidget,ListDialog,MapDialog}, scrollbar/{HorizontalScrollBar,VerticalScrollBar}
│   └── widget/     SimpleButtonTooltip [removed], StringWidget [removed], TextFieldWithErrorWidget, WidgetBuilder,
│                   builder/{AbstractButtonWidgetBuilder,ButtonWidgetBuilder,ConfigGroupWidgetBuilder,EntryWidgetBuilder,
│                   EnumWidgetBuilder,ListWidgetBuilder,MapWidgetBuilder,SeparatorWidgetBuilder,TextFieldWidgetBuilder}
├── test/           TestConfig
└── util/           Comment, CopyOnWriteHashMap [removed], EnumHelper, JupiterUtils, RLUtil [removed],
                    TextFormatter, TextUtil [removed]
```

---

## 3. NEW Jupiter API surface (2.4.2)

Package root: `com.iafenvoy.jupiter` (UNCHANGED). Loaders: **fabric + neoforge only** (forge removed).

```
com.iafenvoy.jupiter
├── ConfigManager.java                (used by project — unchanged)
├── Jupiter.java
├── JupiterProxies.java               [NEW — service locator: PLATFORM, CLIENT_NETWORKING, SERVER_NETWORKING]
├── Platform.java                     [CHANGED — static util → interface, obtained via JupiterProxies.PLATFORM]
├── ServerConfigManager.java          (used by project — unchanged)
├── _loader/
│   ├── fabric/   FabricPlatform [NEW], JupiterFabric, JupiterFabricClient, compat/ModMenu,
│   │             network/FabricClientNetworkHelper [was ClientNetworkHelperImpl],
│   │             network/FabricServerNetworkHelper [was ServerNetworkHelperImpl]
│   └── neoforge/ NeoForgePlatform [NEW], JupiterNeoForge, JupiterNeoForgeClient,
│                 network/NeoForgeClientNetworkHelper [was ClientNetworkHelperImpl],
│                 network/NeoForgeServerNetworkHelper [was ServerNetworkHelperImpl]
├── api/          JupiterConfig, JupiterConfigEntry (Identifier now)
├── compat/       ExtraConfigHolder, ExtraConfigManager, clothconfig/{ClothConfigHolder,ClothConfigLoader},
│                 forgeconfigspec/{ConfigSpecLoader,NightConfigHolder}
├── config/
│   ├── ConfigDataFixer, ConfigGroup, ConfigSide, ConfigSource
│   ├── container/  AbstractConfigContainer, AutoInitConfigContainer (used), FileConfigContainer (used),
│   │               wrapper/{ExtraConfigWrapper, RemoteConfigWrapper}
│   ├── entry/      BaseEntry, BooleanEntry (used), ConfigGroupEntry, DoubleEntry (used), EnumEntry,
│   │               FloatEntry, IntegerEntry (used), LongEntry, StringEntry, SeparatorEntry (used),
│   │               IdentifierEntry [was ResourceLocationEntry], ListBaseEntry, ListBooleanEntry,
│   │               ListDoubleEntry, ListEnumEntry, ListIntegerEntry, ListLongEntry, ListStringEntry,
│   │               MapBaseEntry, MapDoubleEntry, MapIntegerEntry, MapStringEntry, EntryBaseEntry,
│   │               EntryDoubleEntry, EntryIntegerEntry, EntryStringEntry
│   ├── interfaces/  ConfigBuilder, ConfigEntry, ConfigMetaProvider, RangeConfigEntry, TextFieldConfigEntry,
│   │                ValueChangeCallback
│   └── type/        ConfigType, ConfigTypes, ListConfigType, MapConfigType, SingleConfigType
├── internal/       ConfigButtonReplaceStrategy, JupiterSettings
├── mixin/          AutoConfigAccessor, AutoConfigClientMixin [was AutoConfigMixin]
├── network/        ClientConfigNetwork, ClientNetworkHelper, ServerConfigNetwork, ServerNetworkHelper,
│                   payload/{ConfigErrorPayload,ConfigRequestPayload,ConfigSyncPayload}
├── render/
│   ├── BadgeRenderer, TitleStack
│   ├── internal/   JupiterConfigListScreen, JupiterConfigListWidget
│   ├── screen/     ConfigContainerScreen, ConfigListScreen, ConfigSelectScreen (used), JupiterScreen,
│   │               SingleConfigScreen, WidgetBuilderManager, dialog/{...same...}, scrollbar/{...same...}
│   └── widget/     TextFieldWithErrorWidget, WidgetBuilder, builder/{...same...}
├── test/           TestConfig
└── util/           Comment, EnumHelper, JupiterUtils, MinecraftHelper [NEW — replaces TextUtil],
                    TextFormatter
```

---

## 4. Master old → new mapping table (all symbols, incl. ones the project touches)

### 4a. Symbols the project actually uses (migration-relevant)

| Old symbol | New symbol | Where used in project | Migration note |
|---|---|---|---|
| `com.iafenvoy.jupiter.ConfigManager` | **unchanged** `com.iafenvoy.jupiter.ConfigManager` | IceAndFire.java:42, IceAndFireClient.java:22 | No change. `getInstance()`, `registerConfigHandler(AbstractConfigContainer)` identical (new file lines 19,29). |
| `com.iafenvoy.jupiter.ServerConfigManager` | **unchanged** | IceAndFire.java:43 | No change. `registerServerConfig(AbstractConfigContainer, PermissionChecker)` identical (new lines 21-23). |
| `ServerConfigManager.PermissionChecker.IS_OPERATOR` | **unchanged** | IceAndFire.java:43 | No change (new line 59). Constant list identical: `ALWAYS_TRUE`, `ALWAYS_FALSE`, `IS_DEDICATE_SERVER`, `IS_LOCAL_GAME`, `IS_OPERATOR`. |
| `AutoInitConfigContainer#<init>(ResourceLocation, String, String)` | `AutoInitConfigContainer#<init>(Identifier, String, String)` | IafCommonConfig.java:36 `super(ResourceLocation.fromNamespaceAndPath(MOD_ID,"common"), ...)` | **ONLY real code change.** Import `net.minecraft.resources.Identifier` and call `Identifier.fromNamespaceAndPath(...)` (exists, same sig — mc26.2 Identifier.java:40). |
| `AutoInitConfigContainer.AutoInitConfigCategoryBase` (nested) | **unchanged** | IafCommonConfig.java:40..323 (20 subclasses) | No change. Same nested class, same `(String id, String translateKey)` ctor, same reflection-based auto-init. |
| `FileConfigContainer#<init>(ResourceLocation, String, String)` | `FileConfigContainer#<init>(Identifier, String, String)` | IafClientConfig.java:15 `super(ResourceLocation.fromNamespaceAndPath(MOD_ID,"client"), ...)` | Same rename as AutoInitConfigContainer. |
| `AbstractConfigContainer#createTab(String, String)` + `ConfigGroup#addEntry(ConfigEntry<?>)` | **unchanged** | IafClientConfig.java:20-23 | No change (old AbstractConfigContainer.java:46, ConfigGroup.java:32 / new same lines). |
| `BooleanEntry.builder(String, boolean)` / `Builder.key(String)` / `Builder.build()` | **unchanged** | IafCommonConfig.java (many), IafClientConfig.java:10-12 | No change (new BooleanEntry.java:31, BaseEntry.java:118,157). |
| `DoubleEntry.builder(String, double)` / `.min(double)` / `.range(double,double)` / `.key(String)` / `.build()` | **unchanged** | IafCommonConfig.java (many) | No change (new DoubleEntry.java:66-97, BaseEntry). |
| `IntegerEntry.builder(String, int)` / `.min(int)` / `.range(int,int)` / `.key(String)` / `.build()` | **unchanged** | IafCommonConfig.java (many) | No change (new IntegerEntry.java:62-97). |
| `SeparatorEntry.builder()` / `Builder.build()` | **unchanged** | IafCommonConfig.java:46,51,80,264 | No change. Note: new SeparatorEntry removes the deprecated instance mutators `.text()`/`.tooltip()` and the public no-arg ctor; project only uses the builder. |
| `ConfigSelectScreen.builder(String/Component, Screen)` / `.server(AbstractConfigContainer)` / `.client(AbstractConfigContainer)` / `.build()` | **unchanged** | fabric ModMenu.java:16 | No change (new ConfigSelectScreen.java:122-168). The deprecated public ctor `(Component, Screen, FileConfigContainer, FileConfigContainer)` was removed; project uses the builder. |

### 4b. Deprecated-in-old → removed-in-new (not used by project, listed for completeness)

| Old | New | Note |
|---|---|---|
| `BooleanEntry(String, boolean)`, `IntegerEntry(String,int[,int,int])`, `DoubleEntry(String,double[,double,double])`, `SeparatorEntry()`, `BaseEntry(String,T)`, `ConfigGroup.add(IConfigEntry<?>)`, `BaseEntry.json(String)`, `BaseEntry.visible(boolean)` | **removed** | All `@Deprecated(forRemoval=true)` old entry constructors/mutators were deleted in new. Builder-only API now. |
| `SeparatorEntry.text(...)` / `.tooltip(...)` (instance) | **removed** | Builder-only now (`Builder.text`, `Builder.tooltip` remain). |
| `AbstractConfigContainer#getBackgroundTexture(boolean)` | **removed** | Return type `ResourceLocation`/`Identifier`; method deleted. |
| `AbstractConfigContainer#shouldLoad(JsonObject)` / `#readCustomData` / `#writeCustomData` | **removed** | Were already `@Deprecated(forRemoval=true)`. |

### 4c. Renames / removed / added classes (not used by project — context only)

| Old | New | Kind |
|---|---|---|
| `config/entry/ResourceLocationEntry` | `config/entry/IdentifierEntry` | renamed (type `ResourceLocation`→`Identifier`; `ConfigTypes.RESOURCE_LOCATION` constant unchanged) |
| `interfaces/IConfigEntry`, `interfaces/IConfigEnumEntry` | — (removed) | package `com.iafenvoy.jupiter.interfaces` deleted |
| `render/JupiterRenderContext` | — (removed) | render abstraction deleted |
| `render/screen/ClientConfigScreen`, `ServerConfigScreen` | — (removed) | merged into `ConfigSelectScreen` flow |
| `render/widget/StringWidget`, `SimpleButtonTooltip` | — (removed) | widget util classes deleted |
| `util/TextUtil` | `util/MinecraftHelper` | `TextUtil.translatable(...)` replaced by `Component.translatable(key, new Object[]{})`; `MinecraftHelper.openScreen(...)` added |
| `util/RLUtil`, `util/CopyOnWriteHashMap` | — (removed) | replaced by `Identifier.fromNamespaceAndPath` / `java.util.concurrent.ConcurrentHashMap` |
| `network/NetworkConstants` | — (removed) | constants inlined |
| `mixin/AutoConfigMixin` | `mixin/AutoConfigClientMixin` | targets `me.shedaniel.autoconfig.AutoConfigClient` (cloth-config renamed its class) |
| `_loader/fabric/network/ClientNetworkHelperImpl`, `ServerNetworkHelperImpl` | `_loader/fabric/network/FabricClientNetworkHelper`, `FabricServerNetworkHelper` | renamed |
| `_loader/neoforge/network/ClientNetworkHelperImpl`, `ServerNetworkHelperImpl` | `_loader/neoforge/network/NeoForgeClientNetworkHelper`, `NeoForgeServerNetworkHelper` | renamed |
| `_loader/forge/*` (entire loader, incl. network + packet/ByteBufC2S, ByteBufS2C) | — (removed) | Forge loader dropped in new |
| `_loader/neoforge/network/packet/ByteBufC2S`, `ByteBufS2C` | — (removed) | pre-1.20.5 bytebuf packet path removed |
| `_loader/fabric/reloader/ClientConfigReloader`, `ServerConfigReloader` | — (removed) | reload now handled inline via `ServerConfigManager` (implements `ResourceManagerReloadListener`) + new Fabric `ResourceLoader` API |
| `Platform` (final class, static methods, stonecutter loader-switched) | `Platform` (interface) | obtained via `JupiterProxies.PLATFORM`; implemented by `FabricPlatform` / `NeoForgePlatform` |
| — | `com.iafenvoy.jupiter.JupiterProxies` | NEW service locator holding `PLATFORM`, `CLIENT_NETWORKING`, `SERVER_NETWORKING` |
| — | `_loader/fabric/FabricPlatform`, `_loader/neoforge/NeoForgePlatform` | NEW platform impls |
| — | `util/MinecraftHelper` | NEW util |

---

## 5. Notable broader changes (even though not required by current project code)

1. **MC id-type rename `ResourceLocation` → `Identifier`** is systemic throughout new Jupiter (every `ResourceLocation` import in old sources is `Identifier` in new — e.g. ConfigManager.java:5, ServerConfigManager.java:5, AbstractConfigContainer.java:13, JupiterConfigEntry.java:6, IdentifierEntry.java:9). For the project, this only surfaces in the two config-container constructor calls. Cross-reference with the MC API migration doc (`ResourceLocation`→`Identifier` is a 26.2 mojmap rename, verified via `D:/aiminecraftdev/minecraft26.2/net/minecraft/resources/Identifier.java`).
2. **Registration pattern for config containers**: `ConfigManager.getInstance().registerConfigHandler(container)` and `ServerConfigManager.registerServerConfig(container, checker)` are **byte-for-byte the same** in old and new. No new registration step is required by the project.
3. **Fabric entrypoint changed internally**: old `JupiterFabric.onInitialize()` registered a `ServerConfigReloader` via `ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(...)` (Fabric API); new one wires `JupiterProxies.*` then uses the new Fabric API `ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(Identifier, reloadListener)` + `ResourceLoader.get(PackType.CLIENT_RESOURCES)` for the client side. This is a Jupiter-internal change (and a Fabric API change for 26.2); mod authors do nothing.
4. **Cloth-config compat changed**: old mixin targeted `me.shedaniel.autoconfig.AutoConfig`; new `AutoConfigClientMixin` targets `me.shedaniel.autoconfig.AutoConfigClient`. Relevant only to users of Jupiter's cloth-config bridging.
5. **Translation construction**: new Jupiter calls `Component.translatable(key, new Object[]{})` everywhere (26.2 `Component.translatable(String)` was removed/changed). `Component.empty()` replaces `TextUtil.empty()`. Project code already passes `Component.translatable(...)` directly and is unaffected.
6. **`ServerConfigManager.PermissionChecker.IS_LOCAL_GAME` internals changed** (old `player.getGameProfile()` → new `player.nameAndId()`; `server.getOperatorUserPermissionLevel()` → `server.operatorUserPermissions().level()`, new `net.minecraft.server.permissions.Permission.HasCommandLevel`). These are internal implementations; the public constant `IS_OPERATOR` used by the project is unchanged.

---

## 6. Module / Gradle coordinates

- **Old (2.3.7)**: `gradle.properties` → `mod.group=com.iafenvoy`, `mod.version=2.3.7`, `mod.id=jupiter`. Stonecutter versions 1.18.2→1.21.10 × fabric/forge/neoforge. Loader deps: fabric-loader 0.17.3; builds `build.{fabric,forge,neoforge}.gradle.kts`.
- **New (2.4.2)**: `gradle.properties` → `mod.version=2.4.2`. Stonecutter versions **26.1.2 and 26.2 only**, single `build.gradle.kts` using `net.neoforged.moddev` (universal neoforge jar, fabric compileOnly). Modrinth-only publishing (`publish.curseforge` retained; Modrinth id field removed from new gradle.properties).
- **Package root**: `com.iafenvoy.jupiter` in both — unchanged.
- **Project's current dependency declarations** (`D:/IceAndFire-CE`):
  - `common/build.gradle:21` → `modImplementation "maven.modrinth:jupiter:5pNXzmee"`
  - `fabric/build.gradle:44` → `modImplementation "maven.modrinth:jupiter:5pNXzmee"`
  - `neoforge/build.gradle:42` → `modImplementation "maven.modrinth:jupiter:m2itNS7Z"`
- **待确认 (to be confirmed)**: the exact Modrinth version-hash / maven coordinate for Jupiter **2.4.2** (MC 26.x). The on-disk `Jupiter new` tree is a local git checkout (version 2.4.2) with no published coordinate recorded. The migration will need to resolve the 26.x Modrinth hash (or a mavenLocal/JitPack coordinate) — confirm with the mod author or Modrinth API before editing build.gradle.

---

## 7. Practical migration steps for IceAndFire-CE (common config code)

1. Update Jupiter dependency coordinates (common/fabric/neoforge `build.gradle`) to the 26.x artifact (待确认 hash). Package root unchanged, so no import-path rewrites.
2. In `IafCommonConfig.java:36` and `IafClientConfig.java:15`: swap `net.minecraft.resources.ResourceLocation` → `net.minecraft.resources.Identifier` and `ResourceLocation.fromNamespaceAndPath(...)` → `Identifier.fromNamespaceAndPath(...)`. (Same factory name/signature.)
3. Everything else (managers, entry builders, container inheritance, ConfigSelectScreen builder) compiles unchanged against 2.4.2.
4. No changes to the fabric `ModMenu` integration: `ConfigSelectScreen.builder(...).server(IafCommonConfig.INSTANCE).client(IafClientConfig.INSTANCE).build()` is valid in both versions.
5. Do NOT rely on any old deprecated APIs — the project already uses builders exclusively, so it is already future-proof for the removals in §4b.
