package tk.wizardmerlin.operations;

import com.jcabi.ssh.Ssh;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class IpRotator {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpRotator.class);
    private AvailabilityTestService availabilityTester;
    private SshService sshService;
    private String configFilePath;
    private VpsProvider digitalOcean;
    private VpsProvider vultr;
    private DnsHostingService cloudflare;

    public void rotateIp() {

        int port = 8080;

        try {
            LOGGER.debug("ttest log debug");
            String configFileContent = FileUtils.readFileToString(new File(configFilePath), "utf-8");
            JSONArray jsonArray = new JSONObject(new JSONTokener(configFileContent)).getJSONArray("servers");
            List<VpnServer> servers = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                VpnServer server = VpnServer.fromJSONObject(object);
                String vpsProvider = object.getString("vpsProvider");
                switch (vpsProvider) {
                    case "DigitalOcean":
                        server.setVpsProvider(digitalOcean);
                        break;
                    case "Vultr":
                        server.setVpsProvider(vultr);
                        break;
                    default:
                        throw new Exception("unknown VPS provider: " + vpsProvider);
                }
                String dnsHostingService = object.getString("dnsHosting");
                if (dnsHostingService.equals("Cloudflare")) {
                    server.setDnsHosting(cloudflare);
                }
                servers.add(server);
                LOGGER.info(server.toString());
            }
            for (VpnServer server : servers) {
                try {
                    String containerName = "some-nginx";
                    startTomcat(server, port, containerName);
                    String urlToTest = "http://" + server.getHostname() + ":" + port;
                    LOGGER.info("URL to test: " + urlToTest);
                    boolean isAvailable = availabilityTester.test(urlToTest);
                    stopTomcat(server, containerName);
                    if (!isAvailable) {
                        LOGGER.info(urlToTest + " is not available");
                        String originalIpAddr = server.getIpAddr();
                        server.changeIp();
                        updateDnsRecord(server, originalIpAddr);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            JSONArray jsonArray1 = new JSONArray();
            for (VpnServer server : servers) {
                JSONObject object = server.toJSONObject();
                if (server.getVpsProvider() instanceof DigitalOceanVps) {
                    object.put("vpsProvider", "DigitalOcean");
                } else if (server.getVpsProvider() instanceof VultrVps) {
                    object.put("vpsProvider", "Vultr");
                }
                if (server.getDnsHosting() instanceof Cloudflare) {
                    object.put("dnsHosting", "Cloudflare");
                }
                jsonArray1.put(object);
            }
            JSONObject newConfig = new JSONObject();
            newConfig.put("servers", jsonArray1);
            LOGGER.info(newConfig.toString(4));
            FileUtils.write(new File(configFilePath), newConfig.toString(4), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startTomcat(VpnServer vpnServer, int port, String containerName) throws Exception {
        String serverAddr = vpnServer.getHostname();
        String startTomcat = "CONTAINER_NAME=%s\n" +
                "PORT=%s\n" +
                "if [ ! \"$(docker ps -q -f name=$CONTAINER_NAME)\" ]; then\n" +
                "    if [ \"$(docker ps -aq -f status=exited -f name=$CONTAINER_NAME)\" ]; then\n" +
                "        # cleanup\n" +
                "        docker rm $CONTAINER_NAME\n" +
                "    fi\n" +
                "    # run your container\n" +
                "    docker run -d --name=$CONTAINER_NAME -p $PORT:80 wizardmerlin/nginx-helloworld\n" +
                "fi";

        startTomcat = String.format(startTomcat, Ssh.escape(containerName), Ssh.escape(Integer.toString(port)));
        try {
            LOGGER.info("serverAddr: " + serverAddr);
            sshService.executeCommand(serverAddr, vpnServer.getSshPort(), vpnServer.getUsername(),
                    vpnServer.getPrivatekeyLocation(), vpnServer.getPassphrashLocation(), startTomcat);
        } catch (IOException e) {
            throw new Exception("failed to start tomcat", e);
        }
    }

    private void stopTomcat(VpnServer vpnServer, String containerName) {
        String serverAddr = vpnServer.getHostname();
        String stopTomcat = String.format("docker stop %s && docker rm %s", containerName, containerName);
        try {
            sshService.executeCommand(serverAddr, vpnServer.getSshPort(), vpnServer.getUsername(),
                    vpnServer.getPrivatekeyLocation(), vpnServer.getPassphrashLocation(), stopTomcat);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateDnsRecord(VpnServer vpnServer, String previousIpAddr) {
        LOGGER.info("updating DNS record");
        try {
            String zoneId = vpnServer.getDnsHosting().getZoneId("wizardmerlin.me");
            LOGGER.info("zoneId:" + zoneId);
            String type = "A";
            String name = vpnServer.getHostname();
            String dnsRecordId = vpnServer.getDnsHosting().getDnsRecordId(zoneId, type, name, previousIpAddr);
            LOGGER.info("dnsRecordId:" + dnsRecordId);
            vpnServer.getDnsHosting().updateDnsRecord(zoneId, dnsRecordId, type, name, vpnServer.getIpAddr());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AvailabilityTestService getAvailabilityTester() {
        return availabilityTester;
    }

    public void setAvailabilityTester(AvailabilityTestService availabilityTester) {
        this.availabilityTester = availabilityTester;
    }

    public SshService getSshService() {
        return sshService;
    }

    public void setSshService(SshService sshService) {
        this.sshService = sshService;
    }

    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public VpsProvider getDigitalOcean() {
        return digitalOcean;
    }

    public void setDigitalOcean(VpsProvider digitalOcean) {
        this.digitalOcean = digitalOcean;
    }

    public VpsProvider getVultr() {
        return vultr;
    }

    public void setVultr(VpsProvider vultr) {
        this.vultr = vultr;
    }

    public DnsHostingService getCloudflare() {
        return cloudflare;
    }

    public void setCloudflare(DnsHostingService cloudflare) {
        this.cloudflare = cloudflare;
    }
}
