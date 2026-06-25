# Roughly Enough Items (REI) Fork - Disable Partial Recipe Warning

> ⚠️ **Status: DEPRECATED**  
> As of Minecraft version 1.26.1, this fix is no longer necessary as the base mod/game handles this behavior natively. This repository is archived and kept for historical purposes. For the original mod, visit the [Official Roughly Enough Items CurseForge Page](https://minecraft.curseforge.com/projects/roughly-enough-items).

---

## What This Fork Did

In the original REI, if a large server failed to load 100% of the recipes, a **"Partial Recipe Warning"** notification would pop up. While you could click it away, the vanilla REI behavior forgot this preference immediately. 

This meant the warning would aggressively reappear every time you:
* Changed lobbies on a large network.
* Left and rejoined a server.
* Changed dimensions or reloaded chunks in a way that refreshed the recipe cache.

### The Solution
This fork modified the dismissal button to act as a **"Don't show this again"** toggle. Clicking it saved your preference directly to a configuration file, permanently silencing the warning across server switches and sessions.

---

## Installation & Usage (Legacy)

If you are still running an older Minecraft version where this is an issue:
1. Replace your existing REI `.jar` with the build from this fork.
2. When the warning appears, click the **"Don't show it again"** button.
3. To reset this setting, you can manually toggle it in the config file.

## License
This project inherits the original license of Roughly Enough Items. All credits go to the original REI developers.

---
---

# Official Roughly Enough Items Documentation

[Help translate REI on Crowdin!](https://crowdin.com/project/roughly-enough-items)

Roughly Enough Items is a clean and modular mod to view Items and Recipes for Minecraft, supporting mod loaders from Forge, Rift to Fabric.

*This mod is required on both the client side and the server side for full functionality.*

## Maven

Firstly, add the Maven repository (If you already have the architectury maven, you don't need to do this, they are the same repo):

```gradle
repositories {
    maven { url "[https://maven.shedaniel.me](https://maven.shedaniel.me)" }
}
