package au.com.addstar.easteregghunt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;

import com.pauldavdesign.mineauz.minigames.MinigamePlayer;
import com.pauldavdesign.mineauz.minigames.PlayerLoadout;
import com.pauldavdesign.mineauz.minigames.events.EndMinigameEvent;
import com.pauldavdesign.mineauz.minigames.events.JoinMinigameEvent;
import com.pauldavdesign.mineauz.minigames.events.QuitMinigameEvent;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;
import com.pauldavdesign.mineauz.minigames.scoring.ScoreTypeBase;

public class EasterEggLogic extends ScoreTypeBase implements Listener
{
	private HashMap<String, List<String>> mOldFlags;
	
	public EasterEggLogic(Plugin plugin)
	{
		mOldFlags = new HashMap<String, List<String>>();
	}
	
	@Override
	public void balanceTeam( List<MinigamePlayer> players, Minigame mgm )
	{
	}

	@Override
	public String getType()
	{
		return "egghunt";
	}

	@EventHandler(priority=EventPriority.MONITOR)
	private void onMinigameJoin(final JoinMinigameEvent event)
	{
		if(!event.getMinigame().getScoreType().equals("egghunt"))
			return;
		
		updateLoadout(event.getMinigame());

		DisplayManager manager = DisplayManager.getDisplayManager(event.getPlayer());
		manager.displayBossBar(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &f&lEaster Egg Hunt &2\u2756"), 0);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	private void onMinigameLeave(QuitMinigameEvent event)
	{
		if(!event.getMinigame().getScoreType().equals("egghunt"))
			return;
		
		DisplayManager manager = DisplayManager.getDisplayManager(event.getPlayer());
		manager.displayBossBarTemp(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &f&lBad Luck &2\u2756"), 1, 30);
		manager.clearEffects();
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	private void onMinigameLeave(EndMinigameEvent event)
	{
		if(!event.getMinigame().getScoreType().equals("egghunt"))
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
		if(!event.getMinigame().getScoreType().equals("egghunt"))
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
	
	@SuppressWarnings( "deprecation" )
	private void updateBook(MinigamePlayer player)
	{
		ItemStack item = player.getPlayer().getInventory().getItem(0);
		if(item == null || item.getType() != Material.WRITTEN_BOOK)
			return;
		
		BookMeta meta = (BookMeta) item.getItemMeta();
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
		
		PlayerLoadout loadout = minigame.getDefaultPlayerLoadout();
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
