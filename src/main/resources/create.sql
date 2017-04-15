/*==============================================================*/
/* DBMS name:      PostgreSQL 9.x                               */
/* Created on:     2016/4/27 9:39:09                            */
/*==============================================================*/


/*==============================================================*/
/* Table: SYS_LOG                                               */
/*==============================================================*/
CREATE TABLE SYS_LOG (
   ID                   BIGSERIAL            NOT NULL,
   TYPE                 SMALLINT             NOT NULL,
   MODULE               CHARACTER VARYING(32) NOT NULL,
   OPERATE              SMALLINT             NOT NULL,
   RUN_SQL              CHARACTER VARYING(512) NULL,
   SQL_ARG              CHARACTER VARYING(256) NULL,
   IDS                  CHARACTER VARYING(256) NULL,
   ORG_ID               BIGINT               NOT NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_SYS_LOG PRIMARY KEY (ID)
);

COMMENT ON TABLE SYS_LOG IS
'系统日志';

COMMENT ON COLUMN SYS_LOG.TYPE IS
'类型(1:同步;2:业务)';

COMMENT ON COLUMN SYS_LOG.MODULE IS
'操作模块';

COMMENT ON COLUMN SYS_LOG.OPERATE IS
'操作动作(1:新增;2:修改;3:删除)';

COMMENT ON COLUMN SYS_LOG.RUN_SQL IS
'执行SQL';

COMMENT ON COLUMN SYS_LOG.SQL_ARG IS
'SQL参数';

COMMENT ON COLUMN SYS_LOG.IDS IS
'影响ID(多个以'',''间隔)';

COMMENT ON COLUMN SYS_LOG.ORG_ID IS
'所属机构';

COMMENT ON COLUMN SYS_LOG.CREATE_USER IS
'创建人';

COMMENT ON COLUMN SYS_LOG.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN SYS_LOG.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN SYS_LOG.UPDATE_TIME IS
'修改时间';

/*==============================================================*/
/* Index: IDX_LOG_TYPE                                          */
/*==============================================================*/
CREATE  INDEX IDX_LOG_TYPE ON SYS_LOG (
TYPE,
OPERATE
);

/*==============================================================*/
/* Index: IDX_LOG_CREATE_TIME                                   */
/*==============================================================*/
CREATE  INDEX IDX_LOG_CREATE_TIME ON SYS_LOG (
CREATE_TIME
);

/*==============================================================*/
/* Table: SYS_MENU                                              */
/*==============================================================*/
CREATE TABLE SYS_MENU (
   ID                   BIGSERIAL NOT NULL,
   NAME                 CHARACTER VARYING(32) NOT NULL,
   ICON                 CHARACTER VARYING(128) NULL,
   URL                  CHARACTER VARYING(64) NULL,
   RIGHTS               INTEGER              NOT NULL DEFAULT 0,
   SORT                 CHARACTER VARYING(8) NOT NULL,
   LIMITED              BOOLEAN              NOT NULL,
   ENABLE               BOOLEAN              NOT NULL,
   PARENT_ID            BIGINT               NULL,
   SYS_TYPE             SMALLINT             NOT NULL,
   CONSTRAINT PK_SYS_MENU PRIMARY KEY (ID)
);

COMMENT ON TABLE SYS_MENU IS
'菜单';

COMMENT ON COLUMN SYS_MENU.ID IS
'ID';

COMMENT ON COLUMN SYS_MENU.NAME IS
'菜单';

COMMENT ON COLUMN SYS_MENU.ICON IS
'图标';

COMMENT ON COLUMN SYS_MENU.URL IS
'URL';

COMMENT ON COLUMN SYS_MENU.RIGHTS IS
'权限值';

COMMENT ON COLUMN SYS_MENU.SORT IS
'序号';

COMMENT ON COLUMN SYS_MENU.LIMITED IS
'是否限制,1:限制;0:开放';

COMMENT ON COLUMN SYS_MENU.ENABLE IS
'是否显示,1:显示;0:隐藏';

COMMENT ON COLUMN SYS_MENU.PARENT_ID IS
'上级菜单';

COMMENT ON COLUMN SYS_MENU.SYS_TYPE IS
'所属系统';

/*==============================================================*/
/* Table: SYS_ORG                                               */
/*==============================================================*/
CREATE TABLE SYS_ORG (
   ID                   BIGSERIAL            NOT NULL,
   CODE                 CHARACTER VARYING(16) NOT NULL,
   NAME                 CHARACTER VARYING(64) NULL,
   SORT                 CHARACTER VARYING(16) NOT NULL,
   OFFICE               CHARACTER VARYING(256) NULL,
   PHONE                CHARACTER VARYING(16) NULL,
   MANAGER              CHARACTER VARYING(32) NULL,
   PARENT_ID            BIGINT               NULL,
   COMPANY_ID           BIGINT               NULL,
   STATE                SMALLINT             NOT NULL,
   GENUS_AREA           CHARACTER VARYING(256) NULL,
   SETTLED_TIME         TIMESTAMP WITH TIME ZONE NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_SYS_ORG PRIMARY KEY (ID)
);

COMMENT ON TABLE SYS_ORG IS
'组织机构';

COMMENT ON COLUMN SYS_ORG.ID IS
'ID';

COMMENT ON COLUMN SYS_ORG.CODE IS
'编码';

COMMENT ON COLUMN SYS_ORG.NAME IS
'名称';

COMMENT ON COLUMN SYS_ORG.SORT IS
'序号';

COMMENT ON COLUMN SYS_ORG.OFFICE IS
'办公地点';

COMMENT ON COLUMN SYS_ORG.PHONE IS
'办公电话';

COMMENT ON COLUMN SYS_ORG.MANAGER IS
'负责人';

COMMENT ON COLUMN SYS_ORG.PARENT_ID IS
'上级部门';

COMMENT ON COLUMN SYS_ORG.COMPANY_ID IS
'所属公司';

COMMENT ON COLUMN SYS_ORG.STATE IS
'状态';

COMMENT ON COLUMN SYS_ORG.CREATE_USER IS
'创建人';

COMMENT ON COLUMN SYS_ORG.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN SYS_ORG.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN SYS_ORG.UPDATE_TIME IS
'修改时间';

/*==============================================================*/
/* Index: IDX_ORG_NAME                                          */
/*==============================================================*/
CREATE  INDEX IDX_ORG_NAME ON SYS_ORG (
NAME,
TYPE
);

/*==============================================================*/
/* Table: SYS_RIGHTS                                            */
/*==============================================================*/
CREATE TABLE SYS_RIGHTS (
   ID                   BIGSERIAL NOT NULL,
   MENU_ID              BIGINT               NOT NULL,
   ROLE_ID              BIGINT               NOT NULL,
   RIGHTS               INTEGER              NOT NULL,
   CREATE_USER          CHARACTER VARYING(32) NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_SYS_RIGHTS PRIMARY KEY (ID)
);

COMMENT ON TABLE SYS_RIGHTS IS
'权限';

COMMENT ON COLUMN SYS_RIGHTS.ID IS
'ID';

COMMENT ON COLUMN SYS_RIGHTS.MENU_ID IS
'菜单ID';

COMMENT ON COLUMN SYS_RIGHTS.ROLE_ID IS
'角色ID';

COMMENT ON COLUMN SYS_RIGHTS.RIGHTS IS
'权限值';

COMMENT ON COLUMN SYS_RIGHTS.CREATE_USER IS
'创建人';

COMMENT ON COLUMN SYS_RIGHTS.CREATE_TIME IS
'创建时间';

/*==============================================================*/
/* Table: SYS_ROLE                                              */
/*==============================================================*/
CREATE TABLE SYS_ROLE (
   ID                   BIGSERIAL            NOT NULL,
   NAME                 CHARACTER VARYING(32) NOT NULL,
   EDITABLE             BOOLEAN              NOT NULL,
   ORG_ID               BIGINT               NOT NULL,
   SYS_TYPE             SMALLINT             NOT NULL,
   REMARK               CHARACTER VARYING(256) NULL,
   CREATE_USER          CHARACTER VARYING(32) NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   UPDATE_USER          CHARACTER VARYING(32) NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_SYS_ROLE PRIMARY KEY (ID)
);

COMMENT ON TABLE SYS_ROLE IS
'角色';

COMMENT ON COLUMN SYS_ROLE.NAME IS
'角色名称';

COMMENT ON COLUMN SYS_ROLE.EDITABLE IS
'允许编辑';

COMMENT ON COLUMN SYS_ROLE.ORG_ID IS
'所属机构';

COMMENT ON COLUMN SYS_ROLE.SYS_TYPE IS
'所属系统';

COMMENT ON COLUMN SYS_ROLE.REMARK IS
'备注';

COMMENT ON COLUMN SYS_ROLE.CREATE_USER IS
'创建人';

COMMENT ON COLUMN SYS_ROLE.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN SYS_ROLE.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN SYS_ROLE.UPDATE_TIME IS
'修改时间';

/*==============================================================*/
/* Table: SYS_TYPE                                              */
/*==============================================================*/
CREATE TABLE SYS_TYPE (
   ID                   BIGSERIAL            NOT NULL,
   TYPE                 CHARACTER VARYING(16) NOT NULL,
   CODE                 CHARACTER VARYING(16) NOT NULL,
   NAME                 CHARACTER VARYING(16) NOT NULL,
   VALUE                CHARACTER VARYING(256) NULL,
   REF_ID               BIGINT               NULL,
   DISPLAYABLE          BOOLEAN              NOT NULL,
   EDITABLE             BOOLEAN              NOT NULL,
   REMARK               CHARACTER VARYING(256) NULL,
   SYS_TYPE             SMALLINT             NOT NULL,
   ORG_ID               BIGINT               NOT NULL,
   CREATE_USER          CHARACTER VARYING(32) NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          CHARACTER VARYING(32) NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_SYS_TYPE PRIMARY KEY (ID)
);

COMMENT ON TABLE SYS_TYPE IS
'系统类型';

COMMENT ON COLUMN SYS_TYPE.TYPE IS
'参数类型';

COMMENT ON COLUMN SYS_TYPE.CODE IS
'参数编码';

COMMENT ON COLUMN SYS_TYPE.NAME IS
'参数名称';

COMMENT ON COLUMN SYS_TYPE.VALUE IS
'参数值';

COMMENT ON COLUMN SYS_TYPE.REF_ID IS
'关联类型';

COMMENT ON COLUMN SYS_TYPE.DISPLAYABLE IS
'是否显示';

COMMENT ON COLUMN SYS_TYPE.EDITABLE IS
'是否允许编辑';

COMMENT ON COLUMN SYS_TYPE.REMARK IS
'备注';

COMMENT ON COLUMN SYS_TYPE.SYS_TYPE IS
'所属系统';

COMMENT ON COLUMN SYS_TYPE.ORG_ID IS
'所属机构';

COMMENT ON COLUMN SYS_TYPE.CREATE_USER IS
'创建人';

COMMENT ON COLUMN SYS_TYPE.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN SYS_TYPE.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN SYS_TYPE.UPDATE_TIME IS
'修改时间';

