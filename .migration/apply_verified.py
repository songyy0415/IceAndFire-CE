#!/usr/bin/env python3
"""Apply manually-verified Yarn->MojMap class mappings into class_map.json."""
import os, json

HERE = os.path.dirname(os.path.abspath(__file__))

VERIFIED = {
    # yarn -> mojmap (all confirmed against MojMap source)
    'net.minecraft.block.SpreadableBlock': 'net.minecraft.world.level.block.SpreadingSnowyDirtBlock',
    'net.minecraft.block.WallBlock': 'net.minecraft.world.level.block.WallBlock',
    'net.minecraft.enchantment.EnchantmentHelper': 'net.minecraft.world.item.enchantment.EnchantmentHelper',
    'net.minecraft.entity.EntityData': 'net.minecraft.world.entity.SpawnGroupData',
    'net.minecraft.entity.ai.goal.ActiveTargetGoal': 'net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal',
    'net.minecraft.entity.ai.goal.AttackWithOwnerGoal': 'net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal',
    'net.minecraft.entity.ai.goal.FleeEntityGoal': 'net.minecraft.world.entity.ai.goal.AvoidEntityGoal',
    'net.minecraft.entity.ai.goal.TrackOwnerAttackerGoal': 'net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal',
    'net.minecraft.entity.ai.pathing.LandPathNodeMaker': 'net.minecraft.world.level.pathfinder.WalkNodeEvaluator',
    'net.minecraft.entity.mob.AbstractSkeletonEntity': 'net.minecraft.world.entity.monster.AbstractSkeleton',
    'net.minecraft.entity.mob.HostileEntity': 'net.minecraft.world.entity.monster.Monster',
    'net.minecraft.entity.mob.Monster': 'net.minecraft.world.entity.monster.Enemy',
    'net.minecraft.entity.mob.WaterCreatureEntity': 'net.minecraft.world.entity.animal.WaterAnimal',
    'net.minecraft.entity.mob.WitherSkeletonEntity': 'net.minecraft.world.entity.monster.WitherSkeleton',
    'net.minecraft.entity.passive.PolarBearEntity': 'net.minecraft.world.entity.animal.PolarBear',
    'net.minecraft.entity.projectile.ProjectileUtil': 'net.minecraft.world.entity.projectile.ProjectileUtil',
    'net.minecraft.loot.function.LootFunctionType': 'net.minecraft.world.level.storage.loot.functions.LootItemFunctionType',
    'net.minecraft.network.packet.CustomPayload': 'net.minecraft.network.protocol.common.custom.CustomPacketPayload',
    'net.minecraft.screen.PropertyDelegate': 'net.minecraft.world.inventory.ContainerData',
    'net.minecraft.sound.BlockSoundGroup': 'net.minecraft.world.level.block.SoundType',
    'net.minecraft.structure.StructureContext': 'net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext',
    'net.minecraft.util.ActionResult': 'net.minecraft.world.InteractionResult',
    'net.minecraft.util.math.Position': 'net.minecraft.core.Position',
    'net.minecraft.util.math.Vec2f': 'net.minecraft.world.phys.Vec2',
    'net.minecraft.util.math.intprovider.UniformIntProvider': 'net.minecraft.util.valueproviders.UniformInt',
    'net.minecraft.util.math.intprovider.BiasedToBottomIntProvider': 'net.minecraft.util.valueproviders.BiasedToBottomInt',
    'net.minecraft.world.TeleportTarget': 'net.minecraft.world.level.portal.DimensionTransition',
    'net.minecraft.world.WorldAccess': 'net.minecraft.world.level.LevelAccessor',
    'net.minecraft.world.gen.feature.util.FeatureContext': 'net.minecraft.world.level.levelgen.feature.FeaturePlaceContext',
    # client classes (verified against the now-complete MojMap source)
    'net.minecraft.client.option.GameOptions': 'net.minecraft.client.Options',
    'net.minecraft.client.render.VertexConsumer': 'com.mojang.blaze3d.vertex.VertexConsumer',
    'net.minecraft.client.render.entity.model.HorseEntityModel': 'net.minecraft.client.model.HorseModel',
}

def main():
    p = os.path.join(HERE, 'class_map.json')
    cm = json.load(open(p, encoding='utf-8'))
    added = 0
    for y, m in VERIFIED.items():
        if y not in cm:
            cm[y] = m
            added += 1
        elif cm[y] != m:
            print(f'WARN: {y} already mapped to {cm[y]}, not {m}')
    json.dump(cm, open(p, 'w', encoding='utf-8'), indent=0, sort_keys=True)
    print(f'added {added} verified mappings; total class_map size: {len(cm)}')

if __name__ == '__main__':
    main()
