package com.example.pigbuilder.entity;

import java.util.ArrayList;
import java.util.List;

import com.example.pigbuilder.Config;
import com.example.pigbuilder.Smasher;
import com.example.pigbuilder.item.AssemblerItem;
import com.example.pigbuilder.registry.ModBlocks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A rigid block structure that moves as one entity.
 * Entity position = bottom-centre of the structure's bounding box.
 * Physics (server only): gravity, wheel drive, propeller lift, bulldozing.
 */
public class ContraptionEntity extends Entity {
    /** Compact "x,y,z,stateId;" list. Only used for client sync; saving uses codecs. */
    private static final EntityDataAccessor<String> DATA =
            SynchedEntityData.defineId(ContraptionEntity.class, EntityDataSerializers.STRING);

    public record Cell(BlockPos pos, BlockState state) {
        public static final Codec<Cell> CODEC = RecordCodecBuilder.create(i -> i.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(Cell::pos),
                BlockState.CODEC.fieldOf("state").forGetter(Cell::state)
        ).apply(i, Cell::new));
        public static final Codec<List<Cell>> LIST = CODEC.listOf();
    }

    private List<Cell> cells = List.of();
    private int sizeX, sizeY, sizeZ, wheels, props;

    private int controlMove;
    private boolean controlUp;
    private int controlTick = -1000;
    private int bulldozeCooldown;

    public ContraptionEntity(EntityType<? extends ContraptionEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true; // we do our own collision
    }

    // ---------------------------------------------------------------- structure

    public List<Cell> getCells() { return cells; }
    public int getSizeX() { return sizeX; }
    public int getSizeY() { return sizeY; }
    public int getSizeZ() { return sizeZ; }
    public int getWheels() { return wheels; }
    public int getProps() { return props; }

    /** Server: set the structure and sync it to clients. */
    public void setStructure(List<Cell> list) {
        applyCells(list);
        this.entityData.set(DATA, encode(list));
    }

    private void applyCells(List<Cell> list) {
        this.cells = List.copyOf(list);
        int mx = 0, my = 0, mz = 0, w = 0, p = 0;
        for (Cell c : cells) {
            mx = Math.max(mx, c.pos().getX() + 1);
            my = Math.max(my, c.pos().getY() + 1);
            mz = Math.max(mz, c.pos().getZ() + 1);
            if (c.state().is(ModBlocks.WHEEL.get())) w++;
            else if (c.state().is(ModBlocks.PROPELLER.get())) p++;
        }
        sizeX = mx; sizeY = my; sizeZ = mz; wheels = w; props = p;
        refreshDimensions();
        setPos(getX(), getY(), getZ());
    }

    private static String encode(List<Cell> list) {
        StringBuilder sb = new StringBuilder();
        for (Cell c : list) {
            sb.append(c.pos().getX()).append(',')
              .append(c.pos().getY()).append(',')
              .append(c.pos().getZ()).append(',')
              .append(Block.getId(c.state())).append(';');
        }
        return sb.toString();
    }

    private static List<Cell> decode(String s) {
        List<Cell> out = new ArrayList<>();
        if (s == null || s.isEmpty()) return out;
        for (String part : s.split(";")) {
            String[] f = part.split(",");
            if (f.length != 4) continue;
            try {
                out.add(new Cell(
                        new BlockPos(Integer.parseInt(f[0]), Integer.parseInt(f[1]), Integer.parseInt(f[2])),
                        Block.stateById(Integer.parseInt(f[3]))));
            } catch (NumberFormatException ignored) {
                // skip malformed entry
            }
        }
        return out;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA, "");
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA.equals(key) && level().isClientSide()) {
            applyCells(decode(this.entityData.get(DATA)));
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        input.read("Blocks", Cell.LIST).ifPresent(list -> {
            applyCells(list);
            this.entityData.set(DATA, encode(list));
        });
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("Blocks", Cell.LIST, cells);
    }

    // ---------------------------------------------------------------- bounds

    private AABB makeBox() {
        return new AABB(getX() - sizeX / 2.0, getY(), getZ() - sizeZ / 2.0,
                getX() + sizeX / 2.0, getY() + sizeY, getZ() + sizeZ / 2.0);
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        if (sizeX > 0) setBoundingBox(makeBox());
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (sizeX > 0) return EntityDimensions.scalable(Math.max(sizeX, sizeZ), sizeY);
        return super.getDimensions(pose);
    }

    @Override public boolean isPickable() { return !isRemoved(); }
    @Override public boolean isPushable() { return false; }

    // One of these overrides the vanilla method (signature differs between versions); the other is a harmless extra.
    public boolean canBeCollidedWith() { return true; }
    public boolean canBeCollidedWith(Entity other) { return true; }

    // ---------------------------------------------------------------- interaction

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide()) return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown()) {
            if (player.getItemInHand(hand).getItem() instanceof AssemblerItem) {
                disassemble();
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        if (!isVehicle()) player.startRiding(this);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (!isRemoved() && source.getEntity() instanceof Player p && p.isCreative()) {
            disassemble();
            return true;
        }
        return false;
    }

    /** Server: put the blocks back into the world (dropping any that no longer fit) and remove the entity. */
    public void disassemble() {
        if (!(level() instanceof ServerLevel level)) return;
        ejectPassengers();
        BlockPos base = new BlockPos(
                Mth.floor(getX() - sizeX / 2.0 + 0.5),
                Mth.floor(getY() + 0.5),
                Mth.floor(getZ() - sizeZ / 2.0 + 0.5));
        for (Cell c : cells) {
            BlockPos target = base.offset(c.pos());
            BlockState existing = level.getBlockState(target);
            if (existing.isAir() || existing.canBeReplaced()) {
                level.setBlock(target, c.state(), 3);
            } else {
                Block.popResource(level, target, new ItemStack(c.state().getBlock()));
            }
        }
        discard();
    }

    public void setControls(int move, boolean up) {
        this.controlMove = move;
        this.controlUp = up;
        this.controlTick = this.tickCount;
    }

    public List<String> debugLines() {
        double n = Math.max(1, cells.size());
        Vec3 v = getDeltaMovement();
        List<String> out = new ArrayList<>();
        out.add(String.format("id=%d pos=(%.2f, %.2f, %.2f) size=%dx%dx%d", getId(), getX(), getY(), getZ(), sizeX, sizeY, sizeZ));
        out.add(String.format("blocks=%d wheels=%d propellers=%d", cells.size(), wheels, props));
        out.add(String.format("drive=%.2f lift=%.2f (lift >= 0.25 needed to hover)",
                Math.min(1.0, wheels * 4.0 / n), Math.min(1.0, props * 2.0 / n)));
        out.add(String.format("velocity=(%.3f, %.3f) onGround=%b", v.x, v.y, onGround()));
        Entity driver = getFirstPassenger();
        out.add("driver=" + (driver == null ? "none" : driver.getName().getString())
                + " controls(move=" + controlMove + ", up=" + controlUp + ")");
        return out;
    }

    // ---------------------------------------------------------------- tick / physics

    @Override
    public void tick() {
        super.tick();
        if (cells.isEmpty()) {
            if (!level().isClientSide() && tickCount > 40) discard();
            return;
        }
        setBoundingBox(makeBox());
        if (!level().isClientSide()) serverPhysics();
    }

    private void serverPhysics() {
        ServerLevel level = (ServerLevel) level();
        ServerPlayer driver = getFirstPassenger() instanceof ServerPlayer sp ? sp : null;
        boolean live = driver != null && tickCount - controlTick <= 15;
        int move = live ? controlMove : 0;
        boolean up = live && controlUp;

        double n = Math.max(1, cells.size());
        double drive = Math.min(1.0, wheels * 4.0 / n);
        double lift = Math.min(1.0, props * 2.0 / n);
        boolean grounded = onGround();

        Vec3 dm = getDeltaMovement();
        double vx = dm.x, vy = dm.y;

        double maxSpeed, accel, friction;
        if (grounded) {
            if (wheels > 0) {
                maxSpeed = 0.35 * drive;
                accel = 0.05 * drive + 0.005;
                friction = 0.90;
            } else {
                maxSpeed = props > 0 ? 0.08 : 0.0;
                accel = props > 0 ? 0.01 : 0.0;
                friction = 0.6;
            }
        } else {
            maxSpeed = props > 0 ? 0.30 * Math.max(lift, 0.3) : 0.0;
            accel = props > 0 ? 0.03 : 0.0;
            friction = 0.99;
        }

        if (move != 0 && accel > 0) {
            vx = Mth.clamp(vx + move * accel, -maxSpeed, maxSpeed);
        } else {
            vx *= friction;
            if (Math.abs(vx) < 0.003) vx = 0;
        }

        // Bulldozing: smash/push whatever is in front (and above while lifting).
        if (bulldozeCooldown > 0) bulldozeCooldown--;
        if (live && Config.BULLDOZE.get() && bulldozeCooldown == 0) {
            AABB b = getBoundingBox();
            boolean did = false;
            if (move != 0) {
                AABB slab = move > 0
                        ? new AABB(b.maxX, b.minY + 0.05, b.minZ + 0.02, b.maxX + 0.25, b.maxY - 0.05, b.maxZ - 0.02)
                        : new AABB(b.minX - 0.25, b.minY + 0.05, b.minZ + 0.02, b.minX, b.maxY - 0.05, b.maxZ - 0.02);
                did |= bulldoze(level, driver, slab, move);
            }
            if (up && props > 0) {
                AABB slab = new AABB(b.minX + 0.02, b.maxY, b.minZ + 0.02, b.maxX - 0.02, b.maxY + 0.25, b.maxZ - 0.02);
                did |= bulldoze(level, driver, slab, 0);
            }
            if (did) bulldozeCooldown = Config.BULLDOZE_COOLDOWN.get();
        }

        // Vertical
        if (up && props > 0) vy += 0.16 * lift;
        vy -= 0.04;
        vy *= 0.98;
        vy = Mth.clamp(vy, -2.5, 0.5);
        double movedY = moveAxis(Direction.Axis.Y, vy);
        boolean landed = false;
        if (Math.abs(movedY - vy) > 1.0E-9) {
            if (vy < 0) landed = true;
            vy = 0;
        }
        setOnGround(landed);

        // Horizontal
        double movedX = moveAxis(Direction.Axis.X, vx);
        if (Math.abs(movedX - vx) > 1.0E-9) vx = 0;

        setDeltaMovement(vx, vy, 0);

        if (getY() < level.dimensionType().minY() - 32) discard();
    }

    private double moveAxis(Direction.Axis axis, double d) {
        if (d == 0) return 0;
        AABB box = getBoundingBox();
        AABB sweep = switch (axis) {
            case X -> box.expandTowards(d, 0, 0);
            case Y -> box.expandTowards(0, d, 0);
            case Z -> box.expandTowards(0, 0, d);
        };
        double allowed = Shapes.collide(axis, box, level().getBlockCollisions(this, sweep), d);
        setPos(getX() + (axis == Direction.Axis.X ? allowed : 0),
                getY() + (axis == Direction.Axis.Y ? allowed : 0),
                getZ() + (axis == Direction.Axis.Z ? allowed : 0));
        return allowed;
    }

    private boolean bulldoze(ServerLevel level, ServerPlayer driver, AABB slab, int pushDx) {
        boolean did = false;
        int count = 0;
        double maxH = Config.CONTRAPTION_MAX_HARDNESS.get();
        for (BlockPos p : BlockPos.betweenClosed(
                BlockPos.containing(slab.minX, slab.minY, slab.minZ),
                BlockPos.containing(slab.maxX, slab.maxY, slab.maxZ))) {
            BlockPos pos = p.immutable();
            BlockState st = level.getBlockState(pos);
            if (st.isAir() || st.getBlock() instanceof LiquidBlock) continue;
            VoxelShape shape = st.getCollisionShape(level, pos);
            if (shape.isEmpty() || !shape.bounds().move(pos).intersects(slab)) continue;
            if (++count > 12) break;
            if (pushDx != 0 && Config.PUSH_ENABLED.get() && Smasher.tryPush(driver, level, pos, st, pushDx)) {
                did = true;
                continue;
            }
            if (Smasher.breakBlock(driver, level, pos, st, maxH)) did = true;
        }
        return did;
    }
}
