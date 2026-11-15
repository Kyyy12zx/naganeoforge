# Saint's Dragons — NeoForge 1.21.1

Target: Minecraft 1.21.1, NeoForge 21.1.252, Java 21.

**Status: early port in progress; not playable yet.** Bootstrap, shared platform interfaces, NeoForge registry adapter, and NeoForge common-config adapter, and NeoForge custom-payload network adapter are present. Entity/gameplay migration, client rendering, datagen, mixins, and build validation remain.

## Build
Use JDK 21:
- `./gradlew build`
- `./gradlew runClient`

GeckoLib is declared as a required runtime dependency.

The upstream project targets Forge 1.20.1 / Java 17. Its APIs and Minecraft-version changes must be migrated; the Forge entrypoint cannot simply be copied unchanged.
