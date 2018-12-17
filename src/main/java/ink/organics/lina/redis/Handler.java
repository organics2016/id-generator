package ink.organics.lina.redis;

/**
 * Created by wanghc on 2016/7/19.
 * <p>
 * 事件处理的抽象接口
 */


public interface Handler<E, R> {
    R handler(E event);
}
