package validation;

import models.User;
import org.junit.Before;
import org.junit.Test;
import play.data.validation.CheckWithCheck;
import play.test.UnitTest;

public class ComplexPasswordCheckTest extends UnitTest {
    private ComplexPasswordCheck checker;

    @Before
    public void init() {
        checker = new ComplexPasswordCheck();
        checker.checkWithCheck = new CheckWithCheck() ;
    }

    @Test
    public void testMissingUpperCaseCharacter() {
        System.err.println("");
        assertFalse(checker.isSatisfied(new User(), "a.3"));
    }

    @Test
    public void testMissingLowerCaseCharacter() {
        assertFalse(checker.isSatisfied(null, "B.3"));
    }

    @Test
    public void testMissingNumberCharacter() {
        assertFalse(checker.isSatisfied(null, "B.b"));
    }

    @Test
    public void testMissingSpecialCharacter() {
        assertFalse(checker.isSatisfied(null, "B4b"));
    }

    @Test
    public void testWorkingPassword() {
        assertTrue(checker.isSatisfied(null, "B4b."));
    }


}