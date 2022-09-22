package sf.ssf.sfort.ocaip;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AuthScreen extends Screen {

	final Screen parent;
	final ServerAddress address;
	final boolean reqPass;
	final Future<String> powCompute;
	TextFieldWidget pass;
	ButtonWidget button;

	String text = "";

	public AuthScreen(InetSocketAddress address, Screen parent, boolean requirePass, String powPrompt) {
		super(Text.of("OCAIP Password Screen"));
		this.parent = parent;
		this.address = new ServerAddress(address.getHostName(), address.getPort());
		this.reqPass = requirePass;
		this.powCompute = powPrompt == null ? null : POW.computeSolutionAsync(powPrompt, ()->{
			if (this.button != null){
				button.setMessage(Text.of("Register"));
				this.button.active = true;
			}
		}, () -> {
			if (this.button != null){
				button.setMessage(Text.of("Failed"));
				text = "Failed to compute requirements, check logs for full error";
			}
		});
		this.init(MinecraftClient.getInstance(), parent.width, parent.height);
	}

	@Override
	public void init() {
		super.init();
		pass = new TextFieldWidget(textRenderer, width/2-100, height/2-20, 200, 20, Text.of("Password"));
		if (reqPass) {
			text = "Server requires registration password";
		} else {
			pass.visible = false;
			pass.active = false;
		}
		button = new ButtonWidget(width/2-62, height/2+10, 124, 20, Text.of("Register"), a -> {
			ConnectScreen.connect(parent, client, address, null);
			String powRez = null;
			try {
				if (powCompute != null) powRez = powCompute.get();
			} catch (InterruptedException | ExecutionException e) {
				Reel.log.error("Error getting pow", e);
			}
			Tape.auth = new AuthObject(pass.getText(), powRez);
		});
		if (this.powCompute != null){
			button.setMessage(Text.of("Computing requirements"));
			button.active = false;
		}
		addDrawableChild(button);
		addDrawableChild(pass);
		setFocused(pass);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (client == null) return;
		renderBackground(matrices);
		textRenderer.drawWithShadow(matrices, text, width / 2f - textRenderer.getWidth(text) / 2f, height / 2f - 40, -1);
		super.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	public void close() {
		this.client.setScreen(parent);
	}

}
