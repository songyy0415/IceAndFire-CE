#!/usr/bin/env python3
"""Apply manually-verified member mappings (from MojMap source inspection).

Each override: (yarn_class, yarn_member, moj_class, moj_member, kind, note)
"""
import os, json

HERE = os.path.dirname(os.path.abspath(__file__))

OVERRIDES = [
    # (yarn_class, yarn_member, moj_member, kind, note)
    ('net.minecraft.item.ItemStack', 'PACKET_CODEC', 'STREAM_CODEC', 'field',
     'ItemStack.java:149 public static final StreamCodec<RegistryFriendlyByteBuf, ItemStack> STREAM_CODEC'),
    ('net.minecraft.nbt.NbtElement', 'STRING_TYPE', 'TAG_STRING', 'field', 'Tag.java:19'),
    ('net.minecraft.network.codec.PacketCodecs', 'INTEGER', 'INT', 'field', 'ByteBufCodecs.java:81 StreamCodec<ByteBuf, Integer> INT'),
    ('net.minecraft.predicate.entity.EntityPredicates', 'EXCEPT_CREATIVE_OR_SPECTATOR', 'NO_CREATIVE_OR_SPECTATOR', 'field', 'EntitySelector.java:16'),
    ('net.minecraft.predicate.entity.EntityPredicates', 'EXCEPT_SPECTATOR', 'NO_SPECTATORS', 'field', 'EntitySelector.java:18'),
    ('net.minecraft.predicate.entity.EntityPredicates', 'VALID_ENTITY', 'ENTITY_STILL_ALIVE', 'field', 'EntitySelector.java:12'),
    ('net.minecraft.recipe.Ingredient', 'ALLOW_EMPTY_CODEC', 'CODEC', 'field', 'Ingredient.java:42 public static final Codec<Ingredient> CODEC = codec(true)'),
    ('net.minecraft.registry.RegistryOps', 'of', 'create', 'method', 'RegistryOps.java:21 create(DynamicOps, HolderLookup.Provider)'),
    ('net.minecraft.resource.featuretoggle.FeatureFlags', 'VANILLA_FEATURES', 'VANILLA_SET', 'field', 'FeatureFlags.java:14'),
    ('net.minecraft.util.Uuids', 'CODEC', 'CODEC', 'field', 'UUIDUtil.java:23 public static final Codec<UUID> CODEC'),
    ('net.minecraft.village.TradeOffers', 'PROFESSION_TO_LEVELED_TRADE', 'TRADES', 'field', 'VillagerTrades.java:79'),
    ('net.minecraft.world.TeleportTarget', 'SEND_TRAVEL_THROUGH_PORTAL_PACKET', 'PLAY_PORTAL_SOUND', 'field', 'DimensionTransition.java:20'),
    ('net.minecraft.world.TeleportTarget', 'NO_OP', 'DO_NOTHING', 'field', 'DimensionTransition.java:19'),
    ('net.minecraft.world.TeleportTarget', 'ADD_PORTAL_CHUNK_TICKET', 'PLACE_PORTAL_TICKET', 'field', 'DimensionTransition.java:21'),
    ('net.minecraft.block.entity.BlockEntity', 'createNbtWithIdentifyingData', 'saveWithFullMetadata', 'method', 'BlockEntity.java:97 saveWithFullMetadata(HolderLookup.Provider)'),
    ('net.minecraft.block.entity.BlockEntity', 'toInitialChunkDataNbt', 'getUpdateTag', 'method', 'BlockEntity.java:214 getUpdateTag(HolderLookup.Provider)'),
    ('net.minecraft.block.entity.LockableContainerBlockEntity', 'getContainerName', 'getDisplayName', 'method', 'BaseContainerBlockEntity.java:58 getDisplayName()'),
    ('net.minecraft.component.ComponentType', 'codec', 'codec', 'method', 'DataComponentType.java:33 Codec<T> codec()'),
    ('net.minecraft.component.ComponentType', 'packetCodec', 'streamCodec', 'method', 'DataComponentType.java:48 StreamCodec streamCodec()'),

    # ModelPart (rendering-critical)
    ('net.minecraft.client.model.ModelPart', 'pivotX', 'x', 'field', 'ModelPart.java:21 public float x'),
    ('net.minecraft.client.model.ModelPart', 'pivotY', 'y', 'field', 'ModelPart.java:22'),
    ('net.minecraft.client.model.ModelPart', 'pivotZ', 'z', 'field', 'ModelPart.java:23'),
    ('net.minecraft.client.model.ModelPart', 'pitch', 'xRot', 'field', 'ModelPart.java:24'),
    ('net.minecraft.client.model.ModelPart', 'yaw', 'yRot', 'field', 'ModelPart.java:25'),
    ('net.minecraft.client.model.ModelPart', 'roll', 'zRot', 'field', 'ModelPart.java:26'),
    # Particle
    ('net.minecraft.client.particle.Particle', 'velocityX', 'xd', 'field', 'Particle.java:30'),
    ('net.minecraft.client.particle.Particle', 'velocityY', 'yd', 'field', 'Particle.java:31'),
    ('net.minecraft.client.particle.Particle', 'velocityZ', 'zd', 'field', 'Particle.java:32'),
    ('net.minecraft.client.particle.Particle', 'gravityStrength', 'gravity', 'field', 'Particle.java:43'),
    # Entity fields
    ('net.minecraft.entity.Entity', 'prevX', 'xo', 'field', 'Entity.java:163 public double xo'),
    ('net.minecraft.entity.Entity', 'prevY', 'yo', 'field', 'Entity.java:164'),
    ('net.minecraft.entity.Entity', 'prevZ', 'zo', 'field', 'Entity.java:165'),
    ('net.minecraft.entity.Entity', 'lastRenderX', 'xOld', 'field', 'Entity.java:192'),
    ('net.minecraft.entity.Entity', 'lastRenderY', 'yOld', 'field', 'Entity.java:193'),
    ('net.minecraft.entity.Entity', 'lastRenderZ', 'zOld', 'field', 'Entity.java:194'),
    ('net.minecraft.entity.Entity', 'noClip', 'noPhysics', 'field', 'Entity.java:195'),
    ('net.minecraft.entity.Entity', 'age', 'tickCount', 'field', 'Entity.java:197'),
    # LivingEntity fields
    ('net.minecraft.entity.LivingEntity', 'bodyYaw', 'yBodyRot', 'field', 'LivingEntity.java:201'),
    ('net.minecraft.entity.LivingEntity', 'prevBodyYaw', 'yBodyRotO', 'field', 'LivingEntity.java:202'),
    ('net.minecraft.entity.LivingEntity', 'headYaw', 'yHeadRot', 'field', 'LivingEntity.java:203'),
    ('net.minecraft.entity.LivingEntity', 'prevHeadYaw', 'yHeadRotO', 'field', 'LivingEntity.java:204'),
    ('net.minecraft.entity.LivingEntity', 'attacker', 'lastHurtByMob', 'field', 'LivingEntity.java:231'),
    ('net.minecraft.entity.LivingEntity', 'handSwingProgress', 'attackAnim', 'field', 'LivingEntity.java:195'),
    ('net.minecraft.entity.LivingEntity', 'handSwingTicks', 'attackStrengthTicker', 'field', 'LivingEntity.java:196'),
    ('net.minecraft.entity.LivingEntity', 'forwardSpeed', 'zza', 'field', 'LivingEntity.java:220'),
    ('net.minecraft.entity.LivingEntity', 'sidewaysSpeed', 'xxa', 'field', 'LivingEntity.java:218'),
    # textures / rendering
    ('net.minecraft.client.texture.SpriteAtlasTexture', 'BLOCK_ATLAS_TEXTURE', 'LOCATION_BLOCKS', 'field', 'TextureAtlas.java:30'),
    ('net.minecraft.client.render.OverlayTexture', 'DEFAULT_UV', 'NO_OVERLAY', 'field', 'OverlayTexture.java:14'),
    ('net.minecraft.client.render.VertexFormats', 'POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL', 'NEW_ENTITY', 'field', 'DefaultVertexFormat.java:17'),
    ('net.minecraft.client.render.entity.LivingEntityRenderer', 'features', 'layers', 'field', 'LivingEntityRenderer.java:36'),
    ('net.minecraft.client.MinecraftClient', 'textRenderer', 'font', 'field', 'Minecraft.java:283'),
    ('net.minecraft.client.render.Camera', 'pos', 'position', 'field', 'Camera.java:36'),
    ('net.minecraft.client.util.math.MatrixStack', 'peek', 'last', 'method', 'PoseStack.java:64 last()'),
    ('net.minecraft.client.render.GameRenderer', 'ticks', 'tickCount', 'field', 'GameRenderer.java (renderTickCount)'),
    # NBT / network
    ('net.minecraft.nbt.NbtOps', 'EMPTY', 'INSTANCE', 'field', 'NbtOps.java:28'),
    ('net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket', 'PROJECTILE_HIT_PLAYER', 'ARROW_HIT_PLAYER', 'field', 'ClientboundGameEventPacket.java:20'),
    # world / level
    ('net.minecraft.world.World', 'isClient', 'isClientSide', 'field', 'Level.java:114'),
    # registry
    ('net.minecraft.registry.RegistryKey', 'registry', 'registry', 'record', 'ResourceKey.java:61'),
    ('net.minecraft.registry.RegistryKey', 'value', 'location', 'record', 'ResourceKey.java:57'),
    ('net.minecraft.registry.Registry', 'entry', 'getHolder', 'method', 'Registry.java:142'),
    ('net.minecraft.registry.entry.RegistryEntry', 'getType', 'kind', 'method', 'Holder.java:36'),
    ('net.minecraft.server.MinecraftServer', 'getRegistryManager', 'registryAccess', 'method', 'MinecraftServer.java registryAccess()'),
    # inventory / screen
    ('net.minecraft.screen.slot.Slot', 'id', 'index', 'field', 'Slot.java:14'),
    ('net.minecraft.screen.ScreenHandler', 'syncId', 'containerId', 'field', 'AbstractContainerMenu.java:54'),
    ('net.minecraft.entity.player.PlayerInventory', 'selectedSlot', 'selected', 'field', 'Inventory.java:36'),
    # mob
    ('net.minecraft.entity.mob.MobEntity', 'experiencePoints', 'xpReward', 'field', 'Mob.java:107'),
    ('net.minecraft.entity.mob.MobEntity', 'ambientSoundChance', 'ambientSoundTime', 'field', 'Mob.java:106'),
    # entity ai
    ('net.minecraft.entity.ai.control.MoveControl', 'targetX', 'wantedX', 'field', 'MoveControl.java:20'),
    ('net.minecraft.entity.ai.control.MoveControl', 'targetY', 'wantedY', 'field', 'MoveControl.java:21'),
    ('net.minecraft.entity.ai.control.MoveControl', 'targetZ', 'wantedZ', 'field', 'MoveControl.java:22'),
    ('net.minecraft.entity.ai.control.MoveControl', 'speed', 'speedModifier', 'field', 'MoveControl.java:23'),
    ('net.minecraft.entity.ai.control.MoveControl', 'forwardMovement', 'strafeForwards', 'field', 'MoveControl.java:24'),
    ('net.minecraft.entity.ai.control.MoveControl', 'sidewaysMovement', 'strafeRight', 'field', 'MoveControl.java:25'),
    ('net.minecraft.entity.ai.control.MoveControl', 'state', 'operation', 'field', 'MoveControl.java:26'),
    ('net.minecraft.entity.ai.control.LookControl', 'x', 'wantedX', 'field', 'LookControl.java:15'),
    ('net.minecraft.entity.ai.control.LookControl', 'y', 'wantedY', 'field', 'LookControl.java:16'),
    ('net.minecraft.entity.ai.control.LookControl', 'z', 'wantedZ', 'field', 'LookControl.java:17'),
    ('net.minecraft.entity.ai.pathing.EntityNavigation', 'speed', 'speedModifier', 'field', 'PathNavigation.java:37'),
    ('net.minecraft.entity.ai.pathing.EntityNavigation', 'tickCount', 'tick', 'field', 'PathNavigation.java:38'),
    ('net.minecraft.entity.ai.pathing.EntityNavigation', 'pathStartTime', 'lastStuckCheck', 'field', 'PathNavigation.java:39'),
    ('net.minecraft.entity.ai.pathing.EntityNavigation', 'currentNodeMs', 'timeoutTimer', 'field', 'PathNavigation.java:42'),
    ('net.minecraft.entity.ai.pathing.EntityNavigation', 'lastActiveTickMs', 'lastTimeoutCheck', 'field', 'PathNavigation.java:43'),
    ('net.minecraft.entity.ai.pathing.EntityNavigation', 'currentNodeTimeout', 'timeoutLimit', 'field', 'PathNavigation.java:44'),
    ('net.minecraft.entity.ai.pathing.EntityNavigation', 'inRecalculationCooldown', 'hasDelayedRecomputation', 'field', 'PathNavigation.java (hasDelayedRecomputation)'),
    ('net.minecraft.entity.ai.pathing.EntityNavigation', 'lastRecalculateTime', 'timeLastRecompute', 'field', 'PathNavigation.java (timeLastRecompute)'),
    # projectile
    ('net.minecraft.entity.projectile.PersistentProjectileEntity', 'pickupType', 'pickup', 'field', 'AbstractArrow.java:61'),
    ('net.minecraft.entity.projectile.PersistentProjectileEntity', 'stack', 'pickupItemStack', 'field', 'AbstractArrow.java:70'),
    # item
    ('net.minecraft.item.ItemStack', 'getName', 'getHoverName', 'method', 'ItemStack.java:698 getHoverName()'),
    # entity type / structure
    ('net.minecraft.entity.EntityType', 'getName', 'getDescription', 'method', 'EntityType.java:965 getDescription()'),
    ('net.minecraft.structure.StructurePieceType', 'register', 'setPieceId', 'method', 'StructurePieceType.java:27 setPieceId'),
    ('net.minecraft.client.gui.DrawContext', 'getVertexConsumers', 'bufferSource', 'method', 'GuiGraphics.java:119 bufferSource()'),
    # RotationAxis -> com.mojang.math.Axis
    ('net.minecraft.util.math.RotationAxis', 'NEGATIVE_X', 'XN', 'field', 'com.mojang.math.Axis.java:8'),
    ('net.minecraft.util.math.RotationAxis', 'POSITIVE_X', 'XP', 'field', 'Axis.java:9'),
    ('net.minecraft.util.math.RotationAxis', 'NEGATIVE_Y', 'YN', 'field', 'Axis.java:10'),
    ('net.minecraft.util.math.RotationAxis', 'POSITIVE_Y', 'YP', 'field', 'Axis.java:11'),
    ('net.minecraft.util.math.RotationAxis', 'NEGATIVE_Z', 'ZN', 'field', 'Axis.java:12'),
    ('net.minecraft.util.math.RotationAxis', 'POSITIVE_Z', 'ZP', 'field', 'Axis.java:13'),
    # DiffuseLighting -> com.mojang.blaze3d.platform.Lighting
    ('net.minecraft.client.render.DiffuseLighting', 'enableForLevel', 'setupLevel', 'method', 'Lighting.java:22'),
    ('net.minecraft.client.render.DiffuseLighting', 'disableForLevel', 'setupNetherLevel', 'method', 'Lighting.java:18'),
    ('net.minecraft.client.render.DiffuseLighting', 'disableGuiDepthLighting', 'setupForFlatItems', 'method', 'Lighting.java:26'),
    ('net.minecraft.client.render.DiffuseLighting', 'enableGuiDepthLighting', 'setupFor3DItems', 'method', 'Lighting.java:30'),
    ('net.minecraft.client.render.DiffuseLighting', 'method_34742', 'setupForEntityInInventory', 'method', 'Lighting.java:34'),
    ('net.minecraft.client.render.DiffuseLighting', 'method_56819', 'setupForEntityInInventory', 'method', 'Lighting.java:38'),
    # InventoryScreen render helpers
    ('net.minecraft.client.gui.screen.ingame.InventoryScreen', 'drawEntity#10', 'renderEntityInInventoryFollowsMouse', 'method', 'InventoryScreen.java:101'),
    ('net.minecraft.client.gui.screen.ingame.InventoryScreen', 'drawEntity#8', 'renderEntityInInventory', 'method', 'InventoryScreen.java:134'),
    # TradeOffers nested interface
    ('net.minecraft.village.TradeOffers', 'Factory', 'ItemListing', 'class', 'VillagerTrades.ItemListing (nested functional interface)'),
    # VertexFormats
    ('net.minecraft.client.render.VertexFormats', 'POSITION_COLOR', 'POSITION_COLOR', 'field', 'DefaultVertexFormat.java:33'),
]

