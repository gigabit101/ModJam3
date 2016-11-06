package vswe.stevesfactory.components;

public interface IItemBufferElement {
    void prepareSubElements();
    gigabit101.AdvancedSystemManager2.components.IItemBufferSubElement getSubElement();
    void removeSubElement();
    int retrieveItemCount(int moveCount);
    void decreaseStackSize(int moveCount);
    void releaseSubElements();
}
