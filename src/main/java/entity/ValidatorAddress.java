package entity;

/**
 * Created by chao on 2017/11/9.
 */
public class ValidatorAddress {
    private String ip;
    private int port;

    public ValidatorAddress() {
    }

    public ValidatorAddress(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public ValidatorAddress(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "ValidatorAddress{" +
                "ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                '}';
    }
}
