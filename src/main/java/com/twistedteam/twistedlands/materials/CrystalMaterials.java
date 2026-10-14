package com.twistedteam.twistedlands.materials;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.twistedteam.twistedlands.TwistedLands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Map;

/**
 * Materiais da Dream World. Durabilidade, velocidade e dano vêm do addon, que copia a escada do vanilla
 * (madeira, pedra, ferro, diamante, netherite), então o nível de mineração segue a mesma escada.
 * As receitas também ficam aqui, montadas em código em vez de um JSON por receita.
 */
public final class CrystalMaterials {
    private CrystalMaterials() {
    }

    /**
     * Chamado pelo RecipeManagerMixin antes de carregar as receitas. Um JSON com o mesmo id num datapack ganha.
     * Itens sem namespace são do mod; "a|b" aceita qualquer um dos dois.
     */
    public static void addRecipes(Map<ResourceLocation, JsonElement> recipes) {
        shaped(recipes, "crystal_wood_planks", "building", "crystal_wood_planks", 4, new String[] {"A"},
                'A', "crystal_wood_log|stripped_crystal_wood_log");
        shaped(recipes, "red_crystal_wood_planks", "building", "red_crystal_wood_planks", 4, new String[] {"A"},
                'A', "red_crystal_wood_log|stripped_red_crystal_wood_log");
        shaped(recipes, "crystal_crafting_table", "misc", "crystal_crafting_table", 1, new String[] {"##", "##"},
                '#', "crystal_wood_planks|red_crystal_wood_planks");
        shaped(recipes, "crystal_stick", "misc", "crystal_stick", 4, new String[] {"#", "#"},
                '#', "crystal_wood_planks|red_crystal_wood_planks");
        shaped(recipes, "crystal_furnace", "misc", "crystal_furnace", 1, new String[] {"###", "# #", "###"},
                '#', "kyanite");
        crystalShaped(recipes, "tiger_eye_ingot", "misc", "tiger_eye_ingot", 1, new String[] {"SSS", "S##", "## "},
                'S', "tiger_eye_scrap", '#', "pink_tourmaline_ingot");

        storage(recipes, "crystal_coal", "crystal_coal_block");
        storage(recipes, "jade_ingot", "jade_block");
        storage(recipes, "pink_tourmaline_ingot", "pink_tourmaline_block");
        storage(recipes, "tiger_eye_ingot", "tigers_eye_block");
        storage(recipes, "jade_nugget", "jade_ingot");
        storage(recipes, "pink_tourmaline_nugget", "pink_tourmaline_ingot");

        for (Tools tier : Tools.values()) {
            String[][] patterns = {{"A", "A", "B"}, {"AAA", " B ", " B "}, {"AA", "AB", " B"}, {"A", "B", "B"}, {"AA", " B", " B"}};
            String[] tools = {"sword", "pickaxe", "axe", "shovel", "hoe"};
            for (int i = 0; i < tools.length; i++) {
                String id = tier.prefix + "_" + tools[i];
                crystalShaped(recipes, id, "equipment", id, 1, patterns[i], 'A', tier.repair, 'B', "crystal_stick");
            }
        }
        for (Armor material : Armor.values()) {
            String[][] patterns = {{"###", "# #"}, {"# #", "###", "###"}, {"###", "# #", "# #"}, {"# #", "# #"}};
            for (ArmorItem.Type type : ArmorItem.Type.values()) {
                String id = material.prefix + "_" + type.getName();
                crystalShaped(recipes, id, "equipment", id, 1, patterns[type.ordinal()], '#', material.repair);
            }
        }

        smelting(recipes, "raw_jade", "jade_ingot");
        smelting(recipes, "raw_pink_tourmaline", "pink_tourmaline_ingot");
        smelting(recipes, "tigers_eye_debris", "tiger_eye_scrap");
        smelting(recipes, "minecraft:beef", "minecraft:cooked_beef");
        smelting(recipes, "peacock_raw", "peacock_cooked");
        cooking(recipes, "peacock_raw", "peacock_cooked");

        shaped(recipes, "dream_bed", "misc", "dream_bed", 1, new String[] {"#", "B"},
                '#', "dead_rainbow_ant", 'B', "#minecraft:beds");
        crystalShaped(recipes, "irukandji_arrow", "equipment", "irukandji_arrow", 6, new String[] {"D", "S", "P"},
                'D', "dead_irukandji", 'S', "crystal_stick", 'P', "peacock_feather");
        shaped(recipes, "crystal_critter_cage_empty", "equipment", "crystal_critter_cage_empty", 1,
                new String[] {"#S#", "S S", "#S#"}, '#', "pink_tourmaline_ingot", 'S', "crystal_stick");
        shaped(recipes, "peacock_feather_boots", "equipment", "peacock_feather_boots", 1, new String[] {"##", "##"},
                '#', "peacock_feather");
        shaped(recipes, "emerald_peacock", "misc", "emerald_peacock", 1, new String[] {"###", "#A#", "###"},
                '#', "minecraft:emerald", 'A', "peacock_cooked");
    }

