# Changelog

## 0.1.0-alpha.8

- Added the Animal Rancher's persistent 10-bucket milk tank.
- Cow and mooshroom cycles now add 1,000 mB of milk instead of directly creating a bucket.
- Added bucket filling through the Rancher's third slot.
- Exposed the tank through the NeoForge block fluid capability for pipe compatibility.
- Added synchronized milk amount and a dedicated tank gauge to the shared screen.

## 0.1.0-alpha.7

- Ported the Drop Extractor to persistent timed processing with four output slots.
- Restored extracted animal drops and Broken Pattern output.
- Ported the Animal Rancher to persistent timed processing at 4 FE/t.
- Restored species-specific Rancher cycle times and Rancher Gear durability loss.
- All six animal machines now use block entities, saved inventories and the shared menu.

## 0.1.0-alpha.6

- Added persistent inventories and Forge Energy processing to the Animal Breeder and Growth Accelerator.
- Restored the Breeder's 4,800-tick cycle, matching-parent rule and non-consumed parents.
- Restored the Growth Accelerator's 20,000-tick base cycle and four-step food speed multiplier.
- Expanded the shared machine menu to five slots and migrated alpha.5 three-slot inventories.

## 0.1.0-alpha.5

- Replaced instant Digitizer and Materializer interactions with persistent block entities.
- Added three-slot machine inventories, 200-tick progress and 5 FE/t processing.
- Added NeoForge item/energy capabilities, saved machine state and item drops on break.
- Added a shared machine menu and client screen with progress and energy displays.

## 0.1.0-alpha.1

- Started the Minecraft 1.21.1 NeoForge port.
- Added a Java 21 / NeoGradle build.
- Added automated GitHub Actions builds.
- Registered the twelve core items from the original mod.
- Reused and renamed the original item textures for modern resource paths.
- Added English and French translations.
