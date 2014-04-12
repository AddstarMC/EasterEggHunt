package au.com.addstar.easteregghunt;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.pauldavdesign.mineauz.minigames.MinigamePlayer;
import com.pauldavdesign.mineauz.minigames.events.EndMinigameEvent;
import com.pauldavdesign.mineauz.minigames.events.JoinMinigameEvent;
import com.pauldavdesign.mineauz.minigames.events.QuitMinigameEvent;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;
import com.pauldavdesign.mineauz.minigames.scoring.ScoreTypeBase;

public class EasterEggLogic extends ScoreTypeBase implements Listener
{
	public EasterEggLogic(Plugin plugin)
	{
		Bukkit.getPluginManager().registerEvents(this, plugin);
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
	private void onMinigameJoin(JoinMinigameEvent event)
	{
		if(!event.getMinigame().getScoreType().equals("egghunt"))
			return;
		
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
	}
			
}
