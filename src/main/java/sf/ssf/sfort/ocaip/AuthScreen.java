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
import java.util.concurrent.atomic.AtomicInteger;

public class AuthScreen extends Screen {

	final AtomicInteger processCount;
	final Screen parent;
	final ServerAddress address;
	final boolean reqPass;
	final Future<String> powCompute;
	final Future<String> pow512Compute;
	TextFieldWidget pass;
	ButtonWidget button;

	String text = "";

	public AuthScreen(InetSocketAddress address, Screen parent, boolean requirePass, String powPrompt, String pow512Prompt) {
		super(Text.of("OCAIP Password Screen"));
		this.parent = parent;
		this.address = new ServerAddress(address.getHostName(), address.getPort());
		this.reqPass = requirePass;
		this.processCount = new AtomicInteger((powPrompt != null ? 1 : 0) + (pow512Prompt != null ? 1 : 0));
		this.powCompute = powPrompt == null ? null : POW.computeSolutionAsync(powPrompt, this::decrementProcessCount, () -> {
			if (this.button != null){
				button.setMessage(Text.of("Failed"));
				text = "Failed to compute requirements, check logs for full error";
			}
		});
		this.pow512Compute = pow512Prompt == null ? null : POW512.computeSolutionAsync(pow512Prompt, this::decrementProcessCount, () -> {
			if (this.button != null){
				button.setMessage(Text.of("Failed"));
				text = "Failed to compute requirements, check logs for full error";
			}
		});
		this.init(MinecraftClient.getInstance(), parent.width, parent.height);
	}
	public void decrementProcessCount() {
		if (this.button != null & processCount.decrementAndGet() < 1){
			button.setMessage(Text.of("Register"));
			this.button.active = true;
		}
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

		button = ButtonWidget.builder(Text.of("Register"), a -> {
			ConnectScreen.connect(parent, client, address, null);
			String powRez = null;
			try {
				if (powCompute != null) powRez = powCompute.get();
			} catch (InterruptedException | ExecutionException e) {
				Reel.log.error("Error getting pow", e);
			}
			String pow512Rez = null;
			try {
				if (pow512Compute != null) pow512Rez = pow512Compute.get();
			} catch (InterruptedException | ExecutionException e) {
				Reel.log.error("Error getting pow", e);
			}
			Tape.auth = new AuthObject(pass.getText(), powRez, pow512Rez);
		}).position(width/2-62, height/2+10).size(124, 20).build();
		if (processCount.getAcquire() > 0){
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
