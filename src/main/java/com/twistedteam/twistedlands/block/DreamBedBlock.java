package com.twistedteam.twistedlands.block;

import com.twistedteam.twistedlands.network.ScreenFade;
import com.twistedteam.twistedlands.world.backroom.Backroom;
import com.twistedteam.twistedlands.world.dream.DreamWorld;
import com.twistedteam.twistedlands.world.dream.DreamWorldChunkGenerator;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Dream Bed (DreamBed.js). Só funciona de manhã: as coisas do jogador vão pra dois baús ao lado da cama, ele deita,
 * a tela escurece e ele acorda no Dream World sem nada. Dentro do Dream World a cama traz de volta, sem pedir a
 * armadura do Rainbow Portal (no addon ela chama o mesmo join_dimension das portas de bambu).
 * FACING aponta pro travesseiro, que fica no bloco da frente, como na cama vanilla.
 */
public class DreamBedBlock extends HorizontalDirectionalBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 9, 16);
    // isMorning do DreamBed.js.
    private static final long MORNING_END = 5000L;
    // Deita 2 ticks depois do clique, escurece 20 ticks depois e acorda 60 ticks depois de deitar.
    private static final int LIE_DOWN_DELAY = 2;
    private static final int DARKEN_DELAY = 22;
    private static final int WAKE_DELAY = 64;

    public DreamBedBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    /** Deixa o jogador dormir de dia na Dream Bed, deitado no sentido dela. */
    public static void registerSleepEvents() {
        EntitySleepEvents.ALLOW_BED.register((entity, sleepingPos, state, vanillaResult) ->
                bedFacing(entity.level(), sleepingPos) != null ? InteractionResult.SUCCESS : InteractionResult.PASS);
        EntitySleepEvents.ALLOW_SLEEP_TIME.register((player, sleepingPos, vanillaResult) ->
                bedFacing(player.level(), sleepingPos) != null ? InteractionResult.SUCCESS : InteractionResult.PASS);
        EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.register((entity, sleepingPos, sleepingDirection) -> {
            Direction facing = bedFacing(entity.level(), sleepingPos);
            return facing != null ? facing : sleepingDirection;
        });
    }

    // O jogador dorme no bloco do travesseiro, então a cama é o vizinho que aponta pra ele. Cama vanilla ali
    // continua sendo dela.
    @Nullable
    private static Direction bedFacing(BlockGetter level, BlockPos pillow) {
        if (level.getBlockState(pillow).getBlock() instanceof BedBlock) {
            return null;
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockState state = level.getBlockState(pillow.relative(side));
            if (state.getBlock() instanceof DreamBedBlock && state.getValue(FACING) == side.getOpposite()) {
                return state.getValue(FACING);
            }
        }
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        if (player.isSleeping()) {
            return InteractionResult.CONSUME;
        }
        if (level.getDayTime() % 24000L > MORNING_END) {
            player.sendSystemMessage(Component.translatable("dreamBed.cantSleep"));
            return InteractionResult.CONSUME;
        }

        // simulateBlink: piscada preta e a respiração de cavalo.
        ScreenFade.send(serverPlayer, 0x000000, 3, 2);
        Backroom.schedule(serverLevel.getServer(), 2, () -> serverPlayer.playNotifySound(SoundEvents.HORSE_BREATHE,
                SoundSource.PLAYERS, 1.0f, 0.8f));
        storeInventory(serverLevel, pos, serverPlayer);

        BlockPos pillow = pos.relative(state.getValue(FACING));
        Backroom.schedule(serverLevel.getServer(), LIE_DOWN_DELAY, () -> {
            if (!serverPlayer.isRemoved() && serverPlayer.level() == serverLevel) {
                serverPlayer.startSleeping(pillow);
            }
        });
        Backroom.schedule(serverLevel.getServer(), DARKEN_DELAY, () -> {
            if (isAsleepAt(serverPlayer, pillow)) {
                ScreenFade.send(serverPlayer, 0x000000, WAKE_DELAY - DARKEN_DELAY + LIE_DOWN_DELAY, 4);
            }
        });
        Backroom.schedule(serverLevel.getServer(), LIE_DOWN_DELAY + WAKE_DELAY, () -> {
            // Quem saiu da cama antes não viaja (as coisas continuam nos baús).
            if (!isAsleepAt(serverPlayer, pillow)) {
                return;
            }
            serverPlayer.stopSleepInBed(true, true);
            if (serverPlayer.level().dimension() == DreamWorldChunkGenerator.LEVEL) {
                DreamWorld.leave(serverPlayer);
            } else {
                DreamWorld.enter(serverPlayer);
            }
        });
        return InteractionResult.CONSUME;
    }

    private static boolean isAsleepAt(ServerPlayer player, BlockPos pillow) {
        return !player.isRemoved() && player.getSleepingPos().filter(pillow::equals).isPresent();
    }

    // Os dois baús ficam sempre em x-1 e x-2, z+1, qualquer que seja o lado da cama, como no addon. Vai tudo do
    // inventário e da armadura; a mão secundária fica com o jogador (o container do Bedrock não tem ela).
    private static void storeInventory(ServerLevel level, BlockPos bed, ServerPlayer player) {
        BlockPos left = bed.offset(-1, 0, 1);
        BlockState chest = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        level.setBlock(left, chest.setValue(ChestBlock.TYPE, ChestType.LEFT), Block.UPDATE_ALL);
        level.setBlock(left.west(), chest.setValue(ChestBlock.TYPE, ChestType.RIGHT), Block.UPDATE_ALL);
        Container container = ChestBlock.getContainer((ChestBlock) Blocks.CHEST, level.getBlockState(left), level, left, true);
        if (container == null) {
            return;
        }
        int slot = 0;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (!stack.isEmpty()) {
                container.setItem(slot++, stack);
                player.getInventory().items.set(i, ItemStack.EMPTY);
            }
        }
        for (EquipmentSlot armor : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(armor);
            if (!stack.isEmpty()) {
                container.setItem(slot++, stack);
                player.setItemSlot(armor, ItemStack.EMPTY);
            }
        }
    }
}
