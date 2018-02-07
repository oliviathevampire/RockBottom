package de.ellpeck.rockbottom.gui;

import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.IRenderer;
import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.assets.IAssetManager;
import de.ellpeck.rockbottom.api.construction.BasicRecipe;
import de.ellpeck.rockbottom.api.construction.IRecipe;
import de.ellpeck.rockbottom.api.data.settings.Settings;
import de.ellpeck.rockbottom.api.entity.player.AbstractEntityPlayer;
import de.ellpeck.rockbottom.api.gui.GuiContainer;
import de.ellpeck.rockbottom.api.gui.component.ComponentFancyButton;
import de.ellpeck.rockbottom.api.gui.component.ComponentInputField;
import de.ellpeck.rockbottom.api.gui.component.ComponentMenu;
import de.ellpeck.rockbottom.api.gui.component.MenuComponent;
import de.ellpeck.rockbottom.api.gui.component.construction.ComponentConstruct;
import de.ellpeck.rockbottom.api.gui.component.construction.ComponentIngredient;
import de.ellpeck.rockbottom.api.gui.component.construction.ComponentPolaroid;
import de.ellpeck.rockbottom.api.inventory.IInventory;
import de.ellpeck.rockbottom.api.item.ItemInstance;
import de.ellpeck.rockbottom.api.util.BoundBox;
import de.ellpeck.rockbottom.api.util.Colors;
import de.ellpeck.rockbottom.api.util.reg.IResourceName;
import de.ellpeck.rockbottom.gui.container.ContainerInventory;
import de.ellpeck.rockbottom.net.packet.toserver.PacketManualConstruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

public class GuiCompendium extends GuiContainer{

    private static final IResourceName LEFT_PAGE = RockBottomAPI.createInternalRes("gui.construction.page_items");
    private static final IResourceName RIGHT_PAGE = RockBottomAPI.createInternalRes("gui.construction.page_recipes");
    private static final IResourceName SEARCH_ICON = RockBottomAPI.createInternalRes("gui.construction.search_bar");
    private static final IResourceName SEARCH_BAR = RockBottomAPI.createInternalRes("gui.construction.search_bar_extended");

    public static final int PAGE_WIDTH = 72;
    public static final int PAGE_HEIGHT = 94;

    private final BiConsumer<IInventory, Integer> invCallback = (inv, slot) -> this.organize();
    private ComponentMenu menu;
    private final List<ComponentPolaroid> polaroids = new ArrayList<>();
    private final List<ComponentIngredient> ingredients = new ArrayList<>();
    private ComponentConstruct construct;

    private ComponentInputField searchBar;
    private BoundBox searchButtonBox;
    private String searchText = "";

    public IRecipe selectedRecipe;

    public GuiCompendium(AbstractEntityPlayer player){
        super(player, PAGE_WIDTH*2+1, PAGE_HEIGHT+75);
    }

    @Override
    public void init(IGameInstance game){
        super.init(game);

        this.menu = new ComponentMenu(this, 2, 2, PAGE_HEIGHT-4, 3, 4, new BoundBox(0, 0, PAGE_WIDTH, PAGE_HEIGHT).add(this.x, this.y));
        this.components.add(this.menu);

        this.components.add(new ComponentFancyButton(this, 5-16, GuiCompendium.PAGE_HEIGHT+5, 14, 14, () -> {
            game.getGuiManager().closeGui();
            this.player.openGuiContainer(new GuiInventory(this.player), this.player.getInvContainer());
            return true;
        }, RockBottomAPI.createInternalRes("gui.construction.book_open"), "Close the Compendium"));

        this.searchBar = new ComponentInputField(this, 145, 79, 70, 12, false, false, false, 64, false, strg -> {
            if(!strg.equals(this.searchText)){
                this.searchText = strg;
                this.organize();
            }
        });
        this.searchBar.setActive(false);
        this.components.add(this.searchBar);

        this.searchButtonBox = new BoundBox(0, 0, 13, 14).add(this.x+145, this.y+78);

        this.organize();
    }

    private void organize(){
        this.menu.clear();
        this.polaroids.clear();

        boolean containsSelected = false;
        for(BasicRecipe recipe : RockBottomAPI.MANUAL_CONSTRUCTION_RECIPES.getUnmodifiable().values()){
            if(recipe.isKnown(this.player)){
                if(this.searchText.isEmpty() || this.matchesSearch(recipe.getOutputs())){
                    for(ComponentPolaroid polaroid : recipe.getPolaroidButtons(this, this.player, IRecipe.matchesInv(recipe, this.player.getInv()))){
                        polaroid.isSelected = this.selectedRecipe == recipe;
                        if(polaroid.isSelected){
                            containsSelected = true;
                        }

                        this.polaroids.add(polaroid);
                    }
                }
            }
            else if(this.searchText.isEmpty()){
                this.polaroids.add(new ComponentPolaroid(this, null, false));
            }
        }
        if(!containsSelected){
            this.selectedRecipe = null;
        }

        this.polaroids.sort((p1, p2) -> Integer.compare(Boolean.compare(p1.recipe == null, p2.recipe == null)*2, Boolean.compare(p1.canConstruct, p2.canConstruct)));

        for(ComponentPolaroid comp : this.polaroids){
            this.menu.add(new MenuComponent(18, 20).add(0, 2, comp));
        }

        this.menu.organize();

        if(this.selectedRecipe != null){
            this.stockIngredients(this.selectedRecipe.getIngredientButtons(this, this.player));
        }
        else{
            this.stockIngredients(Collections.emptyList());
        }
        this.initConstructButton(this.selectedRecipe);
    }

