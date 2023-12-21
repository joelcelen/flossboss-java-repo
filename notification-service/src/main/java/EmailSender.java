import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailSender {

    private final Session session;

    // Default constructor using the actual configuration
    public EmailSender() {
        this.session = createSession();
    }

    // Constructor that accepts a Session object (for testing purposes)
    public EmailSender(Session session) {
        this.session = session;
    }

    private Session createSession() {
        // Setup properties for the mail session
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        String user = ConfigHandler.getVariable("GMAIL_USER");
        String password = ConfigHandler.getVariable("GMAIL_PW");

        // Explicitly create the mail session
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });
    }


    public boolean sendMessage(String to, String from, String subject, String body) {
        boolean flag = false;

        try {
            Message message = new MimeMessage(session);
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setFrom(new InternetAddress(from));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }
}
