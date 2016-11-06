package vswe.stevesfactory.network;


import net.minecraft.entity.player.EntityPlayer;
import gigabit101.AdvancedSystemManager2.network.DataReader;

public interface IPacketBlock {

    void writeData(gigabit101.AdvancedSystemManager2.network.DataWriter dw, EntityPlayer player, boolean onServer, int id);
    void readData(DataReader dr, EntityPlayer player, boolean onServer, int id);
    int infoBitLength(boolean onServer);
}
