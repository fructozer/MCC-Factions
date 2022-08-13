package dansplugins.factionsystem.worldwar;

import dansplugins.factionsystem.MedievalFactions;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class MTCConfig {

    private final MedievalFactions plugin;

    public MTCConfig(MedievalFactions pl){
        this.plugin = pl;
        reload();
    }

    public String war_start;
    public String war_stop;
    public String war_day;
    public int chunk_durability;
    public Lang lang = new Lang();

    static class Lang{
        public List<String> warningBeforeWarStart;
        public List<String> warningBeforeWarStop;
        public List<String> warStartMessage; //ok
        public List<String> warStopMessage; //ok
        public List<String> raidWarning;
        public List<String> transferNotice; //ok
        public String raidTitle; //ok
    }

    public void reload(){
        File file = new File(plugin.getDataFolder(),"minetrungco.yml");
        if (!file.exists()) plugin.saveResource("minetrungco.yml",false);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        war_start = config.getString("war_start");
        war_stop  = config.getString("war_stop");
        war_day   = config.getString("war_day");
        chunk_durability = config.getInt("chunk_durability");
        if (config.isList("lang.warningBeforeWarStart"))
             lang.warningBeforeWarStart = config.getStringList("lang.warningBeforeWarStart");
        else lang.warningBeforeWarStart = Collections.singletonList(config.getString("lang.warningBeforeWarStart"));
        if (config.isList("lang.warningBeforeWarStop"))
             lang.warningBeforeWarStop = config.getStringList("lang.warningBeforeWarStop");
        else lang.warningBeforeWarStop = Collections.singletonList(config.getString("lang.warningBeforeWarStop"));
        if (config.isList("lang.warStartMessage"))
             lang.warStartMessage = config.getStringList("lang.warStartMessage");
        else lang.warStartMessage = Collections.singletonList(config.getString("lang.warStartMessage"));
        if (config.isList("lang.warStopMessage"))
             lang.warStopMessage = config.getStringList("lang.warStopMessage");
        else lang.warStopMessage = Collections.singletonList(config.getString("lang.warStopMessage"));
        if (config.isList("lang.raidWarning"))
             lang.raidWarning = config.getStringList("lang.raidWarning");
        else lang.raidWarning = Collections.singletonList(config.getString("lang.raidWarning"));
        if (config.isList("lang.transferNotice"))
             lang.transferNotice = config.getStringList("lang.transferNotice");
        else lang.transferNotice = Collections.singletonList(config.getString("lang.transferNotice"));

        lang.raidTitle = config.getString("lang.raidTitle");
    }

    public String c(String s){
        return s==null?null:ChatColor.translateAlternateColorCodes('&',s);
    }
    public void s(Player p,List<String> s){
        s.forEach(str -> p.sendMessage(c(str)));
    }
}
