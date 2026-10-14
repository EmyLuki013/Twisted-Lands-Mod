package com.twistedteam.twistedlands.projectiles;

import com.twistedteam.twistedlands.TwistedLandsEntities;
import com.twistedteam.twistedlands.TwistedLandsItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Flecha de Irukandji. No addon o impacto dá 100 de dano e põe fogo no alvo. Aqui o dano escala com a força do
 * arco como uma flecha comum, chegando em 100 com o arco puxado até o fim (velocidade 3).
 */
public class IrukandjiArrowEntity extends AbstractArrow {
    private static final double FULL_DRAW_DAMAGE = 100.0;
    private static final int FIRE_SECONDS = 5;

    public IrukandjiArrowEntity(EntityType<? extends IrukandjiArrowEntity> type, Level level) {
        super(type, level);
        setBaseDamage(FULL_DRAW_DAMAGE / 3.0);
    }

    public IrukandjiArrowEntity(Level level, LivingEntity shooter) {
        super(TwistedLandsEntities.IRUKANDJI_ARROW, shooter, level);
        setBaseDamage(FULL_DRAW_DAMAGE / 3.0);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        result.getEntity().setSecondsOnFire(FIRE_SECONDS);
        super.onHitEntity(result);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(TwistedLandsItems.IRUKANDJI_ARROW);
    }
}
