package au.com.addstar.easteregghunt;

import java.util.HashMap;
import java.util.Random;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class DisplayManager
{
	private static final int EFFECT_DELAY = 5;
	
	private static WeakHashMap<Player, DisplayManager> mAllManagers = new WeakHashMap<Player, DisplayManager>();
	private static Random mRand = new Random();
	private static Plugin mPlugin;
	
	private Player mPlayer;
	
	private HashMap<Integer, Effect> mEffects;
	private int mNextEffectId;
	private BukkitTask mEffectTimer;
	
	private DisplayManager(Player player)
	{
		mPlayer = player;
		
		mEffects = new HashMap<Integer, DisplayManager.Effect>();
		mNextEffectId = 0;
	}

	public int addEffect(Particle type, Location location, float speed, int count, float spread, int emitCount)
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
	
	private void spawnParticles(Location location, Particle particle, float offX, float offY, float offZ, float speed, int count)
	{
		mPlayer.getWorld().spawnParticle(particle, location, count, offX, offY, offZ);
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
	
	public static void unload(Player player)
	{
		mAllManagers.remove(player);
	}
	
	public static void init(Plugin plugin)
	{
		mPlugin = plugin;
	}
	
	private static class Effect
	{
		public Particle type;
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
