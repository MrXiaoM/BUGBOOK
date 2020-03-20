package bugbook;

import bugbook.Main;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;

public class ConfigGui extends GuiConfig {
	public ConfigGui(GuiScreen parent) {
		super(parent, (new ConfigElement(Main.configFile.getCategory("general"))).getChildElements(), "bugbook",
				false, false, "BUGBOOK");
		this.titleLine2 = "自定义配置页面";
	}
}