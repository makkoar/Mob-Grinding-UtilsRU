package mob_grinding_utils.tile;

import java.util.List;

import mob_grinding_utils.ModBlocks;
import mob_grinding_utils.ModItems;
import mob_grinding_utils.blocks.BlockFan;
import mob_grinding_utils.items.ItemFanUpgrade;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TileEntityFan extends TileEntityInventoryHelper implements ITickableTileEntity {

	private static final int[] SLOTS = new int[] {0, 1, 2};
	public boolean showRenderBox;
	float xPos, yPos, zPos;
	float xNeg, yNeg, zNeg;

	public TileEntityFan() {
		super(ModBlocks.FAN_TILE, 3);
	}

	@Override
	public void tick() {
		if (world.getGameTime()%2==0 && getWorld().getBlockState(getPos()).getBlock() instanceof BlockFan)
			if (getWorld().getBlockState(getPos()).get(BlockFan.POWERED)) {
				activateBlock();
		}
		if (!getWorld().isRemote)
			setAABBWithModifiers();
	}
/*
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, BlockState oldState, BlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}
*/
	public int getWidthModifier() {
		return hasWidthUpgrade() ? getItems().get(0).getCount() : 0;
	}

	public int getHeightModifier() {
		return hasHeightUpgrade() ? getItems().get(1).getCount() : 0;
	}

	public int getSpeedModifier() {
		return hasSpeedUpgrade() ? getItems().get(2).getCount() : 0;
	}
	
	public void setAABBWithModifiers() {
		BlockState state = getWorld().getBlockState(getPos());
		Direction facing = state.get(BlockFan.FACING);

		int distance;
		for (distance = 1; distance < 5 + getSpeedModifier(); distance++) {
			BlockState state2 = getWorld().getBlockState(getPos().offset(facing, distance));
			if (state2 != Blocks.AIR.getDefaultState())
				break;
		}

		if (facing == Direction.UP) {
			yPos = distance;
			yNeg = -1;
			xPos = getHeightModifier();
			xNeg = getHeightModifier();
			zPos = getWidthModifier();
			zNeg = getWidthModifier();
		}
		if (facing == Direction.DOWN) {
			yNeg = distance;
			yPos = -1;
			xPos = getHeightModifier();
			xNeg = getHeightModifier();
			zPos = getWidthModifier();
			zNeg = getWidthModifier();
		}
		if (facing == Direction.WEST) {
			xNeg = distance;
			xPos = -1;
			zPos = getWidthModifier();
			zNeg = getWidthModifier();
			yPos = getHeightModifier();
			yNeg = getHeightModifier();
		}
		if (facing == Direction.EAST) {
			xPos = distance;
			xNeg = -1;
			zPos = getWidthModifier();
			zNeg = getWidthModifier();
			yPos = getHeightModifier();
			yNeg = getHeightModifier();
		}
		if (facing == Direction.NORTH) {
			zNeg = distance;
			zPos = -1;
			xPos = getWidthModifier();
			xNeg = getWidthModifier();
			yPos = getHeightModifier();
			yNeg = getHeightModifier();
		}
		if (facing == Direction.SOUTH) {
			zPos = distance;
			zNeg = -1;
			xPos = getWidthModifier();
			xNeg = getWidthModifier();
			yPos = getHeightModifier();
			yNeg = getHeightModifier();
		}
		getWorld().notifyBlockUpdate(getPos(), state, state, 8);
	}

	public AxisAlignedBB getAABBWithModifiers() {
		return new AxisAlignedBB(getPos().getX() - xNeg, getPos().getY() - yNeg, getPos().getZ() - zNeg, getPos().getX() + 1D + xPos, getPos().getY() + 1D + yPos, getPos().getZ() + 1D + zPos);
	}

	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getAABBForRender() {
		return new AxisAlignedBB(- xNeg, - yNeg, - zNeg, 1D + xPos, 1D + yPos, 1D + zPos);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos().getX() - xNeg, getPos().getY() - yNeg, getPos().getZ() - zNeg, getPos().getX() + 1D + xPos, getPos().getY() + 1D + yPos, getPos().getZ() + 1D + zPos);
	}

	public void toggleRenderBox() {
		if (!showRenderBox)
			showRenderBox = true;
		else
			showRenderBox = false;
		markDirty();
	}

	protected Entity activateBlock() {
		BlockState state = getWorld().getBlockState(getPos());
		Direction facing = state.get(BlockFan.FACING);
		List<LivingEntity> list = getWorld().getEntitiesWithinAABB(LivingEntity.class, getAABBWithModifiers());
		for (int i = 0; i < list.size(); i++) {
			Entity entity = list.get(i);
			if (entity != null) {
				if (entity instanceof LivingEntity) {
					if (facing != Direction.UP && facing != Direction.DOWN) {
						entity.addVelocity(MathHelper.sin(facing.getOpposite().getHorizontalAngle() * 3.141593F / 180.0F) * 0.5D, 0D, -MathHelper.cos(facing.getOpposite().getHorizontalAngle() * 3.141593F / 180.0F) * 0.5D);
					} else if (facing == Direction.UP) {
						//entity.motionY += 0.125D;
						float f = 0.125F;
						Vector3d vec3d = entity.getMotion();
						entity.setMotion(vec3d.x, (double) f, vec3d.z);
						entity.addVelocity(0D, 0.25D, 0D);
						entity.fallDistance = 0;
					} else if (facing == Direction.DOWN)
						entity.addVelocity(0D, -0.2D, 0D);
				}
			}
		}
		return null;
	}

	private boolean hasWidthUpgrade() {
		return !getItems().get(0).isEmpty() && getItems().get(0).getItem() == ModItems.FAN_UPGRADE_WIDTH;
	}

	private boolean hasHeightUpgrade() {
		return !getItems().get(1).isEmpty() && getItems().get(1).getItem() == ModItems.FAN_UPGRADE_HEIGHT;
	}

	private boolean hasSpeedUpgrade() {
		return !getItems().get(2).isEmpty() && getItems().get(2).getItem() == ModItems.FAN_UPGRADE_SPEED;
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		super.write(nbt);
		nbt.putBoolean("showRenderBox", showRenderBox);
		nbt.putFloat("xPos", xPos);
		nbt.putFloat("yPos", yPos);
		nbt.putFloat("zPos", zPos);
		nbt.putFloat("xNeg", xNeg);
		nbt.putFloat("yNeg", yNeg);
		nbt.putFloat("zNeg", zNeg);
		return nbt;
	}

	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		showRenderBox = nbt.getBoolean("showRenderBox");
		xPos = nbt.getFloat("xPos");
		yPos = nbt.getFloat("yPos");
		zPos = nbt.getFloat("zPos");
		xNeg = nbt.getFloat("xNeg");
		yNeg = nbt.getFloat("yNeg");
		zNeg = nbt.getFloat("zNeg");
	}

	@Override
    public CompoundNBT getUpdateTag() {
		CompoundNBT nbt = new CompoundNBT();
        return write(nbt);
    }

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT nbt = new CompoundNBT();
		write(nbt);
		return new SUpdateTileEntityPacket(getPos(), 0, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
		read(null, packet.getNbtCompound());
		onContentsChanged();
	}

	public void onContentsChanged() {
		if (this != null && !getWorld().isRemote) {
			final BlockState state = getWorld().getBlockState(getPos());
			setAABBWithModifiers();
			getWorld().notifyBlockUpdate(getPos(), state, state, 8);
			markDirty();
		}
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return stack.getItem() instanceof ItemFanUpgrade;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return ItemStackHelper.getAndRemove(getItems(), index);
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return SLOTS;
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, Direction direction) {
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
		return false;
	}

}
