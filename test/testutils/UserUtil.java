package testutils;

import models.User;
import org.apache.commons.lang.RandomStringUtils;

public class UserUtil {
    public static User generateTestUser() {
        final User user = new User();
        user.setUsername(RandomStringUtils.randomAlphabetic(9));
        user.setEmail(user.getUsername() + "@localhost.tld");
        user.setPassword("Ab,-34zdfd");
        return user;
    }
}
