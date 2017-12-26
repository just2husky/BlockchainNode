package socket;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import entity.Transaction;
import service.TransactionMessageService;
import service.TransactionService;

/**
 * Created by chao on 2017/11/25.
 */
public class Client {

    public static void main(String[] args) {
        TransactionMessageService txMsgService = TransactionMessageService.getInstance();
        String serverName = "127.0.0.1";
        int port = 8000;
        try
        {
            System.out.println("连接到主机：" + serverName + " ，端口号：" + port);
            Socket client = new Socket(serverName, port);
            System.out.println("远程主机地址：" + client.getRemoteSocketAddress());

            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            List<Transaction> txList = new ArrayList<Transaction>();
            txList.add(TransactionService.genTx("string", "测试"));
            String txMsg = txMsgService.genInstance(txList).toString();
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
