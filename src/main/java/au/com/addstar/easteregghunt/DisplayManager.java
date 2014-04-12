package au.com.addstar.easteregghunt;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Random;
import java.util.WeakHashMap;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public class DisplayManager
{
	private static final int ENTITY_DRAGON_ID = 999999999;
	private static final int EFFECT_DELAY = 5;
	
	private static WeakHashMap<Player, DisplayManager> mAllManagers = new WeakHashMap<Player, DisplayManager>();
	private static ProtocolManager mLib;
	private static Random mRand = new Random();
	private static Plugin mPlugin;
	
	private Player mPlayer;
	private Location mLocation;
	
	private boolean mShowBossBar;
	private String mCurrentBossText;
	private float mCurrentBossValue;
	
	private HashMap<Integer, Effect> mEffects;
	private int mNextEffectId;
	private BukkitTask mEffectTimer;
	
	private DisplayManager(Player player)
	{
		mPlayer = player;
		mLocation = mPlayer.getLocation();
		mShowBossBar = false;
		
		mEffects = new HashMap<Integer, DisplayManager.Effect>();
		mNextEffectId = 0;
	}
	
	public void updateDisplays()
	{
		Location loc = mPlayer.getLocation();
		
		if(mShowBossBar)
		{
			if(mLocation.getWorld() != loc.getWorld())
			{
				int value = (int)Math.min(Math.max(mCurrentBossValue * 200, 1), 200);
				spawnFakeDragon(mCurrentBossText, value);
			}
			else
			{
				double dist = mLocation.distanceSquared(loc);
				
				if(dist >= 640000)
				{
					int value = (int)Math.min(Math.max(mCurrentBossValue * 200, 1), 200);
					spawnFakeDragon(mCurrentBossText, value);
					mLocation = loc;
				}
				else if(dist > 500)
				{
					positionDragon();
					mLocation = loc;
				}
			}
		}
	}
	
	public void displayBossBar(String text, float percent)
	{
		mCurrentBossText = text;
		mCurrentBossValue = percent;
		int value = (int)Math.min(Math.max(percent * 200, 1), 200);
		
		if(!mShowBossBar)
			spawnFakeDragon(text, value);
		else
			updateDragonStats(text, value);
		
		mShowBossBar = true;
	}
	
	public void displayBossBarTemp(String text, float percent, int ticks)
	{
		displayBossBar(text, percent);
		
		Bukkit.getScheduler().runTaskLater(mPlugin, new Runnable()
		{
			@Override
			public void run()
			{
				hideBossBar();
			}
		}, ticks);
	}
	
	public void updateBossBarProgress(float percent)
	{
		mCurrentBossValue = percent;
		int value = (int)Math.min(Math.max(percent * 200, 1), 200);
		
		if(!mShowBossBar)
			spawnFakeDragon(mCurrentBossText, value);
		else
			updateDragonStats(mCurrentBossText, value);
		
		mShowBossBar = true;
	}
	
	public void hideBossBar()
	{
		if(!mShowBossBar)
			return;
		
		mShowBossBar = false;
		removeDragon();
	}
	
	private void spawnFakeDragon(String name, int health)
	{
		Location loc = mPlayer.getLocation();
		
		PacketContainer spawnPacket = mLib.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
		spawnPacket.getIntegers().write(0, ENTITY_DRAGON_ID); // EntityId
		spawnPacket.getIntegers().write(1, 63); // EntityType
		spawnPacket.getIntegers().write(2, loc.getBlockX() * 32); // X
		spawnPacket.getIntegers().write(3, -500 * 32); // Y
		spawnPacket.getIntegers().write(4, loc.getBlockZ() * 32); // Z
		spawnPacket.getBytes().write(0, (byte)0); // Yaw
		spawnPacket.getBytes().write(1, (byte)0); // Pitch
		spawnPacket.getBytes().write(2, (byte)0); // ? head?
		
		spawnPacket.getIntegers().write(5, 0); // MotX
		spawnPacket.getIntegers().write(6, 0); // MotY
		spawnPacket.getIntegers().write(7, 0); // MotZ
		
		WrappedDataWatcher wrapper = new WrappedDataWatcher();
		
		if(name.length() > 64)
			name = name.substring(0,64);
		wrapper.setObject(0, (byte)0x20);
		wrapper.setObject(6, Float.valueOf(health));
		wrapper.setObject(10, name);
		wrapper.setObject(11, Byte.valueOf((byte)1));
		
		spawnPacket.getDataWatcherModifier().write(0, wrapper); // DataWatcher
		
		try
		{
			mLib.sendServerPacket(mPlayer, spawnPacket, false);
		}
		catch ( InvocationTargetException e )
		{
			e.printStackTrace();
		}
	}
	
	private void updateDragonStats(String name, int health)
	{
		PacketContainer update = mLib.createPacket(PacketType.Play.Server.ENTITY_METADATA);
		update.getIntegers().write(0, ENTITY_DRAGON_ID); // EntityId
		
		WrappedDataWatcher wrapper = new WrappedDataWatcher();
		
		if(name.length() > 64)
			name = name.substring(0,64);
		wrapper.setObject(0, (byte)0x20);
		wrapper.setObject(6, Float.valueOf(health));
		wrapper.setObject(10, name);
		wrapper.setObject(11, Byte.valueOf((byte)1));
		
		update.getWatchableCollectionModifier().write(0, wrapper.getWatchableObjects()); // DataWatcher
		
		try
		{
			mLib.sendServerPacket(mPlayer, update, false);
		}
		catch ( InvocationTargetException e )
		{
			e.printStackTrace();
		}
	}
	
	private void positionDragon()
	{
		Location loc = mPlayer.getLocation();
		
		PacketContainer movePacket = mLib.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
		movePacket.getIntegers().write(0, ENTITY_DRAGON_ID); // EntityId
		movePacket.getIntegers().write(1, loc.getBlockX() * 32); // X
		movePacket.getIntegers().write(2, -500 * 32); // Y
		movePacket.getIntegers().write(3, loc.getBlockZ() * 32); // Z
		movePacket.getBytes().write(0, (byte)0); // Yaw
		movePacket.getBytes().write(1, (byte)0); // Pitch
		

		try
		{
			mLib.sendServerPacket(mPlayer, movePacket, false);
		}
		catch ( InvocationTargetException e )
		{
			e.printStackTrace();
		}
	}
	
	private void removeDragon()
	{
		PacketContainer deletePacket = mLib.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
		deletePacket.getIntegerArrays().write(0, new int[] {ENTITY_DRAGON_ID});
		
		try
		{
			mLib.sendServerPacket(mPlayer, deletePacket, false);
		}
		catch ( InvocationTargetException e )
		{
			e.printStackTrace();
		}
	}
	
	public int addEffect(String type, Location location, float speed, int count, float spread, int emitCount)
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
		
		return effect.id;
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
	
	public void clearEffects()
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
		packet.getStrings().write(0, effect);
		packet.getFloat().write(0, (float)location.getX());
		packet.getFloat().write(1, (float)location.getY());
		packet.getFloat().write(2, (float)location.getZ());
		
		packet.getFloat().write(3, offX);
		packet.getFloat().write(4, offY);
		packet.getFloat().write(5, offZ);
		
		packet.getFloat().write(6, speed);
		packet.getIntegers().write(0, count);
		
		try
		{
			mLib.sendServerPacket(mPlayer, packet, false);
		}
		catch ( InvocationTargetException e )
		{
			e.printStackTrace();
		}
	}
	
	public static void initialize(Plugin plugin)
	{
		Validate.isTrue(mLib == null);
		
		mLib = ProtocolLibrary.getProtocolManager();
		
		Bukkit.getPluginManager().registerEvents(new DisplayListener(), plugin);
		mPlugin = plugin;
	}
	
	public static DisplayManager getDisplayManager(Player player)
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
	}
	
	private static class Effect
	{
		public String type;
		public Location location;
		public int id;
		public int count;
		public float spread;
		public int emits;
		public float speed;
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
