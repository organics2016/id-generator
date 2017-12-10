package net.vkits.platform.lina.redis;

/**
 * Created by wanghc on 2016/7/19.
 * <p>
 * 事件处理的抽象接口
 */


public interface Handler<E> {
    void handler(E event);
}
