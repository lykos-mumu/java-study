package lykos.study.com;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author yanfenglin
 * @version 2018/2/8 15:25
 */
public class ClhSpinLock {
    private final ThreadLocal<Node> prev;
    private final ThreadLocal<Node> node;
    /**
     * 全局尾节点，用以构造节点队列
     */
    private final AtomicReference<Node> tail = new AtomicReference<Node>(new Node());

    public ClhSpinLock() {
        this.node = new ThreadLocal<Node>() {
            protected Node initialValue() {
                return new Node();
            }
        };

        this.prev = new ThreadLocal<Node>() {
            protected Node initialValue() {
                return null;
            }
        };
    }

    public void lock() {
        final Node node = this.node.get();
        node.locked = true;
        // 一个CAS操作即可将当前线程对应的节点加入到队列中，
        // 并且同时获得了前继节点的引用，然后就是等待前继释放锁
        Node pred = this.tail.getAndSet(node);
        this.prev.set(pred);
        int i = 0;
        while (pred.locked) {// 进入自旋
            if (i++ % 1000000 == 0)
                System.out.println("thread:"+Thread.currentThread().getId()+" circling...");
        }
    }

    public void unlock() {
        final Node node = this.node.get();
        node.locked = false;
        this.node.set(this.prev.get());
        System.out.println("thread:"+Thread.currentThread().getId()+" release lock");
    }

    private static class Node {
        private volatile boolean locked;
    }
}
