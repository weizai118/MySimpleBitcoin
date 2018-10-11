package pers.lansir.bitcon.webSocket;

/*-----------------Go,My Program----------------*/
/*
 *   @Project:    bitcon
 *   @Package:    pers.lansir.bitcon.webSocket
 *   @Author :     LanSir
 *   @Email:       helloworldlgr@gmail.com
 *   @Time :       2018/10/10 20:25
 *   @Description :
 */


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.util.StringUtils;
import pers.lansir.bitcon.domain.Block;
import pers.lansir.bitcon.domain.Message;
import pers.lansir.bitcon.domain.Note;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class MyWebSocketClient extends WebSocketClient {

    private String name;

    public MyWebSocketClient(URI serverUri, String name) {
        super(serverUri);
        this.name = name;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println(name+"连接成功");
    }

    @Override
    public void onMessage(String message) {
        System.out.println(name+"收发消息"+message);
        //获Note单例
        Note note = Note.getInstance();
        try {
            if (!StringUtils.isEmpty(message)){
                //message是这种数据new Message(1,blockListString);
                //将Message字符串转回对象形式
                ObjectMapper objectMapper = new ObjectMapper();
                Message mes = objectMapper.readValue(message, Message.class);
                if (mes.code==1){
                    JavaType javaType =
                            objectMapper.getTypeFactory()
                                    .constructParametricType(List.class, Block.class);
                    List<Block> newBlockList = objectMapper.readValue(mes.msg, javaType);
                    note.checkNewBlockList(newBlockList);
                }else if (mes.code == 4){
                    //获取交易记录
                    String transactionMsg =  mes.msg;
                    //添加交易记录
                    note.addNote(transactionMsg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println(name+"关闭连接");
    }

    @Override
    public void onError(Exception ex) {
        System.out.println(name+"异常"+ex);
    }
}
