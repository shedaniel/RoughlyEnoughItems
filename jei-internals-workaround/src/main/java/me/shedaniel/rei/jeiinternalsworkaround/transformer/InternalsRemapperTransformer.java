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

package me.shedaniel.rei.jeiinternalsworkaround.transformer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.MethodRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.util.Bytecode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class InternalsRemapperTransformer extends Remapper implements Consumer<ClassNode> {
    private final Map<String, String> classMap = new HashMap<>();
    private final Set<String> redirectInvokeVirtual = new HashSet<>();
    
    public InternalsRemapperTransformer() {
        classToInterface("mezz/jei/Internal", "me/shedaniel/rei/jeicompat/JEIInternalsImitator");
        classToInterface("mezz/jei/runtime/JeiHelpers", "mezz/jei/api/helpers/IJeiHelpers");
        classToInterface("mezz/jei/runtime/JeiRuntime", "mezz/jei/api/runtime/IJeiRuntime");
        classToInterface("mezz/jei/ingredients/IngredientManager", "mezz/jei/api/runtime/IIngredientManager");
        classToInterface("mezz/jei/ingredients/IngredientFilter", "mezz/jei/api/runtime/IIngredientFilter");
        classToInterface("mezz/jei/gui/Focus", "mezz/jei/api/recipe/IFocus");
    }
    
    private void classToInterface(String oldName, String newName) {
        classMap.put(oldName, newName);
        redirectInvokeVirtual.add(oldName);
    }
    
    @Override
    public void accept(ClassNode classNode) {
        ClassNode newClassNode = new ClassNode(Opcodes.ASM9);
        ClassRemapper remapper = new ClassRemapper(Opcodes.ASM9, newClassNode, this) {
            @Override
            protected MethodVisitor createMethodRemapper(MethodVisitor methodVisitor) {
                return new MethodRemapper(api, methodVisitor, remapper) {
                    @Override
                    public void visitMethodInsn(int opcodeAndSource, String owner, String name, String descriptor, boolean isInterface) {
                        if (opcodeAndSource == Opcodes.INVOKEVIRTUAL && redirectInvokeVirtual.contains(owner)) {
                            opcodeAndSource = Opcodes.INVOKEINTERFACE;
                            isInterface = true;
                        }
                        super.visitMethodInsn(opcodeAndSource, owner, name, descriptor, isInterface);
                    }
                };
            }
        };
        classNode.accept(remapper);
        Bytecode.replace(newClassNode, classNode);
    }
    
    @Override
    public String map(String internalName) {
        return classMap.getOrDefault(internalName, internalName);
    }
}
