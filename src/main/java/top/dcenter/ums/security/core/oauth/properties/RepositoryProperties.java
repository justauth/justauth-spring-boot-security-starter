/*
 * MIT License
 * Copyright (c) 2020-2029 YongWu zheng (dcenter.top and gitee.com/pcore and github.com/ZeroOrInfinity)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package top.dcenter.ums.security.core.oauth.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.dcenter.ums.security.core.oauth.repository.factory.Auth2JdbcUsersConnectionRepositoryFactory;
import top.dcenter.ums.security.core.oauth.repository.factory.UsersConnectionRepositoryFactory;

/**
 * OAuth2 第三方登录 user_connection 持久化表字段, sql语句属性 设置
 *
 * @author ZYW
 * @version V1.0  Created by 2020/5/3 19:52
 */
@SuppressWarnings("jol")
@Getter
@Setter
@ConfigurationProperties("ums.repository")
public class RepositoryProperties {


    // ================= 第三方登录相关功能 =================

    /**
     * 第三方登录用户注册时: 用户唯一 ID 字段名称， 默认为 userId
     */
    private String userIdParamName = "userId";
    /**
     * 第三方登录用户注册时: 密码 字段名称， 默认为 password
     */
    private String passwordParamName = "password";

    /**
     * 第三方录用户注册时: 服务商 providerId 字段名称， 默认为 providerId
     */
    private String providerIdParamName = "providerId";
    /**
     * 第三方登录用户注册时: 在服务商那的用户唯一ID providerUserId 字段名称， 默认为 providerUserId
     */
    private String providerUserIdParamName = "providerUserId";
    /**
     * 第三方登录用户注册时: 用户头像 avatarUrl 字段名称， 默认为 avatarUrl
     */
    private String avatarUrlParamName = "avatarUrl";



    // ================= 第三方登录 key 与 secret 加密相关 =================
    /**
     * 第三方登录用户数据库表的字段 accessToken 与 refreshToken 加密专用密码
     */
    private String textEncryptorPassword = "7ca5d913a17b4942942d16a974e3fecc";
    /**
     * 第三方登录用户数据库表的字段 accessToken 与 refreshToken 加密专用 salt
     */
    private String textEncryptorSalt = "cd538b1b077542aca5f86942b6507fe2";


    /* ==========================自定义第三方登录用户表及相关 CURD 语句============================= */

    /**
     * 第三方登录用户数据库表名称，<br><br>
     */
    private String tableName = "user_connection";

    /**
     * 第三方登录用户数据库用户表用户唯一 ID 字段名称， 默认为 userId
     */
    private String userIdColumnName = "userId";
    /**
     * 第三方登录用户数据库用户表服务商 providerId 字段名称， 默认为 providerId
     */
    private String providerIdColumnName = "providerId";
    /**
     * 第三方登录用户数据库用户表服务商用户 providerUserId 字段名称， 默认为 providerUserId
     */
    private String providerUserIdColumnName = "providerUserId";
    /**
     * 第三方登录用户数据库用户表 rank 字段名称， 默认为 `rank`。<br><br>
     *     注意：因为 MySQL 8.0 版本 rank 是个关键字。一定要用 ` 包裹。
     */
    private String rankColumnName = "`rank`";

    /**
     * 第三方登录用户数据库用户表用户显示名称 displayName 字段名称， 默认为 displayName
     */
    private String displayNameColumnName = "displayName";
    /**
     * 第三方登录用户数据库用户表用户主页 profileUrl 字段名称， 默认为 profileUrl
     */
    private String profileUrlColumnName = "profileUrl";
    /**
     * 第三方登录用户数据库用户表用户头像 imageUrl 字段名称， 默认为 imageUrl
     */
    private String imageUrlColumnName = "imageUrl";
    /**
     * 第三方登录用户数据库用户表用户 accessToken 字段名称， 默认为 accessToken
     */
    private String accessTokenColumnName = "accessToken";
    /**
     * 第三方登录用户数据库用户表用户 tokenId 字段名称， 默认为 tokenId
     */
    private String tokenIdColumnName = "tokenId";
    /**
     * 第三方登录用户数据库用户表用户显 refreshToken 字段名称， 默认为 refreshToken
     */
    private String refreshTokenColumnName = "refreshToken";
    /**
     * 第三方登录用户数据库用户表用户过期时间 expireTime 字段名称， 默认为 expireTime
     */
    private String expireTimeColumnName = "expireTime";

