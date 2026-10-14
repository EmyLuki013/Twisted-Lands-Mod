package com.twistedteam.twistedlands;

import com.twistedteam.twistedlands.items.extras.CritterCageItem;
import com.twistedteam.twistedlands.items.extras.MinersDreamItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import com.twistedteam.twistedlands.entities.RainbowPortalEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import com.twistedteam.twistedlands.projectiles.IrukandjiArrowEntity;
import com.twistedteam.twistedlands.items.armor.PeacockFeatherBootsItem;
import com.twistedteam.twistedlands.items.armor.RoyalGeoArmor;
import com.twistedteam.twistedlands.items.extras.CombatPotionItem;
import com.twistedteam.twistedlands.items.extras.LittleMintItem;
import com.twistedteam.twistedlands.items.extras.MagicAppleItem;
import com.twistedteam.twistedlands.items.tools.UltimateChainsaw;
import com.twistedteam.twistedlands.items.enchant.GlintingSpawnEggItem;
import com.twistedteam.twistedlands.items.tools.*;
import com.twistedteam.twistedlands.materials.*;
import com.twistedteam.twistedlands.materials.armor.AmethystArmorMaterial;
import com.twistedteam.twistedlands.materials.armor.RoyalArmorMaterial;
import com.twistedteam.twistedlands.materials.armor.RubyArmorMaterial;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class TwistedLandsItems {
    public static Item register(Item item, String id) {
        ResourceLocation itemID = new ResourceLocation(TwistedLands.MOD_ID, id);

        return Registry.register(BuiltInRegistries.ITEM, itemID, item);
    }

    //Mantis Tier
    private static final Tier MANTIS_TIER = new Tier() {
        @Override public int getUses() { return 1500; }
        @Override public float getSpeed() { return 9.0F; }
        @Override public float getAttackDamageBonus() { return 0.0F; }
        @Override public int getLevel() { return 4; }
        @Override public int getEnchantmentValue() { return 22; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
    };

    //Ingredients

    public static final Item ULTIMATE_UPGRADE = register(
            new TooltipItem(new FabricItemSettings().maxCount(64)),
            "ultimate_upgrade"
    );

    public static final Item LASER_CHARGE = register(
            new Item(new FabricItemSettings().maxCount(1).durability(64)),
            "laser_charge"
    );

    public static final Item DRAKONIC_EYE = register(
            new Item(new FabricItemSettings().maxCount(64)),
            "drakonic_eye"
    );

    public static final Item CRYSTAL_STICK = register(
            new Item(new FabricItemSettings().maxCount(64)),
            "crystal_stick"
    );

    public static final Item ULTIMATE_INGOT = register(
            new Item(new FabricItemSettings().maxCount(64).fireproof()),
            "ultimate_ingot"
    );

    public static final Item SKOTODRAKON_SCALE = register(
            new Item(new FabricItemSettings().maxCount(64).fireproof()),
            "skotodrakon_scale"
    );

    public static final Item MOBJIRA_SCALE = register(
            new Item(new FabricItemSettings().maxCount(64).fireproof()),
            "mobjira_scale"
    );

    public static final Item AMETHYST = register(
            new Item(new FabricItemSettings().maxCount(64)),
            "amethyst"
    );

    public static final Item RUBY = register(
            new Item(new FabricItemSettings().maxCount(64)),
            "ruby"
    );

    public static final Item DINOSSAUR_TOOTH = register(
            new Item(new FabricItemSettings().maxCount(64)),
            "dinossaur_tooth"
    );

    public static final Item MANTIS_CLAW = register(
            new SwordItem(MANTIS_TIER, 9, -1.4f, new FabricItemSettings().maxCount(1)),
            "mantis_claw"
    );

    public static final Item LITTLE_MINT_HAT = register(
            new LittleMintItem(new FabricItemSettings().maxCount(1)),
            "little_mint_hat"
    );

    public static final Item DEAD_RAINBOW_ANT = register(new Item(new FabricItemSettings()), "dead_rainbow_ant");
    public static final Item RAINBOW_ANT_FANG = register(new Item(new FabricItemSettings()), "rainbow_ant_fang");
    public static final Item RAINBOW_ANT_LEG = register(new Item(new FabricItemSettings()), "rainbow_ant_leg");
    public static final Item RAINBOW_ANT_EYE = register(new Item(new FabricItemSettings()), "rainbow_ant_eye");
    public static final Item RAINBOW_ANT_ABDOMEN = register(new Item(new FabricItemSettings()), "rainbow_ant_abdomen");

    // Drop da Brown Ant Queen no addon; aqui por enquanto só serve pra poção de combate.
    public static final Item BROWN_ANT_MANDIBLE = register(new Item(new FabricItemSettings()), "brown_ant_mandible");

    //Foods
    public static FoodProperties RAW_DINO_MEAT_ = new FoodProperties.Builder()
            .nutrition(2)
            .saturationMod(1.0f)
            .meat()
            .build();

    public static final Item RAW_DINO_MEAT = register(
            new Item(new FabricItemSettings().maxCount(64).food(RAW_DINO_MEAT_)),
            "raw_dino_meat"
    );

    public static FoodProperties COOKED_DINO_MEAT_ = new FoodProperties.Builder()
            .nutrition(9)
            .saturationMod(1.0f)
            .meat()
            .build();

    public static final Item COOKED_DINO_MEAT = register(
            new Item(new FabricItemSettings().maxCount(64).food(COOKED_DINO_MEAT_)),
            "cooked_dino_meat"
    );

    public static final Item COMBAT_POTION = register(
            new CombatPotionItem(0, 180, new FabricItemSettings().maxCount(1)),
            "combat_potion"
    );

    public static final Item POWERED_COMBAT_POTION = register(
            new CombatPotionItem(1, 8 * 60, new FabricItemSettings().maxCount(1)),
            "powered_combat_potion"
    );

    public static final Item SALT = register(new ItemNameBlockItem(TwistedLandsBlocks.SALT_DUST, new FabricItemSettings()), "salt");

    // Come ou planta, igual ao block_placer + food do addon.
    public static final Item TOMATO = register(
            new ItemNameBlockItem(TwistedLandsBlocks.TOMATO_PLANT_HEAD, new FabricItemSettings()
                    .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.55f).build())),
            "tomato"
    );

    // Comidas. Fome e saturação iguais ao addon; os efeitos eram dados por script ao comer, aqui ficam na comida.
    public static final Item CANDY_CANE = registerFoods("candy_cane", new FoodProperties.Builder()
            .nutrition(3).saturationMod(0.3f)
            .effect(effect(MobEffects.MOVEMENT_SPEED, 120), 1.0f));

    public static final Item COTTON_CANDY = registerFoods("cotton_candy", new FoodProperties.Builder()
            .nutrition(2).saturationMod(0.1f)
            .effect(effect(MobEffects.JUMP, 100), 1.0f)
            .effect(effect(MobEffects.SLOW_FALLING, 100), 1.0f));

    public static final Item PEANUT_CANDY = registerFoods("peanut_candy", new FoodProperties.Builder()
            .nutrition(4).saturationMod(0.8f));

    public static final Item CHOCOLATE_BAR = registerFoods("chocolate_bar", new FoodProperties.Builder()
            .nutrition(4).saturationMod(0.3f));

    public static final Item PIZZA_PIECE = registerFoods("pizza_piece", new FoodProperties.Builder()
            .nutrition(6).saturationMod(0.6f)
            .effect(effect(MobEffects.REGENERATION, 100), 1.0f));

    // Cerejas caem das folhas do Cherry Orchard no addon.
    public static final Item CHERRIES = registerFoods("cherries", new FoodProperties.Builder()
            .nutrition(3).saturationMod(0.45f));

    public static final Item APPLE_PIE = registerFoods("apple_pie", new FoodProperties.Builder()
            .nutrition(6).saturationMod(0.6f));

    public static final Item CHEESE = registerFoods("cheese", new FoodProperties.Builder()
            .nutrition(4).saturationMod(0.75f));

    public static final Item MAGIC_APPLE = register(new MagicAppleItem(new FabricItemSettings().maxCount(1)
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(1.0f).alwaysEat().build())), "magic_apple");

    private static Item registerFoods(String id, FoodProperties.Builder food) {
        return register(new Item(new FabricItemSettings().food(food.build())), id);
    }

    //Weapons

    public static final Item BIG_BERTHA = register(
            new EnchantedBigSwords(BigBerthaMaterial.BIG_BERTHA_MATERIAL, 499, 1.4f,
                    new FabricItemSettings().maxCount(1).fireproof()), "big_bertha"
    );

    public static final Item ROYAL_GUARDIAN = register(
            new EnchantedBigSwords(RoyalGuardianMaterial.ROYAL_GUARDIAN_MATERIAL, 749, 1.4f,
                    new FabricItemSettings().maxCount(1).fireproof()), "royal_guardian"
    );

    public static final Item STELIX_BERTHA = register(
            new EnchantedBigSwords(StelixBerthaMaterial.STELIX_BERTHA_MATERIAL, 579, 1.4f,
                    new FabricItemSettings().maxCount(1).fireproof()), "stelix_bertha"
    );

    public static final Item ULTIMATE_HAMMER = register(
            new UltimateHammer(UltimateHammerMaterial.ULTIMATE_HAMMER_MATERIAL, 85, 1.0f,
                    new FabricItemSettings().maxCount(1)), "ultimate_hammer"
    );

    public static final Item ULTIMATE_CHAINSAW = register(
            new UltimateChainsaw(UltimateChainsawMaterial.ULTIMATE_HAMMER_MATERIAL, 49, 1.0f,
                    new FabricItemSettings().maxCount(1).fireproof()), "ultimate_chainsaw"
    );

    public static final Item BATTLE_AXE = register(
            new BattleAxe(BattleAxeMaterial.BATTLE_AXE, 49, 1.0f,
                    new FabricItemSettings().maxCount(1).fireproof()), "battle_axe"
    );

    public static final Item QUEEN_BATTLE_AXE = register(
            new BattleAxe(QueenBattleAxeMaterial.QUEEN_BATTLE_AXE, 664, 1.0f,
                    new FabricItemSettings().maxCount(1).fireproof()), "queen_battle_axe"
    );

    public static final Item FIRE_BLASTER = register(
            new FireBlaster(new FabricItemSettings().maxCount(1)),
            "fire_blaster"
    );

    public static final Item WATER_BLASTER = register(
            new WaterBlaster(new FabricItemSettings().maxCount(1)),
            "water_blaster"
    );

    //Spawn Eggs

    public static final Item APPLE_COW_SPAWN_EGG = register(
            new SpawnEggItem(TwistedLandsEntities.APPLE_COW, 0xFFFFFF, 0xFFFFFF,
                    new FabricItemSettings().maxCount(64).fireproof()),
            "apple_cow_spawn_egg"
    );

    public static final Item GOLDEN_APPLE_COW_SPAWN_EGG = register(
            new SpawnEggItem(TwistedLandsEntities.GOLDEN_APPLE_COW, 0xFFFFFF, 0xFFFFFF,
                    new FabricItemSettings().maxCount(64).fireproof()),
            "golden_apple_cow_spawn_egg"
    );

    public static final Item ENCHANTED_GOLDEN_APPLE_COW_SPAWN_EGG = register(
            new GlintingSpawnEggItem(TwistedLandsEntities.ENCHANTED_GOLDEN_APPLE_COW, 0xFFFFFF, 0xFFFFFF,
                    new FabricItemSettings().maxCount(64).fireproof()),
            "enchanted_golden_apple_cow_spawn_egg"
    );

    public static final Item MOBJIRA_SPAWN_EGG = register(
            new SpawnEggItem(TwistedLandsEntities.MOBJIRA, 0xFFFFFF, 0xFFFFFF,
                    new FabricItemSettings().maxCount(64).fireproof()),
            "mobjira_spawn_egg"
    );

    public static final Item NASTYSAURUS_SPAWN_EGG = register(
            new SpawnEggItem(TwistedLandsEntities.NASTYSAURUS, 0xFFFFFF, 0xFFFFFF,
                    new FabricItemSettings().maxCount(64)), "nastysaurus_spawn_egg"
    );

    public static final Item MANTIS_SPAWN_EGG = register(
            new SpawnEggItem(TwistedLandsEntities.MANTIS, 0xFFFFFF, 0xFFFFFF,
                    new FabricItemSettings().maxCount(64)), "mantis_spawn_egg"
    );

    //Amethyst Set
    public static final Item AMETHYST_HELMET = register(
            new ArmorItem(AmethystArmorMaterial.AMETHYST_ARMOR_MATERIAL, ArmorItem.Type.HELMET,
                    new FabricItemSettings().maxCount(1)), "amethyst_helmet"
    );

    public static final Item AMETHYST_CHESTPLATE = register(
            new ArmorItem(AmethystArmorMaterial.AMETHYST_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE,
                    new FabricItemSettings().maxCount(1)), "amethyst_chestplate"
    );

    public static final Item AMETHYST_LEGGINGS = register(
            new ArmorItem(AmethystArmorMaterial.AMETHYST_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS,
                    new FabricItemSettings().maxCount(1)), "amethyst_leggings"
    );

    public static final Item AMETHYST_BOOTS = register(
            new ArmorItem(AmethystArmorMaterial.AMETHYST_ARMOR_MATERIAL, ArmorItem.Type.BOOTS,
                    new FabricItemSettings().maxCount(1)), "amethyst_boots"
    );

    public static final Item AMETHYST_SWORD = register(
            new SwordItem(AmethystToolsMaterial.AMETHYST_TOOLS_MATERIAL, 14, -1.4f,
                    new FabricItemSettings().maxCount(1)), "amethyst_sword"
    );

    public static final Item AMETHYST_PICKAXE = register(
            new PickaxeItem(AmethystToolsMaterial.AMETHYST_TOOLS_MATERIAL, 12, -2.0f,
                    new FabricItemSettings().maxCount(1)), "amethyst_pickaxe"
    );

    public static final Item AMETHYST_AXE = register(
            new AxeItem(AmethystToolsMaterial.AMETHYST_TOOLS_MATERIAL, 13, -2.8f,
                    new FabricItemSettings().maxCount(1)), "amethyst_axe"
    );

    public static final Item AMETHYST_SHOVEL = register(
            new ShovelItem(AmethystToolsMaterial.AMETHYST_TOOLS_MATERIAL, 11, -2.0f,
                    new FabricItemSettings().maxCount(1)), "amethyst_shovel"
    );

    public static final Item AMETHYST_HOE = register(
            new HoeItem(AmethystToolsMaterial.AMETHYST_TOOLS_MATERIAL, 9, -2.0f,
                    new FabricItemSettings().maxCount(1)), "amethyst_hoe"
    );

    //Ruby Set
    public static final Item RUBY_HELMET = register(
            new ArmorItem(RubyArmorMaterial.RUBY_ARMOR_MATERIAL, ArmorItem.Type.HELMET,
                    new FabricItemSettings().maxCount(1)), "ruby_helmet"
    );

    public static final Item RUBY_CHESTPLATE = register(
            new ArmorItem(RubyArmorMaterial.RUBY_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE,
                    new FabricItemSettings().maxCount(1)), "ruby_chestplate"
    );

    public static final Item RUBY_LEGGINGS = register(
            new ArmorItem(RubyArmorMaterial.RUBY_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS,
                    new FabricItemSettings().maxCount(1)), "ruby_leggings"
    );

    public static final Item RUBY_BOOTS = register(
            new ArmorItem(RubyArmorMaterial.RUBY_ARMOR_MATERIAL, ArmorItem.Type.BOOTS,
                    new FabricItemSettings().maxCount(1)), "ruby_boots"
    );

    public static final Item RUBY_SWORD = register(
            new SwordItem(RubyToolsMaterial.AMETHYST_TOOLS_MATERIAL, 19, -1.4f,
                    new FabricItemSettings().maxCount(1)), "ruby_sword"
    );

    public static final Item RUBY_PICKAXE = register(
            new PickaxeItem(RubyToolsMaterial.AMETHYST_TOOLS_MATERIAL, 17, -1.8f,
                    new FabricItemSettings().maxCount(1)), "ruby_pickaxe"
    );

    public static final Item RUBY_AXE = register(
            new AxeItem(RubyToolsMaterial.AMETHYST_TOOLS_MATERIAL, 18.0f, -2.2f,
                    new FabricItemSettings().maxCount(1)), "ruby_axe"
    );

    public static final Item RUBY_SHOVEL = register(
            new ShovelItem(RubyToolsMaterial.AMETHYST_TOOLS_MATERIAL, 16.0f, -1.8f,
                    new FabricItemSettings().maxCount(1)), "ruby_shovel"
    );

    public static final Item RUBY_HOE = register(
            new HoeItem(RubyToolsMaterial.AMETHYST_TOOLS_MATERIAL, 16, -1.8f,
                    new FabricItemSettings().maxCount(1)), "ruby_hoe"
    );

    //Royal Guardian Set
    public static final Item ROYAL_HELMET = register(
            new RoyalGeoArmor(RoyalArmorMaterial.ROYAL_ARMOR_MATERIAL, ArmorItem.Type.HELMET,
                    new FabricItemSettings().maxCount(1).fireproof()), "royal_helmet"
    );

    public static final Item ROYAL_CHESTPLATE = register(
            new RoyalGeoArmor(RoyalArmorMaterial.ROYAL_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE,
                    new FabricItemSettings().maxCount(1).fireproof()), "royal_chestplate"
    );

    public static final Item ROYAL_LEGGINGS = register(
            new RoyalGeoArmor(RoyalArmorMaterial.ROYAL_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS,
                    new FabricItemSettings().maxCount(1).fireproof()), "royal_leggings"
    );

    public static final Item ROYAL_BOOTS = register(
            new RoyalGeoArmor(RoyalArmorMaterial.ROYAL_ARMOR_MATERIAL, ArmorItem.Type.BOOTS,
                    new FabricItemSettings().maxCount(1).fireproof()), "royal_boots"
    );

    // Formiga arco-íris. O ovo usa a textura do addon, por isso as cores ficam brancas (sem tingir).
    public static final Item RAINBOW_ANT_SPAWN_EGG = register(
            new SpawnEggItem(TwistedLandsEntities.RAINBOW_ANT, 0xFFFFFF, 0xFFFFFF, new FabricItemSettings()),
            "rainbow_ant_spawn_egg"
    );

    // Mesma coisa do ovo da formiga: textura do addon, sem tingir.
    public static final Item RAT_SPAWN_EGG = register(
            new SpawnEggItem(TwistedLandsEntities.RAT, 0xFFFFFF, 0xFFFFFF, new FabricItemSettings()),
            "rat_spawn_egg"
    );

    // Dream World. As receitas vêm do RecipeManagerMixin e a ordem desta lista é a da aba criativa.
    public static final Item CRYSTAL_COAL = register(new Item(new FabricItemSettings()), "crystal_coal");
    public static final Item CRYSTAL_APPLE = register(new Item(new FabricItemSettings().food(new FoodProperties.Builder()
            .nutrition(5).saturationMod(0.85f).alwaysEat()
            .effect(effect(MobEffects.DAMAGE_BOOST, 3000), 1f)
            .effect(effect(MobEffects.REGENERATION, 3000), 1f).build())), "crystal_apple");
    // Come ou planta (na crystal grass), como o tomate.
    public static final Item CRYSTAL_CORN = register(new ItemNameBlockItem(TwistedLandsBlocks.CRYSTAL_CORN_PLANT_HEAD,
            new FabricItemSettings().food(new FoodProperties.Builder().nutrition(6).saturationMod(0.75f).build())), "crystal_corn");
    public static final Item CRYSTAL_APPLE_COW_SPAWN_EGG = register(
            new SpawnEggItem(TwistedLandsEntities.CRYSTAL_APPLE_COW, 0xFFFFFF, 0xFFFFFF, new FabricItemSettings()),
            "crystal_apple_cow_spawn_egg");
    public static final Item PEACOCK_SPAWN_EGG = register(
            new SpawnEggItem(TwistedLandsEntities.PEACOCK, 0xFFFFFF, 0xFFFFFF, new FabricItemSettings()),
            "peacock_spawn_egg");
    public static final Item PEACOCK_FEATHER = register(new Item(new FabricItemSettings()), "peacock_feather");
    public static final Item PEACOCK_FEATHER_BOOTS = register(
            new PeacockFeatherBootsItem(new FabricItemSettings().maxCount(1)), "peacock_feather_boots");
    // Saturação 7.2 no cozido, como está no addon (o valor de saturação do frango cozido, não o modificador).
    public static final Item PEACOCK_RAW = registerFoods("peacock_raw", new FoodProperties.Builder()
            .nutrition(2).saturationMod(1.2f).meat());
    public static final Item PEACOCK_COOKED = registerFoods("peacock_cooked", new FoodProperties.Builder()
            .nutrition(6).saturationMod(7.2f).meat());
    public static final Item ROTTEN_PEACOCK = registerFoods("rotten_peacock", new FoodProperties.Builder()
            .nutrition(3).saturationMod(0.8f).meat()
            .effect(effect(MobEffects.HUNGER, 400), 1.0f)
            .effect(effect(MobEffects.CONFUSION, 120), 1.0f));
    public static final Item EMERALD_PEACOCK = registerFoods("emerald_peacock", new FoodProperties.Builder()
            .nutrition(2).saturationMod(1.2f)
            .effect(effect(MobEffects.SLOW_FALLING, 1200), 1.0f));
    public static final Item IRUKANDJI_SPAWN_EGG = register(
            new SpawnEggItem(TwistedLandsEntities.IRUKANDJI, 0xFFFFFF, 0xFFFFFF, new FabricItemSettings()),
            "irukandji_spawn_egg");
    public static final Item DEAD_IRUKANDJI = register(new Item(new FabricItemSettings()), "dead_irukandji");
    // Vai no arco e na besta como uma flecha comum.
    public static final Item IRUKANDJI_ARROW = register(new ArrowItem(new FabricItemSettings()) {
        @Override
        public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter) {
            return new IrukandjiArrowEntity(level, shooter);
        }
    }, "irukandji_arrow");
    // Os ids batem com os dos baús da crystal_dungeon.
    public static final Item BALLOON_SHARK_SPAWN_EGG = register(
            new SpawnEggItem(TwistedLandsEntities.BALLOON_SHARK, 0xFFFFFF, 0xFFFFFF, new FabricItemSettings()),
            "balloon_shark_spawn_egg");
    public static final Item SHARK_FIN = register(new Item(new FabricItemSettings()), "shark_fin");

    public static final Item MINERS_DREAM = register(
            new MinersDreamItem(new FabricItemSettings().maxCount(64), false), "miners_dream");
    public static final Item SUPER_MINERS_DREAM = register(
            new MinersDreamItem(new FabricItemSettings().maxDamage(100), true), "super_miners_dream");

    // As cheias empilham 1 e só saem da vazia.
    public static final Item CRITTER_CAGE_EMPTY = register(
            new CritterCageItem(new FabricItemSettings(), () -> TwistedLandsItems.CRITTER_CAGE_FILLED, false), "critter_cage_empty");
    public static final Item CRITTER_CAGE_FILLED = register(
            new CritterCageItem(new FabricItemSettings().maxCount(1), () -> CRITTER_CAGE_EMPTY, true), "critter_cage_filled");
    public static final Item CRYSTAL_CRITTER_CAGE_EMPTY = register(
            new CritterCageItem(new FabricItemSettings(), () -> TwistedLandsItems.CRYSTAL_CRITTER_CAGE_FILLED, false),
            "crystal_critter_cage_empty");
    public static final Item CRYSTAL_CRITTER_CAGE_FILLED = register(
            new CritterCageItem(new FabricItemSettings().maxCount(1), () -> CRYSTAL_CRITTER_CAGE_EMPTY, true),
            "crystal_critter_cage_filled");

    // entity_placer do addon: põe o portal no bloco clicado, como um spawn egg.
    public static final Item RAINBOW_PORTAL = register(new Item(new FabricItemSettings().maxCount(1)) {
        @Override
        public InteractionResult useOn(UseOnContext context) {
            if (!(context.getLevel() instanceof ServerLevel level)) {
                return InteractionResult.SUCCESS;
            }
            BlockPos pos = context.getClickedPos();
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                pos = pos.relative(context.getClickedFace());
            }
            RainbowPortalEntity portal = TwistedLandsEntities.RAINBOW_PORTAL.create(level);
            if (portal == null) {
                return InteractionResult.FAIL;
            }
            portal.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0f, 0.0f);
            level.addFreshEntity(portal);
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.CONSUME;
        }
    }, "rainbow_portal");
    public static final List<Item> DREAM_WORLD = new ArrayList<>(List.of(CRYSTAL_COAL, PEACOCK_FEATHER, DEAD_IRUKANDJI,
            IRUKANDJI_ARROW, SHARK_FIN));

    static {
        for (String id : List.of("raw_jade", "jade_nugget", "jade_ingot", "raw_pink_tourmaline", "pink_tourmaline_nugget",
                "pink_tourmaline_ingot", "tiger_eye_scrap", "tiger_eye_ingot")) {
            DREAM_WORLD.add(register(new Item(new FabricItemSettings()), id));
        }
        for (CrystalMaterials.Tools tier : CrystalMaterials.Tools.values()) {
            int damage = tier.swordDamage - 1; // o jogador já soma 1
            FabricItemSettings settings = new FabricItemSettings().maxCount(1);
            DREAM_WORLD.add(register(new SwordItem(tier, damage, -2.4f, settings), tier.prefix + "_sword"));
            DREAM_WORLD.add(register(new PickaxeItem(tier, damage - 2, -2.8f, settings), tier.prefix + "_pickaxe"));
            DREAM_WORLD.add(register(new AxeItem(tier, damage - 1, -3.0f, settings), tier.prefix + "_axe"));
            DREAM_WORLD.add(register(new ShovelItem(tier, damage - 3, -3.0f, settings), tier.prefix + "_shovel"));
            DREAM_WORLD.add(register(new HoeItem(tier, damage - 2, -1.0f, settings), tier.prefix + "_hoe"));
        }
        for (CrystalMaterials.Armor material : CrystalMaterials.Armor.values()) {
            for (ArmorItem.Type type : ArmorItem.Type.values()) {
                DREAM_WORLD.add(register(new ArmorItem(material, type, new FabricItemSettings().maxCount(1)),
                        material.prefix + "_" + type.getName()));
            }
        }
        DREAM_WORLD.add(PEACOCK_FEATHER_BOOTS);
        DREAM_WORLD.add(CRYSTAL_APPLE);
        DREAM_WORLD.add(CRYSTAL_CORN);
    }

    // No addon os efeitos vinham sem partícula, mas com o ícone na tela.
    private static MobEffectInstance effect(MobEffect type, int ticks) {
        return new MobEffectInstance(type, ticks, 0, false, false, true);
    }

    public static void initialize() {
    }

    public static boolean isLongReachSword(ItemStack stack) {
        return stack.is(BIG_BERTHA)
                || stack.is(ROYAL_GUARDIAN)
                || stack.is(STELIX_BERTHA);
    }
}
