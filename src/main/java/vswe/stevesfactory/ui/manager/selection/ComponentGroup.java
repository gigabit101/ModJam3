package vswe.stevesfactory.ui.manager.selection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FilenameUtils;
import vswe.stevesfactory.StevesFactoryManager;
import vswe.stevesfactory.api.StevesFactoryManagerAPI;
import vswe.stevesfactory.api.logic.IProcedureType;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ComponentGroup {

    public static final Set<IProcedureType<?>> groupedType = new HashSet<>();
    public static final Set<IProcedureType<?>> ungroupedTypes = new HashSet<>();
    public static final List<ComponentGroup> groups = new ArrayList<>();

    private static final String defaultComponentsPath = "/assets/" + StevesFactoryManager.MODID + "/components/";

    private static File getConfigDirectory() {
        return new File("./config/" + StevesFactoryManager.MODID + "/ComponentGroup/");
    }

    public static void setup() {
        File directory = getConfigDirectory();
        String[] list = directory.list();
        JsonParser parser = new JsonParser();
        if (!directory.exists() || list == null || list.length == 0) {
            copySettings(parser, directory);
        }

        try {
            setupInternal(parser, directory);
        } catch (IOException e) {
            StevesFactoryManager.logger.error("Error setting up groups", e);
        }

        categorizeTypes();
    }

    private static void copySettings(JsonParser parser, File configDir) {
        boolean success = configDir.mkdirs();

        try (InputStreamReader in = new InputStreamReader(StevesFactoryManager.class.getResourceAsStream(defaultComponentsPath + "@loader.json"))) {
            JsonObject root = parser.parse(in).getAsJsonObject();

            // Parsing files
            JsonArray files = root.getAsJsonArray("files");
            for (JsonElement element : files) {
                String fileName = element.getAsString();
                String filePath = defaultComponentsPath + fileName;
                Path configPath = new File(configDir.getPath() + "/" + fileName).toPath();
                try (InputStream fileIn = StevesFactoryManager.class.getResourceAsStream(filePath)) {
                    Files.copy(fileIn, configPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    StevesFactoryManager.logger.error("Error copying default component group config file {}", filePath, e);
                }
            }
        } catch (IOException e) {
            StevesFactoryManager.logger.error("Error reading loader config", e);
        }
    }

    private static void setupInternal(JsonParser parser, File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (!"json".equals(FilenameUtils.getExtension(file.getName()))) {
                continue;
            }
            try (FileReader reader = new FileReader(file)) {
                JsonElement rootElement = parser.parse(reader);
                ComponentGroup group = new ComponentGroup();
                group.setup(rootElement);
                groups.add(group);
            }
        }
    }

    private static void categorizeTypes() {
        for (ComponentGroup group : groups) {
            groupedType.addAll(group.members);
        }
        for (IProcedureType<?> type : StevesFactoryManagerAPI.getProceduresRegistry().getValues()) {
            if (!groupedType.contains(type)) {
                ungroupedTypes.add(type);
            }
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
