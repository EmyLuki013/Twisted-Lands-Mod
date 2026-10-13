package com.twistedteam.twistedlands.items.extras;

import com.twistedteam.twistedlands.TwistedLands;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Maçã mágica: visão noturna e regeneração por 150s, e faz nascer uma árvore gigante no bloco que o
 * jogador está olhando (até 10 blocos), igual ao big_tree_generator do addon.
 */
public class MagicAppleItem extends Item {
    private static final List<ResourceLocation> TREES = List.of(
            TwistedLands.id("big_trees/acacia_big_tree"),
            TwistedLands.id("big_trees/birch_big_tree"),
            TwistedLands.id("big_trees/dark_oak_big_tree"),
            TwistedLands.id("big_trees/pine_big_tree"),
            TwistedLands.id("big_trees/sakura_big_tree"));

    // O gerador do addon colocava a estrutura com esse deslocamento a partir do bloco.
    private static final BlockPos TREE_OFFSET = new BlockPos(-20, -2, -13);

    public MagicAppleItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level instanceof ServerLevel server) {
            entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 3000, 0, false, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 3000, 0, false, false, true));
            growTree(server, entity);
        }
        return super.finishUsingItem(stack, level, entity);
    }

    private static void growTree(ServerLevel level, LivingEntity entity) {
        Vec3 eye = entity.getEyePosition();
        Vec3 end = eye.add(entity.getViewVector(1.0f).scale(10.0));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        ResourceLocation tree = TREES.get(level.random.nextInt(TREES.size()));
        BlockPos origin = hit.getBlockPos().offset(TREE_OFFSET);
        level.getStructureManager().get(tree).ifPresent(template ->
                template.placeInWorld(level, origin, origin, new StructurePlaceSettings(), level.random, 2));
        level.playSound(null, entity.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0f, 1.0f);
    }
}
