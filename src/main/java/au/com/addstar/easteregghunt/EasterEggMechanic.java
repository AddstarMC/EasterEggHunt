package au.com.addstar.easteregghunt;

import java.util.*;

import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.PlayerLoadout;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.mechanics.GameMechanicBase;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.LoadoutModule;
import au.com.mineauz.minigames.minigame.modules.MinigameModule;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;


public class EasterEggMechanic extends GameMechanicBase implements Listener
{
	private HashMap<String, List<String>> mOldFlags;
    private static HuntPlugin plugin;
	private static String mechanicName = "egg_hunt";
	private static final String ARROWCHAR = "\u27AD";
    private static final String OVERLINE = "\u2594";
	EasterEggMechanic(HuntPlugin plugin)
	{
		EasterEggMechanic.plugin = plugin;
		mOldFlags = new HashMap<>();
	}

	static String name(){
	    return mechanicName;
    }
	@Override
	public String getMechanic() {
		return mechanicName;
	}

	@Override
	public EnumSet<MinigameType> validTypes() {
		return EnumSet.of(MinigameType.SINGLEPLAYER);
	}

	@Override
	public boolean checkCanStart(Minigame minigame, MinigamePlayer minigamePlayer) {
		return true;
	}

	@Override
	public List<MinigamePlayer> balanceTeam(List<MinigamePlayer> players, Minigame mgm )
	{
		return Collections.emptyList();
	}

	@Override
	public MinigameModule displaySettings(Minigame minigame) {
		return null;
	}

	@Override
	public void startMinigame(Minigame minigame, MinigamePlayer minigamePlayer) {

	}

	@Override
	public void stopMinigame(Minigame minigame, MinigamePlayer minigamePlayer) {

	}
	
