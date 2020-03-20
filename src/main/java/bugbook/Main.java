package bugbook;

import java.io.IOException;
import java.util.Random;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

@Mod(modid = "bugbook", name = "BugBook", version = "-6.6.6", clientSideOnly = true, guiFactory = "bugbook.GuiFactory")

public class Main {
	public static org.apache.logging.log4j.Logger log;
	public static Configuration configFile;
	private static int lengthPerPage = 255;
	private static int pages = 10;
	private static String bookTitle = "N/A";
	private static boolean publish = false;

	public KeyBinding bug;

	@EventHandler
	public void preInit(FMLPreInitializationEvent preInitEvent) throws IOException {
		log = preInitEvent.getModLog();
		configFile = new Configuration(preInitEvent.getSuggestedConfigurationFile());
		syncConfig();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		this.bug = new KeyBinding("CustomPayLoadPacket", 46, "bugbook");
		ClientRegistry.registerKeyBinding(this.bug);
	}
	
	@SubscribeEvent
	public void onKeyDown(KeyInputEvent event) {
		if (this.bug.isKeyDown()) {
			Minecraft mc = Minecraft.getMinecraft();
			if (mc.thePlayer != null) {
				ItemStack bookObj = Minecraft.getMinecraft().thePlayer.getHeldItem();
				if (bookObj != null) {
					if(Item.getIdFromItem(bookObj.getItem()) == 386)
						this.sendBookToServer(bookObj);
				}
			}
		}
	}

	@SubscribeEvent
	public void onConfigChanged(OnConfigChangedEvent event) {
		if (event.modID.equals("bugbook")) {
			syncConfig();
		}
	}

	public static void syncConfig() {
		lengthPerPage = configFile.getInt("每页需要填充的字符数量", "general", 255, 1, 666666, "");
		pages = configFile.getInt("需要填充的页数", "general", 10, 1, 666666, "");
		bookTitle = configFile.getString("签名时需要的标题", "general", "N/A", "");
		publish = configFile.getBoolean("是否需要签名", "general", false, "");
		if (configFile.hasChanged()) {
			configFile.save();
		}
	}

	private void sendBookToServer(ItemStack bookObj) {
		Minecraft mc = Minecraft.getMinecraft();

		NBTTagList bookPages = new NBTTagList();
		for (int i = 0; i < pages; i++) {
			bookPages.appendTag(new NBTTagString(getRandomString(lengthPerPage)));
		}
		if (bookPages != null) {
			while (bookPages.tagCount() > 1) {
				String s = bookPages.getStringTagAt(bookPages.tagCount() - 1);

				if (s.length() != 0) {
					break;
				}

				bookPages.removeTag(bookPages.tagCount() - 1);
			}

			if (bookObj.hasTagCompound()) {
				NBTTagCompound nbttagcompound = bookObj.getTagCompound();
				nbttagcompound.setTag("pages", bookPages);
			} else {
				bookObj.setTagInfo("pages", bookPages);
			}

			String s2 = "MC|BEdit";

			if (publish) {
				s2 = "MC|BSign";
				bookObj.setTagInfo("author", new NBTTagString(mc.thePlayer.getName()));
				bookObj.setTagInfo("title", new NBTTagString(bookTitle.trim()));
				
				for (int i = 0; i < bookPages.tagCount(); ++i) {
					String s1 = bookPages.getStringTagAt(i);
					IChatComponent ichatcomponent = new ChatComponentText(s1);
					s1 = IChatComponent.Serializer.componentToJson(ichatcomponent);
					bookPages.set(i, new NBTTagString(s1));
				}

				bookObj.setItem(Items.written_book);
			}

			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			packetbuffer.writeItemStackToBuffer(bookObj);
			mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload(s2, packetbuffer));
			mc.thePlayer.addChatMessage(new ChatComponentText("§7[§cBUGBOOK§7] §a已填充手中的书 §7(共" + pages + "页， 每页" + lengthPerPage + "字符)"));
		}
	}
	
	public static String getRandomChinese() {
		return new String(new char[] { (char) (new Random().nextInt(20902) + 19968) });
	}

	public static String getRandomString(int length) {
		String result = "";
		for (int i = 0; i < length; i++) {
			result += getRandomChinese();
		}
		return result;
	}
}
