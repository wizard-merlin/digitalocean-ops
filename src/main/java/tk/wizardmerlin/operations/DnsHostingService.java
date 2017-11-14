package tk.wizardmerlin.operations;

public abstract class DnsHostingService {
    private String authToken;

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public abstract String getZoneId(String domainName) throws Exception;

    public abstract String getDnsRecordId(String zoneId, String type, String name, String content) throws Exception;

    public abstract void updateDnsRecord(String zoneId, String dnsRecordId, String type, String name, String content) throws Exception;
}
