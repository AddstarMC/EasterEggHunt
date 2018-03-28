package au.com.addstar.easteregghunt;

import java.util.*;

import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.PlayerLoadout;
import au.com.mineauz.minigames.events.EndMinigameEvent;
import au.com.mineauz.minigames.events.JoinMinigameEvent;
import au.com.mineauz.minigames.events.QuitMinigameEvent;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.mechanics.GameMechanicBase;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.LoadoutModule;
import au.com.mineauz.minigames.minigame.modules.MinigameModule;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;


public class EasterEggMechanic extends GameMechanicBase implements Listener
{
	private HashMap<String, List<String>> mOldFlags;
	private static HuntPlugin plugin;
	
	EasterEggMechanic(HuntPlugin plugin)
	{
		EasterEggMechanic.plugin = plugin;
		mOldFlags = new HashMap<String, List<String>>();
	}

	@Override
	public String getMechanic() {
		return null;
	}

	@Override
	public EnumSet<MinigameType> validTypes() {
		return null;
	}

	@Override
	public boolean checkCanStart(Minigame minigame, MinigamePlayer minigamePlayer) {
		return false;
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
	
	}
	
	public void onJoinMinigame(Minigame minigame, MinigamePlayer player) {
		joinMinigame(minigame,player);
	}
	
	@Override
	public void quitMinigame(Minigame minigame, MinigamePlayer minigamePlayer, boolean b) {

	}

	@Override
	public void endMinigame(Minigame minigame, List<MinigamePlayer> list, List<MinigamePlayer> list1) {

	}


	@EventHandler(priority=EventPriority.MONITOR)
	private void onMinigameJoin(final JoinMinigameEvent event)
	{
		if(!event.getMinigame().getMechanic().getMechanic().equals("egghunt"))
			return;
		
		updateLoadout(event.getMinigame());

		DisplayManager manager = DisplayManager.getDisplayManager(event.getPlayer());
		manager.displayBossBar(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &f&lEaster Egg Hunt &2\u2756"), 0);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	private void onMinigameLeave(QuitMinigameEvent event)
	{
		if(!event.getMinigame().getMechanic().equals("egghunt"))
			return;
		
		DisplayManager manager = DisplayManager.getDisplayManager(event.getPlayer());
		manager.displayBossBarTemp(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &f&lBad Luck &2\u2756"), 1, 30);
		manager.clearEffects();
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	private void onMinigameLeave(EndMinigameEvent event)
	{
		if(!event.getMinigame().getMechanicName().equals("egghunt"))
			return;
		
		for(MinigamePlayer player : event.getWinners())
		{
			DisplayManager manager = DisplayManager.getDisplayManager(player.getPlayer());
			manager.displayBossBarTemp(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &f&lCongratulations &2\u2756"), 1, 30);
			manager.clearEffects();
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	private void onFlagGrab(FlagGrabEvent event)
	{
		if(!event.getMinigame().getMechanicName().equals("egghunt"))
			return;
		
		DisplayManager manager = DisplayManager.getDisplayManager(event.getPlayer().getPlayer());
		
		int found = event.getPlayer().getFlags().size();
		int total = event.getMinigame().getFlags().size();
		float progress = found / (float)total;
		if(progress > 1)
			progress = 1;
		
		if(found < total)
		{
			event.getPlayer().getPlayer().sendMessage(ChatColor.DARK_GREEN + "[\u2756] " + ChatColor.AQUA + "You found " + ChatColor.GOLD + ChatColor.BOLD + event.getFlag() + ChatColor.AQUA + "! " + ChatColor.GOLD + ChatColor.BOLD.toString() + (total - found) + ChatColor.AQUA + " more to go!");
			manager.displayBossBar(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &f&lEaster Egg Hunt " + found + "/" + total + " &2\u2756"), progress);
		}
		else
		{
			event.getPlayer().getPlayer().sendMessage(ChatColor.DARK_GREEN + "[\u2756] " + ChatColor.AQUA + "You found " + ChatColor.GOLD + ChatColor.BOLD + event.getFlag() + ChatColor.AQUA + "! Thats it! " + ChatColor.YELLOW + "Click the finish sign to win!");
			manager.displayBossBar(ChatColor.translateAlternateColorCodes('&', "&2\u2714 &f&lHead back to the finish sign &2\u2714"), 1);
		}
		
		manager.addEffect("portal", event.getLocation(), 0.2f, 4, 0, 1);
		
		updateBook(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	private void onRespawn(PlayerRespawnEvent event)
	{
		MinigamePlayer player = Minigames.plugin.getPlayerData().getMinigamePlayer(event.getPlayer());
		if(player == null || !player.isInMinigame())
			return;
		
		Minigame game = player.getMinigame();
		
		if(!game.getMechanicName().equals("egghunt"))
			return;
		
		updateBook(player);
	}
	
	@SuppressWarnings( "deprecation" )
	private void updateBook(MinigamePlayer player)
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
		
		meta.setPages(new ArrayList<String>());
		
		StringBuilder builder = new StringBuilder();
		builder.append(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &0Easter Egg Hunt &2\u2756\n"));
		builder.append(ChatColor.DARK_GRAY);
		
		builder.append("\u2594\u2594\u2594\u2594\u2594\u2594\u2594\u2594\u2594\u2594\u2594\u2594\n");
		
		int lines = 2;
		
		for(String flag : player.getMinigame().getFlags())
		{
			if(player.getFlags().contains(flag))
				continue;
			
			builder.append(ChatColor.DARK_BLUE);
			builder.append("  \u27AD ");
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
		
	private void updateLoadout(Minigame minigame)
	{
		List<String> oldFlags = mOldFlags.get(minigame.getName(false));
		
		if(oldFlags != null && oldFlags.equals(minigame.getFlags()))
			return;
		oldFlags = new ArrayList<String>(minigame.getFlags());
		mOldFlags.put(minigame.getName(false), oldFlags);
		
		PlayerLoadout loadout = LoadoutModule.getMinigameModule(minigame).getLoadout("default");
		loadout.clearLoadout();
		
		ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) item.getItemMeta();
		
		meta.setTitle(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &f&lEggs to find &2\u2756"));
		meta.setAuthor("Easter Egg Hunt");
		
		StringBuilder builder = new StringBuilder();
		builder.append(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &0Easter Egg Hunt &2\u2756\n"));
		builder.append(ChatColor.DARK_GRAY);
		
		builder.append("\u2594\u2594\u2594\u2594\u2594\u2594\u2594\u2594\u2594\u2594\u2594\u2594\n");
		int lines = 2;
		
		for(String flag : minigame.getFlags())
		{
			builder.append(ChatColor.DARK_BLUE);
			builder.append("  \u27AD ");
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
}
