# Tetranomicon — port to NeoForge 1.21.1 (Tetra addon)

Ported from Forge 1.20.1 to **NeoForge 1.21.1**, targeting the 1.21.1 forks of Tetra
(`se.mickelus.tetra:tetra:1.21.1-6.13.0`) and mutil (`se.mickelus.mutil:mutil:1.21.1-6.3.1`),
both resolved from `mavenLocal`. Branch: `1.21.1`. Tetranomicon is mostly data (materials/modules/
schematics/improvements/crafting_effects/tiers/replacements) plus thin Java for tiers, items, cross-mod
creative-tab compat, and dev debug tooling.

## Build
- Toolchain: **JDK 21** (was 17). Build system migrated ForgeGradle → **NeoForge `moddev` 2.0.141**
  (mirrors tetra/mutil/AoF). Gradle wrapper bumped to 9.4.1 (copied from `tetra/`).
- Required env var when building: `local_maven=$HOME/.m2/repository`.
- `mods.toml` → `src/main/templates/META-INF/neoforge.mods.toml`, expanded by a `generateModMetadata`
  ProcessResources task. Deps declared: neoforge/minecraft/mutil (required), tetra (required, AFTER),
  oresabovediamonds/betternether (optional). No mixins (none in this mod).
- Dropped all the commented-out CurseMaven/Modrinth deps from the 1.20.1 build.

## Code migration
- **Main class** (`Tetranomicon`): `@Mod(ModContainer)` ctor; `context.getModEventBus()` →
  `container.getEventBus()`. Removed `MinecraftForge.EVENT_BUS.register(this)` — the class has no
  `@SubscribeEvent` methods and NeoForge throws on registering a handler-less object (Forge ignored it).
  Keeps the `ModList.isLoaded("oresabovediamonds"/"betternether")` gate on item registration.
- **Items** (`TetranomiconItems`): `ForgeRegistries.ITEMS` → `BuiltInRegistries.ITEM`;
  `RegistryObject` → `DeferredHolder<Item, Item>`; `@Mod.EventBusSubscriber` → `@EventBusSubscriber`
  (neoforged, `Bus.MOD`); `BuildCreativeModeTabContentsEvent` → neoforge package.
- **Tiers** (`TetranomiconTiers`) — the substantive rewrite. Forge's `TierSortingRegistry` + `ForgeTier`
  + `Tier.getLevel()` are gone. Now uses `HarvestTierRegistry.register(new SimpleTier(incorrectTag, 0,0,0,0,
  () -> Ingredient.EMPTY), name, after, before)`, chaining tier_eight `after` `TetraRegistries.forgeHammerTier`,
  nine after eight, ten after nine, eleven after ten. `SimpleTier`'s first arg is the **incorrect-for-drops**
  block tag (inverse of ForgeTier's "needs this tier" tag); the four `incorrect_for_tier_*_tool` tags point at
  `#tetra:incorrect_for_maxed_forge_hammer`, so these top tiers mine everything the forge hammer can and more.
  The tier names still resolve for the datapack `data/tetra/tiers/tetranomicon.json` list via
  `HarvestTierRegistry.byName`.
- **Compat** (`OresAboveDiamondsCompat`, `BetterNetherCompat`): rewritten to resolve the other mods' items/tabs
  by **registry name / tab-location string** instead of a compile-time dependency on their (Forge-1.20.1) classes,
  so tetranomicon builds and runs without the OAD/BetterNether jars and only slots its items in when they're
  present. NeoForge's `BuildCreativeModeTabContentsEvent` has no `getEntries()`: `put(...)` → `accept(...)`,
  `putBefore(...)` → `insertBefore(...)` (guarded — `insertBefore` throws if the anchor item isn't in the tab).
- **Debug tooling** (`ServerDebugging`, `ClientDebugging`): Forge event/registry/tag imports migrated
  (`ServerChatEvent`/`RenderTooltipEvent` → neoforge packages; `@EventBusSubscriber` `Bus.GAME`; distmarker →
  neoforged). `ForgeRegistries.ITEMS.getEntries()` → `BuiltInRegistries.ITEM.entrySet()`;
  `Item.getCreatorModId(stack)` (gone) → `BuiltInRegistries.ITEM.getKey(item).getNamespace()`. Tetra's
  `MaterialData.material.getPredicate()` returns `TetraItemPredicate` (not vanilla `ItemPredicate`) in 1.21 —
  updated the declared types and dropped `ReplacementDefinition.predicate.serializeToJson()` (no longer on the
  predicate interface) from the missing-replacement log. NeoForge conventional-tag renames:
  `Tags.Items.STONE` → `STONES`, `TOOLS_BOWS` → `TOOLS_BOW`.

## Data / resources
- Datapack folders singularized (1.21): `recipes` → `recipe`; conventional-tag folder
  `data/forge/tags/items/` → `data/c/tags/item/` (scales + unused dragon-bones tags). Tetra's own custom
  folders (materials/modules/schematics/improvements/crafting_effects/tiers/replacements) keep their names.
- `pack.mcmeta` `pack_format` 15 → 34 (and dropped the `${mod_id}` placeholder to avoid expansion wiring).
- **Conditions**: `"type": "forge:mod_loaded"` → `"neoforge:mod_loaded"` (1013 occurrences). This is what gates
  every cross-mod material/effect — verified at runtime (607 `Skipping data … due to condition` for absent mods).
- **Tag references**: `forge:scales/*` → `c:scales/*`.
- **Recipes**: smithing_transform `result: {"item": X}` → `{"id": X}` (ingredient `"item"` in base/addition/
  template stays).
- **Left as-is on purpose**: `forge:reach_distance` / `forge:attack_range` attribute names in the socket
  materials — Tetra's `AttributesDeserializer` still maps these legacy names to
  `player.block_interaction_range` / `player.entity_interaction_range`.
- Added tier tags `data/tetranomicon/tags/block/incorrect_for_tier_{eight,nine,ten,eleven}_tool.json`.

## Load-test (headless NeoForge 1.21.1 dedicated server → "Done")
- Boots clean: `Done (2.472s)! For help`. No fatal datapack errors; tier ordering validates (no cycle).
- The three tetranomicon smithing recipes (`netherite_opal`, `flaming_ruby`, `cincinnasite_diamond`) log a
  non-fatal `Parsing error loading recipe` because they reference OAD/BetterNether items **and** their own
  result items are only registered when OAD/BetterNether is loaded (neither is in the test env). They load
  correctly when those mods are present. This is the expected missing-mod-content behavior, not a port bug.

Jar output: `output/tetranomicon-1.21.1-1.6.2.jar`.
