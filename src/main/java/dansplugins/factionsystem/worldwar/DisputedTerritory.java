package dansplugins.factionsystem.worldwar;


import dansplugins.factionsystem.MedievalFactions;
import dansplugins.factionsystem.data.PersistentData;
import dansplugins.factionsystem.externalapi.MF_Faction;
import dansplugins.factionsystem.objects.domain.ClaimedChunk;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.*;

public class DisputedTerritory {
    private final Chunk chunk;
    private static final WorldUtility util = WorldUtility.inst;
    private final MedievalFactions medievalFactions;
    private final PersistentData persistentData;
    private final BossBar bossBar;
    private final HashMap<String, HashSet<Player>> member = new HashMap<>();
    private final int maxHealth = 100;
    private int currentHealth = 100;
    private final int id;
    private String holder;

    public DisputedTerritory(MedievalFactions medievalFactions, PersistentData accessor, Chunk chunk) {
        this.medievalFactions = medievalFactions;
        this.persistentData = accessor;
        this.chunk = chunk;
        bossBar = createBossBar();
        bossBar.setVisible(true);
        Bukkit.getOnlinePlayers().parallelStream().filter(p -> p.getLocation().getChunk()==chunk).forEach(this::join);
        id = Bukkit.getScheduler().scheduleSyncRepeatingTask(medievalFactions, this::onTick,1,10);
    }

    private void updateHolder(){
        holder = util.getFaction(chunk);
    }

    private BossBar createBossBar(){
        String title = String.format("Chủ sở hữu: %s | Ưu thế: %d/%d",holder,currentHealth,maxHealth);
        double progress = (double) currentHealth/maxHealth;
        BarColor color;
        if      (progress>0.6) color = BarColor.GREEN;
        else if (progress>0.3) color = BarColor.YELLOW;
        else                   color = BarColor.RED;
        BossBar bar = Bukkit.createBossBar(title,color, BarStyle.SOLID);
        bar.setProgress(progress);
        return bar;
    }
    private void updateBar(){
        BossBar bar = createBossBar();
        bossBar.setTitle(bar.getTitle());
        bossBar.setColor(bar.getColor());
        bossBar.setProgress(bar.getProgress());
    }

    public void join(Player player){
        String fac = medievalFactions.getAPI().getFaction(player).getName();
        if (!member.containsKey(fac))
        member.put(fac,new HashSet<>());
        member.get(fac).add(player);
        bossBar.addPlayer(player);
    }
    public void leave(Player player){
        bossBar.removePlayer(player);
        String fac = medievalFactions.getAPI().getFaction(player).getName();
        if (!member.containsKey(fac)) return;
        member.get(fac).remove(player);
        if (member.get(fac).size()==0) member.remove(fac);
    }

    public void onTick(){
        member.values().parallelStream().forEach(s -> s.parallelStream().forEach(
                p -> currentHealth = Math.max(0, Math.min(maxHealth, currentHealth + (util.isSafe(p,chunk) ? 1 : -1)))
        ));
        if (currentHealth==0) changeHolder();
        updateBar();
        updateHolder();
    }

    public void stop(){
        bossBar.setVisible(false);
        Bukkit.getScheduler().cancelTask(id);
    }

    private void changeHolder(){
        HashMap<String, HashSet<Player>> temp = new HashMap<>(member);
        temp.remove(util.getFaction(chunk));
        temp.entrySet().stream()
                .max(Comparator.comparingInt(value -> value.getValue().size()))
                .ifPresent(entry -> {
                    String newHolder = entry.getKey();
                    ClaimedChunk thisChunk = persistentData.getChunkDataAccessor().getClaimedChunk(chunk);
                    thisChunk.setHolder(newHolder);
                    persistentData.getFaction(holder).getClaimedChunks().remove(thisChunk);
                    persistentData.getFaction(newHolder).getClaimedChunks().add(thisChunk);
                    member.values().forEach(m -> m.forEach(pl -> pl.sendMessage("(NO_TRANSLATE) chunk holder change to "+ entry.getKey())));
                }
        );

    }

}
