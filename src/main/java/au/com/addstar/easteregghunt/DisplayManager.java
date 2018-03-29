package au.com.addstar.easteregghunt;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

public class DisplayManager
{
	private static final int EFFECT_DELAY = 5;
	private static WeakHashMap<Player, DisplayManager> mAllManagers = new WeakHashMap<>();
	private static ProtocolManager mLib;
	private static Random mRand = new Random();
	private static Plugin mPlugin;
	
	private Player mPlayer;

	private boolean mShowBossBar;
	private String mCurrentBossText;
	private float mCurrentBossValue;
	private BossBar bar;
	
	private HashMap<Integer, Effect> mEffects;
	private int mNextEffectId;
	private BukkitTask mEffectTimer;
	
	private DisplayManager(Player player)
	{
		mPlayer = player;
		mShowBossBar = false;
		bar = Bukkit.createBossBar(null, BarColor.GREEN, BarStyle.SOLID);
		bar.setVisible(false);
		bar.addPlayer(mPlayer);
		mEffects = new HashMap<>();
		mNextEffectId = 0;
	}
	
	private void updateDisplays()
	{
		bar.setProgress(mCurrentBossValue);
		bar.setTitle(mCurrentBossText);
		if(mShowBossBar){
			bar.setProgress(mCurrentBossValue);
			bar.setTitle(mCurrentBossText);
			bar.setVisible(true);
		}
	}
	
	void displayBossBar(String text, float percent)
	{
		mCurrentBossText = text;
		mCurrentBossValue = percent;
		bar.setTitle(text);
		bar.setProgress(percent);
		bar.setVisible(true);
		mShowBossBar = true;
	}
	
	void displayBossBarTemp(String text, float percent, int ticks)
	{
		displayBossBar(text, percent);
		
		Bukkit.getScheduler().runTaskLater(mPlugin, () -> hideBossBar(), ticks);
	}
	
	public void updateBossBarProgress(float percent)
	{
		mCurrentBossValue = percent;
		bar.setProgress(percent);
		bar.setTitle(mCurrentBossText);
		mShowBossBar = true;
	}
	
	void hideBossBar()
	{
		if(!mShowBossBar)
			return;
		
		mShowBossBar = false;
		bar.setVisible(false);
	}

	
	 void addEffect(String type, Location location, float speed, int count, float spread, int emitCount)
	{
		Effect effect = new Effect();
		effect.id = mNextEffectId++;
		effect.type = type;
		effect.location = location;
		effect.speed = speed;
		effect.count = count;
		effect.spread = spread;
		effect.emits = emitCount;
		
		mEffects.put(effect.id, effect);
		
		if(mEffectTimer == null)
			mEffectTimer = Bukkit.getScheduler().runTaskTimer(mPlugin, new EffectTimer(), EFFECT_DELAY, EFFECT_DELAY);
		
	}
	
	public void removeEffect(int id)
	{
		mEffects.remove(id);
		if(mEffects.isEmpty() && mEffectTimer != null)
		{
			mEffectTimer.cancel();
			mEffectTimer = null;
		}
	}
	
	 void clearEffects()
	{
		mEffects.clear();
		
		if(mEffectTimer != null)
		{
			mEffectTimer.cancel();
			mEffectTimer = null;
		}
	}
	
	private void doEffects()
	{
		for(Effect effect : mEffects.values())
		{
			if(!mPlayer.getWorld().equals(effect.location.getWorld()))
				continue;
			
			if(mPlayer.getLocation().distance(effect.location) > 16)
				continue;
			
			for(int i = 0; i < effect.emits; ++i)
			{
				float offX = (mRand.nextFloat() - 0.5f) * 2 * effect.spread;
				float offY = (mRand.nextFloat() - 0.5f) * 2 * effect.spread;
				float offZ = (mRand.nextFloat() - 0.5f) * 2 * effect.spread;
				
				spawnParticles(effect.location, effect.type, offX, offY, offZ, effect.speed, effect.count);
			}
		}
	}
	
	private void spawnParticles(Location location, String effect, float offX, float offY, float offZ, float speed, int count)
	{

		PacketContainer packet = mLib.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
        EnumWrappers.Particle particle = EnumWrappers.Particle.getByName(effect);
        if(Minigames.plugin.isDebugging())MinigameUtils.debugMessage(packet.toString());
        packet.getParticles().write(0,particle);
		packet.getIntegers().write(0,count);
		packet.getFloat()
                .write(0, (float)location.getX())
                .write(1, (float)location.getY())
                .write(2, (float)location.getZ())
		        .write(3, offX)
                .write(4, offY)
                .write(5, offZ)
                .write(6, speed);
		try
		{
			mLib.sendServerPacket(mPlayer, packet, false);
		}
		catch ( InvocationTargetException e )
		{
			e.printStackTrace();
		}
	}
	
	 static void initialize(Plugin plugin)
	{
		Validate.isTrue(mLib == null);
		
		mLib = ProtocolLibrary.getProtocolManager();
		
		Bukkit.getPluginManager().registerEvents(new DisplayListener(), plugin);
		mPlugin = plugin;
	}
	
	 static DisplayManager getDisplayManager(Player player)
	{
		DisplayManager manager = mAllManagers.get(player);
		if(manager == null)
		{
			manager = new DisplayManager(player);
			mAllManagers.put(player, manager);
		}
		
		return manager;
	}
	
	private static class DisplayListener implements Listener
	{
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		private void onPlayerMove(PlayerMoveEvent event)
		{
			DisplayManager manager = mAllManagers.get(event.getPlayer());
			if(manager != null)
				manager.updateDisplays();
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		private void onPlayerRespawn(PlayerRespawnEvent event)
		{
			final DisplayManager manager = mAllManagers.get(event.getPlayer());
			if(manager != null)
			{
				if(manager.mShowBossBar)
				{
					manager.bar.setVisible(true);
				}
			}
		}
	}
	
	private static class Effect
	{
		 String type;
		Location location;
		 int id;
		 int count;
		 float spread;
		 int emits;
		 float speed;
	}
	
	private class EffectTimer implements Runnable
	{
		@Override
		public void run()
		{
			doEffects();
		}
	}
}
