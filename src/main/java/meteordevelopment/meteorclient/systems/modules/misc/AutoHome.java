package meteordevelopment.meteorclient.systems.modules.misc;

import java.util.Set;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;

// todo
// auto home on someone using vanish
// auto home on low health DONE
// auto home on xyz entity in certain distance (using code from AutoLeave)
    // ^^^ add Ignore friends option for this
// add toggle on use (when used, it turns off, like AutoLeave)
// say XYZ coords when doing home

public class AutoHome extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> onHealth = sgGeneral.add(new BoolSetting.Builder()
        .name("on-low-health")
        .description("Should do /home based on health?")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> healthLevel = sgGeneral.add(new IntSetting.Builder()
        .name("health-level")
        .description("Health level at which to run /home")
        .defaultValue(10)
        .min(1)
        .sliderRange(1, 20)
        .visible(() -> onHealth.get())
        .build()
    );

    private final Setting<Boolean> onLowHunger = sgGeneral.add(new BoolSetting.Builder()
        .name("on-low-hunger")
        .description("Should do /home based on hunger level?")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> hungerLevel = sgGeneral.add(new IntSetting.Builder()
        .name("hunger-level")
        .description("Hunger level at which to run /home (hunger, not saturation)")
        .defaultValue(10)
        .min(1)
        .sliderRange(1, 20)
        .visible(() -> onLowHunger.get())
        .build()
    );

    private final Setting<Boolean> onLowSat = sgGeneral.add(new BoolSetting.Builder()
        .name("on-low-saturation")
        .description("Should do /home based on saturation level?")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> satLevel = sgGeneral.add(new IntSetting.Builder()
        .name("saturation-level")
        .description("Saturation level at which to run /home (saturation, not hunger)")
        .min(1)
        .sliderRange(1, 20)
        .visible(() -> onLowSat.get())
        .build()
    );

    private final Setting<Boolean> toggleOnUse = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-on-autohome")
        .description("Disables module on use (KEEP ENABLED OR IT WILL SPAM)")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay after sending the command in ticks (20 ticks = 1 sec).")
        .defaultValue(10)
        .min(1)
        .sliderRange(1, 40)
        .build()
    );

    private int timer;
    private double preX;
    private double preY;
    private double preZ;

    public AutoHome() {
        super(Categories.Misc, "auto-home", "Uses /home command on certain events.");
    }

    @Override
    public void onActivate() {
        timer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (timer >= delay.get()){
            if (onHealth.get() == true && mc.player.getHealth() <= healthLevel.get()){
                // Code below is commented out because it causes crashes
                // Now I need to found out:
                // is it pre coords? yes, possibly sending the message as well though
                // is it sending the messages?

                // preX = mc.player.getX();
                // preY = mc.player.getY();
                // preZ = mc.player.getZ();
                // Dimension preDimension = PlayerUtils.getDimension();
                ChatUtils.sendPlayerMsg("/home");

                if (toggleOnUse.get() == true)
                {
                    // disable
                    this.toggle();
                }
                else
                {
                    // do nothing, keep enabled.
                }

                // if (preDimension == Dimension.Overworld)
                // {
                //     ChatUtils.info("Before you auto homed, your position was: X: %d Y: %d Z: %d Dimension: Overworld", preX, preY, preZ);
                // }
                // else if (preDimension == Dimension.Nether)
                // {
                //     ChatUtils.info("Before you auto homed, your position was: X: %d Y: %d Z: %d Dimension: The Nether", preX, preY, preZ);
                // }
                // else if (preDimension == Dimension.End)
                // {
                //     ChatUtils.info("Before you auto homed, your position was: X: %d Y: %d Z: %d Dimension: The End", preX, preY, preZ);
                // }
                // else
                // {
                // ChatUtils.info("Before you auto homed, your position was: X: %d Y: %d Z: %d Dimension: could not get", preX, preY, preZ);
                // }

                
                timer = 0;
            }

            if (onLowHunger.get() == true && mc.player.getHungerManager().getFoodLevel() <= hungerLevel.get())
            {
                ChatUtils.sendPlayerMsg("/home");

                if (toggleOnUse.get() == true)
                {
                    // disable
                    this.toggle();
                }
                else
                {
                    // nada
                }
            }

            if (onLowSat.get() == true && mc.player.getHungerManager().getSaturationLevel() <= satLevel.get())
            {
                ChatUtils.sendPlayerMsg("/home");

                if (toggleOnUse.get() == true)
                {
                    // disable
                    this.toggle();
                }
                else
                {
                    // nada
                }
            }

        } else timer ++;
    }
}