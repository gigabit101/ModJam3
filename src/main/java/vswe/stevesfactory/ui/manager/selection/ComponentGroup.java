package vswe.stevesfactory.ui.manager.selection;

import com.google.gson.*;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.val;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import vswe.stevesfactory.StevesFactoryManager;
import vswe.stevesfactory.api.StevesFactoryManagerAPI;
import vswe.stevesfactory.api.logic.IProcedureType;
import vswe.stevesfactory.api.visibility.GUIVisibility;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public final class ComponentGroup {

    public static final Set<IProcedureType<?>> groupedTypes = new HashSet<>();
    public static final Set<IProcedureType<?>> ungroupedTypes = new HashSet<>();
    public static final List<ComponentGroup> groups = new ArrayList<>();

    private static final String DEFAULT_COMPONENTS_PATH = "/assets/${StevesFactoryManagerAPI.MODID}/component_groups/";
    private static final String ORDER_DECLARATION_FILE = "@order.json";

    private static File getConfigDirectory() {
        return new File("./config/${StevesFactoryManagerAPI.MODID}/component_groups/");
    }

    public static void reload(boolean reset) {
        cleanCache();

        File directory = getConfigDirectory();
        JsonParser parser = new JsonParser();
        if (reset) {
            try {
                FileUtils.deleteDirectory(directory);
                copySettings(parser, directory);
            } catch (IOException e) {
                StevesFactoryManager.logger.error("Error resetting component group configs", e);
            }
        } else {
            String[] list = directory.list();
            if (!directory.exists() || list == null || list.length == 0) {
                copySettings(parser, directory);
            }
        }

        try {
            setupInternal(parser, directory);
        } catch (IOException e) {
            StevesFactoryManager.logger.error("Error setting up groups", e);
        }

        categorizeTypes();
    }

    private static void cleanCache() {
        groups.clear();
        groupedTypes.clear();
        ungroupedTypes.clear();
    }

    private static void copySettings(JsonParser parser, File configDir) {
        boolean success = configDir.mkdirs();

        val ordersFileName = "@order.json";
        try (val loaderIn = StevesFactoryManager.class.getResourceAsStream(DEFAULT_COMPONENTS_PATH + "@loader.json")) {
            // No need to close this because it is essentially a wrapper around the InputStream
            val loadReader = new InputStreamReader(loaderIn);
            val loaderRoot = parser.parse(loadReader).getAsJsonObject();

            // Parsing files
            val files = loaderRoot.getAsJsonArray("files");
            for (JsonElement element : files) {
                val fileName = element.getAsString();
                val filePath = DEFAULT_COMPONENTS_PATH + fileName;

                // Copy the definition file to config directory
                try (val fileIn = StevesFactoryManager.class.getResourceAsStream(filePath)) {
                    Files.copy(fileIn, new File(configDir.getPath() + "/" + fileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    StevesFactoryManager.logger.error("Error copying default component group config file {}", filePath, e);
                }
            }

            val ordersElement = loaderRoot.get("orderFile");
            if (ordersElement != null) {
                ordersFileName = ordersElement.getAsString();
            }
        } catch (IOException e) {
            StevesFactoryManager.logger.error("Error reading loader config", e);
        }

        // Copying @order.json
        try (val orderIn = StevesFactoryManager.class.getResourceAsStream(DEFAULT_COMPONENTS_PATH + ordersFileName)) {
            Files.copy(orderIn, new File(configDir.getPath() + "/" + ORDER_DECLARATION_FILE).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            StevesFactoryManager.logger.error("Error copying default component group order config", e);
        }
    }

    private static void setupInternal(JsonParser parser, File directory) throws IOException {
        val orders = new Object2IntOpenHashMap<>();
        val orderFile = new File(directory, ORDER_DECLARATION_FILE);
        try (val reader = new FileReader(orderFile)) {
            val root = parser.parse(reader).getAsJsonObject();
            val entries = root.getAsJsonArray("order");
            int i = 1;
            for (val entry : entries) {
                val name = entry.getAsString();
                orders.put(name, i);
                i++;
            }
        }

        val files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (val file : files) {
            val fileName = file.getName();
            if (!"json".equals(FilenameUtils.getExtension(fileName)) || ORDER_DECLARATION_FILE.equals(fileName)) {
                continue;
            }
            try (val reader = new FileReader(file)) {
                val rootElement = parser.parse(reader);
                val group = new ComponentGroup();
                group.setup(rootElement);
                groups.add(group);
            }
        }

        groups.sort(Comparator.comparingInt(group -> orders.getOrDefault(group.getRegistryName().toString(), 0)));
    }

    private static void categorizeTypes() {
        for (val group : groups) {
            groupedTypes.addAll(group.members);
        }
        for (val type : StevesFactoryManagerAPI.getProceduresRegistry().getValues()) {
            if (!groupedTypes.contains(type) && GUIVisibility.isEnabled(type)) {
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
            val root = (JsonObject) rootElement;
            processName(root.get("name"));
            processIcon(root.get("icon"));
            processTranslationKey(root.get("translation_key"));
            processMembers(root.get("members"));
        }
    }

    private void processName(JsonElement nameElement) {
        val name = nameElement.getAsString();
        registryName = new ResourceLocation(name);
    }

    private void processIcon(JsonElement iconElement) {
        if (iconElement != null) {
            val iconPath = iconElement.getAsString();
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
        val members = membersElement.getAsJsonArray();
        for (val memberElement : members) {
            val member = new ResourceLocation(memberElement.getAsString());
            val type = StevesFactoryManagerAPI.getProceduresRegistry().getValue(member);
            if (type != null && GUIVisibility.isEnabled(type)) {
                this.members.add(type);
            }
        }
    }
}
