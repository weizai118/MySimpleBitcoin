package pers.lansir.bitcon.domain;

/*-----------------Go,My Program----------------*/
/*
 *   @Project:    myBitcon
 *   @Package:    pers.lansir.bitcon.domain
 *   @Author :     LanSir
 *   @Email:       helloworldlgr@gmail.com
 *   @Time :       2018/10/9 9:32
 *   @Description :
 */


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import pers.lansir.bitcon.utils.HashUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Note {

    //NoteD单例模式
    private static volatile Note instance;

    //懒汉式单例
    public static Note getInstance() {
        if (instance == null) {
            synchronized (Note.class) {
                instance = new Note();
            }
        }
        return instance;
    }

    private List<Block> list = new ArrayList<>();

    //构造函数私有化,(为了构造单例)
    private Note() {
        //在note创建的时候,就读取json文件中的数据
        try {
            File file = new File("a.json");
            if (file.exists() && file.length() > 0) {
                ObjectMapper objectMapper = new ObjectMapper();
                //因为保存的json数据时集合形式,因此需要使用javaType
                JavaType javaType = objectMapper.getTypeFactory().constructParametricType(List.class, Block.class);//集合的class字节码和里面泛型的字节码
                list = objectMapper.readValue(file, javaType);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    //添加首页:也就是给账本起名字,相当于比特币中的创世区块
    public void addGenesis(String genesis) {
        if (list.size() > 0) {//在账本是新的时候添加创世块首页
            throw new RuntimeException("添加创世块名称失败");
        }
        String preHash = "0000000000000000000000000000000000000000000000000000000000000000";
        int nonce = mine(genesis, preHash);
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

    //展示数据
    public List<Block> showData() {
       /* for (String s : list) {
            System.out.println(s);
        }*/
        return list;
    }

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

    //校验数据是否被篡改
    public String verify() {
        //被篡改的数据可能有多个,因此需要使用stringBuilder或stringBuffer存储
        StringBuilder sb = new StringBuilder();
//        for (Block block : list) {
//            //获取交易内容
//            String content = block.getContent();
//            int id = block.getId();
//            //获取工作量证明
//            int nonce = block.getNonce();
//            //获取已经保存的hash值
//            String blockHash = block.getHash();
//            //当前区块的上一个hash值
//            String preHash = block.getPreHash();
//            //根据交易内容计算hash值,与已经保存的hash值进行比较
//            String utilHash = HashUtils.sha256(content + nonce + preHash);
//            if (!blockHash.equals(utilHash)) {
//                //记录数据被篡改的数据的id,
//                sb.append("ID为:" + id + "的数据有可能已被篡改,请注意检查<br/>");
//            }
//
//            //验证preHash
//
//        }
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

//    //测试
//    public static void main(String[] args) {
//        Note note = new Note();
//        note.addGenesis("创世币");
//        note.addNote("张三给李四转发100086个创世币");
//        note.showData();
//        note.save2Disk();
//        System.out.println(Integer.MAX_VALUE);
//    }

    //在获取其他人发来的数据之后,对数据进行判断再存储
    //1.判断数据的长度;
    //2.校验数据是否合法
    public void checkNewBlockList(List<Block> newBlockList) {
        if (list.size() < newBlockList.size()) {
            list = newBlockList;
        }
    }
}