    private boolean matchesSearch(List<ItemInstance> outputs){
        String lowerSearch = this.searchText.toLowerCase(Locale.ROOT);
        for(ItemInstance instance : outputs){
            if(instance.getDisplayName().toLowerCase(Locale.ROOT).contains(lowerSearch)){
                return true;
            }
        }
        return false;
    }

    private void stockIngredients(List<ComponentIngredient> actualIngredients){
        if(!this.ingredients.isEmpty()){
            this.components.removeAll(this.ingredients);
            this.ingredients.clear();
        }

        this.ingredients.addAll(actualIngredients);
        while(this.ingredients.size() < 8){
            this.ingredients.add(new ComponentIngredient(this, false, Collections.emptyList()));
        }

        this.components.addAll(this.ingredients);

        int ingrX = 0;
        int ingrY = 0;
        int counter = 0;

        for(ComponentIngredient comp : this.ingredients){
            comp.setPos(78+ingrX, 51+ingrY);

            ingrX += 16;
            counter++;

            if(counter >= 4){
                counter = 0;
                ingrX = 0;

                ingrY += 19;
            }
        }
    }

    private void initConstructButton(IRecipe recipe){
        if(this.construct != null){
            this.components.remove(this.construct);
            this.construct = null;
        }

        if(recipe != null){
            this.construct = recipe.getConstructButton(this, this.player, IRecipe.matchesInv(this.selectedRecipe, this.player.getInv()));
            this.components.add(this.construct);
        }
    }

    @Override
    public void render(IGameInstance game, IAssetManager manager, IRenderer g){
        manager.getTexture(LEFT_PAGE).draw(this.x, this.y, PAGE_WIDTH, PAGE_HEIGHT);
        manager.getTexture(RIGHT_PAGE).draw(this.x+PAGE_WIDTH+1, this.y, PAGE_WIDTH, PAGE_HEIGHT);

        if(this.selectedRecipe != null){
            String strg = this.selectedRecipe.getOutputs().get(0).getDisplayName();
            manager.getFont().drawString(this.x+109-manager.getFont().getWidth(strg, 0.25F)/2, this.y+6, strg, 0, strg.length(), 0.25F, Colors.BLACK, Colors.NO_COLOR);
        }

        if(this.searchBar.isActive()){
            manager.getTexture(SEARCH_BAR).draw(this.x+145, this.y+78, 84, 14);
        }
        else{
            manager.getTexture(SEARCH_ICON).draw(this.x+145, this.y+78, 13, 14);
        }

        super.render(game, manager, g);
    }

    @Override
    public IResourceName getName(){
        return RockBottomAPI.createInternalRes("compendium");
    }

    @Override
    public void onOpened(IGameInstance game){
        super.onOpened(game);
        this.player.getInv().addChangeCallback(this.invCallback);
    }

    @Override
    public void onClosed(IGameInstance game){
        super.onClosed(game);
        this.player.getInv().removeChangeCallback(this.invCallback);
    }

    @Override
    public boolean onMouseAction(IGameInstance game, int button, float x, float y){
        if(!super.onMouseAction(game, button, x, y)){
            if(Settings.KEY_GUI_ACTION_1.isKey(button)){
                if(this.searchButtonBox.contains(x, y)){
                    boolean activeNow = !this.searchBar.isActive();
                    this.searchBar.setActive(activeNow);
                    this.searchBar.setSelected(true);

                    this.searchButtonBox.add((activeNow ? 1 : -1)*71, 0);

                    if(!this.searchText.isEmpty()){
                        this.searchBar.setText("");
                        this.searchText = "";
                        this.organize();
                    }

                    return true;
                }

                if(this.construct != null && this.construct.isMouseOver(game)){
                    if(RockBottomAPI.getNet().isClient()){
                        RockBottomAPI.getNet().sendToServer(new PacketManualConstruction(game.getPlayer().getUniqueId(), RockBottomAPI.ALL_CONSTRUCTION_RECIPES.getId(this.selectedRecipe), 1));
                    }
                    else{
                        ContainerInventory.doInvBasedConstruction(game.getPlayer(), this.selectedRecipe, 1);
                    }
                    return true;
                }

                boolean did = false;

                for(ComponentPolaroid polaroid : this.polaroids){
                    if(polaroid.recipe != null && polaroid.isMouseOverPrioritized(game)){
                        if(this.selectedRecipe != polaroid.recipe){
                            this.selectedRecipe = polaroid.recipe;
                            polaroid.isSelected = true;

                            this.initConstructButton(polaroid.recipe);
                            this.stockIngredients(polaroid.recipe.getIngredientButtons(this, this.player));
                        }
                        did = true;
                    }
                    else{
                        polaroid.isSelected = false;
                    }
                }

                if(!did){
                    if(this.selectedRecipe != null){
                        this.selectedRecipe = null;
                        this.initConstructButton(null);
                        this.stockIngredients(Collections.emptyList());
                    }
                }
                return did;
            }
            return false;
        }
        else{
            return true;
        }
    }

    @Override
    public int getSlotOffsetX(){
        return 5;
    }

    @Override
    public int getSlotOffsetY(){
        return GuiCompendium.PAGE_HEIGHT+5;
    }

    @Override
    public boolean shouldDoFingerCursor(IGameInstance game){
        IRenderer g = game.getRenderer();
        return this.searchButtonBox.contains(g.getMouseInGuiX(), g.getMouseInGuiY()) || super.shouldDoFingerCursor(game);
    }
}