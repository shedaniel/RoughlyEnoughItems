View full changelog [here](https://github.com/shedaniel/RoughlyEnoughItems/blob/1.14/CHANGELOG.md).
## v2.9-beta+build.107 (BETA)
- Fixed: Crashing when clicking tabs
- Added: Buttons to switch category page
- Fixed: NullPointerException by geniii
- Fixed [#58](https://github.com/shedaniel/RoughlyEnoughItems/issues/58): Keybinds (e.g. O, R, U) working even a search field is focused
## v2.9-beta+build.106 (BETA)
- Using: [HammerLib](https://minecraft.curseforge.com/projects/hammer-lib) as a simple opengl scissors api
- New: Mod Name of category new shows in category tooltips
- New: Renderer API
- New: Villager Trading Like Recipe Screen. ![image](https://cdn.discordapp.com/attachments/432055962233470988/576077676305973275/unknown.png)
- New: Recipe Screen Selection Screen
- Removed: All `Locale.ROOT` usage
- Fixed: Button Width Rendering issues when the width is an odd number
- Changed: Mod Name changed from `RoughlyEnoughItems` to `Roughly Enough Items`
- Changed: Lots of internal refractors
- Fixed: Enchanting books not showing its maximum level
## v2.8.2+build.104
- Fixed [#81](https://github.com/shedaniel/RoughlyEnoughItems/issues/81): Scrolling unaffected by exclusion zones.
- Added [#82](https://github.com/shedaniel/RoughlyEnoughItems/issues/82): Close search after pressing "Enter"
- Added [#83](https://github.com/shedaniel/RoughlyEnoughItems/issues/83): 2 More keybinds
## v2.8.1+build.103
- Fixed: Item Searching Layering
- Added: Some tooltips in the config
- Fixed [#58](https://github.com/shedaniel/RoughlyEnoughItems/issues/58): Keybinds (e.g. O, R, U) working even creative search field is focused
## v2.8.1+build.102
- Fixed: Server incorrect permission check
- Fixed: Base Bounds not correctly resizing
- Improved: Search Field
- Added: Right Click for Item Searching ![image](https://cdn.discordapp.com/attachments/530305137382653952/573838234333741064/unknown.png)
## v2.8.1+build.101
- Updated [#77](https://github.com/shedaniel/RoughlyEnoughItems/pull/77): Updated Simplified Chinese by XuyuEre
- Updated: Updated Traditional Chinese by XuyuEre & Danielshe
- New: Better Exclusion Zone Handling
## v2.8.0+build.100
- Fixed [#76](https://github.com/shedaniel/RoughlyEnoughItems/issues/76): Laggy Changing Pages
- New: Better Server Support (You can now drop REI into server mods)
- New: Better Cheat Mode Button
## v2.7.11.97
- Fixed [#72](https://github.com/shedaniel/RoughlyEnoughItems/issues/72): Crash on clicking craftable items filter
- New: Load REI Plugins via entry points, old way works too but support will be dropped
- Removed: 'Enable Legacy Plugin Support' Option
## v2.7.10.96
- Fixed [#69](https://github.com/shedaniel/RoughlyEnoughItems/issues/69): Weird Search Field
## v2.7.10.95
- Fixed [#67](https://github.com/shedaniel/RoughlyEnoughItems/issues/67): Item Panel Crashes
## v2.7.9.94
- Added: Support for `c` namespace ([Cotton](https://github.com/CottonMC/cotton) Common Resources)
- Added: Warping around the recipe book: ![image](https://media.discordapp.net/attachments/530305137382653952/568432084419608576/unknown.png?width=967&height=517) 
- Fixed [#66](https://github.com/shedaniel/RoughlyEnoughItems/issues/66): Tooltips can be out of bounds
## v2.7.8.93
- Fixed: Some localisations having broken json
- Fixed: Depth of Craftable Toggle Button
## v2.7.8.92
- Modified: Bundled with updated API for pre3
- Added: Option for Light Gray Recipe Border (I guess JEI like? I am going to get killed by mezz)
- Modified: Rearranged Config Screen 
## v2.7.7.91
- Updated: Chinese Localisation
## v2.7.6.90
- Fixed [#63](https://github.com/shedaniel/RoughlyEnoughItems/issues/63): Pressing Space in the search bar opens the Config GUI
- Added [#64](https://github.com/shedaniel/RoughlyEnoughItems/issues/64): Option to flip give amount
## v2.7.5.89
- Fixed: Keybinds (e.g. O, R, U) working even if recipe book search field is focused
## v2.7.4.88
- Fixed: Item List Overlay buttons still enabled when there is only 1 page
- Fixed [#58](https://github.com/shedaniel/RoughlyEnoughItems/issues/58): Keybinds (e.g. O, R, U) working even if creative search field is focused
- Fixed [#59](https://github.com/shedaniel/RoughlyEnoughItems/issues/59): Wrong page calculation (Thanks geniiii)
## v2.7.3.87
- Fixed: Credits button not working
- ~~Fixed: Keybinds (e.g. O, R, U) working even creative search field is focused~~
## v2.7.3.86
- Fixed [#56](https://github.com/shedaniel/RoughlyEnoughItems/issues/56): Even tho I don't know how I fixed it
- Added: Credits button in Config Screen
- Fixed: Odd pixel with the recipe screen
- Fixed: Pressing R not working on the side panel while viewing recipes
- Modified: Right-clicking the search field now focus it
- Fixed: Craftable Only button having a weird tint when something else is focused
- Added: Recipes now sort base on their identifiers
## v2.7.2.85
- Bundled with updated APIs for 1.14-pre1
- New DisplayHelper for better bounds calculation
## v2.7.1.84
- Bundled with updated APIs. Fixed crash.
## v2.7.0.83
- Updated to Fabric Loader 0.4.0
- Now bundled with Fabric API, Cloth Events API, Cloth Config API
## v2.6.2.81
- Fix [#53](https://github.com/shedaniel/RoughlyEnoughItems/issues/53): Crash on keyPressed
## v2.6.2.80
- Updated to 19w14b
## v2.6.1.79
- 75% less mixins
- Updated to 19w14a
## v2.6.0.78
- Added config to force enable april fools
- Fix crash related to `getTotalPage()`
- Fix depth on overlay
- Less mixins
## v2.6.0.77
- fish (April Fools [Download](https://minecraft.curseforge.com/projects/roughly-enough-items/files/2693786))
## v2.6.0.76
- New DisplayVisibilityHandler
- Fixed [#49](https://github.com/shedaniel/RoughlyEnoughItems/issues/49): REI Overlay added twice
## v2.5.1.75
- Using Cloth events instead of overwriting methods \o/ (Please update [Cloth](https://minecraft.curseforge.com/projects/cloth))
## v2.5.1.74
- Updated to 19w13a
## v2.5.0.73
- Made REI crash better (lmao wut)
## v2.5.0.72
- Updated to 19w12b
## v2.5.0.71
- Removed mouseScrolled Override in mixins
- Added spectator mode in gamemode switcher
- Gamemodes & Weather are now translatable: [#47](https://github.com/shedaniel/RoughlyEnoughItems/issues/47)
- Fixed Search Field
- Fixed Button Focus
- Added Button Tooltips
- Removed credits button (it will be back)
- Removed disabling credits option
- Update to 19w12a
- New config screen from [Cloth](https://minecraft.curseforge.com/projects/cloth)
## v2.4.2.68
- Fixed [Cloth](https://minecraft.curseforge.com/projects/cloth) missing text
## v2.4.2.67
- Fixed pages being weird
- Added Portuguese
## v2.4.2.66
- Removed Mod Menu hooks, will hook into Mod Menu itself in the future
- Improved gui widgets
## v2.4.1.65
- [Cloth](https://minecraft.curseforge.com/projects/cloth) is now required for REI to run
- Update to 19w11a
## v2.4.0.63
- First implementation of [Cloth](https://minecraft.curseforge.com/projects/cloth), [Cloth](https://minecraft.curseforge.com/projects/cloth) is still not required for REI to boot but it will be a must have dependency when 19w11a comes.
- Used [Cloth](https://minecraft.curseforge.com/projects/cloth) for hooking up [Mod Menu](https://minecraft.curseforge.com/projects/modmenu/) config. Example: [https://streamable.com/ivbsj](https://streamable.com/ivbsj)
- Changed config `true / false` to `Yes / No`
## v2.4.0.61
- Config with comments
- Fix creative inventory even more
- No longer uses Fabric internals
- Config to disable Recipe Book
- Fixes [Issue #27: Second page react as first page recipes](https://github.com/shedaniel/RoughlyEnoughItems/issues/27)
## v2.3.2.56
- Removed Plugin Update Checker
