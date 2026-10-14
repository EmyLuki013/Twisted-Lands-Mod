package com.twistedteam.twistedlands.items.extras;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Critter Cage (CritterCage.js). A vazia pega a criatura pra onde o jogador olha e vira a cheia, que guarda só o
 * tipo dela; a cheia solta uma nova em cima do bloco clicado e volta a ser vazia.
 */
public class CritterCageItem extends Item {
    private static final String CREATURE = "Creature";
    // O getEntitiesFromViewDirection do addon não diz o alcance; 64 é chute.
    private static final double RANGE = 64.0;
    private static final Set<String> FORBIDDEN = Set.of("minecraft:ender_dragon", "minecraft:wither",
            "minecraft:elder_guardian", "minecraft:warden", "minecraft:evoker", "minecraft:vindicator",
            "minecraft:piglin_brute", "twisted:mantis", "twisted:brown_ant_queen", "twisted:nastysaurus",
            "twisted:robo_jeffery", "twisted:robo_tron", "twisted:robo_pouder", "twisted:robo_warrior", "twisted:mobjira",
            "twisted:kraken", "twisted:mothra", "twisted:classic_mobjira", "twisted:skotodrakon_queen",
            "twisted:crysodrakon_queen", "twisted:hercules_beetle", "twisted:arcanus_fulminis", "twisted:hammerhead");

    // A vazia aponta pra cheia e a cheia pra vazia. Supplier porque as duas são registradas uma depois da outra.
    private final Supplier<Item> other;
    private final boolean filled;

    public CritterCageItem(Properties properties, Supplier<Item> other, boolean filled) {
        super(properties);
        this.other = other;
        this.filled = filled;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (filled) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        Entity creature = lookedAt(player);
        if (creature == null) {
            player.displayClientMessage(Component.translatable("critter_cage.not_mob_found"), true);
            return InteractionResultHolder.fail(stack);
        }
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(creature.getType()).toString();
        if (FORBIDDEN.contains(id)) {
            player.displayClientMessage(Component.translatable("critter_cage.forbidden_mob"), true);
            return InteractionResultHolder.fail(stack);
        }
        ItemStack cage = new ItemStack(other.get());
        cage.getOrCreateTag().putString(CREATURE, id);
        if (!player.getInventory().add(cage)) {
            player.displayClientMessage(Component.translatable("critter_cage.inventory_full"), true);
            return InteractionResultHolder.fail(stack);
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0f, 1.0f);
        player.displayClientMessage(Component.translatable("critter_cage.capturing").append(id), true);
        creature.discard();
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        if (!filled || player == null || stack.getTag() == null || !stack.getTag().contains(CREATURE)) {
            return InteractionResult.PASS;
        }
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }
        EntityType.byString(stack.getTag().getString(CREATURE)).ifPresent(type -> {
            BlockPos above = context.getClickedPos().above();
            type.spawn(level, above, MobSpawnType.SPAWN_EGG);
        });
        player.setItemInHand(context.getHand(), new ItemStack(other.get()));
        level.playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (!filled) {
            tooltip.add(Component.translatable("critter_cage.tooltip").withStyle(ChatFormatting.BLUE));
        } else if (stack.getTag() != null && stack.getTag().contains(CREATURE)) {
            tooltip.add(Component.literal(stack.getTag().getString(CREATURE)).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        }
    }

    // Qualquer criatura na mira que não seja jogador; blocos param o raio.
    @Nullable
    private static Entity lookedAt(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0f).scale(RANGE));
        end = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getLocation();
        AABB box = player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, end, box,
                entity -> !(entity instanceof Player) && !entity.isSpectator() && entity.isPickable(), eye.distanceToSqr(end));
        return hit != null ? hit.getEntity() : null;
    }
}
