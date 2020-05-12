package vswe.stevesfactory.logic.procedure;

import lombok.val;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import vswe.stevesfactory.api.capability.CapabilityDocuments;
import vswe.stevesfactory.api.capability.ITextDocument;
import vswe.stevesfactory.api.logic.IExecutionContext;
import vswe.stevesfactory.logic.AbstractProcedure;
import vswe.stevesfactory.setup.ModProcedures;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.menu.InventorySelectionMenu;
import vswe.stevesfactory.ui.manager.menu.SignUpdaterLinesMenu;
import vswe.stevesfactory.utils.IOHelper;
import vswe.stevesfactory.utils.NetworkHelper;

import java.util.*;

// TODO support for RFTools screens
public class SignUpdaterProcedure extends AbstractProcedure implements IInventoryTarget {

    public static final int SIGNS = 0;

    private List<BlockPos> signs = new ArrayList<>();
    private String[] texts = new String[4];

    private transient List<LazyOptional<ITextDocument>> cachedCaps = new ArrayList<>();
    private transient boolean dirty = false;

    public SignUpdaterProcedure() {
        super(ModProcedures.signUpdater);
        Arrays.fill(texts, "");
    }

    @Override
    public void execute(IExecutionContext context) {
        pushFrame(context, 0);
        updateCaches(context);
        for (val cap : cachedCaps) {
            cap.ifPresent(display -> {
                for (int i = 0; i < texts.length; i++) {
                    display.setLine(i, new StringTextComponent(texts[i]));
                }
            });
        }
    }

    private void updateCaches(IExecutionContext context) {
        if (!dirty) {
            return;
        }

        cachedCaps.clear();
        NetworkHelper.cacheCaps(context, cachedCaps, signs, CapabilityDocuments.TEXT_DISPLAY_CAPABILITY, __ -> markDirty());
        dirty = false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public FlowComponent<SignUpdaterProcedure> createFlowComponent() {
        val f = new FlowComponent<>(this);
        f.addMenu(new InventorySelectionMenu<>(SIGNS, I18n.format("menu.sfm.SignUpdater.Signs"), I18n.format("error.sfm.SignUpdater.NoTargets"), CapabilityDocuments.TEXT_DISPLAY_CAPABILITY));
        f.addMenu(new SignUpdaterLinesMenu());
        return f;
    }

    @Override
    public CompoundNBT serialize() {
        val tag = super.serialize();
        tag.put("Signs", IOHelper.writeBlockPoses(signs));
        tag.put("Texts", IOHelper.writeStrings(texts));
        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        super.deserialize(tag);
        signs.clear();
        IOHelper.readBlockPoses(tag.getList("Signs", Constants.NBT.TAG_COMPOUND), signs);
        IOHelper.readStrings(tag.getList("Texts", Constants.NBT.TAG_STRING), texts);
        markDirty();
    }

    @Override
    public List<BlockPos> getInventories(int id) {
        return signs;
    }

    public String[] getTexts() {
        return texts;
    }

    @Override
    public void markDirty() {
        dirty = true;
    }
}
