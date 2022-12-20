package cp2022.solution;

import cp2022.base.Workplace;

import java.util.concurrent.CountDownLatch;

public class WorkplaceDecorator extends Workplace {
    private final Workplace wp;
    private CountDownLatch latch = new CountDownLatch(1);

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    protected WorkplaceDecorator(Workplace wp) {
        super(wp.getId());
        this.wp = wp;
    }

    @Override
    public void use() {
        try {
            latch.countDown();
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("panic: unexpected thread interruption");
        }
        wp.use();
    }
}
