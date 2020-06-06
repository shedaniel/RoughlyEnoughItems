package me.shedaniel.rei.impl.search;

import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.EntryStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@ApiStatus.Internal
public final class ModArgument extends Argument {
    public static final ModArgument INSTANCE = new ModArgument();
    
    @Override
    public String getName() {
        return "mod";
    }
    
    @Override
    public @Nullable String getPrefix() {
        return "@";
    }
    
    @Override
    public boolean matches(Object[] data, EntryStack stack, String searchText, Object searchData) {
        if (data[getDataOrdinal()] == null) {
            data[getDataOrdinal()] = new String[]{
                    stack.getIdentifier().map(Identifier::getNamespace).orElse("").toLowerCase(Locale.ROOT),
                    null
            };
        }
        String[] strings = (String[]) data[getDataOrdinal()];
        if (strings[0].isEmpty() || strings[0].contains(searchText)) return true;
        if (strings[1] == null) {
            strings[1] = ClientHelper.getInstance().getModFromModId(strings[0]).toLowerCase(Locale.ROOT);
        }
        return strings[1].isEmpty() || strings[1].contains(searchText);
    }
    
    private ModArgument() {
    }
}
