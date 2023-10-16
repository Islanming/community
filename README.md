# community
项目名称：小牛论坛
技术选型：Spring Boot、SSM、Redis、Kafka、ElasticSearch、Spring Security、Quatz、Caffeine
项目描述：项目构建在Spring Boot+SSM框架之上，实现了注册登录、发帖评论、回复点赞、消息提醒、内容搜索和网站数据统计的功能，并将用户头像等信息存于七牛云中，统一地进行了状态管理、事务管理、异常处理。
	使用Redis存储登录ticket和验证码，优化登录功能，解决分布式session问题；
	使用Redis存储点赞和关注信息，实现点赞和关注功能，减少数据库负担，提高网站性能；
	使用Kafka实现异步的站内通知、es服务器数据同步和分享长图的上传功能，实现解耦和异步调用，提高系统稳定性；
	使用ElasticSearch实现了全文搜索功能，可准确匹配搜索结果，并高亮显示关键词；
	使用Caffeine+Redis实现了两级缓存，并优化热门帖子访问的性能，较传统访问数据库提升了10余倍；
	使用Spring Security实现权限控制，支持多重角色和URL级别的权限管理，提高系统安全性；
	使用HyperLogLog、Bitmap分别实现了UV、DAU的统计功能，在利用较小内存空间的情况下，实现了数据的统计及可视化；
	使用Quartz实现了任务调度功能，并定时计算帖子分数并刷新，提高系统效率；
	使用Actuator对应用的Bean、缓存、日志、路径等进行监控，并通过自定义的端点监控数据库连接，提高系统可靠性。

