# Bottled Animals Reborn

A modern NeoForge port of **Bottled Animals**, originally created by Emanuele Sarte (Ermans) for Minecraft 1.7.10.

## Target

- Minecraft 1.21.1
- NeoForge 21.1.226+
- Java 21
- Current version: 0.1.0-alpha.7

## Status

The first playable port milestone is available. Animal items preserve their type, empty bottles capture the original nine adult animals, six animal machines process items on right-click, and the Food Crusher/Wireless Feeder have an initial playable food-fluid loop.

All six animal machines now use persistent inventories, timed Forge Energy processing and a shared menu. The Animal Rancher currently outputs milk as a bucket; its original internal fluid tank remains on the roadmap. The Basic Generator exposes a real NeoForge FE capability and pushes energy to adjacent consumers.

The legacy 1.7.10 source remains on the `master` branch. Modern development happens on `1.21.1-neoforge`.

## Port roadmap

- [x] NeoForge 1.21.1 build
- [x] Java 21 and GitHub Actions
- [x] Core item registry and original textures
- [x] Animal data components and capture
- [x] Animal Digitizer and Materializer (persistent inventory, menu and FE)
- [x] Breeder and Growth Accelerator (persistent inventory, menu and FE)
- [x] Drop Extractor and Animal Rancher (persistent inventory, menu and FE)
- [x] Food Crusher and Wireless Feeder (alpha interaction)
- [x] Basic Generator and Forge Energy
- [ ] Remaining machine menus and JEI integration
- [ ] Compatibility tests and first playable release

## License and credits

The original mod and this port are licensed under GNU LGPL v3. See [LICENSE](LICENSE) and [NOTICE](NOTICE).

Original repository: https://github.com/EmanueleSarte/BottledAnimals
