#**New features**
- **Time Favorites** [#110](https://github.com/shedaniel/RoughlyEnoughItems/issues/110)

You can now easily set the time of day for your worlds. You can click the "+" button on the bottom left of
the screen to add this new favorite. We also took some time to re-make the weather icons!
![Example](roughlyenoughitems/2022.2/2022-02-18_01-30.png)

- **Creative Mode Cheats** [#768](https://github.com/shedaniel/RoughlyEnoughItems/issues/768)

You can now tell REI to automatically enable Cheats when in Creative Mode, and
switch it off when you are not.
![Example](roughlyenoughitems/2022.1/2022-02-18_01-32.png)

- **Caching Item Rendering**

The new Caching Item Rendering option will improve performance for rendering items up to **5 times**, 
with an average of **2.5x performance gain**. However, this will break enchantment glints and animated textures.
\n\nDemonstration: [Twitter Post](https://twitter.com/shedaniel_/status/1490675724193497091)
- **Item Zoom** [#430](https://github.com/shedaniel/RoughlyEnoughItems/issues/430)

Instead of highlighting the items on hover, you can now configure REI to zoom in on the item you are hovering.
![Example](roughlyenoughitems/2022.2/2022-02-18_09-05.png)
---
#**Changes**
- **Support for JEI API 9.7** [#821](https://github.com/shedaniel/RoughlyEnoughItems/issues/821)

The JEI compatibility layer has been updated to support APIs from JEI 9.7, this is a completely new way for
JEI plugins to layout their categories, please report any issues you find with this new update.
- **Filtering of Entries in Recipes** [#783](https://github.com/shedaniel/RoughlyEnoughItems/issues/783)

REI will now attempt to filter items / fluids shown in recipes based on the filtering rules, this
will allow mod packs to unify resources easier.
---
#**Bug Fixes**
- **Multiple Outputs for Composite Display** [#617](https://github.com/shedaniel/RoughlyEnoughItems/issues/617)

When you have multiple outputs for a composite display, the outputs will now display properly.

- ... and more!