/*==============================================================*/
/* Table: SYS_USER                                              */
/*==============================================================*/
CREATE TABLE SYS_USER (
   ID                   BIGSERIAL NOT NULL,
   USERNAME             CHARACTER VARYING(128) NOT NULL,
   PASSWORD             CHARACTER VARYING(128) NOT NULL,
   NICKNAME             CHARACTER VARYING(64) NOT NULL,
   REAL_NAME            CHARACTER VARYING(64) NULL,
   MOBILE               CHARACTER VARYING(16) NULL,
   EMAIL                CHARACTER VARYING(64) NULL,
   PORTRAIT             CHARACTER VARYING(128) NULL,
   RETRY                SMALLINT             NOT NULL DEFAULT 0,
   LAST_LOGIN_MACHINE   CHARACTER VARYING(32) NULL,
   LAST_LOGIN_TIME      TIMESTAMP WITH TIME ZONE NULL,
   PWD_UPDATE_TIME      TIMESTAMP WITH TIME ZONE NULL,
   EDITABLE             BOOLEAN              NOT NULL,
   ENABLE               BOOLEAN              NOT NULL,
   ORG_ID               BIGINT               NOT NULL,
   COMPANY_ID           BIGINT               NOT NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   REMARK            CHARACTER VARYING(256) NULL,
   CONSTRAINT PK_SYS_USER PRIMARY KEY (ID)
);

COMMENT ON TABLE SYS_USER IS
'用户';

COMMENT ON COLUMN SYS_USER.ID IS
'ID';

COMMENT ON COLUMN SYS_USER.USERNAME IS
'用户';

COMMENT ON COLUMN SYS_USER.PASSWORD IS
'密码';

COMMENT ON COLUMN SYS_USER.NICKNAME IS
'昵称';

COMMENT ON COLUMN SYS_USER.REAL_NAME IS
'姓名';

COMMENT ON COLUMN SYS_USER.MOBILE IS
'手机';

COMMENT ON COLUMN SYS_USER.EMAIL IS
'电子邮箱';

COMMENT ON COLUMN SYS_USER.PORTRAIT IS
'头像';

COMMENT ON COLUMN SYS_USER.RETRY IS
'密码重试次数';

COMMENT ON COLUMN SYS_USER.LAST_LOGIN_MACHINE IS
'最后登录设备';

COMMENT ON COLUMN SYS_USER.LAST_LOGIN_TIME IS
'最后登录时间';

COMMENT ON COLUMN SYS_USER.PWD_UPDATE_TIME IS
'修改密码时间';

COMMENT ON COLUMN SYS_USER.EDITABLE IS
'允许编辑';

COMMENT ON COLUMN SYS_USER.ENABLE IS
'状态,0:禁用;1:启用';

COMMENT ON COLUMN SYS_USER.ORG_ID IS
'所属机构';

COMMENT ON COLUMN SYS_USER.COMPANY_ID IS
'所属公司';

COMMENT ON COLUMN SYS_USER.CREATE_USER IS
'创建人';

COMMENT ON COLUMN SYS_USER.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN SYS_USER.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN SYS_USER.UPDATE_TIME IS
'修改时间';

/*==============================================================*/
/* Index: IDX_USER_USERNAME                                     */
/*==============================================================*/
CREATE  INDEX IDX_USER_USERNAME ON SYS_USER (
USERNAME
);

/*==============================================================*/
/* Table: SYS_USER_ROLE                                         */
/*==============================================================*/
CREATE TABLE SYS_USER_ROLE (
   ID                   BIGSERIAL            NOT NULL,
   ROLE_ID              BIGINT               NOT NULL,
   USER_ID              BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CREATE_USER          CHARACTER VARYING(32) NULL,
   CONSTRAINT PK_SYS_USER_ROLE PRIMARY KEY (ID)
);

COMMENT ON TABLE SYS_USER_ROLE IS
'用户角色关系表';

COMMENT ON COLUMN SYS_USER_ROLE.ROLE_ID IS
'角色ID';

COMMENT ON COLUMN SYS_USER_ROLE.USER_ID IS
'用户ID';

COMMENT ON COLUMN SYS_USER_ROLE.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN SYS_USER_ROLE.CREATE_USER IS
'创建人';

/*==============================================================*/
/* Table: T_ARTICLE                                             */
/*==============================================================*/
CREATE TABLE T_ARTICLE (
   ID                   BIGSERIAL            NOT NULL,
   TITLE                CHARACTER VARYING(64) NOT NULL,
   SORT                 CHARACTER VARYING(8) NOT NULL,
   IMAGE_LEVEL          SMALLINT             NOT NULL,
   TYPE                 SMALLINT             NOT NULL,
   IMAGE                CHARACTER VARYING(128) NULL,
   CONTENT              TEXT                 NOT NULL,
   STATE                SMALLINT             NULL,
   READ                 INTEGER              NOT NULL,
   REPLY                INTEGER              NOT NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_T_ARTICLE PRIMARY KEY (ID)
);

COMMENT ON TABLE T_ARTICLE IS
'文章';

COMMENT ON COLUMN T_ARTICLE.TITLE IS
'标题';

COMMENT ON COLUMN T_ARTICLE.SORT IS
'序号';

COMMENT ON COLUMN T_ARTICLE.IMAGE_LEVEL IS
'图文级别';

COMMENT ON COLUMN T_ARTICLE.TYPE IS
'分类,1:值得买;2:玩什么;3:住哪里';

COMMENT ON COLUMN T_ARTICLE.IMAGE IS
'大图';

COMMENT ON COLUMN T_ARTICLE.CONTENT IS
'内容';

COMMENT ON COLUMN T_ARTICLE.STATE IS
'状态';

COMMENT ON COLUMN T_ARTICLE.READ IS
'阅读数';

COMMENT ON COLUMN T_ARTICLE.REPLY IS
'评论数';

COMMENT ON COLUMN T_ARTICLE.CREATE_USER IS
'创建人';

COMMENT ON COLUMN T_ARTICLE.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_ARTICLE.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN T_ARTICLE.UPDATE_TIME IS
'修改时间';

/*==============================================================*/
/* Table: T_ARTICLE_PRODUCT                                     */
/*==============================================================*/
CREATE TABLE T_ARTICLE_PRODUCT (
   ID                   BIGSERIAL            NOT NULL,
   IMAGE                CHARACTER VARYING(50) NOT NULL,
   NAME                 CHARACTER VARYING(50) NULL,
   INTRODUCTION         CHARACTER VARYING(200) NULL,
   S_ID                 BIGINT               NOT NULL,
   A_ID                 BIGINT               NOT NULL,
   P_ID                 BIGINT               NOT NULL,
   CONSTRAINT PK_T_ARTICLE_PRODUCT PRIMARY KEY (ID)
);

/*==============================================================*/
/* Table: T_CARD                                                */
/*==============================================================*/
CREATE TABLE T_CARD (
   ID                   BIGSERIAL            NOT NULL,
   CARD_TYPE            SMALLINT             NOT NULL,
   CARD_OWNER           CHARACTER VARYING(32) NOT NULL,
   CARD_NO              CHARACTER VARYING(64) NOT NULL,
   MOBILE_NO            CHARACTER VARYING(16) NOT NULL,
   ORG_ID               BIGINT               NOT NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_T_CARD PRIMARY KEY (ID)
);

COMMENT ON TABLE T_CARD IS
'银行信息表';

/*==============================================================*/
/* Table: T_CATEGORY                                            */
/*==============================================================*/
CREATE TABLE T_CATEGORY (
   ID                   BIGSERIAL NOT NULL,
   CODE                 CHARACTER VARYING(4) NOT NULL,
   NAME                 CHARACTER VARYING(32) NOT NULL,
   PARENT_ID            BIGINT               NULL,
   PARENT_CODE          CHARACTER VARYING(16) NULL,
   LEVEL                SMALLINT             NOT NULL,
   LOGO                 CHARACTER VARYING(128) NULL,
   TAX_RATE             DOUBLE PRECISION     NULL,
   COUNT_ORG            INTEGER              NOT NULL DEFAULT 0,
   COUNT_SKU            INTEGER              NOT NULL DEFAULT 0,
   COUNT_SALE           INTEGER              NOT NULL DEFAULT 0,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_T_CATEGORY PRIMARY KEY (ID)
);

COMMENT ON TABLE T_CATEGORY IS
'商品分类';

COMMENT ON COLUMN T_CATEGORY.ID IS
'ID';

COMMENT ON COLUMN T_CATEGORY.CODE IS
'编码';

COMMENT ON COLUMN T_CATEGORY.NAME IS
'名称';

COMMENT ON COLUMN T_CATEGORY.PARENT_ID IS
'所属分类';

COMMENT ON COLUMN T_CATEGORY.PARENT_CODE IS
'所属分类代码';

COMMENT ON COLUMN T_CATEGORY.LEVEL IS
'分类层级';

COMMENT ON COLUMN T_CATEGORY.LOGO IS
'图标';

COMMENT ON COLUMN T_CATEGORY.TAX_RATE IS
'税率';

COMMENT ON COLUMN T_CATEGORY.COUNT_ORG IS
'商家数量';

COMMENT ON COLUMN T_CATEGORY.COUNT_SKU IS
'商品数量';

COMMENT ON COLUMN T_CATEGORY.COUNT_SALE IS
'在线商品数量';

COMMENT ON COLUMN T_CATEGORY.CREATE_USER IS
'创建人';

COMMENT ON COLUMN T_CATEGORY.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_CATEGORY.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN T_CATEGORY.UPDATE_TIME IS
'修改时间';

/*==============================================================*/
/* Table: T_COLLECT                                             */
/*==============================================================*/
CREATE TABLE T_COLLECT (
   ID                   BIGSERIAL            NOT NULL,
   TYPE                 SMALLINT             NOT NULL,
   INFO_ID              BIGINT               NOT NULL,
   USER_ID              BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   CONSTRAINT PK_T_COLLECT PRIMARY KEY (ID)
);

COMMENT ON TABLE T_COLLECT IS
'用户收藏表';

COMMENT ON COLUMN T_COLLECT.TYPE IS
'类型,字段预留，满足各种业务需要';

COMMENT ON COLUMN T_COLLECT.INFO_ID IS
'所属业务ID,主表关联外键';

COMMENT ON COLUMN T_COLLECT.USER_ID IS
'用户ID';

COMMENT ON COLUMN T_COLLECT.CREATE_TIME IS
'收藏时间';

/*==============================================================*/
/* Index: IDX_COLLECT_USER                                      */
/*==============================================================*/
CREATE  INDEX IDX_COLLECT_USER ON T_COLLECT (
USER_ID
);

/*==============================================================*/
/* Table: T_COUNTRY                                             */
/*==============================================================*/
CREATE TABLE T_COUNTRY (
   ID                   BIGSERIAL            NOT NULL,
   NAME                 CHARACTER VARYING(64) NOT NULL,
   NAME_EN              CHARACTER VARYING(64) NOT NULL,
   WORD2                CHARACTER VARYING(2) NOT NULL,
   WORD3                CHARACTER VARYING(3) NOT NULL,
   CODE                 SMALLINT             NOT NULL,
   CURRENCY             CHARACTER VARYING(4) NOT NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_T_COUNTRY PRIMARY KEY (ID)
);

COMMENT ON TABLE T_COUNTRY IS
'国家地区信息';

COMMENT ON COLUMN T_COUNTRY.ID IS
'ID';

COMMENT ON COLUMN T_COUNTRY.NAME IS
'国家中文名';

COMMENT ON COLUMN T_COUNTRY.NAME_EN IS
'国家英文名';

COMMENT ON COLUMN T_COUNTRY.WORD2 IS
'国家二字码';

COMMENT ON COLUMN T_COUNTRY.WORD3 IS
'国家三字码';

COMMENT ON COLUMN T_COUNTRY.CODE IS
'国家数字码';

