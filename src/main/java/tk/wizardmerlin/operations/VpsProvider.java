package tk.wizardmerlin.operations;

import org.springframework.stereotype.Component;

@Component
public abstract class VpsProvider {
    private String authToken;

    public abstract String changeIp(String originalIp) throws Exception;

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
