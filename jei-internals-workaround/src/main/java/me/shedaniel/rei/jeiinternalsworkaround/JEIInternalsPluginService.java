/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.jeiinternalsworkaround;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import me.shedaniel.rei.jeiinternalsworkaround.transformer.InternalsRemapperTransformer;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.Phases;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class JEIInternalsPluginService implements ILaunchPluginService {
    private final List<Consumer<ClassNode>> transformers = new ArrayList<>();
    
    public JEIInternalsPluginService() {
        this.transformers.add(new InternalsRemapperTransformer());
    }
    
    @NotNull
    @Override
    public String name() {
        return "REI-JEIInternalsTransformationService";
    }
    
    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        return isEmpty ? Phases.NONE : Phases.AFTER_ONLY;
    }
    
    @Override
    public int processClassWithFlags(Phase phase, ClassNode classNode, Type classType, String reason) {
        if (phase == Phase.BEFORE && Objects.equals(reason, "mixin")) {
            return ComputeFlags.NO_REWRITE;
        }
        for (Consumer<ClassNode> transformer : transformers) {
            transformer.accept(classNode);
        }
        return ComputeFlags.SIMPLE_REWRITE;
    }
}
