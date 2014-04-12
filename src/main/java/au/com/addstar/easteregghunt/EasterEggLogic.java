package au.com.addstar.easteregghunt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
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
		Bukkit.getPluginManager().registerEvents(this, plugin);
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
		manager.displayBossBar("Easter Egg Hunt", 0);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	private void onMinigameLeave(QuitMinigameEvent event)
	{
		if(!event.getMinigame().getScoreType().equals("egghunt"))
			return;
		
		DisplayManager manager = DisplayManager.getDisplayManager(event.getPlayer());
		manager.hideBossBar();
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	private void onMinigameLeave(EndMinigameEvent event)
	{
		if(!event.getMinigame().getScoreType().equals("egghunt"))
			return;
		
		for(MinigamePlayer player : event.getWinners())
		{
			DisplayManager manager = DisplayManager.getDisplayManager(player.getPlayer());
			manager.hideBossBar();
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
			event.getPlayer().getPlayer().sendMessage("You found " + event.getFlag() + "!. You have " + (total - found) + " more to go!");
			manager.displayBossBar("Easter Egg Hunt  " + found + "/" + total, progress);
		}
		else
		{
			event.getPlayer().getPlayer().sendMessage("You found " + event.getFlag() + "!. Thats it! Click the finish sign to win!");
			manager.displayBossBar("Head back to the finish sign", 1);
		}
		
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
		int lines = 0;
		
		for(String flag : player.getMinigame().getFlags())
		{
			if(player.getFlags().contains(flag))
				builder.append(ChatColor.STRIKETHROUGH);
			else
				builder.append(ChatColor.RESET);
			
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
		List<String> oldFlags = mOldFlags.get(minigame.getName());
		
		if(oldFlags != null && oldFlags.equals(minigame.getFlags()))
			return;
		oldFlags = new ArrayList<String>(minigame.getFlags());
		mOldFlags.put(minigame.getName(), oldFlags);
		
		PlayerLoadout loadout = minigame.getDefaultPlayerLoadout();
		loadout.clearLoadout();
		
		ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) item.getItemMeta();
		
		meta.setTitle("Easter Egg Hunt Eggs");
		meta.setAuthor("Addstar MC");
		
		StringBuilder builder = new StringBuilder();
		int lines = 0;
		
		for(String flag : minigame.getFlags())
		{
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