COMMENT ON COLUMN T_COUNTRY.CURRENCY IS
'币别';

COMMENT ON COLUMN T_COUNTRY.CREATE_USER IS
'创建人';

COMMENT ON COLUMN T_COUNTRY.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_COUNTRY.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN T_COUNTRY.UPDATE_TIME IS
'修改时间';

/*==============================================================*/
/* Table: T_COUPONE                                             */
/*==============================================================*/
CREATE TABLE T_COUPONE (
   ID                   BIGSERIAL            NOT NULL,
   TITLE                CHARACTER VARYING(64) NOT NULL,
   NICKNAME             CHARACTER VARYING(64) NULL,
   CODE                 CHARACTER VARYING(32) NOT NULL,
   TYPE                 SMALLINT             NOT NULL,
   RANDOM               BOOLEAN              NOT NULL,
   QUANTITY             INTEGER              NOT NULL,
   CODE_START           CHARACTER VARYING(8) NULL,
   CODE_COUNT           INTEGER              NULL,
   BEGIN_DATE           TIMESTAMP WITH TIME ZONE NULL,
   END_DATE             TIMESTAMP WITH TIME ZONE NULL,
   DAYS                 INTEGER              NULL,
   VIRTUAL              BOOLEAN              NOT NULL,
   REMARK               CHARACTER VARYING(256) NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_T_COUPONE PRIMARY KEY (ID)
);

COMMENT ON TABLE T_COUPONE IS
'优惠码';

COMMENT ON COLUMN T_COUPONE.TITLE IS
'标题';

COMMENT ON COLUMN T_COUPONE.NICKNAME IS
'分组名称';

COMMENT ON COLUMN T_COUPONE.CODE IS
'优惠码';

COMMENT ON COLUMN T_COUPONE.TYPE IS
'优惠码类型';

COMMENT ON COLUMN T_COUPONE.RANDOM IS
'是否随机';

COMMENT ON COLUMN T_COUPONE.QUANTITY IS
'数量';

COMMENT ON COLUMN T_COUPONE.CODE_START IS
'起始编码';

COMMENT ON COLUMN T_COUPONE.CODE_COUNT IS
'生成数量';

COMMENT ON COLUMN T_COUPONE.BEGIN_DATE IS
'有效开始时间';

COMMENT ON COLUMN T_COUPONE.END_DATE IS
'有效结束时间';

COMMENT ON COLUMN T_COUPONE.DAYS IS
'有效天数';

COMMENT ON COLUMN T_COUPONE.VIRTUAL IS
'是否虚拟码';

COMMENT ON COLUMN T_COUPONE.REMARK IS
'备注';

COMMENT ON COLUMN T_COUPONE.CREATE_USER IS
'创建人';

COMMENT ON COLUMN T_COUPONE.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_COUPONE.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN T_COUPONE.UPDATE_TIME IS
'修改时间';

/*==============================================================*/
/* Table: T_COUPONE_DETAIL                                      */
/*==============================================================*/
CREATE TABLE T_COUPONE_DETAIL (
   NICK_NAME            VARCHAR(30)          NULL,
   ID                   BIGSERIAL            NOT NULL,
   COUPONE_ID           BIGINT               NOT NULL,
   COUPONE_CODE         CHARACTER VARYING(32) NULL,
   TICKET_ID            BIGINT               NOT NULL,
   STATE                SMALLINT             NOT NULL,
   TYPE                 SMALLINT             NOT NULL,
   AMOUNT               INTEGER              NOT NULL,
   LIMIT_VALUE          INTEGER              NULL,
   ISSUE_TIME           TIMESTAMP WITH TIME ZONE NOT NULL,
   USER_ID              BIGINT               NOT NULL,
   LIMIT_DAY            INTEGER              NULL,
   BEGIN_DATE           TIMESTAMP WITH TIME ZONE NULL,
   END_DATE             TIMESTAMP WITH TIME ZONE NULL,
   USE_TIME             TIMESTAMP WITH TIME ZONE NULL,
   ORG_ID               BIGINT               NULL,
   ORDER_NO             CHARACTER VARYING(32) NULL,
   VERSION              BIGINT               NULL,
   NICKNAME             CHARACTER VARYING(64) NULL,
   VALIDITY_TIME        SMALLINT             NULL,
   QUANTITY             INTEGER              NULL,
   CONSTRAINT PK_T_COUPONE_DETAIL PRIMARY KEY (ID)
);

COMMENT ON TABLE T_COUPONE_DETAIL IS
'优惠码明细';

COMMENT ON COLUMN T_COUPONE_DETAIL.COUPONE_ID IS
'优惠码ID';

COMMENT ON COLUMN T_COUPONE_DETAIL.COUPONE_CODE IS
'优惠码';

COMMENT ON COLUMN T_COUPONE_DETAIL.TICKET_ID IS
'优惠券ID';

COMMENT ON COLUMN T_COUPONE_DETAIL.STATE IS
'状态,0:未使用;1:过期;2,使用中;3:已使用';

COMMENT ON COLUMN T_COUPONE_DETAIL.TYPE IS
'优惠券类型';

COMMENT ON COLUMN T_COUPONE_DETAIL.AMOUNT IS
'面值';

COMMENT ON COLUMN T_COUPONE_DETAIL.LIMIT_VALUE IS
'金额限制';

COMMENT ON COLUMN T_COUPONE_DETAIL.ISSUE_TIME IS
'发放时间';

COMMENT ON COLUMN T_COUPONE_DETAIL.USER_ID IS
'用户ID';

COMMENT ON COLUMN T_COUPONE_DETAIL.LIMIT_DAY IS
'有效天数';

COMMENT ON COLUMN T_COUPONE_DETAIL.BEGIN_DATE IS
'有效开始时间';

COMMENT ON COLUMN T_COUPONE_DETAIL.END_DATE IS
'有效结束时间';

COMMENT ON COLUMN T_COUPONE_DETAIL.USE_TIME IS
'使用时间';

COMMENT ON COLUMN T_COUPONE_DETAIL.ORG_ID IS
'商家ID';

COMMENT ON COLUMN T_COUPONE_DETAIL.ORDER_NO IS
'订单编号';

COMMENT ON COLUMN T_COUPONE_DETAIL.VERSION IS
'乐观锁,乐观行锁标志，解决并发问题';

/*==============================================================*/
/* Index: IDX_COUPONE_STATE                                     */
/*==============================================================*/
CREATE  INDEX IDX_COUPONE_STATE ON T_COUPONE_DETAIL (
STATE
);

/*==============================================================*/
/* Table: T_COUPONE_TICKET                                      */
/*==============================================================*/
CREATE TABLE T_COUPONE_TICKET (
   CURRENCY             CHARACTER VARYING(4) NULL,
   ID                   BIGSERIAL            NOT NULL,
   TITLE                CHARACTER VARYING(32) NOT NULL,
   TYPE                 SMALLINT             NOT NULL,
   AMOUNT               INTEGER              NOT NULL,
   CONDITION            INTEGER              NULL,
   REMARK               CHARACTER VARYING(256) NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_T_COUPONE_TICKET PRIMARY KEY (ID)
);

COMMENT ON TABLE T_COUPONE_TICKET IS
'优惠券';

COMMENT ON COLUMN T_COUPONE_TICKET.TITLE IS
'标题';

COMMENT ON COLUMN T_COUPONE_TICKET.TYPE IS
'优惠券类型';

COMMENT ON COLUMN T_COUPONE_TICKET.AMOUNT IS
'面值';

COMMENT ON COLUMN T_COUPONE_TICKET.CONDITION IS
'使用条件';

COMMENT ON COLUMN T_COUPONE_TICKET.REMARK IS
'备注';

COMMENT ON COLUMN T_COUPONE_TICKET.CREATE_USER IS
'创建人';

COMMENT ON COLUMN T_COUPONE_TICKET.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_COUPONE_TICKET.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN T_COUPONE_TICKET.UPDATE_TIME IS
'修改时间';

/*==============================================================*/
/* Table: T_CURRENCY                                            */
/*==============================================================*/
CREATE TABLE T_CURRENCY (
   ID                   BIGSERIAL            NOT NULL,
   CODE                 CHARACTER VARYING(4) NOT NULL,
   NAME                 CHARACTER VARYING(32) NOT NULL,
   RATE                 DOUBLE PRECISION     NOT NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_T_CURRENCY PRIMARY KEY (ID)
);

COMMENT ON TABLE T_CURRENCY IS
'币种信息';

COMMENT ON COLUMN T_CURRENCY.ID IS
'ID';

COMMENT ON COLUMN T_CURRENCY.CODE IS
'代码';

COMMENT ON COLUMN T_CURRENCY.NAME IS
'名称';

COMMENT ON COLUMN T_CURRENCY.RATE IS
'兑人民币汇率';

COMMENT ON COLUMN T_CURRENCY.CREATE_USER IS
'创建人';

COMMENT ON COLUMN T_CURRENCY.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_CURRENCY.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN T_CURRENCY.UPDATE_TIME IS
'修改时间';

/*==============================================================*/
/* Table: T_DEVICE                                              */
/*==============================================================*/
CREATE TABLE T_DEVICE (
   ID                   BIGSERIAL            NOT NULL,
   DEV_NO               CHARACTER VARYING(16) NULL,
   ADDRESS              CHARACTER VARYING(256) NULL,
   NATRUE               SMALLINT             NULL,
   STATE                SMALLINT             NULL,
   ORG_ID               BIGINT               NOT NULL,
   POINT_ID             BIGINT NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   TYPE                 SMALLINT             NULL,
   CONSTRAINT PK_T_DEVICE PRIMARY KEY (ID)
);

COMMENT ON TABLE T_DEVICE IS
'设备';

COMMENT ON COLUMN T_DEVICE.ORG_ID IS
'城市名称';

COMMENT ON COLUMN T_DEVICE.CREATE_USER IS
'创建人';

COMMENT ON COLUMN T_DEVICE.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_DEVICE.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN T_DEVICE.UPDATE_TIME IS
'修改时间';

COMMENT ON COLUMN T_DEVICE.TYPE IS
'设备类型  1：饮料机  2：弹簧机';

/*==============================================================*/
/* Table: T_DEVICE_AISLE                                        */
/*==============================================================*/
CREATE TABLE T_DEVICE_AISLE (
   ID                   BIGSERIAL            NOT NULL,
   AISLE_NUM            SMALLINT             NOT NULL,
   PRODUCT_ID           BIGINT               NULL,
   PRICE                DOUBLE PRECISION     NULL,
   STOCK                INTEGER              NOT NULL,
   STOCK_REMIND         INTEGER              NULL,
   DEVICE_ID            INTEGER              NULL,
   CABINET_ID           BIGINT               NULL,
   CAPACITY             INTEGER              NOT NULL,
   SALES                INTEGER              NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   PRODUCT_NAME         CHARACTER VARYING(100) NULL,
   PRODUCT_CODE         CHARACTER VARYING(30) NULL,
   SUPPLEMENT_NO        INTEGER              NULL,
   CONSTRAINT PK_T_DEVICE_AISLE PRIMARY KEY (ID)
);

COMMENT ON TABLE T_DEVICE_AISLE IS
'设备货道';

COMMENT ON COLUMN T_DEVICE_AISLE.AISLE_NUM IS
'城市名称';

COMMENT ON COLUMN T_DEVICE_AISLE.CABINET_ID IS
'货柜ID';

COMMENT ON COLUMN T_DEVICE_AISLE.CREATE_USER IS
'创建人';

