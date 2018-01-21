package com.greatmancode.legendarybot.plugins.wowlink;

import net.dv8tion.jda.core.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SyncRankScheduler {

    /**
     * The scheduler for the checks.
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * The Logger
     */
    private final Logger log = LoggerFactory.getLogger(getClass());

    public SyncRankScheduler(WoWLinkPlugin plugin, Guild guild) {

        final Runnable runnable = () -> guild.getMembers().forEach((member -> {
            log.info("Starting update of rank for guild " + guild.getName() + " ID: " + guild.getId());
            try {
                String character = plugin.getMainCharacterForUserInGuild(member.getUser(), guild);
                if (character != null) {
                    String rank = plugin.getWoWRank(guild,character);
                    if (rank != null) {
                        plugin.setDiscordRank(member.getUser(), guild, rank);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            log.info("Update done of rank for guild " + guild.getName() + " ID: " + guild.getId());
        }));

        scheduler.scheduleAtFixedRate(runnable,0,30, TimeUnit.MINUTES);
    }

    public void stop() {
        scheduler.shutdownNow();
    }


}