	@Override
	public void joinMinigame(Minigame minigame, MinigamePlayer minigamePlayer) {
	    boolean completed = false;
	    if(plugin.isWinner(minigamePlayer))completed = true;
	    loadCollectedFlags(minigamePlayer);
	    List<String> gotFlags = minigamePlayer.getFlags();
	    if(completed){
	        minigamePlayer.sendMessage("You have already collected all the eggs....You will feel sick if you eat more!!!","info");
	        plugin.mplugin.getPlayerData().quitMinigame(minigamePlayer,true);
	        return;
        }
		updateLoadout(minigame);
	    updateBook(minigamePlayer);
		DisplayManager manager = DisplayManager.getDisplayManager(minigamePlayer.getPlayer());
		manager.displayBossBar(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &f&lEaster Egg Hunt &2\u2756"), 0);
        if(gotFlags.containsAll(minigame.getFlags())) {
            manager.displayBossBar(ChatColor.translateAlternateColorCodes('&', "&2\u2714 &f&lHead back to the finish sign &2\u2714"), 1);
        }else{
            if(gotFlags.size() > 0){
                float progress = gotFlags.size() / minigame.getFlags().size();
                manager.displayBossBar(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &f&lEaster Egg Hunt " + gotFlags.size() + "/" + minigame.getFlags().size() + " &2\u2756"), progress);
    
            }
        }
    }
	
	private static void loadCollectedFlags(MinigamePlayer player){
	    List<String> flags = HuntPlugin.getCollectedflags(player.getUUID());
	    for(String flag:flags){
	        if(player.hasFlag(flag))continue;
	        player.addFlag(flag);
        }
    }
	
	public void onJoinMinigame(Minigame minigame, MinigamePlayer player) {
		joinMinigame(minigame,player);
	}
	
	@Override
	public void quitMinigame(Minigame minigame, MinigamePlayer minigamePlayer, boolean b) {
		DisplayManager manager = DisplayManager.getDisplayManager(minigamePlayer.getPlayer());
		if(!b) {
            manager.displayBossBarTemp(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &f&lBad Luck &2\u2756"), 1, 30);
        }else{
		    manager.hideBossBar();
        }
		manager.clearEffects();
	}

	@Override
	public void endMinigame(Minigame minigame, List<MinigamePlayer> winners, List<MinigamePlayer> losers) {
        for(MinigamePlayer player : winners) {
            DisplayManager manager = DisplayManager.getDisplayManager(player.getPlayer());
            manager.displayBossBarTemp(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &f&lCongratulations &2\u2756"), 1, 30);
            manager.clearEffects();
            plugin.addWinner(player.getPlayer().getUniqueId());
        }
	}
    private void updateLoadout(Minigame minigame)
    {
        List<String> oldFlags = mOldFlags.get(minigame.getName(false));

        if(oldFlags != null && oldFlags.equals(minigame.getFlags())) {
              //todo work out why we need the old flags...
        }else {
            oldFlags = new ArrayList<>(minigame.getFlags());
            mOldFlags.put(minigame.getName(false), oldFlags);
        }

        PlayerLoadout loadout = LoadoutModule.getMinigameModule(minigame).getLoadout("default");
        loadout.clearLoadout();

        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();

        meta.setTitle(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &f&lEggs to find &2\u2756"));
        meta.setAuthor("Easter Egg Hunt");

        StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &0Easter Egg Hunt &2\u2756\n"));
        builder.append(ChatColor.DARK_GRAY);

        builder.append(OVERLINE+OVERLINE+OVERLINE+OVERLINE+OVERLINE+OVERLINE+OVERLINE+OVERLINE+OVERLINE
                +OVERLINE+OVERLINE+OVERLINE+"\n");
        int lines = 2;

        for(String flag : minigame.getFlags())
        {
            builder.append(ChatColor.DARK_BLUE);
            builder.append(ARROWCHAR);
            builder.append(ChatColor.BLACK);
            builder.append(ChatColor.BOLD);
            builder.append(flag);
            builder.append('\n');
            ++lines;

            if(lines >= 13)
            {
                meta.addPage(builder.toString());
                lines = 0;
                builder = new StringBuilder();
            }
        }

        if(lines > 0)
            meta.addPage(builder.toString());

        item.setItemMeta(meta);

        loadout.addItem(item, 0);
    }

    static void updateBook(MinigamePlayer player)
    {
        ItemStack item = null;
        BookMeta meta = null;

        for(ItemStack i : player.getPlayer().getInventory().all(Material.WRITTEN_BOOK).values())
        {
            if(!i.hasItemMeta())
                continue;

            meta = (BookMeta) i.getItemMeta();

            if(!meta.hasTitle() || !meta.getTitle().equals(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &f&lEggs to find &2\u2756")))
                continue;

            item = i;
            break;
        }

        if(item == null)
            return;

        meta.setPages(new ArrayList<>());

        StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &0Easter Egg Hunt &2\u2756\n"));
        builder.append(ChatColor.DARK_GRAY);
    
        builder.append(OVERLINE+OVERLINE+OVERLINE+OVERLINE+OVERLINE+OVERLINE+OVERLINE+OVERLINE+OVERLINE
                +OVERLINE+OVERLINE+OVERLINE+"\n");
        int lines = 2;

        for(String flag : player.getMinigame().getFlags())
        {
            if(player.getFlags().contains(flag))
                continue;

            builder.append(ChatColor.DARK_BLUE);
            builder.append(ARROWCHAR);
            builder.append(ChatColor.BLACK);
            builder.append(ChatColor.BOLD);

            builder.append(flag);
            builder.append('\n');
            ++lines;

            if(lines >= 13)
            {
                meta.addPage(builder.toString());
                lines = 0;
                builder = new StringBuilder();
            }
        }

        for(String flag : player.getFlags())
        {
            builder.append(ChatColor.DARK_GRAY);
            builder.append("  \u27AD ");
            builder.append(ChatColor.DARK_GRAY);
            builder.append(ChatColor.STRIKETHROUGH);
            builder.append(ChatColor.BOLD);

            builder.append(flag);
            builder.append('\n');
            ++lines;

            if(lines >= 13)
            {
                meta.addPage(builder.toString());
                lines = 0;
                builder = new StringBuilder();
            }
        }

        if(lines > 0)
            meta.addPage(builder.toString());

        item.setItemMeta(meta);

        player.getPlayer().updateInventory();
    }
    
}
