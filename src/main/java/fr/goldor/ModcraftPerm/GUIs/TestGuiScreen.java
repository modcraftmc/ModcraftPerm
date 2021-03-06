package fr.goldor.ModcraftPerm.GUIs;

import com.mojang.authlib.GameProfile;
import com.mojang.text2speech.Narrator;
import fr.goldor.ModcraftPerm.ModcraftPerm;
import net.minecraft.advancements.criterion.SummonedEntityTrigger;
import net.minecraft.client.GameConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.network.login.ClientLoginNetHandler;
import net.minecraft.client.renderer.ScreenSize;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.command.impl.SummonCommand;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Session;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import sun.java2d.loops.RenderCache;

import java.util.*;

public class TestGuiScreen extends Screen {
    private long lastCounting;
    private int count;
    private Button movingButton;
    private Button spawnPlayerButton;
    private Button interfaceButton;

    public TestGuiScreen(){
        super(new StringTextComponent("TestGuiScreenOWO"));
        count = -1;
    }

    public void init() {
        super.init();
        movingButton = new Button(10, 50, 200, 20, new StringTextComponent("Here is a moving button !!"),(b) -> {movingButton(b);});
        spawnPlayerButton = new Button(10,10,100,20,new StringTextComponent("Spawn Player"),(b) -> {spawnPlayer(b);});
        interfaceButton = new Button(10,90,100,20,new StringTextComponent("Interface OMG !"),(b) -> {permInterface(b);});
        this.addButton(movingButton);
        this.addButton(spawnPlayerButton);
        this.addButton(interfaceButton);

    }

    public void tick(){
        super.tick();
        if(count != -1){
            movingButton.setMessage(new StringTextComponent(String.format("Here is a moving button !! your current cpm is : %s",count)));
            movingButton.setWidth(minecraft.getRenderManager().getFontRenderer().getStringWidth(String.format("Here is a moving button !! your current cpm is : %s",count)) + 10);
        }
    }

    public void movingButton(Button button) {

        if(System.currentTimeMillis() - lastCounting > 60000 || count == -1){
            count = 0;
        }

        count++;

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                count--;
            }
        },new Date(System.currentTimeMillis() + 60000));

        Random random = new Random();
        button.y = random.nextInt(this.height - button.getHeightRealms());
        button.x = random.nextInt(this.width - button.getWidth());

        lastCounting = System.currentTimeMillis();
    }

    public void spawnPlayer(Button button){
        ModcraftPerm.GetDedicatedServer().execute(() ->{
            ServerWorld serverWorld = ModcraftPerm.GetDedicatedServer().getWorld(ServerWorld.OVERWORLD);
            String name = "Finally !!";
            UUID uuid = PlayerEntity.getOfflineUUID(name);
            ServerPlayerEntity serverPlayerEntity = ModcraftPerm.GetDedicatedServer().getPlayerList().createPlayerForUser(new GameProfile(uuid,name));

            ModcraftPerm.GetLogger().warn(serverPlayerEntity.getPosition());
        });

    }

    public void permInterface(Button button){

    }
}
