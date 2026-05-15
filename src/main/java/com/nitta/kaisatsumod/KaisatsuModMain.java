import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

@Mod(modid = "yourmodid", version = "1.0")
public class KaisatsuModMain {

    public static Item icCard;
    public static Block ticketGate;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // インスタンス化
        icCard = new ItemICCard();
        ticketGate = new BlockTicketGate();

        // ゲームシステムへ登録
        GameRegistry.registerItem(icCard, "icCard");
        GameRegistry.registerBlock(ticketGate, "ticketGate");
    }
}