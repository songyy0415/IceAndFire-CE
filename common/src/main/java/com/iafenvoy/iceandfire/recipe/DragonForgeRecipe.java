package com.iafenvoy.iceandfire.recipe;

import com.iafenvoy.iceandfire.item.block.entity.DragonForgeBlockEntity;
import com.iafenvoy.iceandfire.registry.IafRecipeSerializers;
import com.iafenvoy.iceandfire.registry.IafRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

public class DragonForgeRecipe implements Recipe<DragonForgeBlockEntity.DragonForgeRecipeInput> {
    private final Ingredient input;
    private final Ingredient blood;
    private final ItemStackTemplate result;
    private final String dragonType;
    private final int cookTime;

    public DragonForgeRecipe(Ingredient input, Ingredient blood, ItemStackTemplate result, String dragonType, int cookTime) {
        this.input = input;
        this.blood = blood;
        this.result = result;
        this.dragonType = dragonType;
        this.cookTime = cookTime;
    }

    public Ingredient getInput() {
        return this.input;
    }

    public Ingredient getBlood() {
        return this.blood;
    }

    public int getCookTime() {
        return this.cookTime;
    }

    public String getDragonType() {
        return this.dragonType;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean matches(DragonForgeBlockEntity.DragonForgeRecipeInput inv, Level worldIn) {
        return this.input.test(inv.getStack(0)) && this.blood.test(inv.getStack(1)) && this.dragonType.equals(inv.getTypeID());
    }

    @Override
    public ItemStack assemble(DragonForgeBlockEntity.DragonForgeRecipeInput input) {
        return this.result.create();
    }

    public boolean isValidInput(ItemStack stack) {
        return this.input.test(stack);
    }

    public boolean isValidBlood(ItemStack blood) {
        return this.blood.test(blood);
    }

    public ItemStack getResultItem() {
        return this.result.create();
    }

    public ItemStackTemplate getResultTemplate() {
        return this.result;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(List.of(this.input, this.blood));
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return new RecipeBookCategory();
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<? extends Recipe<DragonForgeBlockEntity.DragonForgeRecipeInput>> getSerializer() {
        return (RecipeSerializer<? extends Recipe<DragonForgeBlockEntity.DragonForgeRecipeInput>>) (RecipeSerializer<?>) IafRecipeSerializers.DRAGONFORGE_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<DragonForgeBlockEntity.DragonForgeRecipeInput>> getType() {
        return IafRecipes.DRAGON_FORGE_TYPE.get();
    }

    public static final MapCodec<DragonForgeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.fieldOf("input").forGetter(DragonForgeRecipe::getInput),
            Ingredient.CODEC.fieldOf("blood").forGetter(DragonForgeRecipe::getBlood),
            ItemStackTemplate.MAP_CODEC.fieldOf("result").forGetter(DragonForgeRecipe::getResultTemplate),
            Codec.STRING.fieldOf("dragonType").forGetter(DragonForgeRecipe::getDragonType),
            Codec.INT.fieldOf("cookTime").forGetter(DragonForgeRecipe::getCookTime)
    ).apply(i, DragonForgeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DragonForgeRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, DragonForgeRecipe::getInput,
            Ingredient.CONTENTS_STREAM_CODEC, DragonForgeRecipe::getBlood,
            ItemStackTemplate.STREAM_CODEC, DragonForgeRecipe::getResultTemplate,
            ByteBufCodecs.STRING_UTF8, DragonForgeRecipe::getDragonType,
            ByteBufCodecs.INT, DragonForgeRecipe::getCookTime,
            DragonForgeRecipe::new
    );

    public static final RecipeSerializer<DragonForgeRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