def main():
    p = os.path.join(HERE, 'member_map.json')
    mm = json.load(open(p, encoding='utf-8'))
    applied = 0
    for yarn_cls, yarn_mem, moj_mem, kind, note in OVERRIDES:
        if yarn_cls not in mm:
            print(f'SKIP (class not in map): {yarn_cls}')
            continue
        entry = mm[yarn_cls]
        # arity-aware override: yarn_mem may be 'name#N' (match method with N params)
        arity = None
        if '#' in yarn_mem:
            yarn_mem, arity = yarn_mem.split('#')
            arity = int(arity)
        found = False
        for mem in entry['members']:
            yname = mem['yarn']
            if kind in ('method',):
                base = yname.split('(')[0] if '(' in yname else yname
                if arity is not None and '(' in yname:
                    params = yname.split('(')[1].rstrip(')')
                    n = len([p for p in params.split(',') if p.strip()]) if params.strip() else 0
                    if n != arity:
                        continue
            elif kind == 'field':
                base = yname.split(' : ')[0]
            else:
                base = yname
            if base == yarn_mem:
                mem['moj'] = moj_mem if kind in ('field','enum','record','class') else f'{moj_mem}(...)'
                mem['status'] = 'verified'
                mem['note'] = note
                found = True
                applied += 1
                break
        if not found:
            entry['members'].append({'kind': kind, 'yarn': yarn_mem, 'moj': moj_mem, 'status': 'verified', 'note': note})
            applied += 1
    json.dump(mm, open(p, 'w', encoding='utf-8'), indent=0)
    print(f'applied {applied} overrides')

if __name__ == '__main__':
    main()
