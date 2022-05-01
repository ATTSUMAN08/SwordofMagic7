package swordofmagic7;

/*
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.support.forwarding.ForwardedVoteListener;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.MultiThread.MultiThread;

import java.io.File;
import java.io.IOException;

import static swordofmagic7.SomCore.isDevEventServer;

public class Som7Vote implements ForwardedVoteListener {

    private static final File file = new File(DataBase.DataBasePath, "OfflineVote.yml");

    @Override
    public void onForward(Vote vote) {
        MultiThread.TaskRun(() -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(vote.getUsername());
            if (player.isOnline()) {
                voteReward((Player) player, 1);
                Function.BroadCast("§e" + vote.getUsername() + "§aさんが§d投票§aしました §7(" + vote.getServiceName() + ")");
            } else if (isDevEventServer()) {
                try {
                    FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                    String uuid = player.getUniqueId().toString();
                    data.set(uuid, data.getInt(uuid, 0)+1);
                    data.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, "Vote");
    }

    public static void voteReward(Player player, int x) {
        if (player.isOnline() && x > 0) {
            PlayerData playerData = PlayerData.playerData(player);
            playerData.ItemInventory.addItemParameter(DataBase.getItemParameter("投票報酬箱"), 10 * x);
        }
    }

    public static void voteCheck(Player player) {
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        String uuid = player.getUniqueId().toString();
        voteReward(player, data.getInt(uuid, 0));
    }
}
*/