COMMENT ON COLUMN T_DEVICE_AISLE.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_DEVICE_AISLE.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN T_DEVICE_AISLE.UPDATE_TIME IS
'修改时间';

/*==============================================================*/
/* Table: T_DISCOVER                                            */
/*==============================================================*/
CREATE TABLE T_DISCOVER (
   ID                   BIGSERIAL            NOT NULL,
   TITLE                CHARACTER VARYING(64) NOT NULL,
   SORT                 CHARACTER VARYING(8) NOT NULL,
   TYPE                 SMALLINT             NOT NULL,
   IMAGE_INDEX          CHARACTER VARYING(128) NULL,
   IMAGE_INNER          CHARACTER VARYING(128) NULL,
   CONTENT              TEXT                 NOT NULL,
   AUTHOR               CHARACTER VARYING(32) NULL,
   READ                 INTEGER              NOT NULL,
   REPLY                INTEGER              NOT NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_T_DISCOVER PRIMARY KEY (ID)
);

COMMENT ON TABLE T_DISCOVER IS
'发现';

COMMENT ON COLUMN T_DISCOVER.TITLE IS
'标题';

COMMENT ON COLUMN T_DISCOVER.SORT IS
'序号';

COMMENT ON COLUMN T_DISCOVER.TYPE IS
'分类';

COMMENT ON COLUMN T_DISCOVER.IMAGE_INDEX IS
'游物封面图';

COMMENT ON COLUMN T_DISCOVER.IMAGE_INNER IS
'内页大图';

COMMENT ON COLUMN T_DISCOVER.CONTENT IS
'内容';

COMMENT ON COLUMN T_DISCOVER.AUTHOR IS
'作者';

COMMENT ON COLUMN T_DISCOVER.READ IS
'阅读数';

COMMENT ON COLUMN T_DISCOVER.REPLY IS
'评论数';

COMMENT ON COLUMN T_DISCOVER.CREATE_USER IS
'创建人';

COMMENT ON COLUMN T_DISCOVER.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_DISCOVER.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN T_DISCOVER.UPDATE_TIME IS
'修改时间';

/*==============================================================*/
/* Table: T_FILE                                                */
/*==============================================================*/
CREATE TABLE T_FILE (
   ID                   BIGSERIAL            NOT NULL,
   NAME                 CHARACTER VARYING(128) NOT NULL,
   TYPE                 SMALLINT             NOT NULL,
   INFO_ID              BIGINT               NOT NULL,
   SMALL_PATH           CHARACTER VARYING(128) NULL,
   REAL_PATH            CHARACTER VARYING(128) NOT NULL,
   REMOTE               BOOLEAN              NOT NULL,
   FILE_SIZE            BIGINT               NOT NULL,
   THIRD_HASH           CHARACTER VARYING(128) NULL,
   REMARK               CHARACTER VARYING(256) NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
   CONSTRAINT PK_T_FILE PRIMARY KEY (ID)
);

COMMENT ON TABLE T_FILE IS
'存储文件';

COMMENT ON COLUMN T_FILE.ID IS
'ID';

COMMENT ON COLUMN T_FILE.NAME IS
'名称';

COMMENT ON COLUMN T_FILE.TYPE IS
'类型,字段预留，满足各种业务需要';

COMMENT ON COLUMN T_FILE.INFO_ID IS
'所属业务ID,主表关联外键';

COMMENT ON COLUMN T_FILE.SMALL_PATH IS
'预览文件';

COMMENT ON COLUMN T_FILE.REAL_PATH IS
'上传文件';

COMMENT ON COLUMN T_FILE.REMOTE IS
'第三方存储';

COMMENT ON COLUMN T_FILE.FILE_SIZE IS
'文件大小';

COMMENT ON COLUMN T_FILE.THIRD_HASH IS
'第三方HASH';

COMMENT ON COLUMN T_FILE.REMARK IS
'备注';

COMMENT ON COLUMN T_FILE.CREATE_USER IS
'上传人员';

COMMENT ON COLUMN T_FILE.CREATE_TIME IS
'上传时间';

/*==============================================================*/
/* Index: IDX_FILE_TYPE_ID                                      */
/*==============================================================*/
CREATE  INDEX IDX_FILE_TYPE_ID ON T_FILE (
TYPE,
INFO_ID
);

/*==============================================================*/
/* Table: T_INDEX_ARTICLE                                       */
/*==============================================================*/
CREATE TABLE T_INDEX_ARTICLE (
   ID                   BIGSERIAL            NOT NULL,
   SORT                 CHARACTER VARYING(4) NOT NULL,
   IMAGE                CHARACTER VARYING(128) NOT NULL,
   TITLE_DISPLAY        BOOLEAN              NOT NULL,
   LINK_TYPE            SMALLINT             NOT NULL,
   LINK_VALUE           CHARACTER VARYING(128) NOT NULL,
   STATE                SMALLINT             NOT NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_T_INDEX_ARTICLE PRIMARY KEY (ID)
);

COMMENT ON TABLE T_INDEX_ARTICLE IS
'首页发现设置';

COMMENT ON COLUMN T_INDEX_ARTICLE.SORT IS
'序号';

COMMENT ON COLUMN T_INDEX_ARTICLE.IMAGE IS
'图片';

COMMENT ON COLUMN T_INDEX_ARTICLE.TITLE_DISPLAY IS
'是否显示标题';

COMMENT ON COLUMN T_INDEX_ARTICLE.LINK_TYPE IS
'链接类型';

COMMENT ON COLUMN T_INDEX_ARTICLE.LINK_VALUE IS
'链接值';

COMMENT ON COLUMN T_INDEX_ARTICLE.STATE IS
'状态';

COMMENT ON COLUMN T_INDEX_ARTICLE.CREATE_USER IS
'创建人';

COMMENT ON COLUMN T_INDEX_ARTICLE.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_INDEX_ARTICLE.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN T_INDEX_ARTICLE.UPDATE_TIME IS
'修改时间';

/*==============================================================*/
/* Table: T_INDEX_BANNER                                        */
/*==============================================================*/
CREATE TABLE T_INDEX_BANNER (
   ID                   BIGSERIAL            NOT NULL,
   TITLE                CHARACTER VARYING(64) NOT NULL,
   SORT                 CHARACTER VARYING(8) NOT NULL,
   IMAGE                CHARACTER VARYING(128) NULL,
   BEGIN_DATE           DATE                 NOT NULL,
   END_DATE             DATE                 NOT NULL,
   LINK_TYPE            SMALLINT             NOT NULL,
   LINK_VALUE           CHARACTER VARYING(128) NOT NULL,
   PLACE                SMALLINT             NOT NULL,
   USE_LIMIT            SMALLINT             NOT NULL,
   STATE                SMALLINT             NOT NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_T_INDEX_BANNER PRIMARY KEY (ID)
);

COMMENT ON TABLE T_INDEX_BANNER IS
'首页BANNER设置';

COMMENT ON COLUMN T_INDEX_BANNER.TITLE IS
'标题';

COMMENT ON COLUMN T_INDEX_BANNER.SORT IS
'序号';

COMMENT ON COLUMN T_INDEX_BANNER.IMAGE IS
'图片';

COMMENT ON COLUMN T_INDEX_BANNER.BEGIN_DATE IS
'开始日期';

COMMENT ON COLUMN T_INDEX_BANNER.END_DATE IS
'截止日期';

COMMENT ON COLUMN T_INDEX_BANNER.LINK_TYPE IS
'链接类型';

COMMENT ON COLUMN T_INDEX_BANNER.LINK_VALUE IS
'链接值';

COMMENT ON COLUMN T_INDEX_BANNER.PLACE IS
'BANNER位置,1:顶部;2:中部';

COMMENT ON COLUMN T_INDEX_BANNER.USE_LIMIT IS
'适用范围:0,无限制;安卓市场特有标志';

COMMENT ON COLUMN T_INDEX_BANNER.STATE IS
'状态';

COMMENT ON COLUMN T_INDEX_BANNER.CREATE_USER IS
'创建人';

COMMENT ON COLUMN T_INDEX_BANNER.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_INDEX_BANNER.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN T_INDEX_BANNER.UPDATE_TIME IS
'修改时间';

/*==============================================================*/
/* Table: T_INDEX_CATEGORY                                      */
/*==============================================================*/
CREATE TABLE T_INDEX_CATEGORY (
   ID                   BIGSERIAL            NOT NULL,
   PLACE                SMALLINT             NOT NULL,
   IMAGE                CHARACTER VARYING(128) NOT NULL,
   LINK_TYPE            SMALLINT             NOT NULL,
   STATE                SMALLINT             NOT NULL,
   LINK_VALUE           CHARACTER VARYING(128) NOT NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_T_INDEX_CATEGORY PRIMARY KEY (ID)
);

COMMENT ON TABLE T_INDEX_CATEGORY IS
'首页类目设置';

COMMENT ON COLUMN T_INDEX_CATEGORY.PLACE IS
'位置';

COMMENT ON COLUMN T_INDEX_CATEGORY.IMAGE IS
'图片';

COMMENT ON COLUMN T_INDEX_CATEGORY.LINK_TYPE IS
'链接类型';

COMMENT ON COLUMN T_INDEX_CATEGORY.STATE IS
'状态';

COMMENT ON COLUMN T_INDEX_CATEGORY.LINK_VALUE IS
'链接值';

COMMENT ON COLUMN T_INDEX_CATEGORY.CREATE_USER IS
'创建人';

COMMENT ON COLUMN T_INDEX_CATEGORY.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_INDEX_CATEGORY.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN T_INDEX_CATEGORY.UPDATE_TIME IS
'修改时间';

/*==============================================================*/
/* Table: T_MAIL_DETAIL                                         */
/*==============================================================*/
CREATE TABLE T_MAIL_DETAIL (
   ID                   BIGSERIAL            NOT NULL,
   ORDER_ID             BIGINT               NOT NULL,
   USER_ID              BIGINT               NOT NULL,
   EMAIL                CHARACTER VARYING(64) NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   STATE                SMALLINT             NOT NULL,
   CONSTRAINT PK_T_MAIL_DETAIL PRIMARY KEY (ID)
);

COMMENT ON TABLE T_MAIL_DETAIL IS
'电子邮箱发送信息表';

COMMENT ON COLUMN T_MAIL_DETAIL.ORDER_ID IS
'订单ID';

COMMENT ON COLUMN T_MAIL_DETAIL.USER_ID IS
'用户ID';

COMMENT ON COLUMN T_MAIL_DETAIL.EMAIL IS
'电子邮箱';

COMMENT ON COLUMN T_MAIL_DETAIL.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_MAIL_DETAIL.STATE IS
'发送状态';

/*==============================================================*/
/* Table: T_MODEL_OPTION                                        */
/*==============================================================*/
CREATE TABLE T_MODEL_OPTION (
   ID                   BIGSERIAL            NOT NULL,
   PROPERTY_NAME        CHARACTER VARYING(32) NOT NULL,
   ICON                 CHARACTER VARYING(128) NULL,
   MODEL_ID             BIGINT               NOT NULL,
   CONSTRAINT PK_T_MODEL_OPTION PRIMARY KEY (ID)
);

COMMENT ON TABLE T_MODEL_OPTION IS
'商品规格选项';

COMMENT ON COLUMN T_MODEL_OPTION.PROPERTY_NAME IS
'属性名';

COMMENT ON COLUMN T_MODEL_OPTION.ICON IS
'图标';

COMMENT ON COLUMN T_MODEL_OPTION.MODEL_ID IS
'所属分类规格';

