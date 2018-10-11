package pers.lansir.bitcon.domain;

/*-----------------Go,My Program----------------*/
/*
 *   @Project:    bitcon
 *   @Package:    pers.lansir.bitcon.domain
 *   @Author :     LanSir
 *   @Email:       helloworldlgr@gmail.com
 *   @Time :       2018/10/10 15:43
 *   @Description :
 */


public class Message {
    //code:用来标识消息
    //1.传递的是区块链数据
    //2.传递的是节点数据
    //3.区块
    //4.交易的数据
    public int code;
    //交易区块内容
    public String msg;

    public Message() {
    }

    public Message(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
