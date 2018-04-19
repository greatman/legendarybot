package com.greatmancode.legendarybot.plugins.wowlink;

import net.dv8tion.jda.core.entities.Guild;
import org.json.JSONObject;
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

        final Runnable runnable = () -> {
            log.info("Doing Rank Update for Guild " + guild.getName() + ":" + guild.getId());
            plugin.doGuildRankUpdate(null, guild);
            log.info("Done doing rank update for Guild " + guild.getName() + ":" + guild.getId());
        };
        scheduler.scheduleAtFixedRate(runnable,0,30, TimeUnit.MINUTES);
    }

    public void stop() {
        scheduler.shutdownNow();
    }


}
