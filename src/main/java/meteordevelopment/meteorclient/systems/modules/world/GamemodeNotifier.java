package meteordevelopment.meteorclient.systems.modules.world;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.world.GameMode;

import java.util.List;

public class GamemodeNotifier extends Module {

    public GamemodeNotifier() {
        super(Categories.World, "gamemode-notifier", "Notifies user when a player's gamemode is changed.");
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerListS2CPacket packet) {
            for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
                if (!packet.getActions().contains(PlayerListS2CPacket.Action.UPDATE_GAME_MODE)) continue;
                PlayerListEntry entry1 = mc.getNetworkHandler().getPlayerListEntry(entry.profileId());
                if (entry1 == null) continue;
                GameMode gameMode = entry.gameMode();
                if (entry1.getGameMode() != gameMode) {
                    info("Player %s changed gamemode to %s", entry1.getProfile().getName(), entry.gameMode());
                }
            }
        }
    }
}

