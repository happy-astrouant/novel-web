
create table if not exists home_book(
    id bigint unsigned primary key auto_increment,
    type tinyint unsigned not null comment '0:轮播图 2:顶部栏 3:热门推荐 4:精品推荐',
    sort tinyint unsigned not null comment '推荐排序',
    book_id bigint unsigned not null comment '推荐小说id',
    create_time datetime,
    update_time datetime
) comment = '首页小说推荐';

create table if not exists home_friend_link (
    id bigint unsigned primary key auto_increment,
    link_name varchar(50) not null comment '链接名称',
    link_url varchar(100) not null comment '链接url',
    sort tinyint unsigned not null default 11 comment '排序号',
    is_open tinyint unsigned not null default 1 comment '是否打开 0:否 1:是',
    create_time datetime,
    update_time datetime
) comment = '友情链接' ;

# 新闻模块 news_category, news_info, news_content
create table if not exists news_category (
    id bigint unsigned primary key auto_increment,
    name varchar(20) not null comment '分类名称',
    sort tinyint unsigned not null default 10 comment '排序号',
    create_time datetime,
    update_time datetime
) comment = '新闻分类';

create table if not exists news_info (
    id bigint unsigned primary key auto_increment,
    title varchar(100) not null comment '新闻标题',
    category_id bigint unsigned not null comment '分类id',
    category_name varchar(20) not null comment '分类名称',
    source varchar(50) comment '来源',
    create_time datetime,
    update_time datetime
) comment = '新闻信息';

create table if not exists news_content (
    id bigint unsigned primary key auto_increment,
    news_id bigint unsigned not null comment '新闻id',
    content mediumtext not null comment '新闻内容',
    create_time datetime,
    update_time datetime
) comment = '新闻内容';

# 小说模块
#  book_category, book_info, book_chapter, book_content, book_comment, book_comment_reply

create table if not exists book_category (
    id bigint unsigned primary key auto_increment,
    work_direction tinyint unsigned not null comment '作品方向 0:男频 1:女频',
    name varchar(20) not null comment '分类名称',
    sort tinyint unsigned not null default 10 comment '排序号',
    create_time datetime,
    update_time datetime
) comment = '小说分类';

create table if not exists book_info (
    id bigint unsigned primary key auto_increment,
    work_direction tinyint unsigned not null comment '作品方向 0:男频 1:女频',
    category_id bigint unsigned not null comment '分类id',
    category_name varchar(20) not null comment '分类名称',
    pic_url varchar(200) not null comment '小说封面图片url',
    book_name varchar(50) not null comment '小说名称',
    book_desc varchar(2000) not null comment '小说描述',
    author_id bigint unsigned not null comment '作者id',
    author_name varchar(20) not null comment '作者名称',
    score tinyint unsigned not null comment '评分，总分10',
    word_count bigint unsigned not null default 0 comment '字数',
    book_status tinyint unsigned not null default 0 comment '小说状态 0:连载中 1:已完结',
    visit_count int unsigned not null default 0 comment '访问数量',
    comment_count int unsigned not null default 0 comment '评论数量',
    last_chapter_id bigint unsigned comment '最新章节id',
    last_chapter_name varchar(50) comment '最新章节名称',
    last_chapter_update_time datetime comment '最新章节更新时间',
    is_vip tinyint unsigned not null default 0 comment '是否收费 0:免费 1:收费',
    create_time datetime,
    update_time datetime
) comment = '小说信息';

create table if not exists book_chapter (
    id bigint unsigned primary key auto_increment,
    book_id bigint unsigned not null comment '小说id',
    chapter_num int unsigned not null comment '章节编号',
    chapter_name varchar(50) not null comment '章节名称',
    word_count int unsigned not null comment '章节字数',
    is_vip tinyint unsigned not null default 0 comment '是否收费 0:免费 1:收费',
    create_time datetime,
    update_time datetime
) comment = '小说章节';

create table if not exists book_content (
    id bigint unsigned primary key auto_increment,
    chapter_id bigint unsigned not null comment '章节id',
    content mediumtext not null comment '章节内容',
    create_time datetime,
    update_time datetime
) comment = '小说内容';

