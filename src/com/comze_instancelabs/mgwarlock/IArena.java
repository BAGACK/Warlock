package com.comze_instancelabs.mgwarlock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaState;
import com.comze_instancelabs.minigamesapi.Classes;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.util.Util;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class IArena extends Arena {

	private BukkitTask timer;
	Main m = null;
	public int c = 30;
	BukkitTask tt;
	int currentingamecount;

	public IArena(Main m, String arena) {
		super(m, arena);
		this.m = m;
	}

	public void setRadius(int i) {
		this.c = i;
	}

	public void generateArena(Location start) {
		int x = start.getBlockX();
		int y = start.getBlockY();
		int z = start.getBlockZ();

		int c_2 = c * c;

		for (int x_ = -c; x_ <= c; x_++) {
			for (int z_ = -c; z_ <= c; z_++) {
				if ((x_ * x_) + (z_ * z_) <= c_2) {
					Block b = start.getWorld().getBlockAt(new Location(start.getWorld(), x - x_, y, z - z_));
					b.setType(Material.ICE);
				}
			}
		}

		// TODO generate lava floor
	}

	@Override
	public void start() {
		try {
			Bukkit.getScheduler().cancelTask(this.getTaskId());
		} catch (Exception e) {
		}
		currentingamecount = MinigamesAPI.getAPI().pinstances.get(m).getIngameCountdown();
		for (String p_ : this.getArena().getAllPlayers()) {
			Player p = Bukkit.getPlayer(p_);
			p.setWalkSpeed(0.0F);
		}
		Util.teleportAllPlayers(this.getArena().getAllPlayers(), this.getArena().getSpawns());
		final Arena a = this;
		MinigamesAPI.getAPI().pinstances.get(m).scoreboardManager.updateScoreboard(m, a);
		this.setTaskId(Bukkit.getScheduler().runTaskTimer(MinigamesAPI.getAPI(), new Runnable() {
			public void run() {
				currentingamecount--;
				if (currentingamecount == 60 || currentingamecount == 30 || currentingamecount == 15 || currentingamecount == 10 || currentingamecount < 6) {
					for (String p_ : a.getAllPlayers()) {
						if (Validator.isPlayerOnline(p_)) {
							Player p = Bukkit.getPlayer(p_);
							p.sendMessage(MinigamesAPI.getAPI().pinstances.get(m).getMessagesConfig().starting_in.replaceAll("<count>", Integer.toString(currentingamecount)));
						}
					}
				}
				if (currentingamecount < 1) {
					a.getArena().setArenaState(ArenaState.INGAME);
					for (String p_ : a.getAllPlayers()) {
						if (!Classes.hasClass(m, p_)) {
							Classes.setClass(m, "default", p_);
						}
						Classes.getClass(m, p_);
						Player p = Bukkit.getPlayer(p_);
						p.setWalkSpeed(0.2F);
					}

					timer = Bukkit.getScheduler().runTaskTimer(m, new Runnable() {
						public void run() {
							c--;
							if (c > 0) {
								removeCircle(c, Material.PACKED_ICE);
								Bukkit.getScheduler().runTaskLater(m, new Runnable() {
									public void run() {
										removeCircle(c, Material.AIR);
									}
								}, 16L); // 2L
							}
						}
					}, 0L, 20L); // 6L

					try {
						Bukkit.getScheduler().cancelTask(a.getTaskId());
					} catch (Exception e) {
					}
				}
			}
		}, 5L, 20).getTaskId());
	}

	public void removeCircle(int cr, Material mat) {
		int cradius_s = cr * cr;
		Location start = this.getSpawns().get(0);
		int x = start.getBlockX();
		int y = start.getBlockY();
		int z = start.getBlockZ();
		for (int x_ = -cr; x_ <= cr; x_++) {
			for (int z_ = -cr; z_ <= cr; z_++) {
				int t = (x_ * x_) + (z_ * z_);
				if (t >= cradius_s && t <= (cradius_s + 90)) {
					Block b = start.getWorld().getBlockAt(new Location(start.getWorld(), x - x_, y, z - z_));
					b.setType(mat);
				}
			}
		}
	}

	@Override
	public void stop() {
		super.stop();
		final IArena a = this;
		if (timer != null) {
			timer.cancel();
		}
		Bukkit.getScheduler().runTaskLater(m, new Runnable() {
			public void run() {
				a.generateArena(a.getSpawns().get(0));
			}
		}, 10L);
	}

}
