/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.MethodRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.function.Consumer;

public class InternalsRemapperTransformer extends Remapper implements Consumer<ClassNode> {
    private final Map<String, String> classMap = new HashMap<>();
    private final Set<String> redirectInvokeVirtual = new HashSet<>();
    private Boolean isDev;
    
    public InternalsRemapperTransformer() {
        classToInterface("mezz/jei/Internal", "me/shedaniel/rei/jeicompat/imitator/JEIInternalsImitator");
        classToInterface("mezz/jei/runtime/JeiHelpers", "mezz/jei/api/helpers/IJeiHelpers");
        classToInterface("mezz/jei/runtime/JeiRuntime", "mezz/jei/api/runtime/IJeiRuntime");
        classToInterface("mezz/jei/ingredients/IngredientManager", "mezz/jei/api/runtime/IIngredientManager");
        classToInterface("mezz/jei/ingredients/IngredientFilter", "mezz/jei/api/runtime/IIngredientFilter");
        classToInterface("mezz/jei/gui/Focus", "mezz/jei/api/recipe/IFocus");
        classToInterface("mezz/jei/input/IClickedIngredient", "me/shedaniel/rei/jeicompat/imitator/JEIInternalsClickedIngredient");
        classToInterface("mezz/jei/input/ClickedIngredient", "me/shedaniel/rei/jeicompat/imitator/JEIInternalsClickedIngredientImpl");
        classToInterface("mezz/jei/config/ServerInfo", "me/shedaniel/rei/jeicompat/imitator/JEIServerInfo");
        redirect("mezz/jei/color/ColorGetter", "me/shedaniel/rei/jeicompat/imitator/JEiColorGetterImitator");
        redirect("mezz/jei/plugins/jei/info/IngredientInfoRecipe", "me/shedaniel/rei/jeicompat/imitator/IngredientInfoRecipe");
        redirect("mezz/jei/plugins/vanilla/cooking/AbstractCookingCategory", "me/shedaniel/rei/jeicompat/imitator/JEIAbstractCookingCategory");
        redirect("mezz/jei/plugins/vanilla/cooking/FurnaceVariantCategory", "me/shedaniel/rei/jeicompat/imitator/JEIFurnaceVariantCategory");
        redirect("mezz/jei/plugins/vanilla/ingredients/fluid/FluidStackRenderer", "me/shedaniel/rei/jeicompat/imitator/JEIFluidStackRendererImitator");
        redirect("mezz/jei/plugins/vanilla/ingredients/item/ItemStackRenderer", "me/shedaniel/rei/jeicompat/imitator/JEIItemStackRendererImitator");
        redirect("mezz/jei/plugins/vanilla/crafting/CategoryRecipeValidator", "me/shedaniel/rei/jeicompat/imitator/CategoryRecipeValidator");
        redirect("mezz/jei/common/util/Translator", "me/shedaniel/rei/jeicompat/imitator/Translator");
        // Remember to rebuild this module after changing this
    }
    
    private void redirect(String oldName, String newName) {
        classMap.put(oldName, newName);
    }
    
    private void classToInterface(String oldName, String newName) {
        redirect(oldName, newName);
        redirectInvokeVirtual.add(oldName);
    }
    
    public boolean isDev() {
        if (isDev == null) {
            try {
                Class.forName("dev.architectury.transformer.TransformerRuntime");
                System.out.println("we are pogging in dev");
                isDev = true;
            } catch (ClassNotFoundException e) {
                isDev = false;
            }
        }
        
        return isDev;
    }
    
    @Override
    public String mapSignature(String signature, boolean typeSignature) {
        if (signature == null || signature.isEmpty()) return signature;
        return super.mapSignature(signature, typeSignature);
    }
    
    @Override
    public void accept(ClassNode classNode) {
        if (isDev()) {
            if (classNode.name.endsWith("NonNullLazyValue")) {
                for (MethodNode method : classNode.methods) {
                    if (method.name.equals("get")) {
                        method.instructions.clear();
                        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/util/LazyLoadedValue", "get", "()Ljava/lang/Object;"));
                        method.instructions.add(new InsnNode(Opcodes.ARETURN));
                    }
                }
            }
            if (classNode.name.endsWith("/ItemRenderer")) {
                for (FieldNode field : classNode.fields) {
                    field.access = field.access & (~Opcodes.ACC_PRIVATE);
                    field.access = field.access | Opcodes.ACC_PUBLIC;
                }
            }
            if (classNode.name.endsWith("BrewingRecipeHelper")) {
                out:
                for (MethodNode method : classNode.methods) {
                    if (method.name.startsWith("<")) {
                        for (AbstractInsnNode instruction : method.instructions) {
                            if (instruction instanceof LdcInsnNode) {
                                if (Objects.equals(((LdcInsnNode) instruction).cst, "net.minecraft.potion.PotionBrewing$MixPredicate")) {
                                    ((LdcInsnNode) instruction).cst = "net.minecraft.world.item.alchemy.PotionBrewing$Mix";
                                }
                                break out;
                            }
                        }
                    }
                }
            }
        }
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
                    
                    @Override
                    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                        if (Objects.equals(bootstrapMethodHandle.getOwner(), "java/lang/invoke/LambdaMetafactory") && Objects.equals(bootstrapMethodHandle.getName(), "metafactory") && bootstrapMethodArguments.length >= 3) {
                            if (bootstrapMethodArguments[1] instanceof Handle) {
                                Handle handle = (Handle) bootstrapMethodArguments[1];
                                if (redirectInvokeVirtual.contains(handle.getOwner())) {
                                    bootstrapMethodArguments[1] = new Handle(handle.getTag() == Opcodes.H_INVOKEVIRTUAL ? Opcodes.H_INVOKEINTERFACE : handle.getTag(),
                                            handle.getOwner(),
                                            handle.getName(),
                                            handle.getDesc(),
                                            true);
                                }
                            }
                        }
                        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
                    }
                    
