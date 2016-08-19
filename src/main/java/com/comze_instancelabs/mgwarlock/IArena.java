package com.comze_instancelabs.mgwarlock;

import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaConfigStrings;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;

public class IArena extends Arena {

	private BukkitTask timer;
	private BukkitTask starttimer;
	Main m = null;
	public int c = 30;
	BukkitTask tt;
	int currentingamecount;

	private int block_id = 174;

	public IArena(Main m, String arena) {
		super(m, arena);
		this.m = m;
	}

	@Override
	public void init(Location signloc, ArrayList<Location> spawns, Location mainlobby, Location waitinglobby, int max_players, int min_players, boolean viparena) {
		super.init(signloc, spawns, mainlobby, waitinglobby, max_players, min_players, viparena);
		initBlockID();
	}

	public void initBlockID(){
		try {
			if (!this.m.pli.getArenasConfig().getConfig().isSet(ArenaConfigStrings.ARENAS_PREFIX + this.getName() + ".outer_circle_block_id")) {
				this.m.pli.getArenasConfig().getConfig().set(ArenaConfigStrings.ARENAS_PREFIX + this.getName() + ".outer_circle_block_id", 174);
				this.m.pli.getArenasConfig().saveConfig();
			} else {
				block_id = this.m.pli.getArenasConfig().getConfig().getInt(ArenaConfigStrings.ARENAS_PREFIX + this.getName() + ".outer_circle_block_id");
			}
		} catch (Exception e) {
			getPlugin().getLogger().log(Level.WARNING, "Failed initializing outer_circle_block_id config entry.", e);
		}
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
	public void spectate(String playername) {
		super.spectate(playername);
		// give killer a reward:
		if (m.lastdamager.containsKey(playername)) {
			Player killer = Bukkit.getPlayer(m.lastdamager.get(playername));
			m.pli.getRewardsInstance().giveReward(killer.getName());
			killer.sendMessage(MinigamesAPI.getAPI().pinstances.get(m).getMessagesConfig().you_got_a_kill.replaceAll("<player>", playername));
		}
	}

	@Override
	public void start(boolean tp) {
		c = Main.global_arenas_size;
		this.generateArena(this.getSpawns().get(0).clone().add(0D, -1D, 0D));
		super.start(tp);
	}

	@Override
	public void started() {
		initBlockID();
		for (String p_ : this.getAllPlayers()) {
			Player p = Bukkit.getPlayer(p_);
			p.sendMessage(ChatColor.RED + "The platform starts decreasing in 10 seconds!");
		}
		starttimer = Bukkit.getScheduler().runTaskLater(m, new Runnable() {
			public void run() {
				timer = Bukkit.getScheduler().runTaskTimer(m, new Runnable() {
					public void run() {
						c--;
						if (c > 0) {
							removeCircle(c, Material.getMaterial(block_id));
							Bukkit.getScheduler().runTaskLater(m, new Runnable() {
								public void run() {
									removeCircle(c, Material.AIR);
								}
							}, 16L); // 2L
						}
					}
				}, 0L, 20L); // 6L
			}
		}, 20L * 10);
	}

	public void removeCircle(int cr, Material mat) {
		int cradius_s = cr * cr;
		Location start = this.getSpawns().get(0).clone().add(0D, -1D, 0D);
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
		if (starttimer != null) {
			starttimer.cancel();
		}
		Bukkit.getScheduler().runTaskLater(m, new Runnable() {
			public void run() {
				c = Main.global_arenas_size;
				a.generateArena(a.getSpawns().get(0).clone().add(0D, -1D, 0D));
			}
		}, 10L);
	}

}
