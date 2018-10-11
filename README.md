# MySimpleBitcoin
use java 
####  

> 随着比特币的出现,去中心化的概念处处皆是,那么比特币的底层原理究竟是什么呢?试着用代码(java)的形式来查看比特币的底层.也对区块链的底层实现原理有个初步的概念!

###  **基本实现思路:**

```
1.完成基本的首页(创世区块)和交易内容添加,首页和交易内容信息的展示(查询),以及数据的保存;

2.添加数据校验功能;
	2.1通过数字签名的方式检验,运用SHA256;
	2.2通过hash值进行比较
	
3.添加工作量证明(也就是挖矿):找到以"0000"开头的Hash值.hash运算对象为当前交易内容+上一区块hash值+运算次数;

4.封装交易数据,并标记信息;

5.添加生成数字签名方法(在页面中生成);

6.使用WebSocket通信,注册节点和连接功能;

7.实现各个客户端/服务器之间区块链的数据同步更新功能;

以上是大体思路,其中还有诸多细节需要注意!这也只是一个最最基本的版本,但是按照区块链的思想去实现的.
```



###一.基本模块功能实现

#### 1.基本逻辑功能实现

##### 1.1添加信息功能

1.1.1添加创世区块

```java
 //添加首页:也就是给账本起名字,相当于比特币中的创世区块
    public void addGenesis(String genesis) {
        if (list.size() > 0) {//在账本是新的时候添加创世块首页
            throw new RuntimeException("添加创世块名称失败");
        }
        String preHash = "0000000000000000000000000000000000000000000000000000000000000000";
        //工作量证明,也就是挖矿
        int nonce = mine(genesis, preHash);
        //添加数据
        list.add(new Block(
                list.size() + 1,
                genesis,
                HashUtils.sha256(genesis + nonce + preHash),
                nonce,
                preHash
        ));
        //添加数据后,便将数据保存
        save2Disk();
    }
```

1.1.2添加交易记录相关内容

```java
//添加交易记录:相当于比特币中的区块.前提是账本必须已经有了名字
    public void addNote(String content) {
        if (list.size() < 1) {
            throw new RuntimeException("添加交易记录失败");
        }
        Block preBlock = list.get(list.size() - 1);
        String preBlockHash = preBlock.getHash();
        int nonce = mine(content, preBlockHash);
        list.add(new Block(list.size() + 1, content, HashUtils.sha256(content + nonce + preBlockHash), nonce, preBlockHash));
        //添加数据时就将数据保存下来
        save2Disk();
    }
```

#####  1.2展现信息的功能(也就是查询)

```java
//展示数据
    public List<Block> showData() {
       /* for (String s : list) {
            System.out.println(s);
        }*/
        return list;
    }
```

##### 1.3数据的保存功能

```java
    //保存数据:采用json格式保存数据(三种常用的库gson,fastson,jackson)
    //目前通常都是使用json保存数据,ObjectMapper(jackson)是springboot内置的json库;
    public void save2Disk() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            //将账本中数据写到a.json文件中
            objectMapper.writeValue(new File("a.json"), list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
```

#### 2.添加Controller与页面交互

##### 2.1添加创世区块

```java
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
```

##### 2.2添加交易记录

注意:数据需要以JSON形式传输至页面,可使用Gson/fastson/jackson;这里使用SpringBoot自带的ObjectMapper类;

```java
    @PostMapping("/addNote")
    public String addNote(Transaction transaction) {
        if (transaction==null){
            return "请填写交易信息";
        }
        System.out.println(transaction);
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
```

##### 2.3数据展示

```java
    @GetMapping("/showList")
    public List<Block> showList() throws Exception {
        Thread.sleep(1000);
        return note.showData();
    }
```

### 二.数据安全及校验

####  1.创建Bean类存储交易信息

##### 1.1创建Block实体类

```java
package pers.lansir.bitcon.domain;
/*-----------------Go,My Program----------------*/
/*
 *   @Project:    bitcon
 *   @Package:    pers.lansir.bitcon.domain
 *   @Author :     LanSir
 *   @Email:       helloworldlgr@gmail.com
 *   @Time :       2018/10/9 21:02
 *   @Description : Block Bean : 使用对象存储交易信息
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
    ...............................省略了set/get...............................
}

```

#### 2.校验功能

通过Hash256校验相邻区块hash值,在下面工作量证明中会简单说明区块间hash值的联系;

