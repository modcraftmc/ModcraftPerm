package fr.goldor.ModcraftPerm.GUIs.PermInterface;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.goldor.ModcraftPerm.utils.References;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class PermInterfaceScreen extends Screen {

    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(String.format("%s%s",References.MOD_ID,":gui/perminterface.png"));

    public PermInterfaceScreen() {
        super(new StringTextComponent("PermInterface"));
    }

    public void init(){

    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        this.getMinecraft().getTextureManager().bindTexture(RESOURCE_LOCATION);

        blit(matrixStack,100,80,0,0,200,191);
    }
}
