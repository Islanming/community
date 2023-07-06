package com.example.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 敏感词过滤器
 */
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符
    private static final String REPLACEMENT = "***";

    //根节点
    private TrieNode rootNode = new TrieNode();

    /**
     * 初始化，当容器实例化这个bean时，就调用这个方法完成前缀树的初始化
     */
    @PostConstruct
    public void init(){
        try(
            //字节流，读取敏感词文件，通过类加载器从类路径下读取，在编译后文件都会在classes文件夹下
            InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");

//            InputStream resourceAsStream =new FileInputStream("sensitive-words.txt");
            //将字节流转成字符流在转成缓冲流
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
        ){
            String keyword;
            while ((keyword = bufferedReader.readLine())!=null){
                //添加到前缀树
//                System.out.println(keyword);
                this.addKeyword(keyword);
            }

        }catch (IOException e){
            logger.error("加载敏感词文件失败："+e.getMessage());
        }

    }

    /**
     * 将一个敏感词添加到前缀树中
     * @param keyword
     */
    private void addKeyword(String keyword){
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            //判断是否已经存在该字符的结点
            if(subNode == null){
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }

            //指向子节点，进入下一循环
            tempNode = subNode;

            //设置结束标识
            if(i == keyword.length()-1){
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤文本
     * @return 过滤后文本
     */
    public String filter(String text){
        //空值处理
        if(StringUtils.isBlank(text)){
            return null;
        }

        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //结果
        StringBuilder stringBuilder = new StringBuilder();

        while (begin < text.length()){
            if(position < text.length()){
                char c = text.charAt(position);
                //跳过符号
                if(isSymbol(c)){
                    //若指针1指向根节点，将此符号计入结果，让指针2走向下一步
                    if(tempNode == rootNode){
                        stringBuilder.append(c);
                        begin++;
                    }
                    //无论符号在开头或中间，指针3都向下走一步
                    position++;
                    continue;
                }

                //检查下级结点
                tempNode = tempNode.getSubNode(c);
                if(tempNode == null){
                    //以begin指向的字符开头的字符串不是敏感词
                    stringBuilder.append(text.charAt(begin));
                    //进入下一个位置
                    position = ++begin;
                    //指针1重新指向根节点
                    tempNode = rootNode;

                }else if(tempNode.isKeyWordEnd()){
                    //发现敏感词，将begin~position字符串替换掉
                    stringBuilder.append(REPLACEMENT);
                    //进入下一位置
                    begin = ++position;
                    //指针1重新指向根节点
                    tempNode = rootNode;

                }else {
                    //检查下一个字符
                    if(position<text.length()-1){
                        position++;
                    }
                }
            }else {
                stringBuilder.append(text.charAt(begin));
                position = ++begin;
                //指针1重新指向根节点
                tempNode = rootNode;
            }
        }

        return stringBuilder.toString();

    }

    /**
     * 判断是否为符号
     * @param c
     * @return
     */
    private boolean isSymbol(Character c){
        //0x2e800~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    /**
     * 定义前缀树
     */
    private class TrieNode{

        //关键词结束标识
        private boolean isKeyWordEnd = false;

        //子节点（key是下级字符，value下级节点）
        private Map<Character,TrieNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd(){
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd){
            isKeyWordEnd = keyWordEnd;
        }


        //添加子节点
        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }

    }

}
