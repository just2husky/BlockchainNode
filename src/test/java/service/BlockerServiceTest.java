package service;

import entity.NetAddress;
import org.junit.Test;

import static org.junit.Assert.*;

public class BlockerServiceTest {
    private BlockerService blockerService = BlockerService.getInstance();

    @Test
    public void isCurrentBlocker() {
        NetAddress na = new NetAddress("127.0.0.1", 9000);
        boolean result = blockerService.isCurrentBlocker(na, 2);
        System.out.println(result);
//        assertEquals(false, result);
    }
}