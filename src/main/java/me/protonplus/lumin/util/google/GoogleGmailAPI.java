package me.protonplus.lumin.util.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import static me.protonplus.lumin.Lumin.APPLICATION_NAME;
import static me.protonplus.lumin.util.google.GoogleCommon.JSON_FACTORY;
import static me.protonplus.lumin.util.google.GoogleCommon.getCredentials;

public class GoogleGmailAPI {

    private static NetHttpTransport HTTP_TRANSPORT;
    private final Gmail SERVICE;

    public GoogleGmailAPI() throws GeneralSecurityException, IOException {
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        SERVICE = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public void getLatestEmail() throws GeneralSecurityException, IOException {
        // Get the latest email:
        // - Set max results to one.
        // - Get emails from inbox.
        // - Returns a JSON response with message ids.
        ListMessagesResponse messagesResponse = SERVICE.users().messages().list("me").
                setMaxResults(1L)
                .setLabelIds(Arrays.asList("INBOX")).execute();
        String id = messagesResponse.getMessages().get(0).getId();

        Message messageResponse = SERVICE.users().messages().get("me", id).execute();
        System.out.println(messageResponse.getSnippet());
    }
}