/*==============================================================*/
/* Table: T_ORDER                                               */
/*==============================================================*/
CREATE TABLE T_ORDER (
   ID                   BIGSERIAL NOT NULL,
   CODE                 CHARACTER VARYING(32) NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   PAY_TIME             TIMESTAMP WITH TIME ZONE NULL,
   SEND_TIME            TIMESTAMP WITH TIME ZONE NULL,
   CANCEL_TYPE          SMALLINT             NULL,
   CANCEL_TIME          TIMESTAMP WITH TIME ZONE NULL,
   SELF_TIME            TIMESTAMP WITH TIME ZONE NULL,
   FINISH_TIME          TIMESTAMP WITH TIME ZONE NULL,
   STATE                SMALLINT             NOT NULL,
   CURRENCY             CHARACTER VARYING(4) NULL,
   FEE_SHIP             DOUBLE PRECISION     NULL DEFAULT '0',
   REAL_SHIP            DOUBLE PRECISION     NULL,
   COUPONE              DOUBLE PRECISION     NULL DEFAULT '1',
   DISCOUNT             DOUBLE PRECISION     NULL,
   FEE_SCORE            DOUBLE PRECISION     NULL,
   TAX                  DOUBLE PRECISION     NULL DEFAULT '0',
   AMOUNT               DOUBLE PRECISION     NOT NULL DEFAULT '0',
   DIFF                 BOOLEAN              NULL,
   EXPRESS              CHARACTER VARYING(32) NULL,
   PAY_CODE             CHARACTER VARYING(32) NULL,
   TRACKING_NO          CHARACTER VARYING(32) NULL,
   ZIP                  CHARACTER VARYING(8) NULL,
   CONSIGNEE            CHARACTER VARYING(32) NULL,
   ID_TYPE              CHARACTER VARYING(32) NULL,
   ID_CARD              CHARACTER VARYING(32) NULL,
   PHONE                CHARACTER VARYING(16) NULL,
   ADDRESS              CHARACTER VARYING(128) NULL,
   PAYMENT_ID           BIGINT               NULL,
   PARENT_CODE          CHARACTER VARYING(32) NULL,
   USER_ID              BIGINT               NULL,
   USERNAME             CHARACTER VARYING(128) NOT NULL,
   COUNTRY              CHARACTER VARYING(8) NULL,
   ORG_ID               BIGINT               NOT NULL,
   REMARK               CHARACTER VARYING(256) NULL,
   VERSION              BIGINT               NULL,
   DEVICE_NO            CHARACTER VARYING(64) NOT NULL,
   TRACK                CHARACTER VARYING(30) NULL,
   PAY_TYPE             SMALLINT             NULL,
   CONSTRAINT PK_T_ORDER PRIMARY KEY (ID)
);

COMMENT ON TABLE T_ORDER IS
'订单';

COMMENT ON COLUMN T_ORDER.ID IS
'ID';

COMMENT ON COLUMN T_ORDER.CODE IS
'订单编号';

COMMENT ON COLUMN T_ORDER.CREATE_TIME IS
'订单时间';

COMMENT ON COLUMN T_ORDER.PAY_TIME IS
'付款时间';

COMMENT ON COLUMN T_ORDER.SEND_TIME IS
'发货时间';

COMMENT ON COLUMN T_ORDER.CANCEL_TYPE IS
'取消方式:1,系统;2,手动';

COMMENT ON COLUMN T_ORDER.CANCEL_TIME IS
'取消时间';

COMMENT ON COLUMN T_ORDER.SELF_TIME IS
'自提时间';

COMMENT ON COLUMN T_ORDER.FINISH_TIME IS
'完成时间';

COMMENT ON COLUMN T_ORDER.STATE IS
'订单状态';

COMMENT ON COLUMN T_ORDER.CURRENCY IS
'币别';

COMMENT ON COLUMN T_ORDER.FEE_SHIP IS
'运费';

COMMENT ON COLUMN T_ORDER.REAL_SHIP IS
'实付运费';

COMMENT ON COLUMN T_ORDER.COUPONE IS
'抵扣券';

COMMENT ON COLUMN T_ORDER.DISCOUNT IS
'优惠金额';

COMMENT ON COLUMN T_ORDER.FEE_SCORE IS
'积分抵扣金额';

COMMENT ON COLUMN T_ORDER.TAX IS
'税费';

COMMENT ON COLUMN T_ORDER.AMOUNT IS
'订单金额';

COMMENT ON COLUMN T_ORDER.DIFF IS
'子订单金额与总订单金额不一致';

COMMENT ON COLUMN T_ORDER.EXPRESS IS
'承运商';

COMMENT ON COLUMN T_ORDER.PAY_CODE IS
'支付流水号';

COMMENT ON COLUMN T_ORDER.TRACKING_NO IS
'运单号';

COMMENT ON COLUMN T_ORDER.ZIP IS
'邮编';

COMMENT ON COLUMN T_ORDER.CONSIGNEE IS
'收货人';

COMMENT ON COLUMN T_ORDER.ID_TYPE IS
'证件类型';

COMMENT ON COLUMN T_ORDER.ID_CARD IS
'身份证件';

COMMENT ON COLUMN T_ORDER.PHONE IS
'电话';

COMMENT ON COLUMN T_ORDER.ADDRESS IS
'地址';

COMMENT ON COLUMN T_ORDER.PAYMENT_ID IS
'支付ID';

COMMENT ON COLUMN T_ORDER.PARENT_CODE IS
'主订单号';

COMMENT ON COLUMN T_ORDER.USER_ID IS
'用户ID';

COMMENT ON COLUMN T_ORDER.USERNAME IS
'用户';

COMMENT ON COLUMN T_ORDER.COUNTRY IS
'国家地区';

COMMENT ON COLUMN T_ORDER.ORG_ID IS
'所属商家';

COMMENT ON COLUMN T_ORDER.REMARK IS
'备注';

COMMENT ON COLUMN T_ORDER.VERSION IS
'乐观锁,乐观行锁标志，解决并发问题';

/*==============================================================*/
/* Index: IDX_ORDER_CODE                                        */
/*==============================================================*/
CREATE  INDEX IDX_ORDER_CODE ON T_ORDER (
CODE
);

/*==============================================================*/
/* Index: IDX_ORDER_ORDER_TIME                                  */
/*==============================================================*/
CREATE  INDEX IDX_ORDER_ORDER_TIME ON T_ORDER (
CREATE_TIME
);

/*==============================================================*/
/* Index: IDX_ORDER_ORG                                         */
/*==============================================================*/
CREATE  INDEX IDX_ORDER_ORG ON T_ORDER (
ORG_ID
);

/*==============================================================*/
/* Table: T_ORDER_DETAIL                                        */
/*==============================================================*/
CREATE TABLE T_ORDER_DETAIL (
   ID                   BIGSERIAL            NOT NULL,
   SKU_ID               BIGINT               NOT NULL,
   QTY                  INTEGER              NOT NULL,
   PRICE                DOUBLE PRECISION     NOT NULL,
   CURRENCY             CHARACTER VARYING(4) NOT NULL,
   PRODUCT_MODEL        CHARACTER VARYING(32) NULL,
   ORG_ID               BIGINT               NOT NULL,
   ORDER_NO             CHARACTER VARYING(32) NOT NULL,
   PROMOTE_ID           BIGINT               NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   USER_ID              BIGINT               NULL,
   VERSION              BIGINT               NULL,
   CONSTRAINT PK_T_ORDER_DETAIL PRIMARY KEY (ID)
);

COMMENT ON TABLE T_ORDER_DETAIL IS
'订单明细';

COMMENT ON COLUMN T_ORDER_DETAIL.ID IS
'ID';

COMMENT ON COLUMN T_ORDER_DETAIL.SKU_ID IS
'商品ID';

COMMENT ON COLUMN T_ORDER_DETAIL.QTY IS
'数量';

COMMENT ON COLUMN T_ORDER_DETAIL.PRICE IS
'单价';

COMMENT ON COLUMN T_ORDER_DETAIL.CURRENCY IS
'币别';

COMMENT ON COLUMN T_ORDER_DETAIL.PRODUCT_MODEL IS
'商品规格';

COMMENT ON COLUMN T_ORDER_DETAIL.ORG_ID IS
'所属商家';

COMMENT ON COLUMN T_ORDER_DETAIL.ORDER_NO IS
'所属订单';

COMMENT ON COLUMN T_ORDER_DETAIL.PROMOTE_ID IS
'活动ID';

COMMENT ON COLUMN T_ORDER_DETAIL.CREATE_TIME IS
'订单时间';

COMMENT ON COLUMN T_ORDER_DETAIL.USER_ID IS
'用户ID';

COMMENT ON COLUMN T_ORDER_DETAIL.VERSION IS
'乐观锁,乐观行锁标志，解决并发问题';

/*==============================================================*/
/* Index: IDX_ORDER_DETAIL_USER                                 */
/*==============================================================*/
CREATE  INDEX IDX_ORDER_DETAIL_USER ON T_ORDER_DETAIL (
USER_ID
);

/*==============================================================*/
/* Index: IDX_ORDER_DETAIL_ORDER                                */
/*==============================================================*/
CREATE  INDEX IDX_ORDER_DETAIL_ORDER ON T_ORDER_DETAIL (
ORDER_NO
);

/*==============================================================*/
/* Table: T_PRODUCT                                             */
/*==============================================================*/
CREATE TABLE T_PRODUCT (
   ID                   BIGSERIAL            NOT NULL,
   SKU                  CHARACTER VARYING(32) NOT NULL,
   SKU_NAME             CHARACTER VARYING(128) NOT NULL,
   CODE                 CHARACTER VARYING(16) NULL,
   TITLE                CHARACTER VARYING(128) NULL,
   REGIST_NAME          CHARACTER VARYING(128) NULL,
   PRICE                DOUBLE PRECISION     NOT NULL,
   PRICE_MAX            DOUBLE PRECISION     NOT NULL,
   CURRENCY             CHARACTER VARYING(4) NULL,
   BRAND                CHARACTER VARYING(32) NULL,
   EXPIRATION           INTEGER              NULL,
   ORIGIN               CHARACTER VARYING(32) NULL,
   TAX_RATE             DOUBLE PRECISION     NOT NULL,
   INGREDIENT           CHARACTER VARYING(128) NULL,
   AREA                 CHARACTER VARYING(256) NULL,
   LENGTH               INTEGER              NULL,
   WIDTH                INTEGER              NULL,
   HEIGHT               INTEGER              NULL,
   WEIGHT               DOUBLE PRECISION     NOT NULL,
   COMBO                BOOLEAN              NOT NULL,
   STOCK_HOLD           INTEGER              NOT NULL,
   STATE                SMALLINT             NOT NULL,
   STOCK                INTEGER              NOT NULL,
   BUY_LIMIT            INTEGER              NULL,
   DESCRIPTION          TEXT                 NULL,
   REASON               CHARACTER VARYING(256) NULL,
   REMARK               CHARACTER VARYING(256) NULL,
   ORG_ID               BIGINT               NOT NULL,
   ON_TIME              TIMESTAMP WITH TIME ZONE NULL,
   OFF_TIME             TIMESTAMP WITH TIME ZONE NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   PRICE_TWO            DOUBLE PRECISION     NULL,
   SALES                BIGINT               NULL,
   PRICE_ONE            DOUBLE PRECISION     NULL,
   IS_UPLOAD            SMALLINT             NULL,
   FIRE                 CHARACTER VARYING(35) NULL,
   V_STATUS             SMALLINT             NULL,
   CONSTRAINT PK_T_PRODUCT PRIMARY KEY (ID)
);

