package entity;

/**
 * Created by chao on 2017/11/9.
 */
public class NetAddress {
    private String ip;
    private int port;

    public NetAddress() {
    }

    public NetAddress(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public NetAddress(String ip) {
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
        return ip + ":" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NetAddress that = (NetAddress) o;

        if (port != that.port) return false;
        return ip.equals(that.ip);
    }

    @Override
    public int hashCode() {
        int result = ip.hashCode();
        result = 31 * result + port;
        return result;
    }
}
