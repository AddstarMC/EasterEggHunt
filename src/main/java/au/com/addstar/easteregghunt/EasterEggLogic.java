package au.com.addstar.easteregghunt;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import au.com.addstar.monolith.BossDisplay;
import au.com.addstar.monolith.MonoPlayer;
import au.com.addstar.monolith.ParticleEffect;
import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.PlayerLoadout;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.mechanics.GameMechanicBase;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.LoadoutModule;
import au.com.mineauz.minigames.minigame.modules.MinigameModule;

public class EasterEggLogic extends GameMechanicBase implements Listener
{
	private HashMap<String, List<String>> mOldFlags;
	private Plugin mPlugin;
	
	public EasterEggLogic(Plugin plugin)
	{
		mPlugin = plugin;
		
		mOldFlags = new HashMap<String, List<String>>();
	}

	@Override
	public void balanceTeam( List<MinigamePlayer> players, Minigame mgm )
	{
	}

	@Override
	public String getMechanic()
	{
		return "egghunt";
	}
	
	@Override
	public boolean checkCanStart( Minigame minigame, MinigamePlayer player )
	{
		return true;
	}
	
	@Override
	public MinigameModule displaySettings( Minigame minigame )
	{
		return null;
	}
	@Override
	public void endMinigame( Minigame minigame, List<MinigamePlayer> winners, List<MinigamePlayer> losers )
	{
		for(MinigamePlayer player : winners)
		{
			DisplayManager manager = DisplayManager.getDisplayManager(player.getPlayer());
			manager.clearEffects();
			DisplayManager.unload(player.getPlayer());
			
			final MonoPlayer mplayer = MonoPlayer.getPlayer(player.getPlayer());
			if (mplayer.getBossBarDisplay() != null)
			{
				mplayer.getBossBarDisplay().setPercent(1);
				mplayer.getBossBarDisplay().setText(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &f&lCongratulations &2\u2756"));
			}
			
			Bukkit.getScheduler().runTaskLater(mPlugin, new Runnable()
			{
				@Override
				public void run()
				{
					mplayer.setBossBarDisplay(null);
				}
			}, 40);
		}
	}
	
	private void updateScoreboard(MinigamePlayer mplayer)
	{
		int found = mplayer.getFlags().size();
		int total = mplayer.getMinigame().getFlags().size();
		
		Player player = mplayer.getPlayer();
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		player.getPlayer().setScoreboard(scoreboard);
		scoreboard.clearSlot(DisplaySlot.SIDEBAR);
		
		Objective objective = scoreboard.registerNewObjective("main", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&lChristmas Hunt"));
		
		objective.getScore(ChatColor.translateAlternateColorCodes('&', "You have found:")).setScore(2);
		objective.getScore(ChatColor.translateAlternateColorCodes('&', String.format("%d/%d", found, total))).setScore(1);
	}
	
	@Override
	public void joinMinigame( Minigame minigame, MinigamePlayer player )
	{
		updateScoreboard(player);
		
		MonoPlayer mplayer = MonoPlayer.getPlayer(player.getPlayer());
		mplayer.setBossBarDisplay(new BossDisplay(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &a&lChristmas Hunt &2\u2756"), 0));
		
		updateLoadout(minigame);
	}
	
	@Override
	public void quitMinigame( Minigame minigame, MinigamePlayer player, boolean forced )
	{
		player.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		DisplayManager manager = DisplayManager.getDisplayManager(player.getPlayer());
		manager.clearEffects();
		DisplayManager.unload(player.getPlayer());
		
		final MonoPlayer mplayer = MonoPlayer.getPlayer(player.getPlayer());
		if (!forced && mplayer.getBossBarDisplay() != null)
		{
			mplayer.getBossBarDisplay().setPercent(1);
			mplayer.getBossBarDisplay().setText(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &f&lBad Luck &2\u2756"));
		}
		
		Bukkit.getScheduler().runTaskLater(mPlugin, new Runnable()
		{
			@Override
			public void run()
			{
				mplayer.setBossBarDisplay(null);
			}
		}, 40);
	}
	
	@Override
	public void startMinigame( Minigame minigame, MinigamePlayer player )
	{
	}
	
	@Override
	public void stopMinigame( Minigame minigame, MinigamePlayer player )
	{
	}
	
	@Override
	public EnumSet<MinigameType> validTypes()
	{
		return EnumSet.of(MinigameType.SINGLEPLAYER);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	private void onFlagGrab(FlagGrabEvent event)
	{
		if(event.getMinigame().getMechanic() != this)
			return;
		
		DisplayManager manager = DisplayManager.getDisplayManager(event.getPlayer().getPlayer());
		MonoPlayer mplayer = MonoPlayer.getPlayer(event.getPlayer().getPlayer());
		
		int found = event.getPlayer().getFlags().size();
		int total = event.getMinigame().getFlags().size();
		float progress = found / (float)total;
		if(progress > 1)
			progress = 1;
		
		if(found < total)
		{
			event.getPlayer().getPlayer().sendMessage(ChatColor.DARK_GREEN + "[\u2756] " + ChatColor.AQUA + "You found " + ChatColor.GOLD + ChatColor.BOLD + event.getFlag() + ChatColor.AQUA + "! " + ChatColor.GOLD + ChatColor.BOLD.toString() + (total - found) + ChatColor.AQUA + " more to go!");
			if (mplayer.getBossBarDisplay() != null)
			{
				mplayer.getBossBarDisplay().setPercent(progress);
				mplayer.getBossBarDisplay().setText(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &a&lChristmas Hunt " + found + "/" + total + " &2\u2756"));
			}
		}
		else
		{
			event.getPlayer().getPlayer().sendMessage(ChatColor.DARK_GREEN + "[\u2756] " + ChatColor.AQUA + "You found " + ChatColor.GOLD + ChatColor.BOLD + event.getFlag() + ChatColor.AQUA + "! Thats it! " + ChatColor.YELLOW + "Click the finish sign to win!");
			if (mplayer.getBossBarDisplay() != null)
			{
				mplayer.getBossBarDisplay().setPercent(1);
				mplayer.getBossBarDisplay().setText(ChatColor.translateAlternateColorCodes('&', "&2\u2714 &f&lHead back to the finish sign &2\u2714"));
			}
		}
		
		updateScoreboard(event.getPlayer());
		
		manager.addEffect(ParticleEffect.MAGIC_WITCH, event.getLocation().add(0, 0.3, 0), 0, 4, 0.3f, 1);
		
		updateBook(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	private void onRespawn(PlayerRespawnEvent event)
	{
		MinigamePlayer player = Minigames.plugin.pdata.getMinigamePlayer(event.getPlayer());
		if(player == null || !player.isInMinigame())
			return;
		
		Minigame game = player.getMinigame();
		
		if(game.getMechanic() != this)
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
			
			if(!meta.hasTitle() || !meta.getTitle().equals(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &f&lPresents to find &2\u2756")))
				continue;
			
			item = i;
			break;
		}
		
		if(item == null)
			return;
		
		meta.setPages(new ArrayList<String>());
		
		StringBuilder builder = new StringBuilder();
		builder.append(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &0Christmas Hunt &2\u2756\n"));
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
		
		meta.setTitle(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &f&lPresents to find &2\u2756"));
		meta.setAuthor("Christmas Hunt");
		
		StringBuilder builder = new StringBuilder();
		builder.append(ChatColor.translateAlternateColorCodes('&', "&2\u2756 &0Christmas Hunt &2\u2756\n"));
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
		
		minigame.saveMinigame();
	}
}
