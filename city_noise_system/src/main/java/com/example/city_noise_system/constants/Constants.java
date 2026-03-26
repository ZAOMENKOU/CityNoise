package com.example.city_noise_system.constants;

/**
 * 系统常量类
 *
 * 这个文件存放项目中所有固定不变的值
 * 比如：状态码、角色、状态、配置等
 *
 * 使用常量的好处：
 * 1. 避免魔法字符串，提高代码可读性
 * 2. 一处修改，处处生效
 * 3. 减少拼写错误
 * 4. IDE支持自动提示
 */
public class Constants {

    // ==================== 用户角色 ====================
    // 系统中有三种用户角色

    /** 居民：普通用户，可以提交投诉 */
    public static final String ROLE_RESIDENT = "RESIDENT";

    /** 处理人员：负责处理投诉 */
    public static final String ROLE_WORKER = "WORKER";

    /** 管理员：管理整个系统 */
    public static final String ROLE_ADMIN = "ADMIN";

    // ==================== 投诉状态 ====================
    // 投诉从提交到关闭的完整流程

    /** 待处理：刚提交的投诉，等待分配 */
    public static final String COMPLAINT_STATUS_PENDING = "PENDING";

    /** 已分配：已分配给处理人员 */
    public static final String COMPLAINT_STATUS_ASSIGNED = "ASSIGNED";

    /** 处理中：处理人员正在处理 */
    public static final String COMPLAINT_STATUS_PROCESSING = "PROCESSING";

    /** 已解决：处理完成，等待评价 */
    public static final String COMPLAINT_STATUS_RESOLVED = "RESOLVED";

    /** 已关闭：评价完成，流程结束 */
    public static final String COMPLAINT_STATUS_CLOSED = "CLOSED";

    // ==================== 噪音类型 ====================
    // 用户可以选择的不同噪音类型

    /** 施工噪音：建筑工地产生的噪音 */
    public static final String NOISE_TYPE_CONSTRUCTION = "CONSTRUCTION";

    /** 交通噪音：道路、车辆产生的噪音 */
    public static final String NOISE_TYPE_TRAFFIC = "TRAFFIC";

    /** 生活噪音：居民日常生活产生的噪音 */
    public static final String NOISE_TYPE_LIVING = "LIVING";

    /** 商业噪音：商业活动产生的噪音 */
    public static final String NOISE_TYPE_COMMERCIAL = "COMMERCIAL";

    /** 工业噪音：工厂、企业产生的噪音 */
    public static final String NOISE_TYPE_INDUSTRIAL = "INDUSTRIAL";

    // ==================== 处理操作类型 ====================
    // 投诉处理过程中的各种操作

    /** 提交投诉 */
    public static final String ACTION_SUBMIT = "SUBMIT";

    /** 分配任务 */
    public static final String ACTION_ASSIGN = "ASSIGN";

    /** 开始处理 */
    public static final String ACTION_START_PROCESS = "START_PROCESS";

    /** 更新进展 */
    public static final String ACTION_UPDATE = "UPDATE";

    /** 解决投诉 */
    public static final String ACTION_RESOLVE = "RESOLVE";

    /** 关闭投诉 */
    public static final String ACTION_CLOSE = "CLOSE";

    // ==================== 通知类型 ====================
    // 系统通知的三种类型

    /** 系统通知：系统公告、维护通知 */
    public static final String NOTIFICATION_TYPE_SYSTEM = "SYSTEM";

    /** 任务通知：任务分配、状态更新 */
    public static final String NOTIFICATION_TYPE_TASK = "TASK";

    /** 投诉通知：投诉相关通知 */
    public static final String NOTIFICATION_TYPE_COMPLAINT = "COMPLAINT";

    // ==================== 监测点功能区类型 ====================
    // 环境噪声监测点的功能区分类

    /** 1类区：康复疗养区等特别需要安静的区域 */
    public static final String ZONE_CLASS_1 = "CLASS_1";

    /** 2类区：居住、商业、工业混杂区 */
    public static final String ZONE_CLASS_2 = "CLASS_2";

    /** 3类区：工业生产、仓储物流区 */
    public static final String ZONE_CLASS_3 = "CLASS_3";

    /** 4a类区：交通干线两侧区域 */
    public static final String ZONE_CLASS_4A = "CLASS_4A";

    /** 4b类区：铁路干线两侧区域 */
    public static final String ZONE_CLASS_4B = "CLASS_4B";

    // ==================== 时段类型 ====================
    // 噪声监测的时段分类

    /** 昼间：6:00-22:00 */
    public static final String PERIOD_DAY = "DAY";

    /** 夜间：22:00-次日6:00 */
    public static final String PERIOD_NIGHT = "NIGHT";

    // ==================== 分页设置 ====================
    // 列表分页的默认值

    /** 默认每页显示10条记录 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** 默认显示第1页 */
    public static final int DEFAULT_PAGE_NUM = 1;

    // ==================== 文件上传 ====================
    /** 最大文件大小：10MB */
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /** 文件上传目录 */
    public static final String UPLOAD_DIR = "uploads/";

    // ==================== 武汉市行政区划 ====================
    // 武汉市的所有市辖区，用于下拉选择
    public static final String[] WUHAN_DISTRICTS = {
            "江岸区", "江汉区", "硚口区", "汉阳区", "武昌区",
            "青山区", "洪山区", "东西湖区", "汉南区", "蔡甸区",
            "江夏区", "黄陂区", "新洲区", "东湖新技术开发区"
    };

    // ==================== HTTP状态码 ====================
    // 常用的HTTP响应状态码

    /** 200：请求成功 */
    public static final int HTTP_OK = 200;

    /** 400：请求参数错误 */
    public static final int HTTP_BAD_REQUEST = 400;

    /** 401：未认证 */
    public static final int HTTP_UNAUTHORIZED = 401;

    /** 403：无权限 */
    public static final int HTTP_FORBIDDEN = 403;

    /** 404：资源不存在 */
    public static final int HTTP_NOT_FOUND = 404;

    /** 500：服务器内部错误 */
    public static final int HTTP_SERVER_ERROR = 500;

    // ==================== 响应消息 ====================
    // 常用的响应消息

    /** 操作成功 */
    public static final String MSG_SUCCESS = "操作成功";

    /** 操作失败 */
    public static final String MSG_ERROR = "操作失败";

    /** 用户未登录或Token已过期 */
    public static final String MSG_UNAUTHORIZED = "用户未登录或Token已过期";

    /** 权限不足 */
    public static final String MSG_FORBIDDEN = "权限不足";

    /** 资源不存在 */
    public static final String MSG_NOT_FOUND = "资源不存在";

    /** 服务器内部错误，请稍后重试 */
    public static final String MSG_SERVER_ERROR = "服务器内部错误，请稍后重试";
}