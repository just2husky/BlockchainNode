package socket;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

import service.MessageService;
import service.TransactionService;

/**
 * Created by chao on 2017/11/25.
 */
public class Client {
    public static void main(String [] args)
    {
        String serverName = "127.0.0.1";
        int port = 8000;
        try
        {
            System.out.println("连接到主机：" + serverName + " ，端口号：" + port);
            Socket client = new Socket(serverName, port);
            System.out.println("远程主机地址：" + client.getRemoteSocketAddress());

            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            String txMsg = MessageService.genTxMsg("cliMsg", TransactionService.genTx("string", "测试")).toString();
            out.writeUTF(txMsg);

            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            System.out.println("服务器响应： " + in.readUTF());
            client.close();
        }catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