create table if not exists book_comment (
    id bigint unsigned primary key auto_increment,
    book_id bigint unsigned not null comment '小说id',
    user_id bigint unsigned not null comment '用户id',
    reply_count int unsigned not null default 0 comment '回复数量',
    audit_status tinyint unsigned not null default 0 comment '审核状态 0:待审核 1:审核通过 2:审核未通过',
    comment_content varchar(512) not null comment '评论内容',
    create_time datetime,
    update_time datetime
) comment = '小说评论';

create table if not exists book_comment_reply (
    id bigint unsigned primary key auto_increment,
    comment_id bigint unsigned not null comment '评论id',
    user_id bigint unsigned not null comment '用户id',
    reply_content varchar(512) not null comment '回复内容',
    audit_status tinyint unsigned not null default 0 comment '审核状态 0:待审核 1:审核通过 2:审核未通过',
    create_time datetime,
    update_time datetime
) comment = '小说评论回复';

# 用户模块 user_info, user_feedback, user_bookshelf, user_read_history, user_consume_log, user_pay_log

create table if not exists user_info (
    id bigint unsigned primary key auto_increment,
    username varchar(50) not null comment '用户名',
    password varchar(100) not null comment '密码-加密',
    salt varchar(8) not null comment '盐值-加密',
    nick_name varchar(50) comment '昵称',
    user_sex tinyint unsigned default 0 comment '性别 0:男 1:女',
    user_photo varchar(100) comment '用户头像图片url',
    account_balance BIGINT UNSIGNED not null default 0 comment '账户余额',
    status tinyint unsigned not null default 0 comment '状态 0:正常 1:禁用',
    create_time datetime,
    update_time datetime
) comment = '用户信息';

create table if not exists user_feedback (
    id bigint unsigned primary key auto_increment,
    user_id bigint unsigned not null comment '用户id',
    content varchar(512) not null comment '反馈内容',
    create_time datetime,
    update_time datetime
) comment = '用户反馈';

create table if not exists user_bookshelf (
    id bigint unsigned primary key auto_increment,
    user_id bigint unsigned not null comment '用户id',
    book_id bigint unsigned not null comment '小说id',
    pre_content_id bigint unsigned comment '最后阅读章节id',
    create_time datetime,
    update_time datetime
) comment = '用户书架';

create table if not exists user_read_history (
    id bigint unsigned primary key auto_increment,
    user_id bigint unsigned not null comment '用户id',
    book_id bigint unsigned not null comment '小说id',
    pre_content_id bigint unsigned not null comment '最后阅读的章节id',
    create_time datetime,
    update_time datetime
) comment = '用户阅读历史';

create table if not exists user_consume_log (
    id bigint unsigned primary key auto_increment,
    user_id bigint unsigned not null comment '用户id',
    product_type tinyint unsigned not null comment '商品类型 0:付费小说',
    product_id bigint unsigned comment '商品(书名-章节)id',
    product_name varchar(100) not null comment '商品名称',
    product_value int unsigned comment '商品数量',
    amount BIGINT UNSIGNED not null comment '屋币',

    create_time datetime,
    update_time datetime
) comment = '用户消费记录';

create table if not exists user_pay_log (
    id bigint unsigned primary key auto_increment,
    user_id bigint unsigned not null comment '用户id',
    pay_channel tinyint unsigned not null comment '支付渠道 0:支付宝 1:微信',
    out_trade_no varchar(64) not null comment '商户订单号',
    amount INT UNSIGNED not null comment '充值金额',
    product_type tinyint unsigned not null comment '商品类型 0:屋币 1: VIP包年',
    product_id bigint unsigned comment '商品id',
    product_name varchar(100) not null comment '商品名称',
    product_value int unsigned comment '商品数量',
    pay_time datetime not null comment '支付时间',
    create_time datetime,
    update_time datetime
) comment = '用户支付记录';


# 作家模块

