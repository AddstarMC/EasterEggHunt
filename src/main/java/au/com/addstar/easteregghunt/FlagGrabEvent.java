package au.com.addstar.easteregghunt;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.pauldavdesign.mineauz.minigames.MinigamePlayer;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;

public class FlagGrabEvent extends Event
{
	private static HandlerList handlers = new HandlerList();
	
	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}
	
	private MinigamePlayer mPlayer;
	private String mFlag;
	private Minigame mMinigame;
	private Location mLocation;
	
	public FlagGrabEvent(MinigamePlayer player, String flag, Minigame minigame, Location location)
	{
		mPlayer = player;
		mFlag = flag;
		mMinigame = minigame;
		mLocation = location;
	}
	
	public MinigamePlayer getPlayer()
	{
		return mPlayer;
	}
	
	public String getFlag()
	{
		return mFlag;
	}
	
	public Minigame getMinigame()
	{
		return mMinigame;
	}
	
	public Location getLocation()
	{
		return mLocation;
	}
}