    /**
     * 第三方登录用户数据库用户表创建语句。 <br><br>
     * 修改第三方登录用户数据库用户表创建语句时，要注意：修改字段名称可以直接修改上面的字段名称即可，不用修改建表语句，不可以减少字段，但可以另外增加字段。<br><br>
     * 用户需要对第三方登录的用户表与 curd 的 sql 语句结构进行更改时, 必须实现对应的 {@link UsersConnectionRepositoryFactory}，
     * 如果需要，请实现 {@link UsersConnectionRepositoryFactory}，可以参考 {@link Auth2JdbcUsersConnectionRepositoryFactory}。<br><br>
     * 注意： sql 语句中的 %s 必须写上，且 %s 的顺序必须与后面的字段名称所对应的含义对应 :<br><br>
     * tableName、<br><br>
     * userIdColumnName、<br><br>
     * providerIdColumnName、<br><br>
     * providerUserIdColumnName、<br><br>
     * rankColumnName、<br><br>
     * displayNameColumnName、<br><br>
     * profileUrlColumnName、<br><br>
     * imageUrlColumnName、<br><br>
     * accessTokenColumnName、<br><br>
     * tokenIdColumnName、<br><br>
     * refreshTokenColumnName、<br><br>
     * expireTimeColumnName、<br><br>
     * userIdColumnName、<br><br>
     * providerIdColumnName、<br><br>
     * providerUserIdColumnName、<br><br>
     * userIdColumnName、<br><br>
     * providerIdColumnName、<br><br>
     * rankColumnName、<br><br>
     * providerIdColumnName、<br><br>
     * providerUserIdColumnName、<br><br>
     * rankColumnName、<br><br>
     */
    private String creatUserConnectionTableSql =
            // @formatter:off
            "CREATE TABLE %s (" +
            "\t%s varchar(36) NOT NULL COMMENT '本地用户id',\n" +
            "\t%s varchar(20) NOT NULL COMMENT '第三方服务商',\n" +
            "\t%s varchar(36) NOT NULL COMMENT '第三方用户id',\n" +
            "\t%s int(11) NOT NULL COMMENT 'userId 绑定同一个 providerId 的排序',\n" +
            "\t%s varchar(64) COMMENT '第三方用户名',\n" +
            "\t%s varchar(256) COMMENT '主页',\n" +
            "\t%s varchar(256) COMMENT '头像',\n" +
            "\t%s varchar(64) NOT NULL COMMENT 'accessToken',\n" +
            "\t%s bigint(20) COMMENT 'auth_token.id',\n" +
            "\t%s varchar(64) COMMENT 'refreshToken',\n" +
            "\t%s bigint(20) DEFAULT '-1' COMMENT '过期时间, 基于 1970-01-01T00:00:00Z, 无过期时间默认为 -1',\n" +
            "\tPRIMARY KEY (%s, %s, %s),\n" +
            "\tunique KEY `idx_userId_providerId_rank`(%s, %s, %s),\n" +
            "\tKEY `idx_providerId_providerUserId_rank` (%s, %s, %s),\n" +
            "\tKEY `idx_tokenId` (%s)\n" +
            "\t) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            // @formatter:off
    /**
     * 第三方登录用户数据库用户表创建语句。 <br><br>
     * 修改第三方登录用户数据库用户表创建语句时，要注意：修改字段名称可以直接修改上面的字段名称即可，不用修改建表语句，不可以减少字段，但可以另外增加字段。<br><br>
     * 用户需要对第三方登录的用户表与 curd 的 sql 语句结构进行更改时, 必须实现对应的 {@link UsersConnectionRepositoryFactory}，
     * 如果需要，请实现 {@link UsersConnectionRepositoryFactory}，可以参考 {@link Auth2JdbcUsersConnectionRepositoryFactory}。<br><br>
     * 注意： sql 语句中的 %s 必须写上，且 %s 的顺序必须与后面的字段名称所对应的含义对应 :<br><br>
     * tableName、<br><br>
     * userIdColumnName、<br><br>
     * providerIdColumnName、<br><br>
     * providerUserIdColumnName、<br><br>
     * rankColumnName、<br><br>
     * displayNameColumnName、<br><br>
     * profileUrlColumnName、<br><br>
     * imageUrlColumnName、<br><br>
     * accessTokenColumnName、<br><br>
     * tokenIdColumnName、<br><br>
     * refreshTokenColumnName、<br><br>
     * expireTimeColumnName、<br><br>
     * userIdColumnName、<br><br>
     * providerIdColumnName、<br><br>
     * providerUserIdColumnName、<br><br>
     * userIdColumnName、<br><br>
     * providerIdColumnName、<br><br>
     * rankColumnName、<br><br>
     * providerIdColumnName、<br><br>
     * providerUserIdColumnName、<br><br>
     * rankColumnName、<br><br>
     */
    public String getCreatUserConnectionTableSql() {
        return String.format(creatUserConnectionTableSql,
                             tableName,
                             userIdColumnName,
                             providerIdColumnName,
                             providerUserIdColumnName,
                             rankColumnName,
                             displayNameColumnName,
                             profileUrlColumnName,
                             imageUrlColumnName,
                             accessTokenColumnName,
                             tokenIdColumnName,
                             refreshTokenColumnName,
                             expireTimeColumnName,
                             userIdColumnName,
                             providerIdColumnName,
                             providerUserIdColumnName,
                             userIdColumnName,
                             providerIdColumnName,
                             rankColumnName,
                             providerIdColumnName,
                             providerUserIdColumnName,
                             rankColumnName,
                             tokenIdColumnName);
    }