COMMENT ON TABLE T_PRODUCT IS
'商品';

COMMENT ON COLUMN T_PRODUCT.ID IS
'ID';

COMMENT ON COLUMN T_PRODUCT.SKU IS
'SKU';

COMMENT ON COLUMN T_PRODUCT.SKU_NAME IS
'商品名称';

COMMENT ON COLUMN T_PRODUCT.CODE IS
'商家内部编码';

COMMENT ON COLUMN T_PRODUCT.TITLE IS
'附标题';

COMMENT ON COLUMN T_PRODUCT.REGIST_NAME IS
'备案名称';

COMMENT ON COLUMN T_PRODUCT.PRICE IS
'价格';

COMMENT ON COLUMN T_PRODUCT.PRICE_MAX IS
'最高价格';

COMMENT ON COLUMN T_PRODUCT.CURRENCY IS
'币别';

COMMENT ON COLUMN T_PRODUCT.BRAND IS
'品牌';

COMMENT ON COLUMN T_PRODUCT.EXPIRATION IS
'保质期';

COMMENT ON COLUMN T_PRODUCT.ORIGIN IS
'原产地';

COMMENT ON COLUMN T_PRODUCT.TAX_RATE IS
'税率';

COMMENT ON COLUMN T_PRODUCT.INGREDIENT IS
'成分';

COMMENT ON COLUMN T_PRODUCT.AREA IS
'配送范围';

COMMENT ON COLUMN T_PRODUCT.LENGTH IS
'长';

COMMENT ON COLUMN T_PRODUCT.WIDTH IS
'宽';

COMMENT ON COLUMN T_PRODUCT.HEIGHT IS
'高';

COMMENT ON COLUMN T_PRODUCT.WEIGHT IS
'重量';

COMMENT ON COLUMN T_PRODUCT.COMBO IS
'是否组合产品,0:否;1:是';

COMMENT ON COLUMN T_PRODUCT.STOCK_HOLD IS
'锁定库存';

COMMENT ON COLUMN T_PRODUCT.STATE IS
'在线状态,1:上架;0:下架';

COMMENT ON COLUMN T_PRODUCT.STOCK IS
'库存数量';

COMMENT ON COLUMN T_PRODUCT.BUY_LIMIT IS
'限购数量,0表示无限购';

COMMENT ON COLUMN T_PRODUCT.DESCRIPTION IS
'图文描述';

COMMENT ON COLUMN T_PRODUCT.REASON IS
'驳回原因';

COMMENT ON COLUMN T_PRODUCT.REMARK IS
'备注';

COMMENT ON COLUMN T_PRODUCT.ORG_ID IS
'所属商家';

COMMENT ON COLUMN T_PRODUCT.ON_TIME IS
'上架时间';

COMMENT ON COLUMN T_PRODUCT.OFF_TIME IS
'下架时间';

COMMENT ON COLUMN T_PRODUCT.CREATE_USER IS
'创建人';

COMMENT ON COLUMN T_PRODUCT.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_PRODUCT.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN T_PRODUCT.UPDATE_TIME IS
'修改时间';

/*==============================================================*/
/* Index: IDX_PRODUCT_SKU                                       */
/*==============================================================*/
CREATE  INDEX IDX_PRODUCT_SKU ON T_PRODUCT (
SKU
);

/*==============================================================*/
/* Index: IDX_PRODUCT_VENDER                                    */
/*==============================================================*/
CREATE  INDEX IDX_PRODUCT_VENDER ON T_PRODUCT (
ORG_ID
);

/*==============================================================*/
/* Index: IDX_PRODUCT_STATE                                     */
/*==============================================================*/
CREATE  INDEX IDX_PRODUCT_STATE ON T_PRODUCT (
STATE
);

/*==============================================================*/
/* Index: IDX_PRODUCT_NAME                                      */
/*==============================================================*/
CREATE  INDEX IDX_PRODUCT_NAME ON T_PRODUCT (
SKU_NAME
);

/*==============================================================*/
/* Table: T_REFUND                                              */
/*==============================================================*/
CREATE TABLE T_REFUND (
   ID                   BIGSERIAL            NOT NULL,
   CODE                 CHARACTER VARYING(32) NOT NULL,
   ORDER_NO             CHARACTER VARYING(32) NOT NULL,
   PAY_NO               CHARACTER VARYING(32) NOT NULL,
   CHANNEL              CHARACTER VARYING(32) NOT NULL,
   CURRENCY             CHARACTER VARYING(8) NOT NULL,
   AMOUNT               DOUBLE PRECISION     NOT NULL,
   FEE_REFUND           DOUBLE PRECISION     NOT NULL,
   ACCOUNT              CHARACTER VARYING(32) NULL,
   REASON               CHARACTER VARYING(256) NULL,
   TYPE                 CHARACTER VARYING(16) NOT NULL,
   STATE                SMALLINT             NOT NULL,
   PAYEE                CHARACTER VARYING(32) NOT NULL,
   PHONE                CHARACTER VARYING(32) NOT NULL,
   PAYMENT_ID           BIGINT               NOT NULL,
   USER_ID              BIGINT               NOT NULL,
   USERNAME             CHARACTER VARYING(128) NOT NULL,
   ORG_IDS              BIGINT               NOT NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   REMARK               CHARACTER VARYING(256) NULL,
   CONSTRAINT PK_T_REFUND PRIMARY KEY (ID)
);

COMMENT ON TABLE T_REFUND IS
'退款信息表';

COMMENT ON COLUMN T_REFUND.CODE IS
'退款编号';

COMMENT ON COLUMN T_REFUND.ORDER_NO IS
'订单号';

COMMENT ON COLUMN T_REFUND.PAY_NO IS
'支付订单号';

COMMENT ON COLUMN T_REFUND.CHANNEL IS
'付款通道';

COMMENT ON COLUMN T_REFUND.CURRENCY IS
'原始币别';

COMMENT ON COLUMN T_REFUND.AMOUNT IS
'原始金额';

COMMENT ON COLUMN T_REFUND.FEE_REFUND IS
'退款金额';

COMMENT ON COLUMN T_REFUND.ACCOUNT IS
'虚拟账号';

COMMENT ON COLUMN T_REFUND.REASON IS
'退款原因';

COMMENT ON COLUMN T_REFUND.TYPE IS
'退款类别';

COMMENT ON COLUMN T_REFUND.STATE IS
'状态';

COMMENT ON COLUMN T_REFUND.PAYEE IS
'收款人';

COMMENT ON COLUMN T_REFUND.PHONE IS
'收款人电话';

COMMENT ON COLUMN T_REFUND.PAYMENT_ID IS
'支付ID';

COMMENT ON COLUMN T_REFUND.USER_ID IS
'用户ID';

COMMENT ON COLUMN T_REFUND.USERNAME IS
'用户名';

COMMENT ON COLUMN T_REFUND.ORG_IDS IS
'商家ID集';

COMMENT ON COLUMN T_REFUND.CREATE_USER IS
'创建人';

COMMENT ON COLUMN T_REFUND.CREATE_TIME IS
'退款时间';

COMMENT ON COLUMN T_REFUND.UPDATE_TIME IS
'到账时间';

COMMENT ON COLUMN T_REFUND.REMARK IS
'备注';

/*==============================================================*/
/* Table: T_SPREAD                                              */
/*==============================================================*/
CREATE TABLE T_SPREAD (
   ID                   BIGSERIAL            NOT NULL,
   TITLE                CHARACTER VARYING(64) NOT NULL,
   TYPE                 SMALLINT             NOT NULL,
   TOTAL                INTEGER              NOT NULL,
   TEMPLATE_ID          BIGINT               NOT NULL,
   CONTENT              CHARACTER VARYING(256) NOT NULL,
   SHARE                CHARACTER VARYING(256) NOT NULL,
   IMAGE                CHARACTER VARYING(128) NOT NULL,
   LIMIT_NUMBER         INTEGER              NOT NULL,
   USE_LIMIT            SMALLINT             NOT NULL,
   STATE                SMALLINT             NOT NULL,
   BEGIN_APPLY          TIMESTAMP WITH TIME ZONE NOT NULL,
   END_APPLY            TIMESTAMP WITH TIME ZONE NOT NULL,
   BEGIN_DATE           TIMESTAMP WITH TIME ZONE NOT NULL,
   END_DATE             TIMESTAMP WITH TIME ZONE NOT NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_T_SPREAD PRIMARY KEY (ID)
);

COMMENT ON TABLE T_SPREAD IS
'推广活动';

COMMENT ON COLUMN T_SPREAD.TITLE IS
'活动名称';

COMMENT ON COLUMN T_SPREAD.TYPE IS
'活动类型';

COMMENT ON COLUMN T_SPREAD.TOTAL IS
'活动人数';

COMMENT ON COLUMN T_SPREAD.TEMPLATE_ID IS
'活动模板';

COMMENT ON COLUMN T_SPREAD.CONTENT IS
'报名信息,多个以'',''间隔';

COMMENT ON COLUMN T_SPREAD.SHARE IS
'分享内容';

COMMENT ON COLUMN T_SPREAD.IMAGE IS
'活动图片';

COMMENT ON COLUMN T_SPREAD.LIMIT_NUMBER IS
'限制报名数量';

COMMENT ON COLUMN T_SPREAD.USE_LIMIT IS
'适用范围:0,无限制;安卓市场特有标志';

COMMENT ON COLUMN T_SPREAD.STATE IS
'活动状态';

COMMENT ON COLUMN T_SPREAD.BEGIN_APPLY IS
'报名开始时间';

COMMENT ON COLUMN T_SPREAD.END_APPLY IS
'报名截止时间';

COMMENT ON COLUMN T_SPREAD.BEGIN_DATE IS
'活动开始时间';

COMMENT ON COLUMN T_SPREAD.END_DATE IS
'活动截止时间';

COMMENT ON COLUMN T_SPREAD.CREATE_USER IS
'创建人';

COMMENT ON COLUMN T_SPREAD.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_SPREAD.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN T_SPREAD.UPDATE_TIME IS
'修改时间';

/*==============================================================*/
/* Table: T_SPREAD_DETAIL                                       */
/*==============================================================*/
CREATE TABLE T_SPREAD_DETAIL (
   ID                   BIGSERIAL            NOT NULL,
   USER_ID              BIGINT               NOT NULL,
   APPLY_TIME           TIMESTAMP WITH TIME ZONE NOT NULL,
   SPREAD_ID            BIGINT               NOT NULL,
   CONSTRAINT PK_T_SPREAD_DETAIL PRIMARY KEY (ID)
);

COMMENT ON TABLE T_SPREAD_DETAIL IS
'推广活动明细';

COMMENT ON COLUMN T_SPREAD_DETAIL.USER_ID IS
'用户ID';

COMMENT ON COLUMN T_SPREAD_DETAIL.APPLY_TIME IS
'创建时间';

COMMENT ON COLUMN T_SPREAD_DETAIL.SPREAD_ID IS
'所属活动ID';

/*==============================================================*/
/* Table: T_TEMPLATE                                            */
/*==============================================================*/
CREATE TABLE T_TEMPLATE (
   ID                   BIGSERIAL NOT NULL,
   NAME                 CHARACTER VARYING(32) NOT NULL,
   TYPE                 SMALLINT             NOT NULL,
   CONTENT              TEXT                 NOT NULL,
   STATE                SMALLINT             NOT NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_T_TEMPLATE PRIMARY KEY (ID)
);

