package com.lycoris.loconautics.client.ponder;

import com.lycoris.loconautics.content.hose.winch.HoseWinchBlock;
import com.lycoris.loconautics.content.hose.winch.HoseWinchBlockEntity;
import com.lycoris.loconautics.registry.LoconauticsRegistries;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class HoseScenes {

    private static final BlockPos STAND_MOUNT = new BlockPos(2, 1, 2);
    private static final BlockPos STAND = new BlockPos(2, 2, 2);
    private static final BlockPos STAND_REEL_NEAR = new BlockPos(3, 2, 2);
    private static final BlockPos STAND_COG_MID = new BlockPos(3, 1, 2);
    private static final BlockPos STAND_COG_LOW = new BlockPos(3, 0, 2);
    private static final BlockPos STAND_NOZZLE = new BlockPos(2, 1, 0);
    private static final BlockPos STAND_LEVER_MOUNT = new BlockPos(1, 1, 2);
    private static final BlockPos STAND_LEVER = new BlockPos(1, 2, 2);
    private static final Vec3 END_STAND_NEAR = new Vec3(2.5, 1.5, 1.5);
    private static final Vec3 END_STAND_HALF = new Vec3(2.5, 1.5, 1.0);
    private static final Vec3 END_STAND_OUT = new Vec3(2.5, 1.5, 0.5);
    private static final int HOSE_LEAD_TICKS = 1;
    private static final float SLOW_RPM = 8.0f;
    private static final float FAST_RPM = 24.0f;
    private static final float FREE_RPM = 32.0f;

    public static void winch(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = open(builder, "hose_winch", "Reeling Hoses using Hose Winches", STAND_COG_LOW);

        scene.world().showSection(util.select().position(STAND_MOUNT), Direction.DOWN);
        scene.idle(8);
        hosed(scene, STAND);
        scene.world().showSection(util.select().position(STAND), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(60)
                .text("Hose Winches attach flat to any block face")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(STAND));
        scene.idle(70);

        ElementLink<WorldSectionElement> nozzle =
                scene.world().showIndependentSection(util.select().position(STAND_NOZZLE), Direction.DOWN);
        scene.idle(8);
        hose(scene, STAND, END_STAND_OUT, 0.0f);
        scene.idle(10);
        showDrive(scene, util);
        scene.idle(10);
        setSpin(scene, util, -SLOW_RPM);
        scene.effects().rotationSpeedIndicator(STAND_REEL_NEAR);
        scene.overlay().showText(60)
                .text("Rotational Force will reel the hose in or out")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(STAND_REEL_NEAR));
        drag(scene, nozzle, new Vec3(0.0, 0.0, 1.0), STAND, END_STAND_OUT, END_STAND_NEAR, -SLOW_RPM, 60);
        scene.idle(10);

        setSpin(scene, util, SLOW_RPM);
        scene.effects().rotationDirectionIndicator(STAND_REEL_NEAR);
        scene.overlay().showText(70)
                .text("The direction of rotation decides if the hose is paid out or hauled in")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(STAND_REEL_NEAR));
        drag(scene, nozzle, new Vec3(0.0, 0.0, -1.0), STAND, END_STAND_NEAR, END_STAND_OUT, SLOW_RPM, 60);
        scene.idle(15);

        setSpin(scene, util, -FAST_RPM);
        scene.effects().rotationSpeedIndicator(STAND_REEL_NEAR);
        scene.overlay().showText(60)
                .text("A faster rotation reels the hose faster")
                .placeNearTarget()
                .pointAt(util.vector().topOf(STAND_REEL_NEAR));
        drag(scene, nozzle, new Vec3(0.0, 0.0, 1.0), STAND, END_STAND_OUT, END_STAND_NEAR, -FAST_RPM, 20);
        scene.idle(45);

        setSpin(scene, util, 0.0f);
        scene.overlay().showOutline(PonderPalette.RED, "winch", util.select().position(STAND), 70);
        scene.overlay().showText(70)
                .text("Breaking the winch snaps the hose and drops the Hose item")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .pointAt(util.vector().topOf(STAND));
        scene.idle(80);
    }

    public static void freeSpin(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = open(builder, "hose_winch_free", "Letting Hose Winches spin freely", STAND_COG_LOW);

        scene.world().showSection(util.select().position(STAND_MOUNT), Direction.DOWN);
        scene.idle(5);
        hosed(scene, STAND);
        scene.world().showSection(util.select().position(STAND), Direction.DOWN);
        scene.idle(5);
        showDrive(scene, util);
        ElementLink<WorldSectionElement> nozzle =
                scene.world().showIndependentSection(util.select().position(STAND_NOZZLE), Direction.DOWN);
        scene.world().moveSection(nozzle, new Vec3(0.0, 0.0, 1.0), 0);
        scene.idle(8);
        hose(scene, STAND, END_STAND_NEAR, 0.0f);
        scene.idle(10);
        scene.overlay().showText(70)
                .text("An unpowered Hose Winch only reels while Rotational Force drives it")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(STAND));
        scene.idle(80);

        scene.world().showSection(util.select().position(STAND_LEVER_MOUNT), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(STAND_LEVER), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(STAND_LEVER), Pointing.DOWN, 30)
                .rightClick();
        scene.idle(10);
        scene.world().toggleRedstonePower(util.select().position(STAND_LEVER));
        scene.world().modifyBlock(STAND, state -> state.setValue(BlockStateProperties.POWERED, true), false);
        scene.effects().indicateRedstone(STAND_LEVER);
        scene.idle(10);
        setSpin(scene, util, FREE_RPM);
        scene.effects().rotationSpeedIndicator(STAND_REEL_NEAR);
        scene.overlay().showText(60)
                .text("A Redstone signal lets the winch spin free and pay out hose as it is pulled")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(STAND));
        drag(scene, nozzle, new Vec3(0.0, 0.0, -0.5), STAND, END_STAND_NEAR, END_STAND_HALF, FREE_RPM, 60);
        scene.idle(5);

        scene.overlay().showText(60)
                .text("While paying out, the winch turns its shaft and generates Rotational Force")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(STAND_REEL_NEAR));
        drag(scene, nozzle, new Vec3(0.0, 0.0, -0.5), STAND, END_STAND_HALF, END_STAND_OUT, FREE_RPM, 60);
        scene.idle(5);

        setSpin(scene, util, -SLOW_RPM);
        scene.effects().rotationDirectionIndicator(STAND_REEL_NEAR);
        scene.overlay().showText(60)
                .text("Reeling the hose back in still takes Rotational Force")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(STAND));
        drag(scene, nozzle, new Vec3(0.0, 0.0, 1.0), STAND, END_STAND_OUT, END_STAND_NEAR, -SLOW_RPM, 60);
        setSpin(scene, util, 0.0f);
        scene.idle(15);
    }

    public static void connect(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = open(builder, "hose_connect", "Running a Hose", STAND_COG_LOW);

        scene.world().showSection(util.select().position(STAND_MOUNT), Direction.DOWN);
        scene.idle(8);
        scene.world().showSection(util.select().position(STAND), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(STAND_NOZZLE), Direction.DOWN);
        scene.idle(12);
        scene.overlay().showText(60)
                .text("A Hose joins a Hose Winch to a Hose Nozzle or a Hose Connector")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(STAND));
        scene.idle(70);

        scene.overlay().showControls(util.vector().topOf(STAND), Pointing.DOWN, 40)
                .rightClick()
                .withItem(hoseItem());
        scene.idle(30);
        scene.overlay().showText(60)
                .text("Right click the winch with a Hose, then the nozzle")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(STAND));
        scene.idle(70);

        scene.overlay().showLine(PonderPalette.GREEN, util.vector().topOf(STAND), END_STAND_OUT, 70);
        scene.overlay().showText(60)
                .text("A line previews the run while one end is selected")
                .placeNearTarget()
                .pointAt(util.vector().topOf(STAND_NOZZLE));
        scene.idle(70);

        scene.overlay().showControls(util.vector().topOf(STAND), Pointing.DOWN, 40)
                .rightClick()
                .whileSneaking()
                .withItem(hoseItem());
        scene.idle(30);
        scene.overlay().showText(60)
                .text("Sneak and Right click to drop the selection")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(STAND));
        scene.idle(70);

        scene.overlay().showControls(util.vector().topOf(STAND), Pointing.DOWN, 25)
                .rightClick()
                .withItem(hoseItem());
        scene.idle(20);
        scene.overlay().showControls(util.vector().topOf(STAND_NOZZLE), Pointing.DOWN, 25)
                .rightClick()
                .withItem(hoseItem());
        scene.idle(25);
        hosed(scene, STAND);
        hose(scene, STAND, END_STAND_OUT, 0.0f);
        scene.idle(10);
        scene.overlay().showText(70)
                .text("A hose can only be paid out up to its maximum length")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(STAND));
        scene.idle(80);
    }

    private static void showDrive(CreateSceneBuilder scene, SceneBuildingUtil util) {
        scene.world().restoreBlocks(util.select().position(STAND_COG_LOW));
        scene.world().showSection(util.select().position(STAND_REEL_NEAR), Direction.EAST);
        scene.idle(5);
        scene.world().showSection(util.select().position(STAND_COG_MID), Direction.EAST);
    }

    private static void setSpin(CreateSceneBuilder scene, SceneBuildingUtil util, float rpm) {
        scene.world().setKineticSpeed(util.select().position(STAND).add(util.select().position(STAND_REEL_NEAR))
                .add(util.select().position(STAND_COG_LOW)), rpm);
        scene.world().setKineticSpeed(util.select().position(STAND_COG_MID), -rpm);
    }

    private static void hosed(CreateSceneBuilder scene, BlockPos winch) {
        scene.world().modifyBlock(winch, state -> state.setValue(HoseWinchBlock.HAS_COIL, true), false);
        coil(scene, winch);
    }

    private static void coil(CreateSceneBuilder scene, BlockPos winch) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("HasStrand", true);
        scene.world().modifyBlockEntity(winch, HoseWinchBlockEntity.class,
                be -> be.readClient(tag, be.getLevel().registryAccess()));
    }

    private static ItemStack hoseItem() {
        return new ItemStack(LoconauticsRegistries.HOSE.get());
    }

    private static void drag(CreateSceneBuilder scene, ElementLink<WorldSectionElement> nozzle, Vec3 delta,
                             BlockPos winch, Vec3 from, Vec3 to, float speed, int ticks) {
        scene.world().moveSection(nozzle, delta, ticks);
        for (int i = 1; i <= ticks; i++) {
            hose(scene, winch, from.lerp(to, Math.min(1.0, (double) (i + HOSE_LEAD_TICKS) / ticks)), speed);
            scene.idle(1);
        }
    }

    private static void hose(CreateSceneBuilder scene, BlockPos winch, Vec3 end, float speed) {
        Vec3 start = new Vec3(0.5, 0.5, 0.5);
        Vec3 span = end.subtract(winch.getX(), winch.getY(), winch.getZ()).subtract(start);
        int segments = Math.max(2, (int) Math.ceil(span.length()));
        int[] packed = new int[(segments + 1) * 3];
        for (int i = 0; i <= segments; i++) {
            double t = (double) i / segments;
            double sag = -0.4 * Math.sin(Math.PI * t) * Math.min(1.0, span.length() / 3.0);
            double y = Math.max(start.y + span.y * t + sag, 1.1 - winch.getY());
            packed[i * 3] = Float.floatToIntBits((float) (start.x + span.x * t));
            packed[i * 3 + 1] = Float.floatToIntBits((float) y);
            packed[i * 3 + 2] = Float.floatToIntBits((float) (start.z + span.z * t));
        }
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("HasStrand", true);
        tag.putFloat("Length", (float) span.length());
        tag.putFloat("Speed", speed);
        tag.putIntArray("RopePts", packed);
        scene.world().modifyBlockEntity(winch, HoseWinchBlockEntity.class,
                be -> be.readClient(tag, be.getLevel().registryAccess()));
    }

    private static CreateSceneBuilder open(SceneBuilder builder, String id, String title) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(id, title);
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(5);
        return scene;
    }

    private static CreateSceneBuilder open(SceneBuilder builder, String id, String title, BlockPos sunk) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(id, title);
        scene.configureBasePlate(0, 0, 5);
        scene.world().setBlock(sunk, Blocks.SNOW_BLOCK.defaultBlockState(), false);
        scene.showBasePlate();
        scene.idle(5);
        return scene;
    }
}
