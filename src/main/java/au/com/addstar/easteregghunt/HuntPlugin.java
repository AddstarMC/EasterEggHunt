package au.com.addstar.easteregghunt;

import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.mechanics.GameMechanics;
import au.com.mineauz.minigames.minigame.Minigame;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.SpawnEgg;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class HuntPlugin extends JavaPlugin implements Listener {
    private static final String eggName = ChatColor.translateAlternateColorCodes('&', "&rEasterEgg&r");
    private static final EntityType[] mEggTypes =
            new EntityType[]{
                    EntityType.CREEPER,
                    EntityType.ZOMBIE,
                    EntityType.BLAZE,
                    EntityType.CAVE_SPIDER,
                    EntityType.CHICKEN,
                    EntityType.COW,
                    EntityType.ENDERMAN,
                    EntityType.GHAST,
                    EntityType.HORSE,
                    EntityType.MAGMA_CUBE,
                    EntityType.MUSHROOM_COW,
                    EntityType.OCELOT,
                    EntityType.PIG,
                    EntityType.PIG_ZOMBIE,
                    EntityType.SHEEP,
                    EntityType.SKELETON,
                    EntityType.SLIME,
                    EntityType.SPIDER,
                    EntityType.SQUID,
                    EntityType.VILLAGER,
                    EntityType.WITCH,
                    EntityType.WOLF,
                    EntityType.GUARDIAN,
                    EntityType.LLAMA,
                    EntityType.POLAR_BEAR,
                    EntityType.PARROT};
    private static Random mRand = new Random();
    private static Map<UUID, List<String>> collectedflags = new HashMap<>();
    Minigames mplugin;
    private Map<UUID,Long> winners = new HashMap<>();
    private WeakHashMap<Player, String> mWaitingEggs = new WeakHashMap<>();
    private WeakHashMap<Player, Minigame> eggPlacer = new WeakHashMap<>();
    private WeakHashMap<Player, Boolean> mRemoveActive = new WeakHashMap<>();
    private Type token;

    static  List<String> getCollectedflags(UUID uuid) {
        if(collectedflags == null)collectedflags = new HashMap<>();
        return collectedflags.getOrDefault(uuid,new ArrayList<>());
    }

    private static void addCollectedFlag(UUID uuid, String flag) {
        List<String> flags = getCollectedflags(uuid);
        flags.add(flag);
        collectedflags.put(uuid,flags);
    }
    
    private static SpawnEgg randomType() {
        SpawnEgg egg = new SpawnEgg();
        egg.setSpawnedType(mEggTypes[mRand.nextInt(mEggTypes.length)]);
        return egg;
    }
    
    private static ItemStack newEasterEgg(String name) {
        ItemStack item = new ItemStack(Material.MONSTER_EGG);
        item.setDurability(randomType().getData());
        
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(eggName);
        meta.setLore(Collections.singletonList(name));
        item.setItemMeta(meta);
        
        return item;
    }
    
    private static boolean isEasterEgg(Item item) {
        ItemStack stack = item.getItemStack();
        if (!stack.hasItemMeta())
            return false;
        
        ItemMeta meta = stack.getItemMeta();
        return eggName.equals(meta.getDisplayName());
    }
    
    private void loadData() {
        File dataFolder = new File(mplugin.getDataFolder(), "eggs");
        if (!dataFolder.exists() && !dataFolder.mkdir())
            mplugin.getLogger().info("Error creating Easter Egg Data Folder");
        File savedFlags = new File(dataFolder, "flags.json");
        try {
            if (!savedFlags.exists() && !savedFlags.createNewFile())
                mplugin.getLogger().info("Error creating SavedFlags File");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Gson gson = new GsonBuilder().create();
        try {
            InputStream stream = new FileInputStream(savedFlags);
            InputStreamReader reader = new InputStreamReader(stream);
            collectedflags = gson.fromJson(reader, token);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        File winnerList = new File(dataFolder, "winners.json");
        try {
            if (!winnerList.exists() && !winnerList.createNewFile())
                mplugin.getLogger().info("Error creating Winners File");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Gson winnergson = new GsonBuilder().create();
        try {
            InputStream stream = new FileInputStream(winnerList);
            InputStreamReader reader = new InputStreamReader(stream);
            winners = winnergson.fromJson(reader, new TypeToken<Map<UUID,Long>>(){}.getType());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(winners == null)winners = new HashMap<>();
        if(collectedflags == null)collectedflags = new HashMap<>();
    
    }
    
    private void saveData() {
        File dataFolder = new File(mplugin.getDataFolder(), "eggs");
        if (!dataFolder.exists() && !dataFolder.mkdir())
            mplugin.getLogger().info("Error creating Easter Egg Data Folder");
        File savedFlags = new File(dataFolder, "flags.json");
        Gson gson = new GsonBuilder().create();
        try (
                FileWriter writer = new FileWriter(savedFlags, false);
                JsonWriter jsonwriter = new JsonWriter(writer)
        ) {
            gson.toJson(collectedflags, token, jsonwriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        File winnerList = new File(dataFolder, "winners.json");
        Gson winnergson = new GsonBuilder().create();
        try (
                FileWriter writer = new FileWriter(winnerList, false);
                JsonWriter jsonwriter = new JsonWriter(writer)
        ) {
            winnergson.toJson(winners, new TypeToken<Map<UUID,Long>>(){}.getType(), jsonwriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
     }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        
        switch (command.getName()) {
            case "eesetgame":
                if (args.length != 1)
                    return false;
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You must be in-game to do this");
                    return true;
                }
                Minigame game = Minigames.plugin.getMinigameData().getMinigame(args[0]);
                if (game == null) {
                    sender.sendMessage(ChatColor.RED + "That game was not found");
                    return false;
                }
                if (!(game.getMechanic() instanceof EasterEggMechanic)) {
                    sender.sendMessage("Game: " + game.getName(true) + " is not an EggHunt");
                    return false;
                }
                eggPlacer.put((Player) sender, game);
                sender.sendMessage("You are now placing eggs for :" + game.getName(true));
                break;
            case "eecreate":
                if (args.length != 1)
                    return false;
                
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You must be in-game to do this");
                    return true;
                }
                
                mWaitingEggs.put((Player) sender, args[0]);
                sender.sendMessage(ChatColor.GOLD + "Right click the ground to finish placing the egg. Left click to cancel");
                
                return true;
            case "eeremove":
                if (args.length != 0)
                    return false;
                
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You must be in-game to do this");
                    return true;
                }
                
                boolean active;
    
                active = mRemoveActive.getOrDefault(sender, false);
                
                active = !active;
                mRemoveActive.put((Player) sender, active);
                
                if (active) {
                    sender.sendMessage(ChatColor.GOLD + "Easter Egg remove mode on");
                    sender.sendMessage(ChatColor.GOLD + "Click the block the egg is in the remove it");
                } else {
                    sender.sendMessage(ChatColor.GREEN + "Easter Egg remove mode off");
                }
                
                return true;
            case "eeremoveall":
                if (args.length != 0 && args.length != 1)
                    return false;
                
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You must be in-game to do this");
                    return true;
                }
                
                int radius = -1;
                
                if (args.length == 1) {
                    try {
                        radius = Integer.parseInt(args[0]);
                        if (radius <= 0) {
                            sender.sendMessage(ChatColor.RED + "Radius must be a positive number");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Radius must be a positive number");
                        return true;
                    }
                }
                
                if (radius == -1) {
                    for (Item item : ((Player) sender).getWorld().getEntitiesByClass(Item.class)) {
                        if (isEasterEgg(item))
                            item.remove();
                    }
                    
                    sender.sendMessage(ChatColor.GREEN + "Removed all easter eggs from this world");
                    sender.sendMessage(ChatColor.GRAY + "WARNING: Will not have removed eggs in unloaded chunks");
                } else {
                    for (Entity ent : ((Player) sender).getNearbyEntities(radius, radius, radius)) {
                        if (ent instanceof Item) {
                            if (isEasterEgg((Item) ent))
                                ent.remove();
                        }
                    }
                    
                    sender.sendMessage(ChatColor.GREEN + "Removed all easter eggs within a " + radius + " block radius from you.");
                    sender.sendMessage(ChatColor.GRAY + "WARNING: Will not have removed eggs in unloaded chunks");
                }
                break;
            default:
                return false;
        }
        
        return true;
    }
    
    @Override
    public void onDisable() {
        saveData();
        super.onDisable();
    }
    
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        try {
            mplugin = (Minigames) Bukkit.getPluginManager().getPlugin("Minigames");
        } catch (ClassCastException e) {
            mplugin = null;
        }
        if (mplugin == null) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        DisplayManager.initialize(this);
        GameMechanics.addGameMechanic(new EasterEggMechanic(this));
        token = new TypeToken<Map<UUID, List<String>>>() {}.getType();
        loadData();
    }
    
     void addWinner(UUID player){
        if(winners == null)winners = new HashMap<>();
        if(isWinner(player))return;
        winners.put(player,System.currentTimeMillis());
    }
    
    private boolean isWinner(UUID uuid){
        if(winners == null)return false;
        return winners.containsKey(uuid);
    }
    
    boolean isWinner(MinigamePlayer player){
        return isWinner(player.getPlayer().getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    private void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;
        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
            case LEFT_CLICK_BLOCK:
                if (mRemoveActive.containsKey(event.getPlayer()) && mRemoveActive.get(event.getPlayer())) {
                    Location loc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
                    loc.add(0.5, 0, 0.5);
                    
                    List<Entity> entities = event.getPlayer().getNearbyEntities(7, 7, 7);
                    
                    boolean removed = false;
                    for (Entity ent : entities) {
                        if (ent instanceof Item && ent.getLocation().distance(loc) < 1) {
                            ent.remove();
                            removed = true;
                        }
                    }
                    
                    if (removed)
                        event.getPlayer().sendMessage(ChatColor.GOLD + "Easter Egg removed");
                    mRemoveActive.remove(event.getPlayer());
                    event.setCancelled(true);
                }
                if (mWaitingEggs.containsKey(event.getPlayer())) {
                    Minigame game = eggPlacer.get(event.getPlayer());
                    if (game == null) {
                        event.getPlayer().sendMessage("Cannot place egg you must set the game with /eesetgame <gamename>");
                        event.setCancelled(true);
                        return;
                    }
                    String eggName = mWaitingEggs.remove(event.getPlayer());
                    if (eggName != null) {
                        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            Location loc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
                            loc.add(0.5, 0.5, 0.5);
                            
                            Item item = event.getPlayer().getWorld().dropItem(loc, newEasterEgg(eggName));
                            item.setPickupDelay(0);
                            item.setVelocity(new Vector());
                            if (game.getMechanic() instanceof EasterEggMechanic) {
                                game.addFlag(eggName);
                                event.getPlayer().sendMessage(ChatColor.GREEN + "Easter egg '" + eggName + "' has been placed.");
                                event.setCancelled(true);
                                return;
                            }
                        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK)
                            event.getPlayer().sendMessage(ChatColor.GOLD + "Easter egg placement cancelled.");
                        event.setCancelled(true);
                    }
                }
                break;
            default:
                break;
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onItemPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!isEasterEgg(event.getItem())) {
                MinigameUtils.debugMessage("Item was not EGG");
                return;
            }
            event.setCancelled(true);
            
            ItemStack item = event.getItem().getItemStack();
            
            ItemMeta meta = item.getItemMeta();
            if (!meta.hasLore()) {
                getLogger().warning("Easter egg did not have egg name?!");
                return;
            }
            
            String name = meta.getLore().get(0);
            
            MinigamePlayer mPlayer = Minigames.plugin.getPlayerData().getMinigamePlayer(player);
            if (mPlayer == null)
                return;
            
            if (!mPlayer.isInMinigame())
                return;
            
            Minigame game = mPlayer.getMinigame();
            
            if (!game.hasFlags() || !game.getFlags().contains(name) || mPlayer.getFlags().contains(name) || (game.getType() != MinigameType.SINGLEPLAYER && !game.hasStarted())) {
                MinigameUtils.debugMessage("Item was EGG but player couldnt pickup....:" + name);
                /*if (mPlayer.getFlags().contains(name)) {
                    mPlayer.sendMessage("Hey greedyguts!! you already got this Egg!!! ");
                    DisplayManager manager = DisplayManager.getDisplayManager(mPlayer.getPlayer());
                    manager.updateDisplays();
                    if(!manager.hasEffect(event.getItem().getLocation()))
                        manager.addEffect("portal", event.getItem().getLocation(), 0.2f, 4, 0, 1);
                    EasterEggMechanic.updateBook(mPlayer);
                }*/
                return;
                
            }
            mPlayer.addFlag(name);
            addCollectedFlag(mPlayer.getUUID(),name);
            ((Player) event.getEntity()).playSound(event.getEntity().getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
            mPlayer.sendMessage("Wooot Eggs!!");
            Bukkit.getPluginManager().callEvent(new FlagGrabEvent(mPlayer, name, game, event.getItem().getLocation()));
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onItemDespawn(ItemDespawnEvent event) {
        if (isEasterEgg(event.getEntity()))
            event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    private void onFlagGrab(FlagGrabEvent event) {
        if (!event.getMinigame().getMechanicName().equals(EasterEggMechanic.name()))
            return;
        
        DisplayManager manager = DisplayManager.getDisplayManager(event.getPlayer().getPlayer());
        
        int found = event.getPlayer().getFlags().size();
        int total = event.getMinigame().getFlags().size();
        float progress = found / (float) total;
        if (progress > 1)
            progress = 1;
        
        if (found < total) {
            event.getPlayer().getPlayer().sendMessage(ChatColor.DARK_GREEN + "[\u2756] " + ChatColor.AQUA + "You found " + ChatColor.GOLD + ChatColor.BOLD + event.getFlag() + ChatColor.AQUA + "! " + ChatColor.GOLD + ChatColor.BOLD.toString() + (total - found) + ChatColor.AQUA + " more to go!");
            manager.displayBossBar(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &f&lEaster Egg Hunt " + found + "/" + total + " &2\u2756"), progress);
        } else {
            event.getPlayer().getPlayer().sendMessage(ChatColor.DARK_GREEN + "[\u2756] " + ChatColor.AQUA + "You found " + ChatColor.GOLD + ChatColor.BOLD + event.getFlag() + ChatColor.AQUA + "! Thats it! " + ChatColor.YELLOW + "Click the finish sign to win!");
            manager.displayBossBar(ChatColor.translateAlternateColorCodes('&', "&2\u2714 &f&lHead back to the finish sign &2\u2714"), 1);
        }
        
        manager.addEffect("portal", event.getLocation(), 0.2f, 4, 0, 1);
    
        EasterEggMechanic.updateBook(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    private void onRespawn(PlayerRespawnEvent event) {
        MinigamePlayer player = Minigames.plugin.getPlayerData().getMinigamePlayer(event.getPlayer());
        if (player == null || !player.isInMinigame())
            return;
        
        Minigame game = player.getMinigame();
        
        if (!game.getMechanicName().equals(EasterEggMechanic.name()))
            return;
        
        EasterEggMechanic.updateBook(player);
    }
    
    
}
