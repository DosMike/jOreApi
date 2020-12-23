package de.dosmike.spongepowered.oreapi.utility;

import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class MultiPartFormData {

    //region entry definition
    interface Entry {
        void write(String valueName, OutputStream output) throws IOException;
    }

    public static class StringableEntry implements Entry {
        private Object value;

        public StringableEntry(Object value) {
            this.value = value;
        }

        @Override
        public void write(String valueName, OutputStream output) throws IOException {
            OutputStreamWriter osw = new OutputStreamWriter(output);
            osw.write("Content-Disposition: form-data; name=\"");
            osw.write(valueName);
            osw.write("\"\r\n\r\n");
            osw.write(value.toString());
            osw.flush();
        }
    }

    public static class UploadEntry implements Entry {
        private Path source;

        public UploadEntry(Path source) {
            if (!(Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)) &&
                    Files.isReadable(source) && !Files.isSymbolicLink(source))
                throw new IllegalArgumentException("The specified path was not a valid file for upload");
            this.source = source;
        }

        @Override
        public void write(String valueName, OutputStream output) throws IOException {
            OutputStreamWriter osw = new OutputStreamWriter(output);
            osw.write("Content-Disposition: form-data; name=\"");
            osw.write(valueName);
            osw.write("\"; filename=\"");
            osw.write(source.getFileName().toString());
            osw.write("\"\r\nContent-Type: ");
            String contentType = Files.probeContentType(source);
            if (contentType == null) contentType = "application/octet-stream";
            osw.write(contentType);
            osw.write("\r\n\r\n");
            osw.flush();
            //don't close!
            Files.copy(source, output);
            output.flush();
        }
    }
    //endregion

    String boundary = "-----------------------------";
    static SecureRandom rng = new SecureRandom();

    Map<String, Entry> entries = new HashMap<>();

    public MultiPartFormData() {
        //create boundary
        for (int i = 0; i < 5; i++) { //pull six digits at a time
            int x = rng.nextInt(1_000_000);
            boundary += String.format("%03d", x);
        }
    }

    public MultiPartFormData addStringable(@NotNull String key, @NotNull Object value) {
        entries.put(key, new StringableEntry(value));
        return this;
    }

    public MultiPartFormData addUpload(@NotNull String key, @NotNull Path filePath) {
        entries.put(key, new UploadEntry(filePath));
        return this;
    }

    public void write(HttpsURLConnection connection) throws IOException {
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary.substring(2));
        OutputStream out = connection.getOutputStream();
        byte[] newline = new byte[]{'\r', '\n'};
        byte[] boundaryBytes = boundary.getBytes(StandardCharsets.UTF_8);
        for (Map.Entry<String, Entry> entry : entries.entrySet()) {
            out.write(boundaryBytes);
            out.write(newline);
            entry.getValue().write(entry.getKey(), out);
            out.write(newline);
        }
        out.write(boundaryBytes);
        out.write(boundaryBytes, 0, 2);
        out.write(newline);
        out.flush();
    }

}
