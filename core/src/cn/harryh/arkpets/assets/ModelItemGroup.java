/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.assets;

import cn.harryh.arkpets.assets.ModelItem.PropertyExtractor;

import java.util.*;
import java.util.function.Predicate;


/** The class implements the Collection of {@link ModelItem}.
 * <hr>
 * The structure of the root directory may be like what is shown below.
 * Each {@code SubDir} represents an {@code ModelItem}.
 *
 * <blockquote><pre>
 * +-RootDir
 * |-+-SubDir1 (whose name is the model name of xxx)
 * | |---xxx.atlas
 * | |---xxx.png
 * | |---xxx.skel
 * |---SubDir2
 * |---SubDir3
 * |---...</pre>
 * </blockquote>
 *
 * @since ArkPets 2.4
 */
public class ModelItemGroup implements Collection<ModelItem> {
    protected final ArrayList<ModelItem> modelItemList;

    public ModelItemGroup(Collection<ModelItem> modelItemList) {
        this.modelItemList = new ArrayList<>(modelItemList);
    }

    public ModelItemGroup() {
        this(new ArrayList<>());
    }

    /** Searches the Model Items whose {@code name} and {@code appellation} match the given keywords.
     * @param keyWords The given keywords. Each keyword should be separated by a blank.
     * @return A Model Item Group. Returns {@code this} if the parameter {@code keyWords} is {@code null} or empty.
     */
    public ModelItemGroup searchByKeyWords(String keyWords) {
        if (keyWords == null || keyWords.isEmpty())
            return this;
        String[] wordList = keyWords.split(" ");
        ModelItemGroup result = new ModelItemGroup();
        for (ModelItem asset : this) {
            for (String word : wordList) {
                if (asset.name != null &&
                        asset.name.toLowerCase().contains(word.toLowerCase())) {
                    result.add(asset);
                }
            }
            for (String word : wordList) {
                if (asset.appellation != null &&
                        asset.appellation.toLowerCase().contains(word.toLowerCase())) {
                    if (!result.contains(asset))
                        result.add(asset);
                }
            }
        }
        return result;
    }

    /** Searches the Model Item whose relative path provided by {@code getLocation} matches the given path string.
     * @param relPath The given path string.
     * @return The first matched Model Item. Returns {@code null} if no one matched.
     */
    public ModelItem searchByRelPath(String relPath) {
        if (relPath == null || relPath.isEmpty())
            return null;
        for (ModelItem model : this)
            if (model.getLocation().equalsIgnoreCase(relPath))
                return model;
        return null;
    }

    /** Collects the values of a specified property of the Model Items.
     * @param property A property extractor.
     * @return A Set that contains all the possible values of the property.
     * @param <T> The type of the property value.
     */
    public <T> Set<T> extract(PropertyExtractor<T> property) {
        HashSet<T> result = new HashSet<>();
        for (ModelItem item : this)
            result.addAll(property.apply(item));
        return result;
    }

    /** Returns a new Model Item Group consisting of the Model Items that match the given predicate.
     * @param predicate A predicate to apply to each Model Item to determine if it should be included.
     * @return A Model Item Group.
     */
    public ModelItemGroup filter(Predicate<ModelItem> predicate) {
        return new ModelItemGroup(modelItemList.stream().filter(predicate).toList());
    }

    /** Returns a new Model Item Group consisting of the Model Items whose property satisfied the requirements.
     * @param property A property extractor.
     * @param filterValues The property values to be matched.
     * @param mode The {@link ModelItemGroup.FilterMode}.
     * @return A Model Item Group.
     * @param <T> The type of the property value.
     */
    public <T> ModelItemGroup filter(PropertyExtractor<T> property, Set<T> filterValues, int mode) {
        final boolean TRUE = (mode & FilterMode.MATCH_REVERSE) == 0;
        return filter(item -> {
            Set<T> itemValues = property.apply(item);
            if ((mode & FilterMode.MATCH_ANY) != 0) {
                for (T value : itemValues)
                    if (filterValues.contains(value))
                        return TRUE;
            } else {
                if (itemValues.containsAll(filterValues))
                    return TRUE;
            }
            return !TRUE;
        });
    }

    /** Returns a new Model Item Group consisting of the Model Items whose property satisfied the requirements.
     * @param property A property extractor.
     * @param filterValues The property values to be matched.
     * @return A Model Item Group.
     * @param <T> The type of the property value.
     */
    public <T> ModelItemGroup filter(PropertyExtractor<T> property, Set<T> filterValues) {
        return filter(property, filterValues, 0);
    }

    /** Sorts the Model Items by their {@code assetDir} in natural order.
     */
    public void sort() {
        modelItemList.sort(Comparator.comparing(model -> model.assetDir, Comparator.naturalOrder()));
    }


    public static class FilterMode {
        public static final int MATCH_ANY           = 0b1;
        public static final int MATCH_REVERSE       = 0b10;
    }

    @Override
    public Iterator<ModelItem> iterator() {
        return modelItemList.iterator();
    }

    @Override
    public boolean add(ModelItem modelItem) {
        return !modelItemList.contains(modelItem) && modelItemList.add(modelItem);
    }

    @Override
    public boolean addAll(Collection<? extends ModelItem> c) {
        int size = size();
        c.forEach(this::add);
        return size != size();
    }

    @Override
    public boolean contains(Object o) {
        return modelItemList.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return modelItemList.containsAll(c);
    }

    @Override
    public boolean remove(Object o) {
        return modelItemList.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return modelItemList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return modelItemList.retainAll(c);
    }

    @Override
    public void clear() {
        modelItemList.clear();
    }

    @Override
    public boolean isEmpty() {
        return modelItemList.isEmpty();
    }

    @Override
    public int size() {
        return modelItemList.size();
    }

    @Override
    public Object[] toArray() {
        return modelItemList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return modelItemList.toArray(a);
    }
}
