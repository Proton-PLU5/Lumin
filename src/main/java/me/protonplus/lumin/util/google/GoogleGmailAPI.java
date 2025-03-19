package me.protonplus.lumin.util.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import static me.protonplus.lumin.Lumin.APPLICATION_NAME;
import static me.protonplus.lumin.util.google.GoogleCommon.JSON_FACTORY;
import static me.protonplus.lumin.util.google.GoogleCommon.getCredentials;

public class GoogleGmailAPI {

    private final Gmail SERVICE;
    private OffsetDateTime previousCheckTime;

    public GoogleGmailAPI() throws GeneralSecurityException, IOException {
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        SERVICE = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        this.previousCheckTime = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public String getMessageOfEmail(String emailID) throws IOException {
        Message messageResponse = SERVICE.users().messages().get("me", emailID).execute();
        return messageResponse.getSnippet();
    }

    public int checkForNewEmails() throws GeneralSecurityException, IOException {
        String[] labels = {"INBOX", "UNREAD"};
        ListMessagesResponse messagesResponse = this.fetchMessageIds(labels, 10L);

        // Store the number of new emails we found.
        int count = 0;

        // Set the newDateTime to the previousCheckTime in case no new emails were received.
        OffsetDateTime newDateTime = previousCheckTime;

        // Loop through all the message IDs and fetch the full message details
        if (messagesResponse.getMessages() != null) {
            for (Message messageId : messagesResponse.getMessages()) {
                Message fullMessage = this.fetchFullMessage(messageId.getId());

                if (fullMessage.getPayload() == null) {
                    continue;
                }
                Optional<MessagePartHeader> dateHeader = fullMessage.getPayload().getHeaders().stream().filter(e -> e.getName().equalsIgnoreCase("Date")).findAny();
                if (dateHeader.isEmpty()) {
                    continue;
                }

                // Get the date of the email.
                String date = dateHeader.get().get("value").toString();

                // Format it so we can compare the times.
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .appendPattern("EEE, dd MMM yyyy HH:mm:ss ") // Added yyyy to the pattern
                        .appendOffset("+HHMM", "+0000") // Handle the UTC offset
                        .appendLiteral(" (UTC)") // Handle the "(UTC)" part
                        .toFormatter(Locale.ENGLISH);

                OffsetDateTime parsedDateTime;
                try {
                    parsedDateTime = OffsetDateTime.parse(date, formatter);
                } catch (Exception e) {
                    System.err.println("Error parsing date: " + date + " - " + e.getMessage());
                    continue;
                }

                // Check if the parsedDateTime is after the previousCheckTime
                if (parsedDateTime.isAfter(this.previousCheckTime)) {
                    count++;

                    // Update the newDateTime to get the latest email's timestamp.
                    if (parsedDateTime.isAfter(newDateTime)) {
                        newDateTime = parsedDateTime;
                    }
                }
                // Increment if the email is unread.
                else if (fullMessage.getLabelIds().contains("UNREAD")) {
                    count++;
                }
            }
        }
        // Update the previousCheckTime
        this.previousCheckTime = newDateTime;
        return count;
    }

    // Method to fetch only the message IDs
    public ListMessagesResponse fetchMessageIds(String[] label, long count) throws IOException {
        return SERVICE.users().messages().list("me")
                .setMaxResults(count)
                .setLabelIds(Arrays.asList(label))
                .execute();
    }

    // Method to fetch the full details of a message by its ID
    public Message fetchFullMessage(String messageId) throws IOException {
        return SERVICE.users().messages().get("me", messageId).setFormat("full").execute();
    }
}
