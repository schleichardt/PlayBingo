package validation;

import play.data.validation.Check;

import java.util.regex.Pattern;

/**
 * A validator for complex passwords.
 */
public class ComplexPasswordCheck extends Check {

    /**
     * Tests if String Password contains at least: one upper case character, one lower case character, one number and one punctuation character.
     * Does not consider the length of password.
     *
     * @param notUsed        unused parameter
     * @param passwordObject password with us-ascii characters
     * @return true if password is complex enough, otherwise fallse
     */
    @Override
    public boolean isSatisfied(Object notUsed, Object passwordObject) {
        boolean isSatisfied = false;
        if (passwordObject != null) {
            final String password = passwordObject.toString();
            isSatisfied = checkRegex(password, ".*\\p{javaLowerCase}.*", "password.check.missingLowerCase")
                    && checkRegex(password, ".*\\p{javaUpperCase}.*", "password.check.missingUpperCase") &&
                    checkRegex(password, ".*\\p{Digit}.*", "password.check.missingNumber") &&
                    checkRegex(password, ".*\\p{Punct}.*", "password.check.missingPunctuationHtml");
        }


        return isSatisfied;
    }

    private boolean checkRegex(String password, String regex, String messageKey) {
        boolean isSatisfied = Pattern.matches(regex, password);
        if (!isSatisfied) {
            setMessage(messageKey);
        }
        return isSatisfied;
    }
}
