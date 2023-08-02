package com.example.community.service;

import com.example.community.entity.DiscussPost;
import com.example.community.mapper.DiscussPostMapper;
import com.example.community.util.SensitiveFilter;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Lenovo
 */
@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    /**
     * Caffeine核心接口：Cache,LoadingCache,AsyncLoadingCache
     * 帖子列表缓存
     */
    private LoadingCache<String,List<DiscussPost>> postListCache;

    /**
     * 帖子总数缓存
     */
    private LoadingCache<Integer,Integer> postRowsCache;

    @PostConstruct
    public void init(){
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(String s) throws Exception {
                        if(s == null || s.length() == 0){
                            throw new IllegalArgumentException("参数错误！");
                        }

                        String[] params = s.split(":");
                        if(params == null || params.length != 2){
                            throw new IllegalArgumentException("参数错误！");
                        }

                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        // 此处可以先查二级缓存Redis，再查数据库


                        // 查数据库
                        logger.debug("load post list from DB");
                        return discussPostMapper.selectDiscussPosts(0,offset,limit,1);

                    }
                });

        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(Integer integer) throws Exception {
                        logger.debug("load post rows from DB");
                        return discussPostMapper.selectDiscussPostRows(integer);
                    }
                });


    }

    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit,int orderMode){
        // 当查询最热帖子时才从缓存中取，将最热帖子存到缓存是因为其相对变化不大
        if(userId == 0 && orderMode == 1){
            return postListCache.get(offset+":"+limit);
        }
        logger.debug("load post list from DB");
        return discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode);
    }

    public int findDiscussPostRows(int userId){
        if(userId == 0){
            return postRowsCache.get(userId);
        }
        logger.debug("load post rows from DB");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post){
        //判空处理
        if(post == null){
            throw new IllegalArgumentException("参数不能为空！");
        }

        //转义HTML标签,防止显示到浏览器出现显示错误，只需要处理标题和内容
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));

        //过滤敏感词,只需要处理标题和内容
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }


    /**
     * 根据id查询帖子
     * @param id
     * @return
     */
    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    /**
     * 修改评论数量，用于增加评论后修改对应帖子的评论数
     * @param id
     * @param commentCount
     * @return
     */
    public int updateCommentCount(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    public int updateType(int id,int type){
        return discussPostMapper.updateType(id, type);
    }

    public int updateStatus(int id,int status){
        return discussPostMapper.updateStatus(id, status);
    }

    public int updateScore(int id,double score){
        return discussPostMapper.updateScore(id, score);
    }

}