    /**
     * 第三方登录用户数据库用户表查询 userIds 的查询语句。 <br><br>
     * 注意： sql 语句中的 %s 必须写上，问号必须与指定的 %s 相对应, %s按顺序会用对应的 :<br><br>
     * databaseName、<br><br>
     * tableName
     */
    private String queryUserConnectionTableExistSql =
            "SELECT COUNT(1) FROM information_schema.tables WHERE table_schema='%s' AND table_name = '%s'";
    /**
     * 第三方登录用户数据库用户表查询 userIds 的查询语句。 <br><br>
     * 注意： sql 语句中的 %s 必须写上，问号必须与指定的 %s 相对应, %s按顺序会用对应的 :<br><br>
     * databaseName、<br><br>
     * tableName
     */
    public String getQueryUserConnectionTableExistSql(String databaseName) {

        return String.format(queryUserConnectionTableExistSql, databaseName, tableName);
    }


    /**
     * 第三方登录用户数据库用户表查询 userIds 的查询语句。 <br><br>
     * 注意： sql 语句中的 %s 必须写上，问号必须与指定的 %s 相对应, %s按顺序会用对应的 :<br><br>
     * userIdColumnName、<br><br>
     * tableName、<br><br>
     * providerIdColumnName、<br><br>
     * providerUserIdColumnName
     */
    private String findUserIdsWithConnectionSql = "select %s from %s where %s = ? and %s = ?";
    /**
     * 第三方登录用户数据库用户表查询 userIds 的查询语句。 <br><br>
     * 注意： sql 语句中的 %s 必须写上，问号必须与指定的 %s 相对应, %s按顺序会用对应的 :<br><br>
     * userIdColumnName、<br><br>
     * tableName、<br><br>
     * providerIdColumnName、<br><br>
     * providerUserIdColumnName
     */
    public String getFindUserIdsWithConnectionSql() {

        return String.format(findUserIdsWithConnectionSql,
                             userIdColumnName,
                             tableName,
                             providerIdColumnName,
                             providerUserIdColumnName);
    }

