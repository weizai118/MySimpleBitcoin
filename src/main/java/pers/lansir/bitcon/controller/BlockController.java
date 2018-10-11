package pers.lansir.bitcon.controller;

/*-----------------Go,My Program----------------*/
/*
 *   @Project:    bitcon
 *   @Package:    pers.lansir.bitcon
 *   @Author :     LanSir
 *   @Email:       helloworldlgr@gmail.com
 *   @Time :       2018/10/9 20:08
 *   @Description :
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pers.lansir.bitcon.BitconApplication;
import pers.lansir.bitcon.domain.Block;
import pers.lansir.bitcon.domain.Message;
import pers.lansir.bitcon.domain.Note;
import pers.lansir.bitcon.domain.Transaction;
import pers.lansir.bitcon.webSocket.MyWebSocketClient;
import pers.lansir.bitcon.webSocket.MyWebSocketServer;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class BlockController {

    private Note note = Note.getInstance();

    @PostMapping("/addGenesis")
    public String addGenesis(String genesis) {
        try {
            note.addGenesis(genesis);
            return "添加成功";
        } catch (Exception e) {
            e.printStackTrace();
            return "添加失败" + e.getMessage();
        }
    }

//    @PostMapping("/addNote")
//    public String addNote(String content){
//        try {
//            note.addNote(content);
//            return "添加成功";
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "添加失败"+e.getMessage();
//        }
//    }

    @PostMapping("/addNote")
    public String addNote(Transaction transaction) {
        if (transaction==null){
            return "请填写交易信息";
        }
        System.out.println(transaction);
        //Transaction{senderPubKey='null',
        // senderSign='59cefe1c9e0bf9825eafdf792515168aafeecf44b3ec89e44e7f3ba34100fa57467ccda0394d93dac440ae87624430271990dc1ef065ca273425fdfa2a2e698396260c7b0e4c758d8c43249aa29544757a636e74d69704c108a6c6c32a69f479d463bea194bdb0d3448221db92d00098db354a6f4998f3da9530159f24e3bc269ee6feacab3a9efcbf99a80aa5f02a46e248bce1a8b5836b9ab6f2ab55e190b3eaf9992365bb3fda62d8a76693e65a48db60f3e86aeab6563ae967d37df8756337cc915ec7cff6ce58ced9b8a3d8be668a244a1c591200c42fe0b370ceb46ff75ed57c5b4bac1e4a79e8da723c0ed1c6687585ecf512e006eb4f78e91a62260d',
        // receiverPubKey='null', content='222'}
        try {
            if (transaction.verify()) {
                //包装数据
                Message mess= new Message(4, transaction.getContent());
                ObjectMapper objectMapper = new ObjectMapper();
                //把数据写成JSON格式
                String msg = objectMapper.writeValueAsString(mess);
                //将交易信息广播出去
                server.broadcast(msg);
                note.addNote(transaction.getContent());
                return "添加交易信息成功";
            } else {
                return "校验为不合法交易,添加失败";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "添加交易信息失败"+e.getMessage();
        }
    }

    @GetMapping("/showList")
    public List<Block> showList() throws Exception {
        Thread.sleep(1000);
        return note.showData();
    }

    @GetMapping("/verify")
    public String verify() throws Exception {
        String verify = note.verify();
        if (!StringUtils.isEmpty(verify)) {
            return verify;
        }
        return "未发现数据被篡改";
    }

    //定义HashSet集合,存储注册的port
    Set<String> set = new HashSet<>();

    //注册
    @RequestMapping("/register")
    public String register(String port){
        set.add(port);
        return "register sucess~~~";
    }
    //定义list集合,存储client
    List<MyWebSocketClient> clients = new ArrayList<>();
    //连接
    @RequestMapping("/connect")
    public String connect(){
        //遍历虽有已注册的服务器node,并连接
        try {
            for (String node : set) {
                URI uri = new URI("ws://localhost:"+node);
                //创建客户端,并连接
                MyWebSocketClient myWebSocketClient = new MyWebSocketClient(uri,node);
                myWebSocketClient.connect();
                //将客户端存储到集合中
                clients.add(myWebSocketClient);
                return "connect sucessfully~~~";
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return "sucess fail~~";
    }

    public MyWebSocketServer server ;
    //连接服务器,
    @PostConstruct//在创建Controller的时候就执行下面代码
    public void init(){
        //创建服务器
        server = new MyWebSocketServer(Integer.parseInt(BitconApplication.port)+1);
        //连接服务器
        server.start();
    }

    //广播数据
    @RequestMapping("/broadcast")
    public String broadcast(String msg){
        server.broadcast(msg);
        return "broadcast sucess~~~";
    }
    //同步数据
    @RequestMapping("/syncData")
    public String syncData(){
        for (MyWebSocketClient client : clients) {
            client.send("请给我发送一份数据");
        }
        return "同步数据发送成功";
    }

}
