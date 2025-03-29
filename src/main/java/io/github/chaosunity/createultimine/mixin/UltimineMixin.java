package io.github.chaosunity.createultimine.mixin;

import com.simibubi.create.AllItems;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FTBUltimine.class)
public class UltimineMixin {
    @Inject(method = "blockRightClick",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Ldev/ftb/mods/ftbultimine/FTBUltiminePlayerData;updateBlocks(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;ZI)Ldev/ftb/mods/ftbultimine/shape/ShapeContext;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectBlockRightClick(
            Player player,
            InteractionHand hand,
            BlockPos clickPos,
            Direction face,
            CallbackInfoReturnable<EventResult> cir,
            ServerPlayer serverPlayer,
            FTBUltiminePlayerData data,
            HitResult result,
            BlockHitResult blockHitResult,
            ShapeContext shapeContext) {
        if (shapeContext != null && data.isPressed() && data.hasCachedPositions()) {
            int didWork = 0;

            if (CreateUltimineServerConfig.getConfig().getRIGHT_CLICK_ALLOY().get() && RightClickHandlers.INSTANCE.getAlloyApplications().containsKey(serverPlayer.getItemInHand(hand).getItem())) {
                didWork = RightClickHandlers.INSTANCE.alloyApply(serverPlayer, hand, clickPos, data);
            } else if (CreateUltimineServerConfig.getConfig().getRIGHT_CLICK_WRENCH().get() && serverPlayer.getItemInHand(hand).getItem() == AllItems.WRENCH.get()) {
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