CREATE TABLE if not exists author_code (
     id BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '主键',
     invite_code VARCHAR(100) NOT NULL COMMENT '邀请码',
     validity_time DATETIME NOT NULL COMMENT '有效时间',
     is_used TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否使用过：0-未使用，1-使用过',
     create_time DATETIME COMMENT '创建时间',
     update_time DATETIME COMMENT '更新时间'
) COMMENT = '邀请码表：存储系统发放的邀请码及其使用状态';


CREATE TABLE if not exists author_info (
   id BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '主键',
   user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
   invite_code VARCHAR(20) NOT NULL COMMENT '邀请码',
   pen_name VARCHAR(20) NOT NULL COMMENT '笔名',
   tel_phone VARCHAR(20) COMMENT '手机号码',
   chat_account VARCHAR(50) COMMENT 'QQ或微信账号',
   email VARCHAR(50) COMMENT '电子邮箱',
   work_direction TINYINT UNSIGNED COMMENT '作品方向：0-男频，1-女频',
   status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-封禁',
   create_time DATETIME COMMENT '创建时间',
   update_time DATETIME COMMENT '更新时间'
) COMMENT = '用户注册信息表：存储用户注册时填写的邀请码、笔名、联系方式等信息';


CREATE TABLE if not exists author_income (
   id BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '主键',
   author_id BIGINT UNSIGNED NOT NULL COMMENT '作家ID',
   book_id BIGINT UNSIGNED NOT NULL COMMENT '小说ID',
   income_month DATE NOT NULL COMMENT '收入月份（精确到月的第一天，如 2025-04-01）',
   pre_tax_income INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '税前收入，单位：分（如 10000 = 100.00元）',
   after_tax_income INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '税后收入，单位：分',
   pay_status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付，1-已支付',
   confirm_status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '稿费确认状态：0-待确认，1-已确认',
   detail VARCHAR(255) COMMENT '详情，如特殊说明、扣款原因等',
   create_time DATETIME COMMENT '创建时间',
   update_time DATETIME COMMENT '更新时间'
) COMMENT = '稿费收入统计表：按月汇总作家每本书的收入情况';


CREATE TABLE if not exists author_income_detail (
  id BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '主键',
  author_id BIGINT UNSIGNED NOT NULL COMMENT '作家ID',
  book_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '小说ID，0 表示全部作品合并统计',
  income_date DATE NOT NULL COMMENT '收入日期',
  income_account INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订阅总额，单位：分',
  income_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订阅次数（用户点击付费章节的次数）',
  income_number INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订阅人数（去重后的用户数）',
  create_time DATETIME COMMENT '创建时间',
  update_time DATETIME COMMENT '更新时间'
) COMMENT = '稿费收入明细表：按日统计作家每本书或整体的订阅收入明细';

# 支付模块
CREATE TABLE if not exists pay_alipay (
    id BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '主键',
    out_trade_no VARCHAR(64) NOT NULL COMMENT '商户订单号',
    trade_no VARCHAR(64) NOT NULL COMMENT '支付宝交易号',
    buyer_id VARCHAR(16) COMMENT '买家支付宝账号 ID',
    trade_status VARCHAR(32) COMMENT '交易状态：如 TRADE_SUCCESS-交易成功',
    total_amount INT UNSIGNED NOT NULL COMMENT '订单金额，单位：分',
    receipt_amount INT UNSIGNED COMMENT '实收金额，单位：分',
    invoice_amount INT UNSIGNED COMMENT '开票金额，单位：分',
    gmt_create DATETIME COMMENT '交易创建时间（支付宝返回）',
    gmt_payment DATETIME COMMENT '交易付款时间（支付宝返回）',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间'
) COMMENT = '支付宝支付记录表：存储每一笔通过支付宝完成的交易详情';