    /**
     * 通过第三方服务提供商提供的 providerId 与 providerUserIds 从数据库用户表查询 userIds 的查询语句。 <br><br>
     * 注意： sql 语句中的 %s 必须写上，问号必须与指定的 %s 相对应, %s按顺序会用对应的 :<br><br>
     * userIdColumnName、<br><br>
     * tableName、<br><br>
     * providerIdColumnName、<br><br>
     * providerUserIdColumnName
     */
    private String findUserIdsConnectedToSql = "select %s from %S where %s = :%s and %s in (:%s)";
    /**
     * 通过第三方服务提供商提供的 providerId 与 providerUserIds 从数据库用户表查询 userIds 的查询语句。 <br><br>
     * 注意： sql 语句中的 %s 必须写上，问号必须与指定的 %s 相对应, %s按顺序会用对应的 :<br><br>
     * userIdColumnName、<br><br>
     * tableName、<br><br>
     * providerIdColumnName、<br><br>
     * providerUserIdColumnName
     */
    public String getFindUserIdsConnectedToSql() {

        return String.format(findUserIdsConnectedToSql,
                             userIdColumnName,
                             tableName,
                             providerIdColumnName,
                             providerIdColumnName,
                             providerUserIdColumnName,
                             providerUserIdColumnName);
    }

    /**
     * 通过第三方服务提供商提供的 providerId 与 providerUserIds 从数据库用户表查询 userIds 的查询语句。 <br><br>
     * 注意： sql 语句中的 %s 必须写上，问号必须与指定的 %s 相对应, %s按顺序会用对应的 :<br><br>
     * userIdColumnName、<br><br>
     * providerIdColumnName、<br><br>
     * providerUserIdColumnName、<br><br>
     * displayNameColumnName、<br><br>
     * profileUrlColumnName、<br><br>
     * imageUrlColumnName、<br><br>
     * accessTokenColumnName、<br><br>
     * tokenIdColumnName、<br><br>
     * refreshTokenColumnName、<br><br>
     * expireTimeColumnName、<br><br>
     * tableName
     */
    private String selectFromUserConnectionSql = "select %s, %s, %s, %s, %s, %s, %s, %s, %s, %s from %s";

    /**
     * 通过第三方服务提供商提供的 providerId 与 providerUserIds 从数据库用户表查询 userIds 的查询语句。 <br><br>
     * 注意： sql 语句中的 %s 必须写上，问号必须与指定的 %s 相对应, %s按顺序会用对应的 :<br><br>
     * userIdColumnName、<br><br>
     * providerIdColumnName、<br><br>
     * providerUserIdColumnName、<br><br>
     * displayNameColumnName、<br><br>
     * profileUrlColumnName、<br><br>
     * imageUrlColumnName、<br><br>
     * accessTokenColumnName、<br><br>
     * tokenIdColumnName、<br><br>
     * refreshTokenColumnName、<br><br>
     * expireTimeColumnName、<br><br>
     * tableName
     */
    public String getSelectFromUserConnectionSql() {
        return String.format(selectFromUserConnectionSql,
                             userIdColumnName,
                             providerIdColumnName,
                             providerUserIdColumnName,
                             displayNameColumnName,
                             profileUrlColumnName,
                             imageUrlColumnName,
                             accessTokenColumnName,
                             tokenIdColumnName,
                             refreshTokenColumnName,
                             expireTimeColumnName,
                             tableName);
    }

