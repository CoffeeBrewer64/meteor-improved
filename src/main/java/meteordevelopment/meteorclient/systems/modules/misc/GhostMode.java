package meteordevelopment.meteorclient.systems.modules.misc;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.*;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.world.GameMode;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.friends.Friends;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import meteordevelopment.meteorclient.pathing.BaritonePathManager;
import baritone.api.BaritoneAPI;

public class GhostMode extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // private final Setting<Boolean> screenMode = sgGeneral.add(new BoolSetting.Builder()
    //     .name("screen-mode")
    //     .description("Disables HUD and other on-screen things for you to take a 'clean' screenshot")
    //     .defaultValue(true)
    //     .build()
    // );

    // private final Setting<Boolean> spoofClient = sgGeneral.add(new BoolSetting.Builder()
    //     .name("spoof-client")
    //     .description("Spoofs the client using the ServerSpoof module")
    //     .defaultValue(true)
    //     .build()
    // );

    private final Setting<Boolean> toggleOnUse = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-on-trigger")
        .description("Toggles Ghost Mode when it's triggered. Prevents repeating actions, needs to be reenabled though.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> informUserViaInfo = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-logs")
        .description("Tells you in chat that Ghost Mode was triggered")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> trustFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("trust-friends")
        .description("Don't run triggers caused by friends")
        .defaultValue(true)
        .build()
    );

    private final SettingGroup sgActions = settings.createGroup("Actions");

    private final Setting<Boolean> homeOnCaught = sgActions.add(new BoolSetting.Builder()
        .name("home-on-caught")
        .description("Runs /home when triggered")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> panicOnCaught = sgActions.add(new BoolSetting.Builder()
        .name("panic-on-caught")
        .description("Disables all modules when triggered")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> cancelBaritoneOnCaught = sgActions.add(new BoolSetting.Builder()
        .name("stop-baritone-on-caught")
        .description("Stops Baritone when caught")
        .defaultValue(false)
        .build()
    );

    private final SettingGroup sgCauses = settings.createGroup("Triggers");

    private final Setting<Boolean> trigger_Spectator = sgCauses.add(new BoolSetting.Builder()
        .name("spectator")
        .description("Trigger when someone goes into spectator mode")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> trigger_Creative = sgCauses.add(new BoolSetting.Builder()
        .name("creative")
        .description("Trigger when someone goes into creative mode")
        .defaultValue(false)
        .build()
    );

    // private final Setting<Boolean> trigger_Vanish = sgCauses.add(new BoolSetting.Builder()
    //     .name("vanish")
    //     .description("Trigger when someone goes into vanish")
    //     .defaultValue(true)
    //     .build()
    // );

    private final Setting<Boolean> trigger_EntityInRange = sgCauses.add(new BoolSetting.Builder()
        .name("entity-in-range")
        .description("Trigger when a certain entitiy is in a certain range")
        .defaultValue(true)
        .build()
    );

    private final Setting<Set<EntityType<?>>> trigger_EntityInRange_types = sgCauses.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Entity types to react on.")
        .onlyAttackable()
        .defaultValue(EntityType.PLAYER)
        .visible(() -> trigger_EntityInRange.get())
        .build()
    );

    private final Setting<Double> trigger_EntityInRange_range = sgCauses.add(new DoubleSetting.Builder()
        .name("range")
        .description("Range in which to react.")
        .defaultValue(256)
        .min(1)
        .sliderRange(1, 256)
        .visible(() -> trigger_EntityInRange.get())
        .build()
    );

    public GhostMode() {
        super(Categories.Misc, "ghost-mode", "Tries to stop you from being caught hacking with Meteor.");
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerListS2CPacket packet) {
            for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
                if (!packet.getActions().contains(PlayerListS2CPacket.Action.UPDATE_GAME_MODE)) continue;
                PlayerListEntry entry1 = mc.getNetworkHandler().getPlayerListEntry(entry.profileId());
                if (entry1 == null) continue;
                GameMode gameMode = entry.gameMode();
                if (trigger_Spectator.get() == true)
                {
                    if (entry1.getGameMode() != gameMode && entry.gameMode() == GameMode.SPECTATOR) {
                        ghostmodeTrigger();
                    }
                }

                if (trigger_Creative.get() == true)
                {
                    if (entry1.getGameMode() != gameMode && entry.gameMode() == GameMode.CREATIVE)
                    {
                        ghostmodeTrigger();
                    }
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        for (Entity entity : mc.world.getEntities()) {
            if (!trigger_EntityInRange_types.get().contains(entity.getType())) continue;
            if (entity == mc.player || mc.player.distanceTo(entity) >= trigger_EntityInRange_range.get()) continue;
            if (entity instanceof PlayerEntity player) {
                if (trustFriends.get() && Friends.get().isFriend(player)) continue;
            }
            ghostmodeTrigger();
        }
    }

    public void ghostmodeTrigger()
    {
        if (informUserViaInfo.get() == true)
        {
            ChatUtils.info("Ghost Mode triggered");
        }

        if (homeOnCaught.get() == true)
        {
            ChatUtils.sendPlayerMsg("/home");
        }

        if (cancelBaritoneOnCaught.get() == true)
        {
            // TODO: Use API instead (BaritonePathManager.java)
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
        }

        if (panicOnCaught.get() == true)
        {
            new ArrayList<>(Modules.get().getActive()).forEach(Module::toggle);
        }

        if (toggleOnUse.get() == true)
        {
            this.toggle();
        }
    }
}

