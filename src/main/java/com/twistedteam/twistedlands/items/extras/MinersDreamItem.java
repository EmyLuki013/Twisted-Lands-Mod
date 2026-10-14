package com.twistedteam.twistedlands.items.extras;

import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.world.backroom.Backroom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

import java.util.Set;

/**
 * Miner's Dream (miners_dream.js). Clicando num bloco, abre 7 salas de 9 x 6 x 9 na direção em que o jogador
 * olha, uma a cada meio segundo, com suportes, chão e teto. O normal só funciona no subterrâneo e nos blocos de
 * pedra do entity_placer; o super funciona em qualquer bloco, gasta durabilidade e tem 2s de cooldown.
 */
public class MinersDreamItem extends Item {
    private static final int WIDTH = 9;
    private static final int HEIGHT = 6;
    private static final int MIDDLE = 4;
    private static final int ROOMS = 7;
    private static final int ROOM_DELAY = 10;
    private static final Set<Block> USE_ON = Set.of(Blocks.NETHERRACK, Blocks.STONE, Blocks.GRAVEL, Blocks.COBBLESTONE,
            Blocks.DEEPSLATE, Blocks.TUFF, Blocks.BASALT, Blocks.BLACKSTONE, Blocks.NETHER_BRICKS, Blocks.MAGMA_BLOCK,
            Blocks.SOUL_SAND, Blocks.SOUL_SOIL, Blocks.WARPED_NYLIUM, Blocks.CRIMSON_NYLIUM);
    private static final Set<Block> DESTROY = Set.of(Blocks.STONE, Blocks.BLACKSTONE, Blocks.NETHERRACK,
            Blocks.MAGMA_BLOCK, Blocks.SOUL_SAND, Blocks.SOUL_SOIL, Blocks.DEEPSLATE, Blocks.TUFF, Blocks.GRAVEL,
            Blocks.DIRT, Blocks.WATER, Blocks.LAVA, Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE, Blocks.CLAY,
            Blocks.MOSS_BLOCK, Blocks.AZALEA, Blocks.FLOWERING_AZALEA, Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT,
            Blocks.WARPED_NYLIUM, Blocks.CRIMSON_NYLIUM, Blocks.POLISHED_BLACKSTONE_BRICKS,
            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, Blocks.BASALT, Blocks.CRIMSON_STEM, Blocks.WARPED_STEM,
            Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT,
            Blocks.NETHER_WART_BLOCK, Blocks.WARPED_WART_BLOCK, Blocks.DRIPSTONE_BLOCK, Blocks.POINTED_DRIPSTONE);

    private final boolean isSuper;

    public MinersDreamItem(Properties properties, boolean isSuper) {
        super(properties);
        this.isSuper = isSuper;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        if (player == null || !isSuper && !USE_ON.contains(context.getLevel().getBlockState(pos).getBlock())) {
            return InteractionResult.PASS;
        }
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }
        if (!isSuper && !isUnderground(level, pos)) {
            player.displayClientMessage(Component.translatable("miners_dream.error"), true);
            return InteractionResult.FAIL;
        }
        if (!player.getAbilities().instabuild) {
            if (isSuper) {
                context.getItemInHand().hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
            } else {
                context.getItemInHand().shrink(1);
            }
        }
        if (isSuper) {
            player.getCooldowns().addCooldown(this, 40);
        }
        Direction facing = player.getDirection();
        for (int i = 0; i < ROOMS; i++) {
            BlockPos origin = pos.relative(facing, WIDTH * i);
            Backroom.schedule(level.getServer(), i * ROOM_DELAY, () -> excavate(level, origin, facing));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isSuper;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return isSuper && repair.is(Items.NETHERITE_INGOT);
    }

    // O bloco clicado e os 9 de cima não podem ser ar.
    private static boolean isUnderground(Level level, BlockPos pos) {
        for (int y = 0; y < 10; y++) {
            if (level.getBlockState(pos.above(y)).isAir()) {
                return false;
            }
        }
        return true;
    }

    // A sala começa no bloco clicado e vai 9 blocos pra frente, 4 pra cada lado.
    private static void excavate(ServerLevel level, BlockPos origin, Direction facing) {
        Direction side = facing.getClockWise();
        BlockPos corner = origin.relative(side, -MIDDLE);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int forward = 0; forward < WIDTH; forward++) {
            for (int lateral = 0; lateral < WIDTH; lateral++) {
                for (int y = 0; y < HEIGHT; y++) {
                    cursor.set(corner).move(facing, forward).move(side, lateral).move(Direction.UP, y);
                    if (DESTROY.contains(level.getBlockState(cursor).getBlock())) {
                        level.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
        Backroom.schedule(level.getServer(), ROOM_DELAY, () -> {
            BlockPos middle = origin.relative(facing, MIDDLE).above(3);
            level.playSound(null, middle, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0f, 1.0f);
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, middle.getX() + 0.5, middle.getY(), middle.getZ() + 0.5,
                    1, 0.0, 0.0, 0.0, 0.0);
            boolean nether = level.dimension() == Level.NETHER;
            // O addon carrega a estrutura sempre a partir do bloco clicado, centrada na sala, sem girar pelo sentido.
            boolean turned = facing.getAxis() == Direction.Axis.X;
            String name = "miners_dream/" + (turned ? "miners_dream_2" : "miners_dream") + (nether ? "_nether" : "");
            BlockPos structure = turned ? origin.north(MIDDLE) : origin.west(MIDDLE);
            level.getStructureManager().get(TwistedLands.id(name)).ifPresent(template ->
                    template.placeInWorld(level, structure, structure, new StructurePlaceSettings(), level.random, Block.UPDATE_ALL));
            BlockState base = (nether ? Blocks.NETHER_BRICKS : Blocks.COBBLESTONE).defaultBlockState();
            for (int forward = 0; forward < WIDTH; forward++) {
                for (int lateral = 0; lateral < WIDTH; lateral++) {
                    cursor.set(corner).move(facing, forward).move(side, lateral);
                    fillBase(level, cursor.below(), base);
                    fillBase(level, cursor.above(HEIGHT), base);
                }
            }
        });
    }

    private static void fillBase(ServerLevel level, BlockPos pos, BlockState base) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
            level.setBlock(pos, base, Block.UPDATE_ALL);
        }
    }
}
