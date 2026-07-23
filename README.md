<p align="center">
  <img src="https://i.imgur.com/jRg4gSC.png" alt="Canned Cuisine" width="100%">
</p>

<p align="center">
  <a href="https://github.com/atomorphosis/canned-cuisine/actions/workflows/build.yml"><img src="https://github.com/atomorphosis/canned-cuisine/actions/workflows/build.yml/badge.svg" alt="Build"></a>
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-62b47a" alt="Minecraft 1.21.1">
  <img src="https://img.shields.io/badge/NeoForge-21.1.235%2B-e78a3c" alt="NeoForge 21.1.235 or newer">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-6a5acd" alt="MIT License"></a>
</p>

### Overview

**Canned Cuisine** is a NeoForge mod that turns Minecraft ingredients into procedural canned meals designed by the player.

Combine three to six ingredients in the **Pressure Canner** and their identities and proportions will determine the result: its culinary style, localized name, quality, nutrition, yield, label color, and possible effects. Duplicate ingredients express proportions, while stack sizes determine how many times the same batch can be produced.

Every experiment remains useful and readable. The canner provides a server-calculated preview before processing, production never consumes inputs unless the complete result fits, and even failed mixtures remain edible outcomes to discover. Finished meals can be eaten at full hunger and return an Empty Can after consumption.

Canned Cuisine works as a standalone Vanilla expansion, while optional integrations broaden its culinary catalog and make its systems easier to explore:

- **JEI and EMI** provide a procedural Pressure Canning atlas and effect-affinity pages;
- **Jade, AppleSkin, and Tooltip Overhaul** integrate with familiar interface conventions;
- **Farmer's Delight, Croptopia, The Aether, Aquaculture, and Naturalist** contribute supported raw ingredients when installed;
- **KubeJS and datapacks** can add, replace, or remove ingredient profiles, archetypes, and effect rules.

It's time to find out what fits in a can.

### Installation

Canned Cuisine requires **Minecraft 1.21.1**, **NeoForge 21.1.235 or newer in the 21.1 series**, and **Java 21**. Place the mod JAR in the instance's `mods` directory. All listed integrations are optional and are never bundled with the mod.

### Data Packs

Server data packs can extend the procedural catalogs under:

```text
data/<namespace>/canned_cuisine/ingredient_profiles/
data/<namespace>/canned_cuisine/archetypes/
data/<namespace>/canned_cuisine/effect_rules/
```

KubeJS installations can apply the same validated changes through `CannedCuisineEvents.data(...)`. Existing cans retain their resolved properties when server data changes.

### Contributing

Constructive feedback, bug reports, compatibility data, and code improvements are welcome. If you find incorrect behavior or a simpler way to implement a system, please open an [issue](https://github.com/atomorphosis/canned-cuisine/issues) or pull request with clear reproduction steps.

Build and test the project with Java 21:

```bash
./gradlew test
./gradlew build -Pcompat_runtime=minimal -Pviewer_runtime=none
```

The deterministic procedural engine under `atomorphosis.cannedcuisine.engine` is kept independent of Minecraft and NeoForge classes.

### License

Canned Cuisine is available under the [MIT License](LICENSE). `TEMPLATE_LICENSE.txt` preserves the notice for files inherited from the NeoForge MDK template.
