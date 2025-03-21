package mob_grinding_utils.inventory.client;

import mob_grinding_utils.inventory.server.ContainerSaw;
import mob_grinding_utils.util.RL;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiSaw extends MGUScreen<ContainerSaw> {
	public GuiSaw(ContainerSaw containerSaw, Inventory playerInventory, Component title) {
		super(containerSaw, playerInventory, title, RL.mgu("textures/gui/saw_gui.png"));
		imageHeight = 132;
	}
}