COMMENT ON TABLE T_TEMPLATE IS
'模板设置';

COMMENT ON COLUMN T_TEMPLATE.ID IS
'ID';

COMMENT ON COLUMN T_TEMPLATE.NAME IS
'模板名称';

COMMENT ON COLUMN T_TEMPLATE.TYPE IS
'模板类型';

COMMENT ON COLUMN T_TEMPLATE.CONTENT IS
'模板';

COMMENT ON COLUMN T_TEMPLATE.STATE IS
'状态';

COMMENT ON COLUMN T_TEMPLATE.CREATE_USER IS
'创建人';

COMMENT ON COLUMN T_TEMPLATE.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_TEMPLATE.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN T_TEMPLATE.UPDATE_TIME IS
'修改时间';

/*==============================================================*/
/* Table: T_TICKET_LIMIT                                        */
/*==============================================================*/
CREATE TABLE T_TICKET_LIMIT (
   ID                   BIGSERIAL            NOT NULL,
   TYPE                 SMALLINT             NOT NULL,
   INFO_ID              BIGINT               NOT NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_T_TICKET_LIMIT PRIMARY KEY (ID)
);

COMMENT ON TABLE T_TICKET_LIMIT IS
'优惠券适用范围';

COMMENT ON COLUMN T_TICKET_LIMIT.TYPE IS
'类型,字段预留，满足各种业务需要';

COMMENT ON COLUMN T_TICKET_LIMIT.INFO_ID IS
'所属业务ID,主表关联外键';

COMMENT ON COLUMN T_TICKET_LIMIT.CREATE_USER IS
'创建人';

COMMENT ON COLUMN T_TICKET_LIMIT.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_TICKET_LIMIT.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN T_TICKET_LIMIT.UPDATE_TIME IS
'修改时间';

/*==============================================================*/
/* Table: T_TL_TRADE_DETAIL                                     */
/*==============================================================*/
CREATE TABLE T_TL_TRADE_DETAIL (
   ID                   BIGSERIAL            NOT NULL,
   TL_TRADE_ID          BIGINT               NOT NULL,
   PRODUCT_ID           BIGINT               NOT NULL,
   REPLENISH_TYPE       SMALLINT             NOT NULL,
   REPLENISH_NUM        INTEGER              NOT NULL,
   TRADE_TIME           TIMESTAMP WITH TIME ZONE NOT NULL,
   CONSTRAINT PK_T_TL_TRADE_DETAIL PRIMARY KEY (ID)
);

COMMENT ON TABLE T_TL_TRADE_DETAIL IS
'通联交易明细表';

/*==============================================================*/
/* Table: T_TL_TRADE_INFO                                       */
/*==============================================================*/
CREATE TABLE T_TL_TRADE_INFO (
   ID                   BIGSERIAL            NOT NULL,
   ORG_ID               BIGINT               NOT NULL,
   USER_ID              BIGINT               NOT NULL,
   TRADE_TYPE           SMALLINT             NOT NULL,
   TRADE_NO             CHARACTER VARYING(64) NOT NULL,
   TRADE_AMOUNT         DOUBLE PRECISION     NOT NULL,
   TRADE_TIME           TIMESTAMP WITH TIME ZONE NOT NULL,
   TRADE_STATUS         SMALLINT             NOT NULL,
   WX_TRADE_NO          CHARACTER VARYING(128) NULL,
   REMARK               CHARACTER VARYING(64) NULL,
   TL_TRADE_NO          CHARACTER VARYING(128) NULL,
   APP_ID               CHARACTER VARYING(64) NOT NULL,
   CONSTRAINT PK_T_TL_TRADE_INFO PRIMARY KEY (ID)
);

COMMENT ON TABLE T_TL_TRADE_INFO IS
'通联交易信息表';

/*==============================================================*/
/* Table: T_TRADE_FLOW                                          */
/*==============================================================*/
CREATE TABLE T_TRADE_FLOW (
   ID                   BIGSERIAL            NOT NULL,
   ORG_ID               BIGINT               NOT NULL,
   USER_ID              BIGINT               NOT NULL,
   TRADE_TYPE           SMALLINT             NOT NULL,
   TRADE_AMOUNT         DOUBLE PRECISION     NOT NULL,
   TRADE_TIME           TIMESTAMP WITH TIME ZONE NOT NULL,
   BALANCE              DOUBLE PRECISION     NOT NULL,
   TRADE_STATUS         SMALLINT             NOT NULL,
   CONSTRAINT PK_T_TRADE_FLOW PRIMARY KEY (ID)
);

COMMENT ON TABLE T_TRADE_FLOW IS
'交易流水表';

/*==============================================================*/
/* Table: T_USER_BIND                                           */
/*==============================================================*/
CREATE TABLE T_USER_BIND (
   ID                   BIGSERIAL            NOT NULL,
   USER_ID              BIGINT               NOT NULL,
   THIRD_KEY            CHARACTER VARYING(64) NOT NULL,
   THIRD_TYPE           CHARACTER VARYING(8) NOT NULL,
   CONSTRAINT PK_T_USER_BIND PRIMARY KEY (ID)
);

COMMENT ON TABLE T_USER_BIND IS
'用户第三方绑定表';

COMMENT ON COLUMN T_USER_BIND.USER_ID IS
'用户ID';

COMMENT ON COLUMN T_USER_BIND.THIRD_KEY IS
'第三方KEY值';

COMMENT ON COLUMN T_USER_BIND.THIRD_TYPE IS
'第三方类型';

/*==============================================================*/
/* Table: T_WE_USER                                             */
/*==============================================================*/
CREATE TABLE T_WE_USER (
   ID                   BIGSERIAL            NOT NULL,
   OPEN_ID              CHARACTER VARYING(64) NOT NULL,
   ORG_ID               BIGINT               NOT NULL,
   DEVICE_ID            BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UNION_ID             CHARACTER VARYING(64) NOT NULL,
   NICKNAME             CHARACTER VARYING(255) NULL,
   DEV_NO             CHARACTER VARYING(16) NULL,
   CONSTRAINT PK_T_WE_USER PRIMARY KEY (ID)
);

COMMENT ON TABLE T_WE_USER IS
'微商城引流表';

COMMENT ON COLUMN T_WE_USER.OPEN_ID IS
'城市名称';

COMMENT ON COLUMN T_WE_USER.ORG_ID IS
'城市名称';

COMMENT ON COLUMN T_WE_USER.DEVICE_ID IS
'城市名称';

COMMENT ON COLUMN T_WE_USER.CREATE_TIME IS
'创建时间';

/*==============================================================*/
/* Table: T_POINT_PLACE                                         */
/*==============================================================*/
CREATE TABLE T_POINT_PLACE (
   ID                   BIGSERIAL            NOT NULL,
   POINT_NO             CHARACTER VARYING(16) NOT NULL,
   POINT_NAME           CHARACTER VARYING(64) NULL,
   POINT_ADDRESS        CHARACTER VARYING(256) NULL,
   POINT_TYPE           SMALLINT             NOT NULL,
   ORG_ID               BIGINT               NOT NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_T_POINT_PLACE PRIMARY KEY (ID)
);

COMMENT ON TABLE T_POINT_PLACE IS
'点位表';

COMMENT ON COLUMN T_POINT_PLACE.ORG_ID IS
'所属机构';

COMMENT ON COLUMN T_POINT_PLACE.CREATE_USER IS
'创建人';

COMMENT ON COLUMN T_POINT_PLACE.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_POINT_PLACE.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN T_POINT_PLACE.UPDATE_TIME IS
'修改时间';


/*==============================================================*/
/* Table: T_CABINET                                             */
/*==============================================================*/
CREATE TABLE T_CABINET (
   ID                   BIGSERIAL            NOT NULL,
   DEVICE_ID            BIGINT               NULL,
   CABINET_NO           CHARACTER VARYING(32) NULL,
   AISLE_COUNT          SMALLINT             NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   UPDATE_USER          BIGINT               NULL,
   UPDATE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   MODEL                CHARACTER VARYING(32) NULL,
   FACTORY_NO           CHARACTER VARYING(64) NULL,
   FACTORY_TIME         TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_T_CABINET PRIMARY KEY (ID)
);

COMMENT ON TABLE T_CABINET IS
'货柜';

COMMENT ON COLUMN T_CABINET.DEVICE_ID IS
'设备ID';

COMMENT ON COLUMN T_CABINET.CABINET_NO IS
'货柜编号';

COMMENT ON COLUMN T_CABINET.AISLE_COUNT IS
'货道数量';

COMMENT ON COLUMN T_CABINET.CREATE_USER IS
'创建人';

COMMENT ON COLUMN T_CABINET.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_CABINET.UPDATE_USER IS
'修改人';

COMMENT ON COLUMN T_CABINET.UPDATE_TIME IS
'修改时间';

COMMENT ON COLUMN T_CABINET.MODEL IS
'型号';

COMMENT ON COLUMN T_CABINET.FACTORY_NO IS
'出厂编号
出厂编号
';

COMMENT ON COLUMN T_CABINET.FACTORY_TIME IS
'出厂日期';


/*==============================================================*/
/* Table: T_ACTIVE_CODE                                         */
/*==============================================================*/
CREATE TABLE T_ACTIVE_CODE (
   ID                   BIGSERIAL            NOT NULL,
   ACTIVE_CODE          CHARACTER VARYING(128) NULL,
   STATE                SMALLINT             NULL,
   DEVICE_ID            BIGINT               NOT NULL,
   CREATE_USER          BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   ACTIVE_TIME          TIMESTAMP WITH TIME ZONE NULL,
   CONSTRAINT PK_T_ACTIVE_CODE PRIMARY KEY (ID)
);

COMMENT ON TABLE T_ACTIVE_CODE IS
'设备激活码表';

COMMENT ON COLUMN T_ACTIVE_CODE.ACTIVE_CODE IS
'激活码，唯一';

COMMENT ON COLUMN T_ACTIVE_CODE.STATE IS
'激活码状态  0：未使用， 1：已使用';

COMMENT ON COLUMN T_ACTIVE_CODE.DEVICE_ID IS
'激活设备ID';

COMMENT ON COLUMN T_ACTIVE_CODE.CREATE_USER IS
'创建人';

COMMENT ON COLUMN T_ACTIVE_CODE.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_ACTIVE_CODE.ACTIVE_TIME IS
'激活时间';


/*==============================================================*/
/* Table: T_APP_EXCEPTION                                       */
/*==============================================================*/
CREATE TABLE T_APP_EXCEPTION (
   ID                   BIGSERIAL            NOT NULL,
   DEVICE_ID            BIGINT               NOT NULL,
   DEVICE_NO            CHARACTER VARYING(64) NULL,
   VERSION              CHARACTER VARYING(16) NULL,
   EXCEPTIONS           CHARACTER VARYING(256) NOT NULL,
   REMARK               CHARACTER VARYING(256) NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   CONSTRAINT PK_T_APP_EXCEPTION PRIMARY KEY (ID)
);

COMMENT ON TABLE T_APP_EXCEPTION IS
'APP异常信息表';

COMMENT ON COLUMN T_APP_EXCEPTION.ID IS
'ID';

COMMENT ON COLUMN T_APP_EXCEPTION.DEVICE_ID IS
'设备ID';

COMMENT ON COLUMN T_APP_EXCEPTION.DEVICE_NO IS
'设备编号';

