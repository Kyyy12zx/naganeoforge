# Saint's Dragons — NeoForge 1.21.1

Target: Minecraft 1.21.1, NeoForge 21.1.252, Java 21.

**Status: early port in progress; not playable yet.** Bootstrap, shared platform interfaces, NeoForge registry/config/network adapters, and several shared gameplay utilities have been ported. Current migrated utilities include dragon gender/variant/locomotion primitives, motion math, aiming, collision-expression parsing, combat body-facing lock, generic and humanoid body/look controllers, ground-effect surface snapping, multipart entity provider contract, weak identity caching, smooth interpolation, temporary dragon-powered redstone tracking, and the base dragon-ability section/type/runtime plus AI combat pacing component. Full dragon entity classes, registries, species gameplay, client rendering, datagen, mixins, and build validation remain.

## Build
Use JDK 21:
- `./gradlew build`
- `./gradlew runClient`

GeckoLib is declared as a required runtime dependency.

The upstream project targets Forge 1.20.1 / Java 17. Its APIs and Minecraft-version changes must be migrated; the Forge entrypoint cannot simply be copied unchanged. Code migration retains upstream attribution and applicable license terms; restricted art/audio assets are not copied.

Additional shared entity interfaces ported: DragonChestCarrier, DragonSaddleCarrier, DrinkingDragon, PassiveTreeDestroyer, ScentAssessingDragon, ShakesScreen, and HumanSoundProfile, jukebox dancing, and movement capability enum.

Also migrated the Raevyx lightning damage-type key/factory and its 1.21.1 datapack damage-type and lightning-tag definitions. These are groundwork only; full entity integration and build validation remain pending.
