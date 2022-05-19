package sf.ssf.sfort.ocaip.mixin;

import net.minecraft.client.gui.screen.ConnectScreen;
import org.spongepowered.asm.mixin.Mixin;
import sf.ssf.sfort.ocaip.OCAIPPassworded;

@Mixin(ConnectScreen.class)
public class ConnectScreenMixin implements OCAIPPassworded {

    String ocaip$pass = null;

    @Override
    public void ocaip$setPassword(String pass) {
        ocaip$pass = pass;
    }

    @Override
    public String ocaip$getPassword() {
        return ocaip$pass;
    }

}
