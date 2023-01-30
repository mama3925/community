# 项目简介
本项目主要涉及模块有登录模块、注册模块、帖子模块、消息模块、搜索模块、权限模块、统计模块等。
# 技术栈
Spring Boot+Thymeleaf+Mybatis-Plus+Redis+Kafka+Elasticsearch+Spring Security+Quartz+Caffeine
# 项目亮点
- 使用Redis非关系型数据库存储实体的点赞以及用户关注者，相比关系型数据库提高了查询效率，降低了存储空间
- 使用TrieTree根据字典对帖子、评论和私信的内容进行敏感词检测和替换
- 使用ElasticSearch实现对帖子的分词搜索功能，并使用Kafka来异步处理帖子提交和变动事件
- 使用Redis记录变动过的帖子，并使用Quartz定期对这些变动帖子计算热点分数，降低计算量
- 使用Caffeine和Redis建立二级缓存避免缓存雪崩，将首页热门帖子的TPS从60提升到762
# 博客
## [仿牛客论坛项目(上)](https://blog.csdn.net/QiuYuSy/article/details/128739479)
## [仿牛客论坛项目(下)](https://blog.csdn.net/QiuYuSy/article/details/128783892)
## [Docker部署项目](https://blog.csdn.net/QiuYuSy/article/details/128809894)
## [项目遇到问题汇总](https://blog.csdn.net/QiuYuSy/article/details/128809861)

# 部署
- master为windows运行版
- linux分支为linux部署版



