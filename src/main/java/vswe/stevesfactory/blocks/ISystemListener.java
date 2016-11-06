package vswe.stevesfactory.blocks;


import gigabit101.AdvancedSystemManager2.blocks.TileEntityManager;

public interface ISystemListener {

    void added(gigabit101.AdvancedSystemManager2.blocks.TileEntityManager owner);
    void removed(TileEntityManager owner);

}
