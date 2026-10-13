package com.example.campushub.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ApiClient {

    private static final String BASE_URL =
            "http://10.79.76.92:8000/";


    // =====================================================
    // NORMAL POST
    // =====================================================

    public static String post(
            String endpoint,
            String jsonBody
    ) throws Exception {

        URL url =
                new URL(BASE_URL + endpoint);

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");

        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        connection.setDoOutput(true);

        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        try (OutputStream outputStream =
                     connection.getOutputStream()) {

            outputStream.write(
                    jsonBody.getBytes(
                            StandardCharsets.UTF_8
                    )
            );
        }

        return readResponse(connection);
    }


    // =====================================================
    // AUTHENTICATED POST
    // =====================================================

    public static String postAuthenticated(
            String endpoint,
            String jsonBody,
            String accessToken
    ) throws Exception {

        URL url =
                new URL(BASE_URL + endpoint);

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");

        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        connection.setRequestProperty(
                "Authorization",
                "Bearer " + accessToken
        );

        connection.setDoOutput(true);

        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        try (OutputStream outputStream =
                     connection.getOutputStream()) {

            outputStream.write(
                    jsonBody.getBytes(
                            StandardCharsets.UTF_8
                    )
            );
        }

        return readResponse(connection);
    }


    // =====================================================
    // AUTHENTICATED GET
    // =====================================================

    public static String get(
            String endpoint,
            String accessToken
    ) throws Exception {

        URL url =
                new URL(BASE_URL + endpoint);

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        connection.setRequestProperty(
                "Authorization",
                "Bearer " + accessToken
        );

        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        return readResponse(connection);
    }


    // =====================================================
    // MULTIPART POST
    //
    // Sends:
    // - department
    // - class_teacher
    // - from_date
    // - to_date
    // - reason
    // - optional attachment
    // =====================================================

    public static String postMultipart(
            String endpoint,
            String accessToken,
            Map<String, String> fields,
            String fileFieldName,
            String fileName,
            String mimeType,
            InputStream fileInputStream
    ) throws Exception {

        String boundary =
                "----CampusHubBoundary"
                        + System.currentTimeMillis();

        URL url =
                new URL(BASE_URL + endpoint);

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");

        connection.setRequestProperty(
                "Authorization",
                "Bearer " + accessToken
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        connection.setRequestProperty(
                "Content-Type",
                "multipart/form-data; boundary="
                        + boundary
        );

        connection.setDoOutput(true);
        connection.setUseCaches(false);

        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);

        try (OutputStream outputStream =
                     connection.getOutputStream()) {

            // =============================================
            // NORMAL FORM FIELDS
            // =============================================

            if (fields != null) {

                for (Map.Entry<String, String> entry :
                        fields.entrySet()) {

                    String fieldName =
                            entry.getKey();

                    String fieldValue =
                            entry.getValue();

                    String fieldPart =
                            "--" + boundary + "\r\n"
                                    + "Content-Disposition: form-data; "
                                    + "name=\""
                                    + fieldName
                                    + "\"\r\n\r\n"
                                    + fieldValue
                                    + "\r\n";

                    outputStream.write(
                            fieldPart.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );
                }
            }

            // =============================================
            // FILE
            // =============================================

            if (fileInputStream != null &&
                    fileFieldName != null &&
                    fileName != null) {

                if (mimeType == null ||
                        mimeType.isEmpty()) {

                    mimeType =
                            "application/octet-stream";
                }

                String fileHeader =
                        "--" + boundary + "\r\n"
                                + "Content-Disposition: form-data; "
                                + "name=\""
                                + fileFieldName
                                + "\"; filename=\""
                                + fileName
                                + "\"\r\n"
                                + "Content-Type: "
                                + mimeType
                                + "\r\n\r\n";

                outputStream.write(
                        fileHeader.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

                byte[] buffer =
                        new byte[8192];

                int bytesRead;

                while ((bytesRead =
                        fileInputStream.read(buffer)) != -1) {

                    outputStream.write(
                            buffer,
                            0,
                            bytesRead
                    );
                }

                outputStream.write(
                        "\r\n".getBytes(
                                StandardCharsets.UTF_8
                        )
                );
            }

            // =============================================
            // END MULTIPART
            // =============================================

            String ending =
                    "--" + boundary + "--\r\n";

            outputStream.write(
                    ending.getBytes(
                            StandardCharsets.UTF_8
                    )
            );

            outputStream.flush();

        } finally {

            if (fileInputStream != null) {

                try {
                    fileInputStream.close();
                } catch (Exception ignored) {
                }
            }
        }

        return readResponse(connection);
    }


    // =====================================================
    // OLD FILE UPLOAD METHOD
    // =====================================================

    public static String uploadFile(
            String endpoint,
            String accessToken,
            String fileFieldName,
            String fileName,
            String mimeType,
            InputStream fileInputStream
    ) throws Exception {

        return postMultipart(
                endpoint,
                accessToken,
                null,
                fileFieldName,
                fileName,
                mimeType,
                fileInputStream
        );
    }


    // =====================================================
    // READ RESPONSE
    // =====================================================

    private static String readResponse(
            HttpURLConnection connection
    ) throws Exception {

        int responseCode =
                connection.getResponseCode();

        InputStream inputStream;

        if (responseCode >= 200 &&
                responseCode < 400) {

            inputStream =
                    connection.getInputStream();

        } else {

            inputStream =
                    connection.getErrorStream();
        }

        if (inputStream == null) {

            connection.disconnect();

            return "";
        }

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                inputStream,
                                StandardCharsets.UTF_8
                        )
                );

        StringBuilder response =
                new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {

            response.append(line);
        }

        reader.close();

        connection.disconnect();

        return response.toString();
    }
}