package io.github.chaosunity.createultimine.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import dev.architectury.event.EventResult;
import dev.ftb.mods.ftbultimine.CooldownTracker;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.shape.ShapeContext;
import io.github.chaosunity.createultimine.RightClickHandlers;
import io.github.chaosunity.createultimine.config.CreateUltimineServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FTBUltimine.class)
public class UltimineMixin {
    @Inject(method = "blockRightClick",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Ldev/ftb/mods/ftbultimine/FTBUltiminePlayerData;updateBlocks(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;ZI)Ldev/ftb/mods/ftbultimine/shape/ShapeContext;"
            ),
            cancellable = true
    )
    private void injectBlockRightClick(
            Player player,
            InteractionHand hand,
            BlockPos clickPos,
            Direction face,
            CallbackInfoReturnable<EventResult> cir,
            @Local ServerPlayer serverPlayer,
            @Local FTBUltiminePlayerData data,
            @Local BlockHitResult blockHitResult,
            @Local ShapeContext shapeContext) {
        if (shapeContext != null && data.isPressed() && data.hasCachedPositions()) {
            Level level = serverPlayer.level();
            ManualApplicationRecipe recipe = RightClickHandlers.INSTANCE
                    .isManualApplicable(
                            level,
                            level.getBlockState(clickPos),
                            serverPlayer.getItemInHand(hand)
                    );
            int didWork = 0;

            if (CreateUltimineServerConfig.getRIGHT_CLICK_ALLOY().get() && recipe != null) {
                didWork = RightClickHandlers.INSTANCE.itemApplication(serverPlayer, hand, clickPos, recipe, data);
            } else if (CreateUltimineServerConfig.getRIGHT_CLICK_WRENCH().get() && serverPlayer.getItemInHand(hand).getItem() == AllItems.WRENCH.get()) {
                didWork = RightClickHandlers.INSTANCE.onWrenchUse(serverPlayer, hand, blockHitResult, data);
            }

            if (didWork > 0) {
                player.swing(hand);
                if (!player.isCreative()) {
                    CooldownTracker.setLastUltimineTime(player, System.currentTimeMillis());
                    data.addPendingXPCost(serverPlayer, Math.max(0, didWork - 1));
                }

                cir.setReturnValue(EventResult.interruptFalse());
            }
        }
    }
}
