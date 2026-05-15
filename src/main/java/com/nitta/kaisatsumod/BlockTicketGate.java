import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class BlockTicketGate extends Block {
    public BlockTicketGate() {
        super(Material.iron); // 鉄の材質
        this.setBlockName("ticketGate");
        this.setBlockTextureName("yourmodid:ticket_gate");
        this.setCreativeTab(CreativeTabs.tabTransport);
        this.setHardness(3.0F); // 硬さ
    }

    // ブロックを右クリックした時の処理 (1.7.10のメソッドシグネチャ)
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        // プレイヤーが手に持っているアイテムを取得
        if (player.getCurrentEquippedItem() != null) {
            // 持っているアイテムがICカードかどうか判定
            if (player.getCurrentEquippedItem().getItem() instanceof ItemICCard) {
                // サーバー側のみで処理を実行
                if (!world.isRemote) {
                    player.addChatMessage(new ChatComponentText("ピピッ！"));
                    // ※ここに扉の開閉処理やNBTを使った残高処理を追加していく
                }
                return true; // 処理を完了
            }
        }
        return false;
    }
}