    // 9 itens viram o bloco (só na Crystal Crafting Table) e o bloco volta a ser 9 itens (nas duas mesas).
    private static void storage(Map<ResourceLocation, JsonElement> recipes, String item, String block) {
        crystalShaped(recipes, block + "_from_" + item, "building", block, 1, new String[] {"###", "###", "###"}, '#', item);
        shaped(recipes, item + "_from_" + block, "misc", item, 9, new String[] {"#"}, '#', block);
    }

    private static void shaped(Map<ResourceLocation, JsonElement> recipes, String id, String category, String result,
                               int count, String[] pattern, Object... key) {
        shapedOfType(recipes, "minecraft:crafting_shaped", id, category, result, count, pattern, key);
    }

    // Receitas que no addon só têm a tag "crystal": não saem na crafting table vanilla nem no inventário.
    private static void crystalShaped(Map<ResourceLocation, JsonElement> recipes, String id, String category, String result,
                                      int count, String[] pattern, Object... key) {
        shapedOfType(recipes, "twisted:crystal_shaped", id, category, result, count, pattern, key);
    }

    private static void shapedOfType(Map<ResourceLocation, JsonElement> recipes, String type, String id, String category,
                                     String result, int count, String[] pattern, Object... key) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.addProperty("category", category);
        JsonArray rows = new JsonArray();
        for (String row : pattern) rows.add(row);
        json.add("pattern", rows);
        JsonObject keys = new JsonObject();
        for (int i = 0; i < key.length; i += 2) {
            JsonArray options = new JsonArray();
            for (String item : ((String) key[i + 1]).split("\\|")) options.add(item(item));
            keys.add(String.valueOf(key[i]), options);
        }
        json.add("key", keys);
        JsonObject output = item(result);
        output.addProperty("count", count);
        json.add("result", output);
        recipes.putIfAbsent(TwistedLands.id(id), json);
    }

    // Receita da Crystal Furnace. No addon era instantânea; aqui é 50% mais rápida que a fornalha comum (200 ticks).
    private static void smelting(Map<ResourceLocation, JsonElement> recipes, String input, String result) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "twisted:crystal_smelting");
        json.add("ingredient", item(input));
        json.addProperty("result", qualify(result));
        json.addProperty("experience", 0.7f);
        json.addProperty("cookingtime", 133);
        recipes.putIfAbsent(TwistedLands.id("crystal_furnace/" + new ResourceLocation(qualify(result)).getPath()), json);
    }

    // Fornalha, defumador e fogueira, com os tempos e o XP do frango.
    private static void cooking(Map<ResourceLocation, JsonElement> recipes, String input, String result) {
        String[][] types = {{"smelting", "200"}, {"smoking", "100"}, {"campfire_cooking", "600"}};
        for (String[] type : types) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "minecraft:" + type[0]);
            json.addProperty("category", "food");
            json.add("ingredient", item(input));
            json.addProperty("result", qualify(result));
            json.addProperty("experience", 0.35f);
            json.addProperty("cookingtime", Integer.parseInt(type[1]));
            recipes.putIfAbsent(TwistedLands.id(result + "_from_" + type[0]), json);
        }
    }

    // "#namespace:tag" vira ingrediente de tag.
    private static JsonObject item(String id) {
        JsonObject json = new JsonObject();
        if (id.startsWith("#")) {
            json.addProperty("tag", id.substring(1));
        } else {
            json.addProperty("item", qualify(id));
        }
        return json;
    }

    private static String qualify(String id) {
        return id.contains(":") ? id : TwistedLands.MOD_ID + ":" + id;
    }

    private static Ingredient ingredient(String id) {
        return Ingredient.of(BuiltInRegistries.ITEM.get(TwistedLands.id(id)));
    }

    public enum Tools implements Tier {
        CRYSTAL_WOOD("crystal_wood", 0, 60, 2, 4, "crystal_wood_planks"),
        KYANITE("kyanite", 1, 132, 4, 5, "kyanite"),
        PINK_TOURMALINE("pink_tourmaline", 2, 250, 6, 6, "pink_tourmaline_ingot"),
        TIGERS_EYE("tigers_eye", 3, 1562, 8, 8, "tiger_eye_ingot"),
        JADE("jade", 4, 2032, 9, 7, "jade_ingot");

        public final String prefix;
        private final int level;
        private final int uses;
        private final float speed;
        // Dano total da espada no addon. As outras ferramentas tiram 1 (machado), 2 (picareta e enxada) ou 3 (pá).
        public final int swordDamage;
        private final String repair;

        Tools(String prefix, int level, int uses, float speed, int swordDamage, String repair) {
            this.prefix = prefix;
            this.level = level;
            this.uses = uses;
            this.speed = speed;
            this.swordDamage = swordDamage;
            this.repair = repair;
        }

        @Override public int getUses() { return uses; }
        @Override public float getSpeed() { return speed; }
        @Override public float getAttackDamageBonus() { return 0; }
        @Override public int getLevel() { return level; }
        @Override public int getEnchantmentValue() { return 14; }
        @Override public Ingredient getRepairIngredient() { return ingredient(repair); }
    }

    public enum Armor implements ArmorMaterial {
        // Proteção na ordem do ArmorItem.Type: capacete, peitoral, calças, botas.
        PINK_TOURMALINE("pink_tourmaline", 250, new int[] {2, 6, 5, 2}, "pink_tourmaline_ingot"),
        TIGERS_EYE("tigers_eye", 2032, new int[] {6, 11, 9, 6}, "tiger_eye_ingot"),
        JADE("jade", 2032, new int[] {3, 8, 6, 3}, "jade_ingot");

        public final String prefix;
        // No addon todas as peças têm a mesma durabilidade.
        private final int durability;
        private final int[] protection;
        private final String repair;

        Armor(String prefix, int durability, int[] protection, String repair) {
            this.prefix = prefix;
            this.durability = durability;
            this.protection = protection;
            this.repair = repair;
        }

        @Override public int getDurabilityForType(ArmorItem.Type type) { return durability; }
        @Override public int getDefenseForType(ArmorItem.Type type) { return protection[type.ordinal()]; }
        @Override public int getEnchantmentValue() { return 9; }
        @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_DIAMOND; }
        @Override public Ingredient getRepairIngredient() { return ingredient(repair); }
        // Textura em textures/models/armor/<prefix>_layer_1.png e _layer_2.png.
        @Override public String getName() { return TwistedLands.MOD_ID + ":" + prefix; }
        @Override public float getToughness() { return 0; }
        @Override public float getKnockbackResistance() { return 0; }
    }
}
