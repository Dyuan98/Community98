package com.yuan.community.util;

/**
 * 用于生成Redis的key的工具类
 * 用于复用
 */
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";   // 实体key前缀
    private static final String PREFIX_USER_LIKE = "like:user";     // 用户key前缀
    private static final String PREFIX_FOLLOWEE = "followee";     // 关注目标key前缀
    private static final String PREFIX_FOLLOWER = "follower";   // 粉丝key前缀
    private static final String PREFIX_KAPTCHA = "kaptcha";    // 验证码key前缀
    private static final String PREFIX_TICKET = "ticket";   // 登陆凭证key前缀
    private static final String PREFIX_USER = "user";      //  user用户key前缀
    private static final String PREFIX_UV = "uv";         // 独立访客key前缀
    private static final String PREFIX_DAU = "dau";     // 日活跃用户key前缀

    // 某个实体的赞
    // like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户的赞
    // like:user:userId -> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个用户关注的实体
    // followee:userId:entityType -> ZSet(entityId,now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体拥有的粉丝
    // follower:entityType:entityId -> ZSet(userId,now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 登录验证码，在用户访问登陆页面时，给它发一个凭证owner，标识验证码属于哪个用户
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录的凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 用户
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    // 单日UV
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 区间UV
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 单日活跃用户
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    // 区间活跃用户
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }
}
