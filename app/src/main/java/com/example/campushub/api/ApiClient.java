package com.example.campushub.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ApiClient {

    // Replace YOUR-LAPTOP-IPV4 with the IPv4 from ipconfig (keep http:// and :8000/)
    private static String baseUrl = "http://127.0.0.1:8000/";

    private static final int DEFAULT_TIMEOUT_MS = 10000;
    private static final int MULTIPART_TIMEOUT_MS = 30000;

    private ApiClient() {}

    public static void setBaseUrl(String ipOrUrl) {
        if (ipOrUrl == null || ipOrUrl.trim().isEmpty()) return;
        String formatted = ipOrUrl.trim();
        if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
            formatted = "http://" + formatted;
        }
        if (!formatted.endsWith("/")) {
            formatted += "/";
        }
        baseUrl = formatted;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    private static HttpURLConnection createConnection(
            String endpoint,
            String method,
            String accessToken,
            boolean hasBody
    ) throws IOException {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(DEFAULT_TIMEOUT_MS);
        connection.setReadTimeout(DEFAULT_TIMEOUT_MS);
        connection.setRequestProperty("Accept", "application/json");

        if (accessToken != null && !accessToken.isEmpty()) {
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        }

        if (hasBody) {
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
        }

        return connection;
    }

    private static String executeJsonRequest(
            String endpoint,
            String method,
            String jsonBody,
            String accessToken
    ) throws Exception {
        HttpURLConnection connection = createConnection(endpoint, method, accessToken, jsonBody != null);

        if (jsonBody != null) {
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }
        }

        return readResponse(connection);
    }

    public static String post(String endpoint, String jsonBody) throws Exception {
        return executeJsonRequest(endpoint, "POST", jsonBody, null);
    }

    public static String postAuthenticated(String endpoint, String jsonBody, String accessToken) throws Exception {
        return executeJsonRequest(endpoint, "POST", jsonBody, accessToken);
    }

    public static String get(String endpoint, String accessToken) throws Exception {
        return executeJsonRequest(endpoint, "GET", null, accessToken);
    }

    public static String patchAuthenticated(String endpoint, String jsonBody, String accessToken) throws Exception {
        return executeJsonRequest(endpoint, "PATCH", jsonBody, accessToken);
    }

    public static File downloadAuthenticated(String endpoint, String accessToken, File destination) throws Exception {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        if (accessToken != null && !accessToken.isEmpty()) {
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        }
        connection.setRequestProperty("Accept", "application/json, application/pdf;q=0.9, */*;q=0.8");
        connection.setConnectTimeout(DEFAULT_TIMEOUT_MS);
        connection.setReadTimeout(MULTIPART_TIMEOUT_MS);

        try {
            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                String message = "Download failed (HTTP " + responseCode + ")";
                InputStream error = connection.getErrorStream();
                if (error != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(error, StandardCharsets.UTF_8))) {
                        StringBuilder body = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            body.append(line);
                        }
                        if (body.length() > 0) {
                            message = body.toString();
                        }
                    }
                }
                throw new IOException(message);
            }

            try (InputStream input = connection.getInputStream();
                 FileOutputStream output = new FileOutputStream(destination)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
            }
            return destination;
        } finally {
            connection.disconnect();
        }
    }

    public static String postMultipart(
            String endpoint,
            String accessToken,
            Map<String, String> fields,
            String fileFieldName,
            String fileName,
            String mimeType,
            InputStream fileInputStream
    ) throws Exception {
        return postMultipartFiles(endpoint, accessToken, fields,
                new String[]{fileFieldName}, new String[]{fileName},
                new String[]{mimeType}, new InputStream[]{fileInputStream});
    }

    public static String postMultipartFiles(
            String endpoint,
            String accessToken,
            Map<String, String> fields,
            String[] fileFieldNames,
            String[] fileNames,
            String[] mimeTypes,
            InputStream[] fileInputStreams
    ) throws Exception {
        String boundary = "----CampusHubBoundary" + System.currentTimeMillis();
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        if (accessToken != null && !accessToken.isEmpty()) {
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        }
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setConnectTimeout(DEFAULT_TIMEOUT_MS);
        connection.setReadTimeout(MULTIPART_TIMEOUT_MS);

        try (OutputStream outputStream = connection.getOutputStream()) {
            if (fields != null) {
                for (Map.Entry<String, String> entry : fields.entrySet()) {
                    String fieldPart = "--" + boundary + "\r\n"
                            + "Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n"
                            + entry.getValue() + "\r\n";
                    outputStream.write(fieldPart.getBytes(StandardCharsets.UTF_8));
                }
            }

            if (fileInputStreams != null) {
                for (int i = 0; i < fileInputStreams.length; i++) {
                    InputStream fileInputStream = fileInputStreams[i];
                    String fileFieldName = fileFieldNames != null && i < fileFieldNames.length ? fileFieldNames[i] : null;
                    String fileName = fileNames != null && i < fileNames.length ? fileNames[i] : null;
                    String mimeType = mimeTypes != null && i < mimeTypes.length ? mimeTypes[i] : null;

                    if (fileInputStream == null || fileFieldName == null || fileName == null) continue;
                    if (mimeType == null || mimeType.isEmpty()) mimeType = "application/octet-stream";

                    String fileHeader = "--" + boundary + "\r\n"
                            + "Content-Disposition: form-data; name=\"" + fileFieldName
                            + "\"; filename=\"" + fileName + "\"\r\n"
                            + "Content-Type: " + mimeType + "\r\n\r\n";
                    outputStream.write(fileHeader.getBytes(StandardCharsets.UTF_8));
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
                }
            }

            String ending = "--" + boundary + "--\r\n";
            outputStream.write(ending.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } finally {
            if (fileInputStreams != null) {
                for (InputStream inputStream : fileInputStreams) {
                    if (inputStream != null) {
                        try { inputStream.close(); } catch (Exception ignored) {}
                    }
                }
            }
        }

        return readResponse(connection);
    }

    public static String uploadFile(
            String endpoint,
            String accessToken,
            String fileFieldName,
            String fileName,
            String mimeType,
            InputStream fileInputStream
    ) throws Exception {
        return postMultipart(endpoint, accessToken, null, fileFieldName, fileName, mimeType, fileInputStream);
    }

    private static String readResponse(HttpURLConnection connection) throws Exception {
        try {
            int responseCode = connection.getResponseCode();
            InputStream inputStream = (responseCode >= 200 && responseCode < 400)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            if (inputStream == null) {
                return "";
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } finally {
            connection.disconnect();
        }
    }
}