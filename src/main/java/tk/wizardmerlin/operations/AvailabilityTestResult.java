package tk.wizardmerlin.operations;

public class AvailabilityTestResult {
    private String ipAddr;
    private int httpStatus;
    private double totalTimeInMs;
    private String city = "N/A";
    private String province = "N/A";
    private String ISP = "N/A";

    AvailabilityTestResult ipAddr(String ipAddr) {
        this.ipAddr = ipAddr;
        return this;
    }

    AvailabilityTestResult httpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    AvailabilityTestResult totalTimeInMs(double totalTimeInMs) {
        this.totalTimeInMs = totalTimeInMs;
        return this;
    }

    AvailabilityTestResult city(String city) {
        this.city = city;
        return this;
    }

    AvailabilityTestResult province(String province) {
        this.province = province;
        return this;
    }

    AvailabilityTestResult ISP(String ISP) {
        this.ISP = ISP;
        return this;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public double getTotalTimeInMs() {
        return totalTimeInMs;
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getISP() {
        return ISP;
    }

    @Override
    public String toString() {
        return "AvailabilityTestResult{" +
                "ipAddr='" + ipAddr + '\'' +
                ", httpStatus=" + httpStatus +
                ", totalTimeInMs=" + totalTimeInMs +
                ", city='" + city + '\'' +
                ", province='" + province + '\'' +
                ", ISP='" + ISP + '\'' +
                '}';
    }
}
