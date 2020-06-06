package me.shedaniel.rei.impl.search;

import me.shedaniel.rei.api.EntryStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class TagArgument extends Argument {
    public static final TagArgument INSTANCE = new TagArgument();
    private static final MinecraftClient minecraft = MinecraftClient.getInstance();
    
    @Override
    public String getName() {
        return "tag";
    }
    
    @Override
    public @Nullable String getPrefix() {
        return "$";
    }
    
    @Override
    public boolean matches(Object[] data, EntryStack stack, String searchText, Object searchData) {
        if (data[getDataOrdinal()] == null) {
            if (stack.getType() == EntryStack.Type.ITEM) {
                Identifier[] tagsFor = minecraft.getNetworkHandler().getTagManager().items().getTagsFor(stack.getItem()).toArray(new Identifier[0]);
                data[getDataOrdinal()] = new String[tagsFor.length];
                for (int i = 0; i < tagsFor.length; i++)
                    ((String[]) data[getDataOrdinal()])[i] = tagsFor[i].toString();
            } else if (stack.getType() == EntryStack.Type.FLUID) {
                Identifier[] tagsFor = minecraft.getNetworkHandler().getTagManager().fluids().getTagsFor(stack.getFluid()).toArray(new Identifier[0]);
                data[getDataOrdinal()] = new String[tagsFor.length];
                for (int i = 0; i < tagsFor.length; i++)
                    ((String[]) data[getDataOrdinal()])[i] = tagsFor[i].toString();
            } else
                data[getDataOrdinal()] = new String[0];
        }
        String[] tags = (String[]) data[getDataOrdinal()];
        if (tags.length > 0) {
            for (String tag : tags)
                if (tag.isEmpty() || tag.contains(searchText))
                    return true;
        }
        return false;
    }
    
    private TagArgument() {
    }
}