    /**
     * 第三方登录用户数据库用户表更新语句。 <br><br>
     * 注意： sql 语句中的 %s 必须写上，问号必须与指定的 %s 相对应, %s按顺序会用对应的 :<br><br>
     * tableName、<br><br>
     * displayNameColumnName、<br><br>
     * profileUrlColumnName、<br><br>
     * imageUrlColumnName、<br><br>
     * accessTokenColumnName、<br><br>
     * tokenIdColumnName、<br><br>
     * refreshTokenColumnName、<br><br>
     * expireTimeColumnName、<br><br>
     * userIdColumnName、<br><br>
     * providerIdColumnName、<br><br>
     * providerUserIdColumnName
     */
    private String updateConnectionSql =
            // @formatter:off
            "update %s " +
             "set %s = ?, " +
             "%s = ?, " +
             "%s = ?, " +
             "%s = ?, " +
             "%s = ?, " +
             "%s = ?, " +
             "%s = ? " +
             "where %s = ? and " +
             "%s = ? and " +
             "%s = ?";
             // @formatter:on

    /**
     * 第三方登录用户数据库用户表更新语句。 "update %s set %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ? where %s = ? and %s = ? and %s = ?"<br><br>
     * 注意： sql 语句中的 %s 必须写上，问号必须与指定的 %s 相对应, %s按顺序会用对应的 :<br><br>
     * tableName、<br><br>
     * displayNameColumnName、<br><br>
     * profileUrlColumnName、<br><br>
     * imageUrlColumnName、<br><br>
     * accessTokenColumnName、<br><br>
     * tokenIdColumnName、<br><br>
     * refreshTokenColumnName、<br><br>
     * expireTimeColumnName、<br><br>
     * userIdColumnName、<br><br>
     * providerIdColumnName、<br><br>
     * providerUserIdColumnName
     */
    public String getUpdateConnectionSql() {

        return String.format(updateConnectionSql,
                             tableName,
                             displayNameColumnName,
                             profileUrlColumnName,
                             imageUrlColumnName,
                             accessTokenColumnName,
                             tokenIdColumnName,
                             refreshTokenColumnName,
                             expireTimeColumnName,
                             userIdColumnName,
                             providerIdColumnName,
                             providerUserIdColumnName);
    }

    /**
     * 第三方登录用户数据库用户表添加用户语句。 <br><br>
     * 注意： sql 语句中的 %s 必须写上，问号必须与指定的 %s 相对应, %s按顺序会用对应的 :<br><br>
     * tableName、<br><br>
     * userIdColumnName、<br><br>
     * providerIdColumnName、<br><br>
     * providerUserIdColumnName、<br><br>
     * rankColumnName、<br><br>
     * displayNameColumnName、<br><br>
     * profileUrlColumnName、<br><br>
     * imageUrlColumnName、<br><br>
     * accessTokenColumnName、<br><br>
     * tokenIdColumnName、<br><br>
     * refreshTokenColumnName、<br><br>
     * expireTimeColumnName
     */
    private String addConnectionSql =
            // @formatter:off
            "insert into %s(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) " +
            "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            // @formatter:on

    /**
     * 第三方登录用户数据库用户表添加用户语句。 <br><br>
     * 注意： sql 语句中的 %s 必须写上，问号必须与指定的 %s 相对应, %s按顺序会用对应的 :<br><br>
     * tableName、<br><br>
     * userIdColumnName、<br><br>
     * providerIdColumnName、<br><br>
     * providerUserIdColumnName、<br><br>
     * rankColumnName、<br><br>
     * displayNameColumnName、<br><br>
     * profileUrlColumnName、<br><br>
     * imageUrlColumnName、<br><br>
     * accessTokenColumnName、<br><br>
     * tokenIdColumnName、<br><br>
     * refreshTokenColumnName、<br><br>
     * expireTimeColumnName
     */
    public String getAddConnectionSql() {

        return String.format(addConnectionSql,
                             tableName,
                             userIdColumnName,
                             providerIdColumnName,
                             providerUserIdColumnName,
                             rankColumnName,
                             displayNameColumnName,
                             profileUrlColumnName,
                             imageUrlColumnName,
                             accessTokenColumnName,
                             tokenIdColumnName,
                             refreshTokenColumnName,
                             expireTimeColumnName);
    }

