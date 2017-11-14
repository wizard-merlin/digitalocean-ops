package tk.wizardmerlin.operations;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class VpnServer {

    private String domain;
    private String subDomain;
    private String ipAddr;
    private VpsProvider vpsProvider;
    private DnsHostingService dnsHosting;
    private int sshPort;
    private String username;
    private String privatekeyLocation;
    private String passphrashLocation;

    public static VpnServer fromJSONObject(JSONObject object) {
        VpnServer server = new VpnServer();
        server.setDomain(object.getString("domain"));
        server.setSubDomain(object.getString("subDomain"));
        server.setIpAddr(object.getString("ipAddr"));
        server.setSshPort(object.getInt("sshPort"));
        server.setPassphrashLocation(object.getString("passphrashLocation"));
        server.setPrivatekeyLocation(object.getString("privatekeyLocation"));
        server.setUsername(object.getString("username"));
        return server;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPrivatekeyLocation() {
        return privatekeyLocation;
    }

    public void setPrivatekeyLocation(String privatekeyLocation) {
        this.privatekeyLocation = privatekeyLocation;
    }

    public String getPassphrashLocation() {
        return passphrashLocation;
    }

    public void setPassphrashLocation(String passphrashLocation) {
        this.passphrashLocation = passphrashLocation;
    }

    public DnsHostingService getDnsHosting() {
        return dnsHosting;
    }

    public void setDnsHosting(DnsHostingService dnsHosting) {
        this.dnsHosting = dnsHosting;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSubDomain() {
        return subDomain;
    }

    public void setSubDomain(String subDomain) {
        this.subDomain = subDomain;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public VpsProvider getVpsProvider() {
        return vpsProvider;
    }

    public void setVpsProvider(VpsProvider vpsProvider) {
        this.vpsProvider = vpsProvider;
    }

    public int getSshPort() {
        return sshPort;
    }

    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    public String getHostname() {
        return getSubDomain() + "." + getDomain();
    }

    public void changeIp() throws Exception {
        setIpAddr(vpsProvider.changeIp(getIpAddr()));
    }

    @Override
    public String toString() {
        return "VpnServer{" + "domain='" + domain + '\'' +
                ", subDomain='" + subDomain + '\'' +
                ", ipAddr='" + ipAddr + '\'' +
                ", vpsProvider=" + vpsProvider +
                ", dnsHosting=" + dnsHosting +
                ", sshPort=" + sshPort +
                ", username='" + username + '\'' +
                ", privatekeyLocation='" + privatekeyLocation + '\'' +
                ", passphrashLocation='" + passphrashLocation + '\'' +
                '}';
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject(this);
        jsonObject.remove("vpsProvider");
        jsonObject.remove("dnsHosting");
        return jsonObject;
    }
}
