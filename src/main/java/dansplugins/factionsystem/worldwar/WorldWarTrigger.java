package dansplugins.factionsystem.worldwar;

import dansplugins.factionsystem.MedievalFactions;
import dansplugins.factionsystem.data.PersistentData;
import dansplugins.factionsystem.eventhandlers.MoveHandler;
import dansplugins.factionsystem.events.TerritoryEnterEvent;
import dansplugins.factionsystem.events.TerritoryLeaveEvent;
import dansplugins.factionsystem.externalapi.MF_Faction;
import dansplugins.factionsystem.objects.domain.ClaimedChunk;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.RegisteredListener;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;

public class WorldWarTrigger implements Listener {
    private static WorldWarTrigger instance;
    private final MedievalFactions medievalFactions;
    private final HashMap<Chunk,DisputedTerritory> disputing = new HashMap<>();
    private final PersistentData persistentData;
    private int warStartHour;
    private int warStartMinute;
    private int warStopHour;
    private int warStopMinute;
    private DayOfWeek warDay;

    private Timer currentTimer = null;

    public static WorldWarTrigger inst(MedievalFactions medievalFactions, PersistentData accessor)
    {return instance==null?new WorldWarTrigger(medievalFactions,accessor):instance;}
    private WorldWarTrigger(MedievalFactions plugin, PersistentData accessor){
        new WorldUtility(plugin,accessor);
        this.medievalFactions = plugin;
        this.persistentData = accessor;
        loadConfig();
        if (checkTime()) onEnable(); else timerStart();
        instance = this;
    }
    private void loadConfig(){
        File cf = new File(medievalFactions.getDataFolder(),"minetrungco.yml");
        if (!cf.exists()) medievalFactions.saveResource("minetrungco.yml",false);
        YamlConfiguration ycf = YamlConfiguration.loadConfiguration(cf);
        warStartHour   = Integer.parseInt(Objects.requireNonNull(ycf.getString("war_start", "19:00")).split(":")[0]);
        warStartMinute = Integer.parseInt(Objects.requireNonNull(ycf.getString("war_start", "19:00")).split(":")[1]);
        warStopHour    = Integer.parseInt(Objects.requireNonNull(ycf.getString("war_stop" , "23:00")).split(":")[0]);
        warStopMinute  = Integer.parseInt(Objects.requireNonNull(ycf.getString("war_stop" , "23:00")).split(":")[1]);
        warDay         = DayOfWeek.valueOf(Objects.requireNonNull(ycf.getString("war_day", "sunday")).toUpperCase());

    }
    private boolean isWar = false;

    private void timerStart(){
        LocalDateTime now = LocalDateTime.now();
        int toSunday = warDay.getValue()-now.getDayOfWeek().getValue();
        if (toSunday<0) toSunday+=7;
        LocalDateTime sunday = checkHourStart(now)?now.plusDays(toSunday):now.plusDays(1+toSunday);
        int day     = sunday.getDayOfMonth();
        int month   = sunday.getMonth().getValue()-1;
        int year    = sunday.getYear();
        Calendar c = new GregorianCalendar();
        //noinspection MagicConstant
        c.set(year,month,day,warStartHour,warStartMinute,0);
        if (currentTimer!=null) try {
            currentTimer.cancel();
        }catch ( IllegalStateException ignored ){}
        currentTimer = new Timer();
        currentTimer.schedule(new TimerTask() {
            @Override
            public void run() {onEnable();}
        }, c.getTimeInMillis()-System.currentTimeMillis());

    }

    private boolean checkHourStart(LocalDateTime now){
        if (now.getHour()<warStartHour) return true;
        if (now.getHour()>warStartHour) return false;
        return now.getMinute()<warStartMinute;
    }

    private void timerStop(){
        LocalDateTime now = LocalDateTime.now();
        int toSunday = warDay.getValue()-now.getDayOfWeek().getValue();
        LocalDateTime sunday = checkHourStop(now)?now.plusDays(toSunday):now.plusDays(1+toSunday);
        int day     = sunday.getDayOfMonth();
        int month   = sunday.getMonth().getValue()-1;
        int year    = sunday.getYear();
        Calendar c = new GregorianCalendar();
        //noinspection MagicConstant
        c.set(year,month,day,warStopHour,warStopMinute,0);
        if (currentTimer!=null) try {
            currentTimer.cancel();
        }catch ( IllegalStateException ignored ){}
        currentTimer = new Timer();
        currentTimer.schedule(new TimerTask() {
            @Override
            public void run() {onDisable();}
        }, c.getTimeInMillis()-System.currentTimeMillis());
    }

    private boolean checkHourStop(LocalDateTime now){
        if (now.getHour()<warStopHour) return true;
        if (now.getHour()>warStopHour) return false;
        return now.getMinute()<warStopMinute;
    }

    private boolean checkTime(){
        LocalDateTime now = LocalDateTime.now();
        if (now.getDayOfWeek()!=warDay) return false;
        return  !checkHourStart(now) && checkHourStop(now);
    }

    public void onEnable(){
        if (isWar) return;
        isWar = true;
        startAllDisputed();
        timerStop();
        Bukkit.getOnlinePlayers().parallelStream().forEach(p -> p.sendMessage("§aWorld War has been started"));
    }

    public void onDisable(){
        if (!isWar) return;
        isWar = false;
        StopAllDisputed();
        timerStart();
        Bukkit.getOnlinePlayers().parallelStream().forEach(p -> p.sendMessage("§aWorld War has been stoped"));
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e){
        PlayerMoveEvent me = new PlayerMoveEvent(e.getPlayer(),e.getFrom(),e.getTo());
        Optional<RegisteredListener> handler = HandlerList.getRegisteredListeners(medievalFactions).stream()
                .filter(d -> d.getListener() instanceof MoveHandler)
                .findFirst();
        if (handler.isPresent()) {
            MoveHandler mh = (MoveHandler) handler.get().getListener();
            mh.handle(me);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        if (!isWar) return;
        if (disputing.containsKey(e.getEntity().getLocation().getChunk()))
            disputing.get(e.getEntity().getLocation().getChunk()).leave(e.getEntity().getPlayer());
    }

    @EventHandler
    public void onChunkEnter(TerritoryEnterEvent e){
        if (!isWar) return;
        if (!disputing.containsKey(e.getChunk())) {//if not disputed -> create
            if (persistentData.getChunkDataAccessor().getClaimedChunk(e.getChunk())!=null)
            startDisputedAt(e.getChunk());
        }
        else// if disputed -> join
            disputing.get(e.getChunk()).join(e.getPlayer());
    }

    @EventHandler
    public void onChunkLeave(TerritoryLeaveEvent e){
        if (!isWar) return;
        if (disputing.containsKey(e.getChunk()))
            disputing.get(e.getChunk()).leave(e.getPlayer());
    }

    public void startDisputedAt(Chunk chunk){
        disputing.put(chunk,new DisputedTerritory(medievalFactions, persistentData,chunk));
    }

    public void startAllDisputed(){
        if (!isWar) return;
        Bukkit.getOnlinePlayers().parallelStream().forEach(p -> {
            Chunk chunk = p.getLocation().getChunk();
            if (!WorldUtility.inst.isSafe(p,chunk)) startDisputedAt(chunk);
        });
    }

    public void StopAllDisputed(){
        disputing.values().forEach(DisputedTerritory::stop);
        disputing.clear();
    }

}
