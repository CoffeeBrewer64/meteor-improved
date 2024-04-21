package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AutoPanic extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
            .name("entities")
            .description("Entity types to react on.")
            .onlyAttackable()
            .defaultValue(EntityType.PLAYER)
            .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
            .name("range")
            .description("Range in which to react.")
            .defaultValue(5)
            .min(1)
            .sliderRange(1, 256)
            .build()
    );

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
            .name("ignore-friends")
            .description("Don't react to players added as friends.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> toggleOff = sgGeneral.add(new BoolSetting.Builder()
            .name("toggle-off")
            .description("Disables Auto Panic after usage.")
            .defaultValue(false)
            .build()
    );

    private boolean work;

    public AutoPanic() {
        super(Categories.Player, "auto-panic", "Automatically disables all Meteor modules if an entity is in range.");
    }

    @Override
    public void onActivate() {
        work = true;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        for (Entity entity : mc.world.getEntities()) {
            if (!entities.get().contains(entity.getType())) continue;
            if (entity == mc.player || mc.player.distanceTo(entity) >= range.get()) continue;
            if (entity instanceof PlayerEntity player) {
                if (ignoreFriends.get() && Friends.get().isFriend(player)) continue;
            }
            if (work) {
                new ArrayList<>(Modules.get().getActive()).forEach(Module::toggle);
                ChatUtils.info("AutoPanic triggered!", null);
                work = !work;
            }
            if (toggleOff.get()) this.toggle();
        }
    }
}