# Saint's Dragons — NeoForge 1.21.1

Target: Minecraft 1.21.1, NeoForge 21.1.252, Java 21.

**Status: bootstrap scaffold only; not a playable port yet.** The original Forge platform integration, common gameplay code, registrations, networking, configs, datagen, mixins, and client integrations still require migration and validation.

## Build
Use JDK 21:
- `./gradlew build`
- `./gradlew runClient`

GeckoLib is declared as a required runtime dependency.

The upstream project targets Forge 1.20.1 / Java 17. Its APIs and Minecraft-version changes must be migrated; the Forge entrypoint cannot simply be copied unchanged.