                    @Override
                    public void visitLdcInsn(Object value) {
                        if (value instanceof String) {
                            String possibleReplacement = classMap.get(((String) value).replace('.', '/'));
                            
                            if (possibleReplacement != null) {
                                super.visitLdcInsn(possibleReplacement.replace('/', '.'));
                                return;
                            }
                        }
                        super.visitLdcInsn(value);
                    }
                };
            }
        };
        classNode.accept(remapper);
        replace(newClassNode, classNode);
    }
    
    @Override
    public String map(String internalName) {
        return classMap.getOrDefault(internalName, internalName);
    }
    
    /*
     * This file is part of Mixin, licensed under the MIT License (MIT).
     *
     * Copyright (c) SpongePowered <https://www.spongepowered.org>
     * Copyright (c) contributors
     *
     * Permission is hereby granted, free of charge, to any person obtaining a copy
     * of this software and associated documentation files (the "Software"), to deal
     * in the Software without restriction, including without limitation the rights
     * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
     * copies of the Software, and to permit persons to whom the Software is
     * furnished to do so, subject to the following conditions:
     *
     * The above copyright notice and this permission notice shall be included in
     * all copies or substantial portions of the Software.
     *
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
     * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
     * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
     * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
     * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
     * THE SOFTWARE.
     */
    public static void merge(ClassNode source, ClassNode dest) {
        if (source == null) {
            return;
        }
        
        if (dest == null) {
            throw new NullPointerException("Target ClassNode for merge must not be null");
        }
        
        dest.version = Math.max(source.version, dest.version);
        
        dest.interfaces = InternalsRemapperTransformer.merge(source.interfaces, dest.interfaces);
        dest.invisibleAnnotations = InternalsRemapperTransformer.merge(source.invisibleAnnotations, dest.invisibleAnnotations);
        dest.visibleAnnotations = InternalsRemapperTransformer.merge(source.visibleAnnotations, dest.visibleAnnotations);
        dest.visibleTypeAnnotations = InternalsRemapperTransformer.merge(source.visibleTypeAnnotations, dest.visibleTypeAnnotations);
        dest.invisibleTypeAnnotations = InternalsRemapperTransformer.merge(source.invisibleTypeAnnotations, dest.invisibleTypeAnnotations);
        dest.attrs = InternalsRemapperTransformer.merge(source.attrs, dest.attrs);
        dest.innerClasses = InternalsRemapperTransformer.merge(source.innerClasses, dest.innerClasses);
        dest.fields = InternalsRemapperTransformer.merge(source.fields, dest.fields);
        dest.methods = InternalsRemapperTransformer.merge(source.methods, dest.methods);
    }
    
    public static void replace(ClassNode source, ClassNode dest) {
        if (source == null) {
            return;
        }
        
        if (dest == null) {
            throw new NullPointerException("Target ClassNode for replace must not be null");
        }
        
        dest.name = source.name;
        dest.signature = source.signature;
        dest.superName = source.superName;
        
        dest.version = source.version;
        dest.access = source.access;
        dest.sourceDebug = source.sourceDebug;
        
        dest.sourceFile = source.sourceFile;
        dest.outerClass = source.outerClass;
        dest.outerMethod = source.outerMethod;
        dest.outerMethodDesc = source.outerMethodDesc;
        
        InternalsRemapperTransformer.clear(dest.interfaces);
        InternalsRemapperTransformer.clear(dest.visibleAnnotations);
        InternalsRemapperTransformer.clear(dest.invisibleAnnotations);
        InternalsRemapperTransformer.clear(dest.visibleTypeAnnotations);
        InternalsRemapperTransformer.clear(dest.invisibleTypeAnnotations);
        InternalsRemapperTransformer.clear(dest.attrs);
        InternalsRemapperTransformer.clear(dest.innerClasses);
        InternalsRemapperTransformer.clear(dest.fields);
        InternalsRemapperTransformer.clear(dest.methods);
        
        dest.module = source.module;
        
        dest.nestHostClass = source.nestHostClass;
        InternalsRemapperTransformer.clear(dest.nestMembers);
        dest.nestMembers = InternalsRemapperTransformer.merge(source.nestMembers, dest.nestMembers);
        
        InternalsRemapperTransformer.merge(source, dest);
    }
    
    private static <T> void clear(List<T> list) {
        if (list != null) {
            list.clear();
        }
    }
    
    private static <T> List<T> merge(List<T> source, List<T> destination) {
        if (source == null || source.isEmpty()) {
            return destination;
        }
        
        if (destination == null) {
            return new ArrayList<T>(source);
        }
        
        destination.addAll(source);
        return destination;
    }
}