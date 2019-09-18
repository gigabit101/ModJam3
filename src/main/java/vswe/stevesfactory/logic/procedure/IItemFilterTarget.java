package vswe.stevesfactory.logic.procedure;

import vswe.stevesfactory.logic.item.IItemFilter;
import vswe.stevesfactory.logic.item.ItemTraitsFilter;

public interface IItemFilterTarget {

    IItemFilter getFilter(int id);

    default IItemFilter getFilter() {
        return getFilter(0);
    }
}
