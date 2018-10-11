package pers.lansir.bitcon.domain;

/*-----------------Go,My Program----------------*/
/*
 *   @Project:    bitcon
 *   @Package:    pers.lansir.bitcon.domain
 *   @Author :     LanSir
 *   @Email:       helloworldlgr@gmail.com
 *   @Time :       2018/10/9 21:02
 *   @Description :
 */


public class Block {
    private int id;//当前交易id
    private String content;//交易内容
    private String hash;//当前交易产生的hash值
    private int nonce;//工作量证明
    private String preHash;//上一份交易记录的hash值

    public Block(int id, String content, String hash, int nonce, String preHash) {
        this.id = id;
        this.content = content;
        this.hash = hash;
        this.nonce = nonce;
        this.preHash = preHash;
    }

    public Block() {
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPreHash() {
        return preHash;
    }

    public void setPreHash(String preHash) {
        this.preHash = preHash;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
