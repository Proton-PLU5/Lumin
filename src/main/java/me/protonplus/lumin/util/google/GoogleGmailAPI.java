package me.protonplus.lumin.util.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.apache.commons.codec.binary.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.protonplus.lumin.Lumin.APPLICATION_NAME;
import static me.protonplus.lumin.util.google.GoogleCommon.JSON_FACTORY;
import static me.protonplus.lumin.util.google.GoogleCommon.getCredentials;

public class GoogleGmailAPI {

    private final Gmail SERVICE;
    private OffsetDateTime previousCheckTime;

    private static final String USER = "me";
    private static final Pattern TIMEZONE_CLEANUP_PATTERN = Pattern.compile(" \\([^)]*\\)$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME;

    public GoogleGmailAPI() throws GeneralSecurityException, IOException {
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        SERVICE = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        this.previousCheckTime = OffsetDateTime.now(ZoneOffset.UTC).minusDays(2);
        System.out.println("GoogleGmailAPI initialized. Initial previousCheckTime set to: " + this.previousCheckTime);
    }

    public OffsetDateTime getPreviousCheckTime() {
        return previousCheckTime;
    }

    public void setPreviousCheckTime(OffsetDateTime previousCheckTime) {
        if (previousCheckTime != null) {
            System.out.println("Manually setting previousCheckTime to: " + previousCheckTime);
            this.previousCheckTime = previousCheckTime;
        }
    }

    public int getNewEmailCountSinceLastCheck() throws IOException {
        OffsetDateTime sinceTime = this.previousCheckTime; // Use the instance's current checkpoint
        String query = "in:inbox";

        if (sinceTime != null) {
            long epochSeconds = sinceTime.toEpochSecond();
            query += " after:" + epochSeconds;
            System.out.println("Executing query for new emails: " + query);
        }

        ListMessagesResponse response = SERVICE.users().messages().list(USER)
                .setQ(query)
                .setMaxResults(100L)
                .execute();

        int newEmailCount = 0;

        OffsetDateTime latestTimestampFound = sinceTime;

        List<Message> messages = response.getMessages();
        if (messages != null && !messages.isEmpty()) {
            System.out.println("Found " + messages.size() + " potential new messages to check.");
            for (Message messageId : messages) {
                Message fullMessage = fetchFullMessage(messageId.getId());
                if (fullMessage == null) continue;

                Optional<OffsetDateTime> parsedDateTimeOpt = parseEmailDate(fullMessage);

                if (parsedDateTimeOpt.isPresent()) {
                    OffsetDateTime parsedDateTime = parsedDateTimeOpt.get();

                    if (parsedDateTime.isAfter(sinceTime)) {
                        newEmailCount++;
                        System.out.println(getMessageContent(messageId.getId()));
                        if (parsedDateTime.isAfter(latestTimestampFound)) {
                            latestTimestampFound = parsedDateTime;
                        }
                    }
                }
            }
        } else {
            System.out.println("No new message IDs returned by the query.");
        }

        if (latestTimestampFound != null && latestTimestampFound.isAfter(sinceTime)) {
            System.out.println("Updating internal previousCheckTime from " + this.previousCheckTime + " to " + latestTimestampFound);
            this.setPreviousCheckTime(latestTimestampFound);
        } else {
            System.out.println("No newer timestamp found than " + sinceTime + ". Internal previousCheckTime remains unchanged.");
        }

        return newEmailCount;
    }

    public String getMessageContent(String emailID) throws IOException {
        Message messageResponse = SERVICE.users().messages().get(USER, emailID).execute();
        StringBuilder content = new StringBuilder();

        // Check if the payload has parts (multipart email)
        if (messageResponse.getPayload().getParts() != null) {
            for (MessagePart part : messageResponse.getPayload().getParts()) {
                if (part.getMimeType() != null && ("text/plain".equals(part.getMimeType()) || "text/plain".equalsIgnoreCase(part.getMimeType()))) {
                    byte[] dataBytes = Base64.getUrlDecoder().decode(part.getBody().getData());
                    content.append(new String(dataBytes, StandardCharsets.UTF_8));
                }
            }
        } else {
            // Handle single-part email (plain text directly in the body)
            if (messageResponse.getPayload().getMimeType() != null && ("text/plain".equals(messageResponse.getPayload().getMimeType()) || "text/plain".equalsIgnoreCase(messageResponse.getPayload().getMimeType()))) {
                byte[] dataBytes = Base64.getUrlDecoder().decode(messageResponse.getPayload().getBody().getData());
                content.append(new String(dataBytes, StandardCharsets.UTF_8));
            }
        }

        return content.toString();
    }

    public void scanInboxForScamEmails() {

    }

    private Message fetchFullMessage(String messageId) throws IOException {
        try {
            return SERVICE.users().messages().get(USER, messageId)
                    .setFields("id,labelIds,payload/headers")
                    .execute();
        } catch (IOException e) {
            System.err.println("Error fetching message details for ID: " + messageId + " - " + e.getMessage());
            return null;
        }
    }

    private Optional<OffsetDateTime> parseEmailDate(Message fullMessage) {
        // (Implementation from previous response - handles header extraction, cleaning, parsing)
        if (fullMessage == null || fullMessage.getPayload() == null || fullMessage.getPayload().getHeaders() == null) {
            System.err.println("Cannot parse date: Message structure incomplete for ID: " + (fullMessage != null ? fullMessage.getId() : "null"));
            return Optional.empty();
        }
        Optional<MessagePartHeader> dateHeader = fullMessage.getPayload().getHeaders().stream()
                .filter(h -> h != null && h.getName() != null && h.getName().equalsIgnoreCase("Date"))
                .findFirst();
        if (dateHeader.isEmpty() || dateHeader.get().getValue() == null) {
            System.err.println("Missing or null Date header value for message ID: " + fullMessage.getId());
            return Optional.empty();
        }
        String rawDate = dateHeader.get().getValue();
        Matcher matcher = TIMEZONE_CLEANUP_PATTERN.matcher(rawDate);
        String cleanedDate = matcher.replaceAll("");
        try {
            OffsetDateTime parsedDateTime = OffsetDateTime.parse(cleanedDate, DATE_FORMATTER);
            return Optional.of(parsedDateTime);
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing date for message ID " + fullMessage.getId() + ". Raw: '" + rawDate + "', Cleaned: '" + cleanedDate + "' - " + e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Unexpected error parsing date for message ID " + fullMessage.getId() + ": '" + rawDate + "' - " + e.getMessage());
            return Optional.empty();
        }
    }
}