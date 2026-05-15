import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemICCard extends Item {
    public ItemICCard() {
        super();
        this.setUnlocalizedName("icCard"); // 内部名
        this.setTextureName("yourmodid:ic_card"); // テクスチャのパス
        this.setCreativeTab(CreativeTabs.tabTransport); // クリエイティブタブ
        this.setMaxStackSize(1); // 1枚しか持てないようにする
    }
}
