package validation;

import net.sf.oval.context.OValContext;
import play.data.validation.Validation;
import play.db.jpa.Model;
import play.test.*;
import org.junit.*;

import javax.persistence.Column;
import javax.persistence.Entity;

public class UniqueCheckTest extends UnitTest {
    @Test
    public void testUniqueness() {
        new TestModelWithUniqueField("foo").save();
        new TestModelWithUniqueField("bar").save();

        final Validation validation = Validation.current();
        Validation.clear();
        validation.valid(new TestModelWithUniqueField("free"));
        if (Validation.hasErrors()) {
            fail("tested String should not be in the database");
        }
        Validation.clear();
        validation.valid(new TestModelWithUniqueField("foo"));
        if (!Validation.hasErrors()) {
            fail("tested String should already be in the database");
        }
        Validation.clear();
        final TestModelWithUniqueField existingObject = TestModelWithUniqueField.find("byUniqueMember", "bar").<TestModelWithUniqueField>first();
        validation.required(existingObject);
        validation.valid(existingObject);
        if (Validation.hasErrors()) {
            fail("tested entity already exists and should be valid");
        }
    }

    @After
    public void clean() {
        Fixtures.deleteAllModels();
    }

    @Entity
    private static class TestModelWithUniqueField extends Model {
        @Unique
        @Column(unique = true)
        private String uniqueMember;

        public TestModelWithUniqueField() {
        }

        public TestModelWithUniqueField(String uniqueMember) {
            this.uniqueMember = uniqueMember;
        }

        public String getUniqueMember() {
            return uniqueMember;
        }

        public void setUniqueMember(String uniqueMember) {
            this.uniqueMember = uniqueMember;
        }
    }
}
