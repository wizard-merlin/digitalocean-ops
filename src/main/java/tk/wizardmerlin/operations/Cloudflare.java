package tk.wizardmerlin.operations;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cloudflare extends DnsHostingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(Cloudflare.class);
    private String apiBaseUrl;
    private String authEmail;

    @Override
    public String getZoneId(String name) throws Exception {
        LOGGER.info("getting zone ID");
        HttpResponse<JsonNode> jsonResponse = Unirest.get(apiBaseUrl + "zones/?status={status}&name={name}&match={match}")
                .routeParam("status", "active")
                .routeParam("name", name)
                .routeParam("match", "all")
                .header("X-Auth-Key", getAuthToken())
                .header("X-Auth-Email", authEmail)
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .asJson();
        LOGGER.info("response: " + jsonResponse.getBody());
        return jsonResponse.getBody().getObject().getJSONArray("result").getJSONObject(0).getString("id");
    }

    @Override
    public String getDnsRecordId(String zoneId, String type, String name, String content) throws Exception {
        LOGGER.info("getting DNS record ID");
        HttpResponse<JsonNode> jsonResponse = Unirest.get(apiBaseUrl + "zones/{zoneId}/dns_records?type={type}&name={name}&content={content}&match={match}")
                .routeParam("zoneId", zoneId)
                .routeParam("type", type)
                .routeParam("name", name)
                .routeParam("content", content)
                .routeParam("match", "all")
                .header("X-Auth-Key", getAuthToken())
                .header("X-Auth-Email", authEmail)
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .asJson();
        LOGGER.info("response: " + jsonResponse.getBody());
        return jsonResponse.getBody().getObject().getJSONArray("result").getJSONObject(0).getString("id");
    }

    @Override
    public void updateDnsRecord(String zoneId, String dnsRecordId, String type, String name, String content) throws Exception {
        LOGGER.info("update the DNS record to " + type + " " + name + " " + content);
        JSONObject body = new JSONObject()
                .put("type", type)
                .put("name", name)
                .put("content", content);
        HttpResponse<JsonNode> jsonResponse = Unirest.put(apiBaseUrl + "zones/{zoneId}/dns_records/{dnsRecordId}")
                .routeParam("zoneId", zoneId)
                .routeParam("dnsRecordId", dnsRecordId)
                .header("X-Auth-Key", getAuthToken())
                .header("X-Auth-Email", authEmail)
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .body(body)
                .asJson();
        LOGGER.info("response: " + jsonResponse.getBody());
        if (!jsonResponse.getBody().getObject().getBoolean("success")) {
            throw new Exception("failed to update DNS record " + dnsRecordId);
        }
    }


    public String getAuthEmail() {
        return authEmail;
    }

    public void setAuthEmail(String authEmail) {
        this.authEmail = authEmail;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }
}
