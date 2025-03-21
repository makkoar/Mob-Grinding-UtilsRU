package mob_grinding_utils.tile;

import io.netty.buffer.Unpooled;
import mob_grinding_utils.ModBlocks;
import mob_grinding_utils.ModItems;
import mob_grinding_utils.blocks.BlockFan;
import mob_grinding_utils.config.ServerConfig;
import mob_grinding_utils.inventory.server.ContainerFan;
import mob_grinding_utils.items.ItemFanUpgrade;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.List;

public class TileEntityFan extends TileEntityInventoryHelper implements MenuProvider, BEGuiClickable {

	private static final int[] SLOTS = new int[] {0, 1, 2};
	public boolean showRenderBox;
	float xPos, yPos, zPos;
	float xNeg, yNeg, zNeg;

	public TileEntityFan(BlockPos pos, BlockState state) {
		super(ModBlocks.FAN.getTileEntityType(), 3, pos, state);
	}

	public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, T t) {
		if(t instanceof TileEntityFan fan) {
			if (level.getGameTime() % 2 == 0 && level.getBlockState(pos).getBlock() instanceof BlockFan)
				if (level.getBlockState(pos).getValue(BlockFan.POWERED)) {
					fan.activateBlock();
				}
			if (!level.isClientSide)
				fan.setAABBWithModifiers();
		}
	}

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
		BlockState state = getLevel().getBlockState(getBlockPos());
		if (!(state.getBlock() instanceof BlockFan))
			return;
		Direction facing = state.getValue(BlockFan.FACING);
		boolean strongBlades = ServerConfig.FAN_REINFORCED_BLADES.get();

		int distance;
		for (distance = 1; distance < 5 + getSpeedModifier(); distance++) {
			BlockState targetState = getLevel().getBlockState(getBlockPos().relative(facing, distance));
			if (!(targetState.getBlock() instanceof AirBlock) && (strongBlades ? targetState.canOcclude() : !(targetState.is(BlockTags.REPLACEABLE_BY_TREES) || targetState.is(BlockTags.SMALL_FLOWERS))))
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
		getLevel().sendBlockUpdated(getBlockPos(), state, state, 8);
	}

	public AABB getAABBWithModifiers() {
		return new AABB(getBlockPos().getX() - xNeg, getBlockPos().getY() - yNeg, getBlockPos().getZ() - zNeg, getBlockPos().getX() + 1D + xPos, getBlockPos().getY() + 1D + yPos, getBlockPos().getZ() + 1D + zPos);
	}

	@OnlyIn(Dist.CLIENT)
	public AABB getAABBForRender() {
		return new AABB(- xNeg, - yNeg, - zNeg, 1D + xPos, 1D + yPos, 1D + zPos);
	}

	public AABB getRenderBoundingBox() {
		return new AABB(getBlockPos().getX() - xNeg, getBlockPos().getY() - yNeg, getBlockPos().getZ() - zNeg, getBlockPos().getX() + 1D + xPos, getBlockPos().getY() + 1D + yPos, getBlockPos().getZ() + 1D + zPos);
	}
	public void toggleRenderBox() {
		showRenderBox = !showRenderBox;
		setChanged();
	}

	protected void activateBlock() {
		BlockState state = getLevel().getBlockState(getBlockPos());
		if (!(state.getBlock() instanceof BlockFan))
			return;
		Direction facing = state.getValue(BlockFan.FACING);
		List<LivingEntity> list = getLevel().getEntitiesOfClass(LivingEntity.class, getAABBWithModifiers());
		for (Entity entity : list) {
			if (entity != null) {
				if (entity instanceof LivingEntity) {
					if (facing != Direction.UP && facing != Direction.DOWN) {
						entity.push(Mth.sin(facing.getOpposite().toYRot() * 3.141593F / 180.0F) * 0.5D, 0D, -Mth.cos(facing.getOpposite().toYRot() * 3.141593F / 180.0F) * 0.5D);
					} else if (facing == Direction.UP) {
						//entity.motionY += 0.125D;
						float f = 0.125F;
						Vec3 vec3d = entity.getDeltaMovement();
						entity.setDeltaMovement(vec3d.x, (double) f, vec3d.z);
						entity.push(0D, 0.25D, 0D);
						entity.fallDistance = 0;
					} else entity.push(0D, -0.2D, 0D);
				}
			}
		}
		return;
	}

	private boolean hasWidthUpgrade() {
		return !getItems().get(0).isEmpty() && getItems().get(0).getItem() == ModItems.FAN_UPGRADE_WIDTH.get();
	}

	private boolean hasHeightUpgrade() {
		return !getItems().get(1).isEmpty() && getItems().get(1).getItem() == ModItems.FAN_UPGRADE_HEIGHT.get();
	}

	private boolean hasSpeedUpgrade() {
		return !getItems().get(2).isEmpty() && getItems().get(2).getItem() == ModItems.FAN_UPGRADE_SPEED.get();
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putBoolean("showRenderBox", showRenderBox);
		nbt.putFloat("xPos", xPos);
		nbt.putFloat("yPos", yPos);
		nbt.putFloat("zPos", zPos);
		nbt.putFloat("xNeg", xNeg);
		nbt.putFloat("yNeg", yNeg);
		nbt.putFloat("zNeg", zNeg);
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		showRenderBox = nbt.getBoolean("showRenderBox");
		xPos = nbt.getFloat("xPos");
		yPos = nbt.getFloat("yPos");
		zPos = nbt.getFloat("zPos");
		xNeg = nbt.getFloat("xNeg");
		yNeg = nbt.getFloat("yNeg");
		zNeg = nbt.getFloat("zNeg");
	}

	@Nonnull
	@Override
	public CompoundTag getUpdateTag(@Nonnull HolderLookup.Provider registries) {
		CompoundTag nbt = new CompoundTag();
		saveAdditional(nbt, registries);
		return nbt;
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		CompoundTag nbt = new CompoundTag();
		saveAdditional(nbt, level.registryAccess());
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(@Nonnull Connection net, ClientboundBlockEntityDataPacket packet, @Nonnull HolderLookup.Provider registries) {
		loadAdditional(packet.getTag(), registries);
		onContentsChanged();
	}

	public void onContentsChanged() {
		if (!getLevel().isClientSide) {
			final BlockState state = getLevel().getBlockState(getBlockPos());
			setAABBWithModifiers();
			getLevel().sendBlockUpdated(getBlockPos(), state, state, 8);
			setChanged();
		}
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return stack.getItem() instanceof ItemFanUpgrade;
	}

	@Nonnull
	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return ContainerHelper.takeItem(getItems(), index);
	}

    @Nonnull
	@Override
	public int[] getSlotsForFace(@Nonnull Direction side) {
		return SLOTS;
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, @Nonnull ItemStack itemStackIn, Direction direction) {
		return false;
	}

	@Override
	public boolean canTakeItemThroughFace(int index, @Nonnull ItemStack stack, @Nonnull Direction direction) {
		return false;
	}
	
	@Override
	public AbstractContainerMenu createMenu(int windowID, @Nonnull Inventory playerInventory, @Nonnull Player player) {
		return new ContainerFan(windowID, playerInventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(worldPosition));
	}

	@Nonnull
	@Override
	public Component getDisplayName() {
		return Component.translatable("block.mob_grinding_utils.fan");
	}

	@Override
	public void buttonClicked(int buttonID) {
		if (buttonID == 0) {
			toggleRenderBox();
			getLevel().sendBlockUpdated(worldPosition, getLevel().getBlockState(worldPosition), getLevel().getBlockState(worldPosition), 3);
		}
	}
}
