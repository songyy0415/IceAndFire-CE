package com.iafenvoy.iceandfire.compat.ponder;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.FireDragonEntity;
import com.iafenvoy.iceandfire.entity.IceDragonEntity;
import com.iafenvoy.iceandfire.entity.LightningDragonEntity;
import com.iafenvoy.iceandfire.registry.IafBlocks;
import com.iafenvoy.iceandfire.registry.IafDragonTypes;
import com.zurrtum.create.client.ponder.api.registration.PonderPlugin;
import com.zurrtum.create.client.ponder.api.registration.PonderSceneRegistrationHelper;
import com.zurrtum.create.client.ponder.api.registration.PonderTagRegistrationHelper;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class IceAndFirePonderPlugin implements PonderPlugin {
    private static final Identifier DRAGON_TAG_ID = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "dragon_forge");

    @Override
    public @NotNull String getModId() {
        return IceAndFire.MOD_ID;
    }

    @Override
    public void registerScenes(@NotNull PonderSceneRegistrationHelper<Identifier> helper) {
        helper.forComponents(IafBlocks.DRAGONFORGE_FIRE_CORE_DISABLED.getId())
                .addStoryBoard(DragonForgeStoryBoard.id(IafDragonTypes.FIRE), new DragonForgeStoryBoard<>(IafDragonTypes.FIRE::createEntity, FireDragonEntity.class));
        helper.forComponents(IafBlocks.DRAGONFORGE_ICE_CORE_DISABLED.getId())
                .addStoryBoard(DragonForgeStoryBoard.id(IafDragonTypes.ICE), new DragonForgeStoryBoard<>(IafDragonTypes.ICE::createEntity, IceDragonEntity.class));
        helper.forComponents(IafBlocks.DRAGONFORGE_LIGHTNING_CORE_DISABLED.getId())
                .addStoryBoard(DragonForgeStoryBoard.id(IafDragonTypes.LIGHTNING), new DragonForgeStoryBoard<>(IafDragonTypes.LIGHTNING::createEntity, LightningDragonEntity.class));
    }

    @Override
    public void registerTags(@NotNull PonderTagRegistrationHelper<Identifier> helper) {
        helper.registerTag(DRAGON_TAG_ID)
                .addToIndex()
                .item(IafBlocks.DRAGONFORGE_FIRE_CORE_DISABLED.get(), true, false)
                .title("Dragon Forge")
                .description("Dragon Forge")
                .register();
        helper.addToTag(DRAGON_TAG_ID)
                .add(IafBlocks.DRAGONFORGE_FIRE_CORE_DISABLED.getId())
                .add(IafBlocks.DRAGONFORGE_ICE_CORE_DISABLED.getId())
                .add(IafBlocks.DRAGONFORGE_LIGHTNING_CORE_DISABLED.getId());
    }

    public static void init() {
        PonderIndex.addPlugin(new IceAndFirePonderPlugin());
    }
}
