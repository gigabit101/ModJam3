package vswe.stevesfactory.blocks;

import vswe.stevesfactory.tiles.TileEntityManager;

public interface ISystemListener
{
    void added(TileEntityManager owner);
    void removed(TileEntityManager owner);
}