**从上面图可以看出,每一个区块的hash值都是和上一个区块hash值存在紧密联系的,因此,一旦中间某个区块数据被篡改,那么后面的区块都会出现问题;这也就是所说的需要拥有全球51%的算力才有可能攻击比特币**

```java
	//校验数据是否被篡改
    public String verify() {
        //被篡改的数据可能有多个,因此需要使用stringBuilder或stringBuffer存储
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            Block block = list.get(i);
            String content = block.getContent();
            String preHash = block.getPreHash();
            int nonce = block.getNonce();
            int id = block.getId();
            String blockHash = block.getHash();
            String utilHash = HashUtils.sha256(content + nonce + preHash);
            if (!utilHash.equals(blockHash)) {
                sb.append("ID为:" + id + "的数据有可能已被篡改,请注意检查<br/>");
            }
            //验证preHash
            if (i > 0) {
                Block preBlock = list.get(list.size() - 1);
                String preBlockHash = preBlock.getHash();
                if (!preBlockHash.equals(preHash)) {
                    sb.append("ID为:" + id + "的数据有可能已被篡改,请注意检查<br/>");
                }
            }
        }
        return sb.toString();
    }
```

![](C:\Users\蓝Sir\Desktop\区块链数据校验.PNG)

### 三.工作量证明(俗称挖矿)

#### 1.添加工作量证明方法

通过交易内容 content+穷举次数 i+上一个区块Hash值 来计算当前区块满足以"0000"开头的Hash值;

下面展示了挖矿的计算!

```java
	//工作量证明:通过穷举获得0000开头的哈希值
    public int mine(String content, String preHash) {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            String newHash = HashUtils.sha256(content + i + preHash);
            if (newHash.startsWith("0000")) {
                System.out.println("挖矿成功,共计算" + i + "次");
                return i;
            } else {
                System.out.println("这是第" + i + "次挖矿失败");
            }
        }
        throw new RuntimeException("挖矿失败");
    }
```

![](C:\Users\蓝Sir\Desktop\挖矿.PNG)

### 四.形成区块

完善相关功能及字段

#### 1.完善Block类字段;

```java
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
```

####  2.完善上述功能;

####3.编写页面中的方法

```html
<div class="form-group">
    <label>请输入内容</label>
    <input type="text" class="form-control" id="inputContent">
</div>
<div class="btn-group btn-group-lg">
    <button type="button" class="btn btn-default" onclick="addGenesis()">添加封面</button>
    <button type="button" class="btn btn-default" onclick="addNote()">添加记录</button>
    <button type="button" class="btn btn-default" onclick="showList()">展示数据</button>
    <button type="button" class="btn btn-default" onclick="verify()">校验数据</button>
</div>
<p class="bg-success" id="result">sucess</p>
<div class="bs-example" data-example-id="simple-table">
    <table class="table">
        <thead>
        <tr>
            <th>ID</th>
            <th>记录</th>
            <th>当前区块hash值</th>
            <th>工作量证明</th>
            <th>上一个区块hash值</th>
        </tr>
        </thead>
        <tbody id="Tbody">

        </tbody>
    </table>
</div>

function addGenesis() {
        //展示进度条
        loading.baosight.showPageLoadingMsg(false);
        var genesis = $("#inputContent").val();
        $.post("addGenesis",
            "genesis="+genesis,
            function (data) {
                $("#result").html(data);
                //清空输入框
                $("#inputContent").val("");
                //执行完函数后隐藏进度条
                loading.baosight.hidePageLoadingMsg();
                showList()
            });
    }
    function addNote() {
        loading.baosight.showPageLoadingMsg(false);
        var content = $("#inputContent").val();//获取交易内容
        var senderPublickey = $("#senderPublickey").val();//获取发送者公钥
        var senderPrivatekey = $("#senderPrivatekey").val();//获取发送者私钥
        var receiverPublickey = $("#receiverPublickey").val();//获取接受者公钥

        //生成发送者私钥
        var prvKey = KEYUTIL.getKey(senderPrivatekey);
        //指定数字签名的算法
        var sig = new KJUR.crypto.Signature({"alg": "SHA256withRSA"});
        ///初始化私钥
        sig.init(prvKey);
        //指定原文
        sig.updateString(content);
        //生成签名
        var sigValueHex = sig.sign();

        $.post("addNote",
            {
                senderPubKey:senderPublickey,
                receiverPubKey:receiverPublickey,
                senderSign:sigValueHex,
                content:content
            },
            function (data) {
                $("#result").html(data);
                //清空输入框
                $("#inputContent").val("");
                loading.baosight.hidePageLoadingMsg();
                showList();
            })
    }
	function showList() {
        loading.baosight.showPageLoadingMsg(false);
        $.get("showList",
            function (data) {
                $("#Tbody").html("");
                for (var i = 0; i < data.length; i++) {
                    var block = data[i];
                    var id = block.id;
                    var content = block.content;
                    var hash = block.hash;
                    var nonce = block.nonce;
                    var preHash = block.preHash;
                    $("#Tbody").append("<tr><td>"+id+"</td><td>"+content+"</td><td>"+hash+"</td><td>"+nonce+"</td><td>"+preHash+"</td></tr>")
                }
                // $("#result").html(data);
                loading.baosight.hidePageLoadingMsg()
            })
    }
    function verify() {
        loading.baosight.showPageLoadingMsg(false);
        $.get("verify",
            function (data) {
                $("#result").html(data);
                loading.baosight.hidePageLoadingMsg()
            })
    }
                                                
	//入口函数
    //作用:一进入页面就显示加载的数据
    $(function () {
        showList();
    })
```

