import models.User;
import play.test.*;
import org.junit.*;

public class BootstrapTest extends UnitTest {

    @Test
    public void test() {
        Fixtures.deleteAllModels();
        new Bootstrap().doJob();
        final long count = User.count();
        assertTrue(count > 0);
        new Bootstrap().doJob();
        assertTrue(count == User.count());
    }
}