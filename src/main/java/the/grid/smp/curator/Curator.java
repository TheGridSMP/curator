package the.grid.smp.curator;

import org.bukkit.plugin.java.JavaPlugin;
import the.grid.smp.curator.asm.ClassTransformer;

public final class Curator extends JavaPlugin {

    private final ClassTransformer transformer = new ClassTransformer();

    @Override
    public void onEnable() {
        this.getLogger().info("Boo! You have discovered the secrets of ancient art of bytecode manipulation!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public ClassTransformer getTransformer() {
        return transformer;
    }
}
