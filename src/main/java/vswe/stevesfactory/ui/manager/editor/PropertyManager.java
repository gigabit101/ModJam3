package vswe.stevesfactory.ui.manager.editor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.api.logic.IClientDataStorage;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.contextmenu.DefaultEntry;
import vswe.stevesfactory.library.gui.contextmenu.IEntry;
import vswe.stevesfactory.logic.item.IItemFilter;
import vswe.stevesfactory.logic.item.ItemTagFilter;
import vswe.stevesfactory.logic.item.ItemTraitsFilter;
import vswe.stevesfactory.logic.procedure.IItemFilterTarget;
import vswe.stevesfactory.ui.manager.menu.ItemTagFilterMenu;
import vswe.stevesfactory.ui.manager.menu.ItemTraitsFilterMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PropertyManager<T, P extends IProcedure & IClientDataStorage> {

    public static <P extends IProcedure & IClientDataStorage & IItemFilterTarget> PropertyManager<IItemFilter, P> createFilterMenu(P procedure, FlowComponent<P> flowComponent, int filterID) {
        PropertyManager<IItemFilter, P> pm = new PropertyManager<>(
                flowComponent,
                () -> procedure.getFilter(filterID),
                filter -> procedure.setFilter(filterID, filter));
        pm.on(filter -> filter instanceof ItemTraitsFilter)
                .name(I18n.format("menu.sfm.ItemFilter.Traits"))
                .prop(ItemTraitsFilter::new)
                .then(() -> new ItemTraitsFilterMenu<>(filterID, I18n.format("menu.sfm.ItemFilter.Traits")));
        pm.on(filter -> filter instanceof ItemTagFilter)
                .name(I18n.format("menu.sfm.ItemFilter.Tags"))
                .prop(ItemTagFilter::new)
                .then(() -> new ItemTagFilterMenu<>(filterID, I18n.format("menu.sfm.ItemFilter.Tags")));
        pm.actionCycling();
        pm.setProperty(procedure.getFilter(filterID));
        return pm;
    }

    private static final List<Supplier<IEntry>> EMPTY_LIST = ImmutableList.of();
    private final FlowComponent<P> flowComponent;
    private final List<Case<T, P>> cases = new ArrayList<>();
    private final Supplier<T> propertyGetter;
    private final Consumer<T> propertySetter;

    private List<Supplier<IEntry>> actions = EMPTY_LIST;
    private Menu<P> menu;

    private int selectedIndex = -1;

    public PropertyManager(FlowComponent<P> flowComponent, Supplier<T> propertyGetter, Consumer<T> propertySetter) {
        this.flowComponent = flowComponent;
        this.propertyGetter = propertyGetter;
        this.propertySetter = propertySetter;
    }

    public Case<T, P> on(Predicate<T> condition) {
        Case<T, P> caseElement = new Case<>(condition);
        cases.add(caseElement);
        return caseElement;
    }

    public T getProperty() {
        return propertyGetter.get();
    }

    public void setProperty(@Nonnull T property) {
        Preconditions.checkNotNull(property);
        int i = 0;
        for (Case<T, P> caseElement : cases) {
            if (caseElement.matches(property)) {
                if (i != selectedIndex) {
                    setPropertyBase(i, caseElement, property);
                }
                break;
            }
            i++;
        }
    }

    private void setProperty(int index) {
        Case<T, P> expectedCase = cases.get(index);
        T newProp = expectedCase.createProperty();
        Preconditions.checkNotNull(newProp);
        Preconditions.checkState(expectedCase.matches(newProp));
        setPropertyBase(index, expectedCase, newProp);
    }

    private void setPropertyBase(int index, Case<T, P> caseElement, T property) {
        selectedIndex = index;

        Menu<P> oldMenu = menu;
        if (oldMenu != null) {
            flowComponent.getMenusBox().getChildren().remove(oldMenu);
            oldMenu.onRemoved();
        }

        propertySetter.accept(property);

        menu = caseElement.menuFactory.get();
        flowComponent.addMenu(menu);
        menu.setParentWidget(flowComponent.getMenusBox());
        flowComponent.getMenusBox().reflow();
        menu.useActionList(actions);
    }

    public Menu<P> getMenu() {
        return menu;
    }

    private void enableActions() {
        if (actions == EMPTY_LIST) {
            actions = new ArrayList<>();
        }
    }

    public void action(Supplier<IEntry> action) {
        enableActions();
        actions.add(action);
    }

    public void actionCycling() {
        // Custom entry name logic implemented, dummy value here
        action(() -> new DefaultEntry(null, "") {
            private int cachedIndex = -1;
            private String cachedText;

            @Override
            public String getText() {
                updateText();
                return cachedText;
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                int nextIndex = selectedIndex + 1 >= cases.size() ? 0 : selectedIndex + 1;
                setProperty(nextIndex);
                getWindow().alive = false;
                return true;
            }

            private void updateText() {
                // Lazy initialization so that even if the property is updated without using the cycling action, the text will still be in sync
                int nextIndex = selectedIndex + 1 >= cases.size() ? 0 : selectedIndex + 1;
                if (cachedIndex != nextIndex || cachedText == null) {
                    Case<T, P> nextCase = cases.get(nextIndex);
                    cachedText = nextCase.hasName()
                            ? I18n.format("gui.sfm.FactoryManager.Tool.Inspector.Props.CycleProperty.Named", nextCase.getName())
                            : I18n.format("gui.sfm.FactoryManager.Tool.Inspector.Props.CycleProperty");
                    cachedIndex = nextIndex;
                    reflowSafe();
                }
            }

            private void reflowSafe() {
                if (getWindow() != null) {
                    getWindow().reflow();
                }
            }
        });
    }

    public static final class Case<T, P extends IProcedure & IClientDataStorage> {

        private Predicate<T> condition;
        private Supplier<T> propertyFactory;
        private Supplier<Menu<P>> menuFactory;
        private String name;

        private Case(Predicate<T> condition) {
            this.condition = condition;
        }

        public Case<T, P> then(Supplier<Menu<P>> factory) {
            Preconditions.checkState(this.menuFactory == null);
            this.menuFactory = factory;
            return this;
        }

        public Case<T, P> prop(Supplier<T> factory) {
            Preconditions.checkState(this.propertyFactory == null);
            this.propertyFactory = factory;
            return this;
        }

        public Case<T, P> name(String name) {
            this.name = name;
            return this;
        }

        public boolean matches(T property) {
            return condition.test(property);
        }

        @Nullable
        private T createProperty() {
            return propertyFactory == null ? null : propertyFactory.get();
        }

        public boolean hasName() {
            return name != null;
        }

        public String getName() {
            return name == null ? "" : name;
        }
    }
}
