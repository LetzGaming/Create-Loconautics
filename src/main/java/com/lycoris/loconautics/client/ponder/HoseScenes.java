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
    private static final BlockPos F_LOOSE = new BlockPos(4, 1, 1);
    private static final Vec3 END_F_LOOSE = new Vec3(4.5, 1.5, 1.5);
    private static final Vec3 PULL_OUT_F = new Vec3(2.0, -1.1875, 0.0);
    private static final BlockPos WINCH_MOUNT = new BlockPos(2, 1, 4);
    private static final BlockPos WINCH = new BlockPos(2, 2, 4);
    private static final BlockPos PUMP = new BlockPos(2, 3, 4);
    private static final BlockPos SOURCE = new BlockPos(2, 4, 4);
    private static final BlockPos PUMP_COG = new BlockPos(3, 3, 4);
    private static final BlockPos PUMP_SHAFT_NEAR = new BlockPos(3, 2, 4);
    private static final BlockPos PUMP_SHAFT_FAR = new BlockPos(3, 1, 4);
    private static final BlockPos PUMP_SHAFT_BASE = new BlockPos(3, 0, 4);
    private static final BlockPos TOWER_TANK = new BlockPos(2, 1, 1);
    private static final BlockPos SOCKET = new BlockPos(2, 2, 1);
    private static final BlockPos DOCKED = new BlockPos(2, 3, 1);
    private static final BlockPos SOCKET_MOUNT = new BlockPos(2, 1, 2);
    private static final BlockPos SOCKET_ALONE = new BlockPos(2, 2, 2);
    private static final BlockPos NOZZLE_ALONE = new BlockPos(2, 3, 2);
    private static final BlockPos SCAFFOLD = new BlockPos(1, 1, 2);
    private static final BlockPos LEVER = new BlockPos(1, 2, 2);
    private static final BlockPos LEGS_FROM = new BlockPos(1, 1, 2);
    private static final BlockPos LEGS_TO = new BlockPos(3, 1, 2);
    private static final BlockPos FIREBOX = new BlockPos(1, 2, 2);
    private static final BlockPos BODY_FIRST = new BlockPos(2, 2, 2);
    private static final BlockPos BODY_LAST = new BlockPos(3, 2, 2);
    private static final BlockPos BOILER_SOCKET = new BlockPos(2, 3, 2);
    private static final BlockPos BOILER_NOZZLE = new BlockPos(2, 4, 2);
    private static final Vec3 END_LOOSE_SIDE = new Vec3(3.5, 1.5, 0.5);
    private static final Vec3 END_DOCKED = new Vec3(2.5, 2.6875, 1.5);
    private static final int HOSE_LEAD_TICKS = 1;
    private static final float SLOW_RPM = 8.0f;
    private static final float FAST_RPM = 24.0f;
    private static final float FREE_RPM = 32.0f;
    private static final Vec3 DOCK_DROP = new Vec3(0.0, -0.8125, 0.0);
    private static final Vec3 PULL_OUT = new Vec3(1.0, -1.1875, -1.0);
    private static final Vec3 PUSH_IN = new Vec3(-1.0, 1.1875, 1.0);

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

    public static void nozzle(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = open(builder, "hose_nozzle", "Handling Hose Nozzles");

        scene.world().showSection(util.select().position(WINCH_MOUNT), Direction.DOWN);
        scene.idle(5);
        hosed(scene, WINCH);
        scene.world().showSection(util.select().position(WINCH), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(TOWER_TANK, SOCKET), Direction.DOWN);
        scene.idle(10);
        ElementLink<WorldSectionElement> nozzle =
                scene.world().showIndependentSection(util.select().position(DOCKED), Direction.DOWN);
        scene.world().moveSection(nozzle, new Vec3(1.0, -2.0, -1.0), 0);
        scene.world().modifyBlock(DOCKED, state -> state.setValue(BlockStateProperties.POWERED, false), false);
        scene.idle(10);
        hose(scene, WINCH, END_LOOSE_SIDE, 0.0f);
        scene.idle(10);

        scene.overlay().showControls(util.vector().topOf(new BlockPos(3, 1, 0)), Pointing.DOWN, 40)
                .rightClick();
        scene.idle(25);
        scene.overlay().showText(60)
                .text("Right click a nozzle with empty hands to hold it")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(new BlockPos(3, 1, 0)));
        scene.idle(70);

        scene.overlay().showText(60)
                .text("Scrolling pushes it out or pulls it closer")
                .placeNearTarget()
                .pointAt(util.vector().topOf(new BlockPos(3, 1, 0)));
        scene.idle(70);

        scene.overlay().showText(60)
                .text("Holding the rotate key while moving the mouse turns it")
                .placeNearTarget()
                .pointAt(util.vector().topOf(new BlockPos(3, 1, 0)));
        scene.idle(70);

        scene.world().modifyBlock(DOCKED, state -> state.setValue(BlockStateProperties.POWERED, true), false);
        scene.overlay().showOutline(PonderPalette.GREEN, "socket", util.select().position(SOCKET), 60);
        scene.overlay().showText(60)
                .text("Near an open Nozzle Socket, a nozzle docks itself upright")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(SOCKET));
        drag(scene, nozzle, PUSH_IN, WINCH, END_LOOSE_SIDE, END_DOCKED, 0.0f, 20);
        scene.idle(50);

        scene.world().modifyBlock(DOCKED, state -> state.setValue(BlockStateProperties.POWERED, false), false);
        scene.overlay().showText(60)
                .text("A taut hose pulls a docked nozzle out of its socket")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(SOCKET));
        drag(scene, nozzle, PULL_OUT, WINCH, END_DOCKED, END_LOOSE_SIDE, 0.0f, 20);
        scene.idle(50);
    }

    public static void fluid(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = open(builder, "hose_fluid", "Pumping Fluids through Hoses", PUMP_SHAFT_BASE);
        Selection pump = util.select().position(PUMP);
        Selection drive = driveOf(util);

        scene.world().showSection(util.select().position(WINCH_MOUNT), Direction.DOWN);
        scene.idle(5);
        hosed(scene, WINCH);
        scene.world().showSection(util.select().position(WINCH), Direction.DOWN);
        scene.idle(5);
        scene.world().modifyBlock(SOCKET, state -> state.setValue(BlockStateProperties.OPEN, true), false);
        scene.world().showSection(util.select().fromTo(TOWER_TANK, SOCKET), Direction.DOWN);
        scene.idle(8);
        ElementLink<WorldSectionElement> nozzle =
                scene.world().showIndependentSection(util.select().position(DOCKED), Direction.DOWN);
        scene.world().moveSection(nozzle, DOCK_DROP, 0);
        scene.idle(8);
        hose(scene, WINCH, END_DOCKED, 0.0f);
        scene.world().showSection(pump, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(SOURCE), Direction.DOWN);
        scene.idle(5);
        scene.world().modifyBlockEntity(SOURCE, FluidTankBlockEntity.class, be -> be.getTankInventory()
                .fill(new FluidStack(Fluids.WATER, 24000), FluidAction.EXECUTE));
        scene.idle(10);
        scene.overlay().showText(60)
                .text("Hoses need a Mechanical Pump on the face opposite the mount")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(PUMP));
        scene.idle(70);

        scene.world().showSection(util.select().position(PUMP_COG), Direction.DOWN);
        scene.idle(5);
        scene.world().restoreBlocks(util.select().position(PUMP_SHAFT_BASE));
        scene.world().showSection(util.select().fromTo(PUMP_SHAFT_NEAR, PUMP_SHAFT_FAR), Direction.DOWN);
        scene.idle(15);
        pumpSpeed(scene, pump, drive, 64.0f);
        scene.world().propagatePipeChange(PUMP);
        scene.effects().rotationSpeedIndicator(PUMP_COG);
        scene.overlay().showText(60)
                .text("Pump speed sets how far the pressure reaches...")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(PUMP_COG));
        scene.idle(70);

        scene.overlay().showText(60)
                .text("...a hose only works once the pressure covers its whole length")
                .placeNearTarget()
                .pointAt(util.vector().topOf(WINCH));
        scene.idle(70);

        scene.overlay().showOutline(PonderPalette.GREEN, "socket", util.select().position(SOCKET), 70);
        scene.world().modifyBlockEntity(TOWER_TANK, FluidTankBlockEntity.class, be -> be.getTankInventory()
                .fill(new FluidStack(Fluids.WATER, 8000), FluidAction.EXECUTE));
        scene.overlay().showText(60)
                .text("Docked in a Nozzle Socket, everything pumped goes into it")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(SOCKET));
        scene.idle(70);

        pumpSpeed(scene, pump, drive, -64.0f);
        scene.world().propagatePipeChange(PUMP);
        scene.effects().rotationDirectionIndicator(PUMP_COG);
        scene.overlay().showText(60)
                .text("A reversed pump pulls Fluid back out through the hose")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(PUMP));
        scene.idle(70);

        pumpSpeed(scene, pump, drive, 64.0f);
        scene.world().propagatePipeChange(PUMP);
        scene.world().modifyBlock(DOCKED, state -> state.setValue(BlockStateProperties.POWERED, false), false);
        drag(scene, nozzle, PULL_OUT_F, WINCH, END_DOCKED, END_F_LOOSE, 0.0f, 20);
        scene.effects().emitParticles(new Vec3(4.5, 1.2, 1.5),
                scene.effects().simpleParticleEmitter(ParticleTypes.FALLING_WATER, new Vec3(0.0, -0.15, 0.0)),
                3.0f, 40);
        scene.overlay().showText(60)
                .text("A loose nozzle sprays what is pumped, and the jet pushes it back")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(F_LOOSE));
        scene.idle(70);
    }

    public static void socket(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = open(builder, "nozzle_socket", "Docking Nozzles in Nozzle Sockets");

        scene.world().showSection(util.select().position(SOCKET_MOUNT), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(SOCKET_ALONE), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(60)
                .text("Nozzle Sockets hold a nozzle upright and take its Fluid")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(SOCKET_ALONE));
        scene.idle(70);

        scene.overlay().showControls(util.vector().topOf(SOCKET_ALONE), Pointing.DOWN, 30)
                .rightClick();
        scene.idle(10);
        scene.world().modifyBlock(SOCKET_ALONE, state -> state.setValue(BlockStateProperties.OPEN, true), false);
        scene.overlay().showText(50)
                .text("Right click the lid to open it")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(SOCKET_ALONE));
        scene.idle(55);

        ElementLink<WorldSectionElement> nozzle =
                scene.world().showIndependentSection(util.select().position(NOZZLE_ALONE), Direction.DOWN);
        scene.idle(15);
        scene.world().moveSection(nozzle, DOCK_DROP, 12);
        scene.overlay().showText(60)
                .text("A nozzle that comes near an open socket docks itself")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(SOCKET_ALONE));
        scene.idle(70);

        scene.overlay().showText(60)
                .text("Alone, a socket stores Water in its own tank")
                .placeNearTarget()
                .pointAt(util.vector().topOf(SOCKET_ALONE));
        scene.idle(70);

        scene.world().setBlock(SOCKET_MOUNT, AllBlocks.FLUID_TANK.getDefaultState(), true);
        scene.idle(5);
        scene.world().modifyBlockEntity(SOCKET_MOUNT, FluidTankBlockEntity.class, be -> be.getTankInventory()
                .fill(new FluidStack(Fluids.WATER, 8000), FluidAction.EXECUTE));
        scene.overlay().showText(60)
                .text("With a Fluid Tank behind it, the Fluid goes there instead")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(SOCKET_MOUNT));
        scene.idle(70);

        scene.world().showSection(util.select().position(SCAFFOLD), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(LEVER), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(LEVER), Pointing.DOWN, 30)
                .rightClick();
        scene.idle(10);
        scene.world().toggleRedstonePower(util.select().position(LEVER));
        scene.effects().indicateRedstone(LEVER);
        scene.world().moveSection(nozzle, new Vec3(0.0, 1.0, 0.0), 8);
        scene.overlay().showText(60)
                .text("A Redstone signal shuts docking and pops the nozzle out")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(SOCKET_ALONE));
        scene.idle(70);
    }

    public static void boiler(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = open(builder, "nozzle_socket_boiler", "Watering Boilers through Nozzle Sockets");

        scene.world().showSection(util.select().fromTo(LEGS_FROM, LEGS_TO), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(FIREBOX), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(BODY_FIRST, BODY_LAST), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(BOILER_SOCKET), Direction.DOWN);
        scene.idle(10);
        scene.world().modifyBlock(BOILER_SOCKET, state -> state.setValue(BlockStateProperties.OPEN, true), false);
        scene.overlay().showText(60)
                .text("A Nozzle Socket touching a Boiler Body is a water inlet")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(BOILER_SOCKET));
        scene.idle(70);

        ElementLink<WorldSectionElement> nozzle =
                scene.world().showIndependentSection(util.select().position(BOILER_NOZZLE), Direction.DOWN);
        scene.idle(15);
        scene.world().moveSection(nozzle, new Vec3(0.0, -0.3125, 0.0), 12);
        scene.idle(15);
        scene.overlay().showLine(PonderPalette.BLUE, new Vec3(2.5, 4.2, 2.5), new Vec3(2.5, 2.6, 2.5), 60);
        scene.overlay().showText(60)
                .text("A docked nozzle pours its Water through the socket into the boiler")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(BOILER_SOCKET));
        scene.idle(70);

        scene.world().hideIndependentSection(nozzle, Direction.UP);
        scene.idle(10);
        scene.overlay().showOutline(PonderPalette.BLUE, "socket", util.select().position(BOILER_SOCKET), 60);
        scene.overlay().showText(60)
                .text("Without a nozzle the socket keeps Water in its own tank...")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(BOILER_SOCKET));
        scene.idle(70);

        scene.overlay().showOutline(PonderPalette.BLUE, "firebox", util.select().position(FIREBOX), 60);
        scene.overlay().showLine(PonderPalette.BLUE, new Vec3(2.5, 3.4, 2.5), new Vec3(1.5, 2.6, 2.5), 60);
        scene.overlay().showText(60)
                .text("...and the Firebox draws from it until the boiler is full")
                .placeNearTarget()
                .pointAt(util.vector().topOf(FIREBOX));
        scene.idle(70);

        scene.overlay().showOutline(PonderPalette.RED, "body", util.select().fromTo(BODY_FIRST, BODY_LAST), 60);
        scene.overlay().showText(60)
                .text("A Boiler Body only takes in Water")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .pointAt(util.vector().topOf(new BlockPos(3, 2, 2)));
        scene.idle(70);
    }

    private static Selection driveOf(SceneBuildingUtil util) {
        return util.select().position(PUMP_COG).add(util.select().fromTo(PUMP_SHAFT_BASE, PUMP_SHAFT_NEAR));
    }

    private static void pumpSpeed(CreateSceneBuilder scene, Selection pump, Selection drive, float speed) {
        scene.world().setKineticSpeed(pump, speed);
        scene.world().setKineticSpeed(drive, -speed);
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
