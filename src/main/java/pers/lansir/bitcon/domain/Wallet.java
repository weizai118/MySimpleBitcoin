package pers.lansir.bitcon.domain;

/*-----------------Go,My Program----------------*/
/*
 *   @Project:    bitcon
 *   @Package:    pers.lansir.bitcon.domain
 *   @Author :     LanSir
 *   @Email:       helloworldlgr@gmail.com
 *   @Time :       2018/10/10 12:20
 *   @Description : 钱包,主要功能是生成私钥与秘钥,以及签名;
 *   实际应用中,该钱包没有作用.只是用来在开发时生成公钥/私钥开发使用!
 *   公钥私钥由页面生成!!!
 */
import com.sun.org.apache.xml.internal.security.utils.Base64;
import pers.lansir.bitcon.utils.RSAUtils;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Wallet {
    private PublicKey pubKey;
    private PrivateKey priKey;
    private String name;

    //测试:生成公钥私钥
    public static void main(String[] args) {
        Wallet a = new Wallet("a");
        Wallet b = new Wallet("b");

    }

    //获取签名
    public Transaction sendTransaction(String receiverPubKey, String content){
        String sendPubKey = Base64.encode(pubKey.getEncoded());
        String signature = RSAUtils.getSignature("SHA256withRSA", priKey, content);
        return new Transaction(sendPubKey,signature,receiverPubKey,content);
    }
    //在钱包创建的时候就生成公钥以及私钥
    public Wallet(String name) {
        this.name = name;
        File priKeyFile = new File(name+".pri");
        File pubKeyFile = new File(name+".pub");
        if (!priKeyFile.exists() || priKeyFile.length()==0 || !pubKeyFile.exists()||pubKeyFile.length()==0 ){
            RSAUtils.generateKeysJS("RSA",name+".pri",name+".pub");
        }
    }

    public PublicKey getPubKey() {
        return pubKey;
    }

    public void setPubKey(PublicKey pubKey) {
        this.pubKey = pubKey;
    }

    public PrivateKey getPriKey() {
        return priKey;
    }

    public void setPriKey(PrivateKey priKey) {
        this.priKey = priKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
