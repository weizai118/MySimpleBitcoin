package pers.lansir.bitcon.webSocket;

/*-----------------Go,My Program----------------*/
/*
 *   @Project:    bitcon
 *   @Package:    pers.lansir.bitcon.webSocket
 *   @Author :     LanSir
 *   @Email:       helloworldlgr@gmail.com
 *   @Time :       2018/10/10 20:26
 *   @Description :
 */


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import pers.lansir.bitcon.domain.Block;
import pers.lansir.bitcon.domain.Message;
import pers.lansir.bitcon.domain.Note;

import java.net.InetSocketAddress;
import java.util.List;

public class MyWebSocketServer extends WebSocketServer {

    private int port;

    public MyWebSocketServer(int port) {
        super(new InetSocketAddress(port));
        this.port = port;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("服务器连接");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("服务器连接关闭");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("服务器通信"+message);

        try {
            if ("请给我发送一份数据".equals(message)){
                //获取note单例
                Note note = Note.getInstance();
                //获取数据List<Block>
                List<Block> blockList = note.showData();
                //将List<Block>转换为字符串
                ObjectMapper objectMapper = new ObjectMapper();
                String blockListString = objectMapper.writeValueAsString(blockList);
                //包装数据
                Message mes = new Message(1,blockListString);
                String messageString = objectMapper.writeValueAsString(mes);
                //将数据广播出去
                broadcast(messageString);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("服务器错误"+ex);
    }

    @Override
    public void onStart() {
        System.out.println("服务器开启");
    }
}
