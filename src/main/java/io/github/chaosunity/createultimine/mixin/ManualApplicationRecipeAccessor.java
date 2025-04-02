package io.github.chaosunity.createultimine.mixin;

import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ManualApplicationRecipe.class)
public interface ManualApplicationRecipeAccessor {
    @Invoker("awardAdvancements")
    static void invokeAwardAdvancements(Player player, BlockState placed) {
        throw new AssertionError();
    }
}
