package com.sstock2005.playtime;

import io.papermc.lib.PaperLib;
import net.kyori.adventure.text.Component;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public class PlaytimePlugin extends JavaPlugin implements Listener 
{
    private static final int MAX_PLAYTIME = 18000;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final String TARGET_USERNAME = "FusionArmy456";
    private int playerPlaytime;
    private UUID targetPlayerUUID;

    @Override
    public void onEnable() {
        PaperLib.suggestPaper(this);
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        playerPlaytime = 0;
        targetPlayerUUID = null;
        startPlaytimeChecker();
    }

    private void startPlaytimeChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (targetPlayerUUID != null) {
                    Player player = getServer().getPlayer(targetPlayerUUID);
                    if (player != null && player.isOnline()) {
                        playerPlaytime++; // Increment playtime by 1 second

                        if (playerPlaytime > MAX_PLAYTIME) {
                            player.kick(Component.text("You have played for more than 5 hours. Please take a break."));
                            String currentDate = LocalDate.now().format(DATE_FORMATTER);
                            getConfig().set("lastKickDate", currentDate);
                            saveConfig();
                        }
                    }
                }
            }
        }.runTaskTimer(this, 20L, 20L); // Run every second (20 ticks)
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getName().equals(TARGET_USERNAME)) {
            targetPlayerUUID = player.getUniqueId();

            // Initialize player data if not exists
            if (!getConfig().isSet("playtime")) {
                getConfig().set("playtime", 0);
                getConfig().set("lastKickDate", "");
                saveConfig();
            }

            String lastKickDate = getConfig().getString("lastKickDate", "");
            String currentDate = LocalDate.now().format(DATE_FORMATTER);

            // Reset playtime if it's a new day
            if (!Objects.equals(lastKickDate, currentDate)) {
                getConfig().set("playtime", 0);
                getConfig().set("lastKickDate", "");
                saveConfig();
            }

            playerPlaytime = getConfig().getInt("playtime", 0);

            if (playerPlaytime > MAX_PLAYTIME) {
                player.kick(Component.text("You have played for more than 5 hours. Please take a break."));
                getConfig().set("lastKickDate", currentDate);
                saveConfig();
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.getUniqueId().equals(targetPlayerUUID)) {
            getConfig().set("playtime", playerPlaytime);
            saveConfig();
            targetPlayerUUID = null;
        }
    }
}