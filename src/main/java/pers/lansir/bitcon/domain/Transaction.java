package pers.lansir.bitcon.domain;

/*-----------------Go,My Program----------------*/
/*
 *   @Project:    bitcon
 *   @Package:    pers.lansir.bitcon.domain
 *   @Author :     LanSir
 *   @Email:       helloworldlgr@gmail.com
 *   @Time :       2018/10/10 12:20
 *   @Description : 交易内容
 */

//发送发公钥
//发送方签名
//收款方公钥
//金额 Amount
import pers.lansir.bitcon.utils.RSAUtils;
import java.security.PublicKey;

public class Transaction {

    private String senderPubKey;//发送发公钥
    private String receiverPubKey;//接收人公钥
    private String senderSign;//发送人数字签名
    private String content;//交易内容

    //通过数字签名校验交易是否合法
    public boolean verify(){
        //获取发送人的公钥
        PublicKey senderPublicKey = RSAUtils.getPublicKeyFromString("RSA", senderPubKey);
        //校验发送人的数字签名是否合法
        return RSAUtils.verifyDataJS("SHA256withRSA",senderPublicKey,content,senderSign);

//        PublicKey publicKey = RSAUtils.getPublicKeyFromString("RSA", senderAddress);
//        return RSAUtils.verifyDataJS("SHA256withRSA", publicKey, content, signature);
    }

    public Transaction() {
    }

    public Transaction(String senderPubKey, String receiverPubKey, String senderSign, String content) {
        this.senderPubKey = senderPubKey;
        this.receiverPubKey = receiverPubKey;
        this.senderSign = senderSign;
        this.content = content;
    }

    public String getSenderPubKey() {
        return senderPubKey;
    }

    public void setSenderPubKey(String senderPubKey) {
        this.senderPubKey = senderPubKey;
    }

    public String getSenderSign() {
        return senderSign;
    }

    public void setSenderSign(String senderSign) {
        this.senderSign = senderSign;
    }

    public String getReceiverPubKey() {
        return receiverPubKey;
    }

    public void setReceiverPubKey(String receiverPubKey) {
        this.receiverPubKey = receiverPubKey;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "senderPubKey='" + senderPubKey + '\'' +
                ", senderSign='" + senderSign + '\'' +
                ", receiverPubKey='" + receiverPubKey + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
