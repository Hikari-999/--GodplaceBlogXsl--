package org.example.utils;

public class Const {
    //JWT令牌
    public static final String JWT_BLACK_LIST = "jwt:blacklist:";
    public final static String JWT_FREQUENCY = "jwt:frequency:";

    //过滤器优先级
    public final static int ORDER_FLOW_LIMIT = -101;
    public static final int ORDER_CORS = -102;

    //邮件验证码
    public final static String VERIFY_EMAIL_LIMIT = "verify:email:limit:";
    public final static String VERIFY_EMAIL_DATA = "verify:email:data:";


    //请求自定义属性
    public final static String ATTR_USER_ID = "userId";  //在注册的时候就将用户id存放进去 方便后续拿取

    //请求频率限制
    public final static String FLOW_LIMIT_COUNTER = "flow:counter:";
    public final static String FLOW_LIMIT_BLOCK = "flow:block:";

    //用户角色
    public final static String ROLE_ADMIN = "admin";
    public final static String ROLE_NORMAL = "user";
}