### 五.转账功能

#### 1.构建Transaction Bean

```java
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
    }

    public Transaction() {
    }

    public Transaction(String senderPubKey, String receiverPubKey, String senderSign, String content) {
        this.senderPubKey = senderPubKey;
        this.receiverPubKey = receiverPubKey;
        this.senderSign = senderSign;
        this.content = content;
    }
	.................此处省略set/get.......................
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
```

#### 2.页面中添加生成数字签名方法

参照下面方法进行修改在页面代码中修改

```javascript
    function getData() {
        //获取id为content的标签中内容
        var str = $("#content").val()
        console.log(str)
        //获取私钥
        var prvKey = KEYUTIL.getKey(privkey);
        //sha256加密签名对象
        var sig = new KJUR.crypto.Signature({"alg": "SHA256withRSA"});
        //初始化私钥
        sig.init(prvKey);
        //传入原文
        sig.updateString(str)
        //生成签名
        var sigValueHex = sig.sign()
        console.log(sigValueHex);
    }
```

###  六.WebSocket通信

#### 1.节点注册功能

```java
    //定义HashSet集合,存储注册的port
    Set<String> set = new HashSet<>();

    //注册
    @RequestMapping("/register")
    public String register(String port){
        set.add(port);
        return "register sucess~~~";
    }
```

```javascript
 function register() {
        loading.baosight.showPageLoadingMsg(false) // 显示进度条
        // 1.获取输入的内容
        var port = $("#inputContent").val();
        // 2. 发起请求
        $.post("register", // 请求路径
            "port=" + port,// 传递的数据
            function (data) { // 请求成功的回调函数
                // 展示操作结果
                $("#result").html(data)
                // 清空输入框
                $("#inputContent").val("");
                loading.baosight.hidePageLoadingMsg() // 隐藏进度条
            });
    }
```

#### 2.连接功能

21.服务器连接

```java
    public MyWebSocketServer server ;
    //连接服务器,
    @PostConstruct//在创建Controller的时候就执行下面代码
    public void init(){
        //创建服务器
        server = new MyWebSocketServer(Integer.parseInt(BitconApplication.port)+1);
        //连接服务器
        server.start();
    }
```

2.2客户端连接

```java
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
```

```javascript
function connect() {
        loading.baosight.showPageLoadingMsg(false) // 显示进度条
        $.post("connect", // 请求路径
            function (data) { // 请求成功的回调函数
                // 展示操作结果
                $("#result").html(data)
                loading.baosight.hidePageLoadingMsg() // 隐藏进度条
            });
    }
```

### 七.数据同步功能及广播

#### 1.添加Message Bean封装数据

```java
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
```

#### 2.添加同步方法

```java
	//同步数据
    @RequestMapping("/syncData")
    public String syncData(){
        for (MyWebSocketClient client : clients) {
            client.send("请给我发送一份数据");
        }
        return "同步数据发送成功";
    }
```

#### 3.WebSocket客户端和服务器收发信息

服务器:Server

```javascript
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
```



客户端:Client

```java
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
```
