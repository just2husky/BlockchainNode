package util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chao on 2017/12/9.
 */
public class MerkleTree {

    // transaction List
    private List<String> txList;
    // Merkle Root
    private String root;

    /**
     * constructor
     * @param txList transaction List
     */
    public MerkleTree(List<String> txList) {
        this.txList = txList;
        build();
    }

    /**
     * execute merkle_tree and set root.
     */
    public void build() {

        List<String> tempTxList = new ArrayList<String>();

        for (int i = 0; i < this.txList.size(); i++) {
            tempTxList.add(this.txList.get(i));
        }

        List<String> newTxList = getNewTxList(tempTxList);
        while (newTxList.size() != 1) {
            newTxList = getNewTxList(newTxList);
        }

        this.root = newTxList.get(0);
    }

    /**
     * return Node Hash List.
     * @param tempTxList
     * @return
     */
    private List<String> getNewTxList(List<String> tempTxList) {

        List<String> newTxList = new ArrayList<String>();
        int index = 0;
        while (index < tempTxList.size()) {
            // left
            String left = tempTxList.get(index);
            index++;

            // right
            String right = "";
            if (index != tempTxList.size()) {
                right = tempTxList.get(index);
            }

            // sha2 hex value
            String hashValue = hashAndEncodeBase64(left + right);
            newTxList.add(hashValue);
            index++;

        }

        return newTxList;
    }

    /**
     * return base64 string
     * @param str
     * @return
     */
    public String hashAndEncodeBase64(String str) {
        byte[] cipher_byte;
        try{
            MessageDigest md = MessageDigest.getInstance(Const.HASH_ALG);
            md.update(str.getBytes());
            cipher_byte = md.digest();
            return Base64.encodeBase64String(cipher_byte);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Get Root
     * @return
     */
    public String getRoot() {
        return this.root;
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
        for(int i=0; i<10; i++) {
            list.add(i+"");
        }

        MerkleTree mt = new MerkleTree(list);
        System.out.println(mt.getRoot());
    }
}