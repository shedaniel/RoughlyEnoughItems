package me.shedaniel.rei.api.search;

import me.shedaniel.rei.api.plugins.PluginManager;
import me.shedaniel.rei.api.registry.Reloadable;

public interface SearchProvider extends Reloadable {
    static SearchProvider getInstance() {
        return PluginManager.getInstance().get(SearchProvider.class);
    }
    
    /**
     * Creates a search filter, which respects user's config options and
     * respects argument prefixes.
     *
     * @param searchTerm the search term of the filter
     * @return the search filter
     */
    SearchFilter createFilter(String searchTerm);
}
