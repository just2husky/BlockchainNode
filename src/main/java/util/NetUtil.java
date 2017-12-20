package util;

import entity.NetAddress;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by chao on 2017/11/10.
 */
public class NetUtil {
    private static String readIp;
    private final static List<NetAddress> validatorList = JsonUtil.getValidatorAddressList(Const.BlockChainNodesFile);
    static {
        String localip = null;// 本地IP，如果没有配置外网IP则返回它
        String netip = null;// 外网IP

        Enumeration<NetworkInterface> netInterfaces = null;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        InetAddress ip = null;
        boolean finded = false;// 是否找到外网IP
        assert netInterfaces != null;
        while (netInterfaces.hasMoreElements() && !finded) {
            NetworkInterface ni = netInterfaces.nextElement();
            Enumeration<InetAddress> address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                ip = address.nextElement();
                if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && !ip.getHostAddress().contains(":")) {// 外网IP
                    netip = ip.getHostAddress();
                    finded = true;
                    break;
                } else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
                        && !ip.getHostAddress().contains(":")) {// 内网IP
                    localip = ip.getHostAddress();
                }
            }
        }

        if (netip != null && !"".equals(netip)) {
            readIp = netip;
        } else {
            readIp = localip;
        }
    }

    public static String getRealIp() {
        return readIp;
    }

    /**
     * 以 map 的形式返回 ip 与 port
     * @return
     */
    public static Map<String, String> getPrimaryNode(){
        Map<String, String> map = new HashMap<String, String>();
        NetAddress va = validatorList.get(0);
        map.put("ip", va.getIp());
        map.put("port", String.valueOf(va.getPort()));
        return map;
    }

    /**
     * 以 *.*.*.*:**** 的形式返回主节点url
     * @return
     */
    public static String getPrimaryNodUrl(){
        return getPrimaryNode().get("ip") + ":" + getPrimaryNode().get("port");
    }
}
