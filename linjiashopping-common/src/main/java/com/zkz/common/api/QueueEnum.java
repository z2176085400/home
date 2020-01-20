package com.zkz.common.api;

import lombok.Getter;

/**
 * 消息队列枚举配置
 */
@Getter
public enum QueueEnum {

    /**
     * 消息通知队列
     */
    QUEUE_LOGIN_CANCEL("email.login.direct", "email.login.cancel", "email.login.cancel"),
    /**
     * 消息通知ttl队列
     */
    QUEUE_TTL_lOGIN_CANCEL("email.login.direct.ttl", "email.login.cancel.ttl", "email.login.cancel.ttl");

    /**
     * 交换名称
     */
    private String exchange;
    /**
     * 队列名称
     */
    private String name;
    /**
     * 路由键
     */
    private String routeKey;

    QueueEnum(String exchange, String name, String routeKey) {
        this.exchange = exchange;
        this.name = name;
        this.routeKey = routeKey;
    }
}
