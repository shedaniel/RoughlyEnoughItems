# Roughly Enough Items
https://minecraft.curseforge.com/projects/roughly-enough-items <br>
Roughly Enough Items is a mod to view Items and Recipes for Minecraft 1.13 - 1.18, supporting mod loaders from Forge, Rift to Fabric.
-----

[Help translate REI on Crowdin!](https://crowdin.com/project/roughly-enough-items)

![](https://i.imgur.com/eQsWDrM.png)

![](https://i.imgur.com/OcOQLip.png)

This mod is both client sided and server sided.

# Maven
Firstly, add my Maven repository (If you already have the architectury maven, you don't need to do this, they are the same repo)
```gradle
repositories {
    maven { url "https://maven.shedaniel.me" }
}
```

## Choosing the correct artifact to depend on
### Fabric
REI recommends you to declare a compile dependency on REI's API, and a runtime dependency on REI's full package.
```gradle
dependencies {
    modCompileOnly "me.shedaniel:RoughlyEnoughItems-api-fabric:VERSION"
    modRuntimeOnly "me.shedaniel:RoughlyEnoughItems-fabric:VERSION"
}
```

Additionally, if you want to interact with the builtin plugins, you may declare a compile dependency on it as well.
```gradle
dependencies {
    modCompileOnly "me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:VERSION"
}
```

### Forge (ForgeGradle)
REI recommends you to just depend on REI's full package.
```gradle
dependencies {
    implementation fg.deobf("me.shedaniel:RoughlyEnoughItems-forge:VERSION")
}
```

### Forge (Architectury Loom)
REI recommends you to declare a compile dependency on REI's API, and a runtime dependency on REI's full package.
```gradle
dependencies {
    modCompileOnly "me.shedaniel:RoughlyEnoughItems-api-forge:VERSION"
    modRuntimeOnly "me.shedaniel:RoughlyEnoughItems-forge:VERSION"
}
```

Additionally, if you want to interact with the builtin plugins, you may declare a compile dependency on it as well.
```gradle
dependencies {
    modCompileOnly "me.shedaniel:RoughlyEnoughItems-default-plugin-forge:VERSION"
}
```

### Architectury
REI recommends you to declare a compile dependency on REI's common API, and declare the full package on the individual platform's subprojects.
```gradle
// Common
dependencies {
    modCompileOnly "me.shedaniel:RoughlyEnoughItems-api:VERSION"
}

// Fabric
dependencies {
    modRuntimeOnly "me.shedaniel:RoughlyEnoughItems-fabric:VERSION"
}

// Forge
dependencies {
    modRuntimeOnly "me.shedaniel:RoughlyEnoughItems-forge:VERSION"
}
```

Additionally, if you want to interact with the builtin plugins, you may declare a compile dependency on it as well.
```gradle
// Common
dependencies {
    modCompileOnly "me.shedaniel:RoughlyEnoughItems-default-plugin:VERSION"
}
```

### List of artifacts
- **me.shedaniel:RoughlyEnoughItems-api**: REI API for Architectury Common
- **me.shedaniel:RoughlyEnoughItems-default-plugin**: REI Default Plugin for Architectury Common
- **me.shedaniel:RoughlyEnoughItems-runtime**: REI Runtime for Architectury Common
- **me.shedaniel:RoughlyEnoughItems-api-fabric**: REI API for Fabric
- **me.shedaniel:RoughlyEnoughItems-default-plugin-fabric**: REI Default Plugin for Fabric
- **me.shedaniel:RoughlyEnoughItems-runtime-fabric**: REI Runtime for Fabric
- **me.shedaniel:RoughlyEnoughItems-api-forge**: REI API for Forge
- **me.shedaniel:RoughlyEnoughItems-default-plugin-forge**: REI Default Plugin for Forge
- **me.shedaniel:RoughlyEnoughItems-runtime-forge**: REI Runtime for Forge
- **me.shedaniel:RoughlyEnoughItems-fabric**: Full REI for Fabric
- **me.shedaniel:RoughlyEnoughItems-forge**: Full REI for Forge