    /**
     * 第三方登录用户数据库用户表查询添加用户时的所需 rank 的值。 <br><br>
     * 注意： sql 语句中的 %s 必须写上，问号必须与指定的 %s 相对应, %s按顺序会用对应的 :<br><br>
     * rankColumnName、<br><br>
     * rankColumnName、<br><br>
     * tableName、<br><br>
     * userIdColumnName、<br><br>
     * providerIdColumnName
     */
    private String addConnectionQueryForRankSql = "select coalesce(max(%s) + 1, 1) as %s from %s where %s = ? and %s = ?";

    /**
     * 第三方登录用户数据库用户表查询添加用户时的所需 rank 的值。 <br><br>
     *     select coalesce(max(%s) + 1, 1) as %s from %s where %s = ? and %s = ?
     * 注意： sql 语句中的 %s 必须写上，问号必须与指定的 %s 相对应, %s按顺序会用对应的 :<br><br>
     * rankColumnName、<br><br>
     * rankColumnName、<br><br>
     * tableName、<br><br>
     * userIdColumnName、<br><br>
     * providerIdColumnName
     */
    public String getAddConnectionQueryForRankSql() {

        return String.format(addConnectionQueryForRankSql,
                             rankColumnName,
                             rankColumnName,
                             tableName,
                             userIdColumnName,
                             providerIdColumnName);
    }

    /**
     * 第三方登录用户数据库用户表根据 userId 与 providerId 删除多个用户。 <br><br>
     * 注意： sql 语句中的 %s 必须写上，问号必须与指定的 %s 相对应, %s按顺序会用对应的 :<br><br>
     * tableName、<br><br>
     * userIdColumnName、<br><br>
     * providerIdColumnName
     */
    private String removeConnectionsSql = "delete from %s where %s = ? and %s = ?";
    /**
     * 第三方登录用户数据库用户表根据 userId 与 providerId 删除多个用户。 <br><br>
     * 注意： sql 语句中的 %s 必须写上，问号必须与指定的 %s 相对应, %s按顺序会用对应的 :<br><br>
     * tableName、<br><br>
     * userIdColumnName、<br><br>
     * providerIdColumnName
     */
    public String getRemoveConnectionsSql() {

        return String.format(removeConnectionsSql,
                             tableName,
                             userIdColumnName,
                             providerIdColumnName);
    }
    /**
     * 第三方登录用户数据库用户表根据 userId、providerId、providerUserId 删除一个用户。 <br><br>
     * 注意： sql 语句中的 %s 必须写上，问号必须与指定的 %s 相对应, %s按顺序会用对应的 :<br><br>
     * tableName、<br><br>
     * userIdColumnName、<br><br>
     * providerIdColumnName、<br><br>
     * providerUserIdColumnName
     */
    private String removeConnectionSql = "delete from %s where %s = ? and %s = ? and %s = ?";

    /**
     * 第三方登录用户数据库用户表根据 userId、providerId、providerUserId 删除一个用户。 <br><br>
     * 注意： sql 语句中的 %s 必须写上，问号必须与指定的 %s 相对应, %s按顺序会用对应的 :<br><br>
     * tableName、<br><br>
     * userIdColumnName、<br><br>
     * providerIdColumnName、<br><br>
     * providerUserIdColumnName
     */
    public String getRemoveConnectionSql() {

        return String.format(removeConnectionSql,
                             tableName,
                             userIdColumnName,
                             providerIdColumnName,
                             providerUserIdColumnName);
    }

}