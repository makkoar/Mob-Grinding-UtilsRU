package mob_grinding_utils.components;

import mob_grinding_utils.Reference;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MGUComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, Reference.MOD_ID);

    public static final Supplier<DataComponentType<ResourceLocation>> MOB_DNA = DATA_COMPONENT_TYPES.register("mob_dna", () ->
            DataComponentType.<ResourceLocation>builder()
                    .persistent(ResourceLocation.CODEC)
                    .networkSynchronized(ResourceLocation.STREAM_CODEC)
                    .build());

    public static void init(IEventBus bus) {
        DATA_COMPONENT_TYPES.register(bus);
    }
}
