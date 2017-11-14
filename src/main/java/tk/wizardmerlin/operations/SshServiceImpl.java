package tk.wizardmerlin.operations;

import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class SshServiceImpl implements SshService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SshServiceImpl.class);

    public String executeCommand(String serverAddr, int port, String username, String privatekeyLocation, String passphrashLocation, String cmd) throws IOException {
        LOGGER.info("serverAddr: " + serverAddr);
        LOGGER.info("port: " + port);
        LOGGER.info("username: " + username);
        LOGGER.info("privatekeyLocation: " + username);
        LOGGER.info("passphrashLocation: " + passphrashLocation);
        String privateKey = FileUtils.readFileToString(new File(privatekeyLocation), "utf-8");
        String passphrase = FileUtils.readFileToString(new File(passphrashLocation), "utf-8").trim();
        Shell shell = new Ssh(serverAddr, port, username, privateKey, passphrase);
        String stdout = new Shell.Plain(shell).exec(cmd);
        LOGGER.info(stdout);
        return stdout;
    }
}
