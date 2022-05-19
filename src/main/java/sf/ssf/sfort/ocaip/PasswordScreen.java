package sf.ssf.sfort.ocaip;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.net.InetSocketAddress;

public class PasswordScreen extends Screen {
    final Screen parent;
    final ServerAddress address;
    TextFieldWidget pass;
    ButtonWidget button;

    public PasswordScreen(InetSocketAddress address, Screen parent) {
        super(new LiteralText("OCAIP Password Screen"));
        this.parent = parent;
        this.address = new ServerAddress(address.getHostName(), address.getPort());
        this.init(MinecraftClient.getInstance(), parent.width, parent.height);
    }

    @Override
    public void init() {
        super.init();
        pass = new TextFieldWidget(textRenderer, width/2-100, height/2-20, 200, 20, new LiteralText("Password"));
        button = new ButtonWidget(width/2-50, height/2+10, 100, 20, new LiteralText("Register"), a -> {
            ConnectScreen.connect(parent, client, address, null);
            if (client.currentScreen instanceof OCAIPPassworded) ((OCAIPPassworded)client.currentScreen).ocaip$setPassword(pass.getText());
        });
        addDrawableChild(button);
        addDrawableChild(pass);
        setFocused(pass);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (client == null) return;
        renderBackground(matrices);
        String s = "Server requires registration password";
        textRenderer.drawWithShadow(matrices, s, width / 2f - textRenderer.getWidth(s) / 2f, height / 2f - 40, -1);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

}
