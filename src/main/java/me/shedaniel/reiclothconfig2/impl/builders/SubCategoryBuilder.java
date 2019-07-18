package me.shedaniel.reiclothconfig2.impl.builders;

import com.google.common.collect.Lists;
import me.shedaniel.reiclothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.reiclothconfig2.gui.entries.SubCategoryListEntry;

import java.util.*;
import java.util.function.Supplier;

public class SubCategoryBuilder extends FieldBuilder<Object, SubCategoryListEntry> implements List<AbstractConfigListEntry> {
    
    private List<AbstractConfigListEntry> entries;
    private Supplier<Optional<String[]>> tooltipSupplier = null;
    private boolean expended = false;
    
    public SubCategoryBuilder(String resetButtonKey, String fieldNameKey) {
        super(resetButtonKey, fieldNameKey);
        this.entries = Lists.newArrayList();
    }
    
    @Override
    public void requireRestart(boolean requireRestart) {
        throw new UnsupportedOperationException();
    }
    
    public SubCategoryBuilder setTooltipSupplier(Supplier<Optional<String[]>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }
    
    public SubCategoryBuilder setTooltip(Optional<String[]> tooltip) {
        this.tooltipSupplier = () -> tooltip;
        return this;
    }
    
    public SubCategoryBuilder setTooltip(String... tooltip) {
        this.tooltipSupplier = () -> Optional.ofNullable(tooltip);
        return this;
    }
    
    public SubCategoryBuilder setExpended(boolean expended) {
        this.expended = expended;
        return this;
    }
    
    @Override
    public SubCategoryListEntry build() {
        SubCategoryListEntry entry = new SubCategoryListEntry(getFieldNameKey(), entries, expended);
        entry.setTooltipSupplier(tooltipSupplier);
        return entry;
    }
    
    @Override
    public int size() {
        return entries.size();
    }
    
    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }
    
    @Override
    public boolean contains(Object o) {
        return entries.contains(o);
    }
    
    @Override
    public Iterator<AbstractConfigListEntry> iterator() {
        return entries.iterator();
    }
    
    @Override
    public Object[] toArray() {
        return entries.toArray();
    }
    
    @Override
    public <T> T[] toArray(T[] a) {
        return entries.toArray(a);
    }
    
    @Override
    public boolean add(AbstractConfigListEntry abstractConfigListEntry) {
        return entries.add(abstractConfigListEntry);
    }
    
    @Override
    public boolean remove(Object o) {
        return entries.remove(o);
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        return entries.containsAll(c);
    }
    
    @Override
    public boolean addAll(Collection<? extends AbstractConfigListEntry> c) {
        return entries.addAll(c);
    }
    
    @Override
    public boolean addAll(int index, Collection<? extends AbstractConfigListEntry> c) {
        return entries.addAll(index, c);
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        return entries.removeAll(c);
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        return entries.retainAll(c);
    }
    
    @Override
    public void clear() {
        entries.clear();
    }
    
    @Override
    public AbstractConfigListEntry get(int index) {
        return entries.get(index);
    }
    
    @Override
    public AbstractConfigListEntry set(int index, AbstractConfigListEntry element) {
        return entries.set(index, element);
    }
    
    @Override
    public void add(int index, AbstractConfigListEntry element) {
        entries.add(index, element);
    }
    
    @Override
    public AbstractConfigListEntry remove(int index) {
        return entries.remove(index);
    }
    
    @Override
    public int indexOf(Object o) {
        return entries.indexOf(o);
    }
    
    @Override
    public int lastIndexOf(Object o) {
        return entries.lastIndexOf(o);
    }
    
    @Override
    public ListIterator<AbstractConfigListEntry> listIterator() {
        return entries.listIterator();
    }
    
    @Override
    public ListIterator<AbstractConfigListEntry> listIterator(int index) {
        return entries.listIterator(index);
    }
    
    @Override
    public List<AbstractConfigListEntry> subList(int fromIndex, int toIndex) {
        return entries.subList(fromIndex, toIndex);
    }
    
}