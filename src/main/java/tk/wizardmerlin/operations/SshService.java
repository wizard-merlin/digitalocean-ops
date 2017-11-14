package tk.wizardmerlin.operations;

import java.io.IOException;

public interface SshService {
    String executeCommand(String serverAddr, int port, String username, String privatekeyLocation, String passphrashLocation, String cmd) throws IOException;
}
