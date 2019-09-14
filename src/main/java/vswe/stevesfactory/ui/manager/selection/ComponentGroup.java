package vswe.stevesfactory.ui.manager.selection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.FileUtil;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import vswe.stevesfactory.StevesFactoryManager;
import vswe.stevesfactory.api.StevesFactoryManagerAPI;
import vswe.stevesfactory.api.logic.IProcedureType;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ComponentGroup {

    public static final List<ComponentGroup> groups = new ArrayList<>();

    private static File getConfigDirectory() {
        return new File("./config/" + StevesFactoryManager.MODID + "/ComponentGroup/");
    }

    public static void setup() {
        try {
            setupInternal();
        } catch (IOException e) {
            StevesFactoryManager.logger.error("Error setting up groups", e);
        }
    }

    private static void setupInternal() throws IOException {

        File directory = getConfigDirectory();
        if (!directory.exists()) {
            boolean success = directory.mkdirs();
            Validate.isTrue(success);
        }

        JsonParser parser = new JsonParser();
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (!"json".equals(FilenameUtils.getExtension(file.getName()))) {
                continue;
            }

            JsonElement rootElement = parser.parse(new FileReader(file));
            ComponentGroup group = new ComponentGroup();
            group.setup(rootElement);
            groups.add(group);
        }
    }

    private ResourceLocation registryName;
    private ResourceLocation icon;
    private String translationKey;
    private List<IProcedureType<?>> members = new ArrayList<>();

    private ComponentGroup() {
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    @Nullable
    public ResourceLocation getIcon() {
        return icon;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public List<IProcedureType<?>> getMembers() {
        return members;
    }

    private void setup(JsonElement rootElement) {
        if (rootElement.isJsonObject()) {
            JsonObject root = (JsonObject) rootElement;
            processName(root.get("name"));
            processIcon(root.get("icon"));
            processTranslationKey(root.get("translation_key"));
            processMembers(root.get("members"));
        }
    }

    private void processName(JsonElement nameElement) {
        String name = nameElement.getAsString();
        registryName = new ResourceLocation(name);
    }

    private void processIcon(JsonElement iconElement) {
        if (iconElement != null) {
            String iconPath = iconElement.getAsString();
            icon = new ResourceLocation(iconPath);
        }
    }

    private void processTranslationKey(JsonElement translationKeyElement) {
        if (translationKeyElement != null) {
            translationKey = translationKeyElement.getAsString();
        } else {
            translationKey = registryName.toString();
        }
    }

    private void processMembers(JsonElement membersElement) {
        JsonArray members = membersElement.getAsJsonArray();
        for (JsonElement memberElement : members) {
            ResourceLocation member = new ResourceLocation(memberElement.getAsString());
            IProcedureType<?> type = StevesFactoryManagerAPI.getProceduresRegistry().getValue(member);
            this.members.add(type);
        }
    }
}