create table if not exists pay_wechat (
      id bigint unsigned not null primary key comment '主键',
      out_trade_no varchar(32) not null comment '商户订单号',
      transaction_id varchar(32) not null comment '微信支付订单号',
      trade_type varchar(16) comment '交易类型：JSAPI-公众号支付, NATIVE-扫码支付, APP-APP支付, MICROPAY-付款码支付, MWEB-H5支付, FACEPAY-刷脸支付',
      trade_state varchar(32) comment '交易状态：SUCCESS-支付成功, REFUND-转入退款, NOTPAY-未支付, CLOSED-已关闭, REVOKED-已撤销, USERPAYING-用户支付中, PAYERROR-支付失败',
      trade_state_desc varchar(255) comment '交易状态描述',
      amount int unsigned not null comment '订单总金额，单位：分',
      payer_total int unsigned comment '用户支付金额，单位：分',
      success_time datetime comment '支付完成时间',
      payer_openid varchar(128) comment '支付者用户标识，用户在直连商户appid下的唯一标识',
      create_time datetime comment '创建时间',
      update_time datetime comment '更新时间'
) comment '微信支付记录表：存储每一笔微信支付的交易详情';

# 管理系统模块

create table if not exists sys_user (
    id bigint unsigned not null primary key comment '主键',
    username varchar(50) not null comment '用户名',
    password varchar(50) not null comment '密码（建议存储加密后的内容）',
    name varchar(100) comment '真实姓名',
    sex tinyint unsigned comment '性别：0-男，1-女',
    birth datetime comment '出生日期',
    email varchar(100) comment '邮箱',
    mobile varchar(100) comment '手机号',
    status tinyint unsigned not null default 1 comment '状态：0-禁用，1-正常',
    create_time datetime comment '创建时间',
    update_time datetime comment '更新时间'
) comment '系统用户表：存储后台管理系统用户账号信息';


create table if not exists sys_role (
    id bigint unsigned not null primary key comment '主键',
    role_name varchar(100) not null comment '角色名称',
    role_sign varchar(100) comment '角色标识，用于权限控制（如 admin, editor）',
    remark varchar(100) comment '备注信息',
    create_time datetime comment '创建时间',
    update_time datetime comment '更新时间'
) comment '角色表：定义系统中的角色，用于权限分配';


create table if not exists sys_user_role (
     id bigint unsigned not null primary key comment '主键',
     user_id bigint unsigned not null comment '用户ID，关联 sys_user.id',
     role_id bigint unsigned not null comment '角色ID，关联 sys_role.id',
     create_time datetime comment '创建时间',
     update_time datetime comment '更新时间'
) comment '用户角色关系表：维护用户与角色的多对多关系';


create table if not exists sys_menu (
    id bigint unsigned not null primary key comment '主键',
    parent_id bigint unsigned not null default 0 comment '父菜单ID，一级菜单的 parent_id 为 0',
    name varchar(50) not null comment '菜单名称',
    url varchar(200) comment '菜单URL，点击后跳转的路由地址',
    type tinyint unsigned not null comment '菜单类型：0-目录，1-菜单',
    icon varchar(50) comment '菜单图标（如 fa fa-home, el-icon-menu）',
    sort tinyint unsigned comment '显示排序，数值越小越靠前',
    create_time datetime comment '创建时间',
    update_time datetime comment '更新时间'
) comment '系统菜单表：定义后台管理系统的导航菜单结构';


create table if not exists sys_role_menu (
     id bigint unsigned not null primary key comment '主键',
     role_id bigint unsigned not null comment '角色ID，关联 sys_role.id',
     menu_id bigint unsigned not null comment '菜单ID，关联 sys_menu.id',
     create_time datetime comment '创建时间',
     update_time datetime comment '更新时间'
) comment '角色菜单关系表：维护角色与菜单的多对多权限关系';


create table if not exists sys_log (
   id bigint unsigned not null primary key comment '主键',
   user_id bigint unsigned comment '用户ID，操作者ID，可为空（如未登录操作）',
   username varchar(50) comment '用户名，操作者登录名',
   operation varchar(50) comment '用户操作内容，如：登录系统、新增用户、删除菜单等',
   time int unsigned comment '响应时间，单位：毫秒',
   method varchar(200) comment '请求方法，如：com.example.controller.UserController.saveUser',
   params varchar(5000) comment '请求参数，JSON 格式存储，便于排查问题',
   ip varchar(64) comment '操作IP地址，支持IPv4和IPv6',
   create_time datetime comment '日志创建时间'
) comment '系统日志表：记录后台管理系统的用户操作行为，用于审计和监控';