COMMENT ON COLUMN T_APP_EXCEPTION.VERSION IS
'版本号';

COMMENT ON COLUMN T_APP_EXCEPTION.EXCEPTIONS IS
'异常信息';

COMMENT ON COLUMN T_APP_EXCEPTION.REMARK IS
'备注';

COMMENT ON COLUMN T_APP_EXCEPTION.CREATE_TIME IS
'创建时间';

/*==============================================================*/
/* Table: T_ADVERTISEMENT                                       */
/*==============================================================*/
CREATE TABLE T_ADVERTISEMENT (
   ID                   BIGSERIAL            NOT NULL,
   ADV_NAME             CHARACTER VARYING(120) NOT NULL,
   INDEX                SMALLINT             NOT NULL,
   ADV_POSITION         SMALLINT             NOT NULL,
   STATUS               SMALLINT             NOT NULL,
   ORG_ID               BIGINT               NOT NULL,
   BEGIN_TIME           TIMESTAMP WITH TIME ZONE NOT NULL,
   END_TIME             TIMESTAMP WITH TIME ZONE NOT NULL,
   CREATIME             TIMESTAMP WITH TIME ZONE NOT NULL,
   SCREEN_TYPE          SMALLINT             NOT NULL,
   ALL_STORE            SMALLINT             NOT NULL,
   REPEAT               CHARACTER VARYING(32) NULL,
   CONSTRAINT PK_T_ADVERTISEMENT PRIMARY KEY (ID)
);

COMMENT ON TABLE T_ADVERTISEMENT IS
'广告信息';

COMMENT ON COLUMN T_ADVERTISEMENT.ID IS
'主键自增ID';

COMMENT ON COLUMN T_ADVERTISEMENT.ADV_NAME IS
'广告名称';

COMMENT ON COLUMN T_ADVERTISEMENT.INDEX IS
'轮播排序';

COMMENT ON COLUMN T_ADVERTISEMENT.ADV_POSITION IS
'广告位置  1：上部  2：下部  3：背景';

COMMENT ON COLUMN T_ADVERTISEMENT.STATUS IS
'状态 0-未上线 1-进行中 2-已下线';

COMMENT ON COLUMN T_ADVERTISEMENT.ORG_ID IS
'机构ID';

COMMENT ON COLUMN T_ADVERTISEMENT.BEGIN_TIME IS
'开始时间';

COMMENT ON COLUMN T_ADVERTISEMENT.END_TIME IS
'结束时间';

COMMENT ON COLUMN T_ADVERTISEMENT.CREATIME IS
'创建时间';

COMMENT ON COLUMN T_ADVERTISEMENT.SCREEN_TYPE IS
'投放屏幕类型  1:横屏  2:竖屏';

COMMENT ON COLUMN T_ADVERTISEMENT.ALL_STORE IS
'是否投放所有店铺  0：否  1：是';

COMMENT ON COLUMN T_ADVERTISEMENT.REPEAT IS
'重复规则  1：周一  2：周二 ... 7：周日  0：不重复';



/*==============================================================*/
/* Table: T_POINT_ADV                                           */
/*==============================================================*/
CREATE TABLE T_POINT_ADV (
   ID                   BIGSERIAL            NOT NULL,
   POINT_ID             BIGINT               NOT NULL,
   ADVERTISE_ID         BIGINT               NOT NULL,
   CONSTRAINT PK_T_POINT_ADV PRIMARY KEY (ID)
);

COMMENT ON TABLE T_POINT_ADV IS
'店铺广告关联表';

COMMENT ON COLUMN T_POINT_ADV.ID IS
'主键ID';

COMMENT ON COLUMN T_POINT_ADV.POINT_ID IS
'店铺ID';

COMMENT ON COLUMN T_POINT_ADV.ADVERTISE_ID IS
'广告ID';


/*==============================================================*/
/* Table: T_DEVICE_PUSH                                         */
/*==============================================================*/
CREATE TABLE T_DEVICE_PUSH (
   ID                   BIGSERIAL            NOT NULL,
   DEVICE_NO            CHARACTER VARYING(16) NOT NULL,
   PUSH_DEVICE_ID       CHARACTER VARYING(64) NOT NULL,
   CONSTRAINT PK_T_DEVICE_PUSH PRIMARY KEY (ID)
);

COMMENT ON TABLE T_DEVICE_PUSH IS
'设备推送关联信息';

COMMENT ON COLUMN T_DEVICE_PUSH.ID IS
'ID';

COMMENT ON COLUMN T_DEVICE_PUSH.FACTORY_DEV_NO IS
'设备组号';

COMMENT ON COLUMN T_DEVICE_PUSH.PUSH_DEVICE_ID IS
'推送设备ID';


/*==============================================================*/
/* Table: T_DEVICE_LOG                                          */
/*==============================================================*/
CREATE TABLE T_DEVICE_LOG (
   ID                   BIGSERIAL            NOT NULL,
   DEVICE_NO            CHARACTER VARYING(16) NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   DEVICE_STATUS        SMALLINT             NOT NULL,
   CONSTRAINT PK_T_DEVICE_LOG PRIMARY KEY (ID)
);

COMMENT ON TABLE T_DEVICE_LOG IS
'设备日志表';

COMMENT ON COLUMN T_DEVICE_LOG.ID IS
'主键ID';

COMMENT ON COLUMN T_DEVICE_LOG.DEVICE_NO IS
'设备组号';

COMMENT ON COLUMN T_DEVICE_LOG.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_DEVICE_LOG.DEVICE_STATUS IS
'设备状态  1：在线  2：离线   3：待补货';


/*==============================================================*/
/* Table: T_PRODUCT_LOG                                         */
/*==============================================================*/
CREATE TABLE T_PRODUCT_LOG (
   ID                   BIGSERIAL            NOT NULL,
   PRODUCT_ID           BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   PRODUCT_STATUS       SMALLINT             NOT NULL,
   CONSTRAINT PK_T_PRODUCT_LOG PRIMARY KEY (ID)
);

COMMENT ON TABLE T_PRODUCT_LOG IS
'商品日志表';

COMMENT ON COLUMN T_PRODUCT_LOG.ID IS
'主键ID';

COMMENT ON COLUMN T_PRODUCT_LOG.PRODUCT_ID IS
'商品ID';

COMMENT ON COLUMN T_PRODUCT_LOG.CREATE_TIME IS
'创建时间';

COMMENT ON COLUMN T_PRODUCT_LOG.PRODUCT_STATUS IS
'商品状态  1：库存不足';




/*==============================================================*/
/* Table: T_DEVICE_RULE                                         */
/*==============================================================*/
CREATE TABLE T_DEVICE_RULE (
   ID                   BIGSERIAL            NOT NULL,
   FACTORY_CODE         CHARACTER VARYING(16) NOT NULL,
   FACTORY_NAME         CHARACTER VARYING(64) NOT NULL,
   MODEL                CHARACTER VARYING(64) NOT NULL,
   ROAD_COMBO           CHARACTER VARYING(256) NULL,
   ROAD_LENGTH          SMALLINT             NULL,
   ROAD_CAPACITY        SMALLINT             NULL,
   CONSTRAINT PK_T_DEVICE_RULE PRIMARY KEY (ID)
);

COMMENT ON TABLE T_DEVICE_RULE IS
'设备规则表';

COMMENT ON COLUMN T_DEVICE_RULE.ID IS
'主键ID';

COMMENT ON COLUMN T_DEVICE_RULE.FACTORY_CODE IS
'设备厂商编码';

COMMENT ON COLUMN T_DEVICE_RULE.FACTORY_NAME IS
'设备厂商名称';

COMMENT ON COLUMN T_DEVICE_RULE.MODEL IS
'设备型号';

COMMENT ON COLUMN T_DEVICE_RULE.ROAD_COMBO IS
'货道组合，多个的话以逗号分割';

COMMENT ON COLUMN T_DEVICE_RULE.ROAD_LENGTH IS
'货道长度';

COMMENT ON COLUMN T_DEVICE_RULE.ROAD_CAPACITY IS
'货道容量';



/*==============================================================*/
/* Table: T_REPLENISHMENT_APP_VERSION                           */
/*==============================================================*/
CREATE TABLE T_REPLENISHMENT_APP_VERSION (
   ID                   BIGSERIAL            NOT NULL,
   IS_SILENT            BOOLEAN              NOT NULL,
   IS_FORCE             BOOLEAN              NOT NULL,
   IS_AUTO_INSTALL      BOOLEAN              NOT NULL,
   IS_IGNORABLE         BOOLEAN              NOT NULL,
   IS_PATCH             BOOLEAN              NOT NULL,
   VERSION_CODE         INTEGER              NOT NULL,
   VERSION_NAME         CHARACTER VARYING(30) NOT NULL,
   UPDATE_CONTENT       CHARACTER VARYING(256) NOT NULL,
   URL                  CHARACTER VARYING(256) NOT NULL,
   MD5                  CHARACTER VARYING(256) NOT NULL,
   SIZE                 BIGINT               NOT NULL,
   PATCH_URL            CHARACTER VARYING(256) NOT NULL,
   PATCH_MD5            CHARACTER VARYING(256) NOT NULL,
   PATCH_SIZE           BIGINT               NOT NULL,
   CREATE_TIME          TIMESTAMP WITH TIME ZONE NOT NULL,
   CONSTRAINT PK_T_REPLENISHMENT_APP_VERSION PRIMARY KEY (ID)
);

COMMENT ON TABLE T_REPLENISHMENT_APP_VERSION IS
'补货APP版本信息表';

COMMENT ON COLUMN T_REPLENISHMENT_APP_VERSION.IS_SILENT IS
'是否静默下载：有新版本时不提示直接下载';

COMMENT ON COLUMN T_REPLENISHMENT_APP_VERSION.IS_FORCE IS
'是否强制安装：不安装无法使用app';

COMMENT ON COLUMN T_REPLENISHMENT_APP_VERSION.IS_AUTO_INSTALL IS
'是否下载完成后自动安装';

COMMENT ON COLUMN T_REPLENISHMENT_APP_VERSION.IS_IGNORABLE IS
'是否可忽略该版本';

COMMENT ON COLUMN T_REPLENISHMENT_APP_VERSION.IS_PATCH IS
'是否是增量补丁包';

COMMENT ON COLUMN T_REPLENISHMENT_APP_VERSION.VERSION_CODE IS
'版本号';

COMMENT ON COLUMN T_REPLENISHMENT_APP_VERSION.VERSION_NAME IS
'版本名称';

COMMENT ON COLUMN T_REPLENISHMENT_APP_VERSION.UPDATE_CONTENT IS
'更新内容';

COMMENT ON COLUMN T_REPLENISHMENT_APP_VERSION.URL IS
'apk下载地址';

COMMENT ON COLUMN T_REPLENISHMENT_APP_VERSION.MD5 IS
'md5加密串';

COMMENT ON COLUMN T_REPLENISHMENT_APP_VERSION.SIZE IS
'apk安装包大小';

COMMENT ON COLUMN T_REPLENISHMENT_APP_VERSION.PATCH_URL IS
'补丁apk下载地址';

COMMENT ON COLUMN T_REPLENISHMENT_APP_VERSION.PATCH_MD5 IS
'补丁md5加密串';

COMMENT ON COLUMN T_REPLENISHMENT_APP_VERSION.PATCH_SIZE IS
'补丁apk安装包大小';

COMMENT ON COLUMN T_REPLENISHMENT_APP_VERSION.CREATE_TIME IS
'创建时间';

