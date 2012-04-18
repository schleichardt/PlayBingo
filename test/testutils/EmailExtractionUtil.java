package testutils;

import org.apache.commons.lang.StringUtils;
import play.libs.Mail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailExtractionUtil {

    /**
     * Returns the link for confirmation of the email address.
     * Format: /abc/def?param=value
     * @param emailOfRecipient  the email of the user for email confirmation
     * @return  confirmation link
     */
    public static String extractLink(String emailOfRecipient) {
        final String message = Mail.Mock.getLastMessageReceivedBy(emailOfRecipient);
        Pattern pattern = Pattern.compile("URL: (\\S+)");
        Matcher matcher = pattern.matcher(message);
        String emailConfirmationLink = null;
        if (matcher.find()) {
            final String urlWithHost = matcher.group(1);
            emailConfirmationLink = StringUtils.substringAfter(urlWithHost, "localhost");
        }
        return emailConfirmationLink;
    }
}
