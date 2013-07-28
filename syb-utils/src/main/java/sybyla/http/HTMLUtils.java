package sybyla.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.utils.CharsetUtils;

public class HTMLUtils {

    // Use the widest, most common charset as our default.
    public static final String DEFAULT_CHARSET = "windows-1252";

    private static final int META_TAG_BUFFER_SIZE = 8192;
    private static final Pattern HTTP_EQUIV_PATTERN = Pattern.compile(
                    "(?is)<meta\\s+http-equiv\\s*=\\s*['\\\"]\\s*" +
                    "Content-Type['\\\"]\\s+content\\s*=\\s*['\\\"]" +
                    "([^'\\\"]+)['\\\"]");


    public static String getEncoding(InputStream stream, Metadata metadata) throws IOException {
        String incomingType = metadata.get(Metadata.CONTENT_TYPE);
        if (incomingType != null) {
            MediaType mt = MediaType.parse(incomingType);
            if (mt != null) {
                String charset = CharsetUtils.clean(mt.getParameters().get("charset"));
                if ((charset != null) && CharsetUtils.isSupported(charset)) {
                    return charset;
                }
            }
        }
        
        // Nothing valid in response header for content type, let's check out the HTML meta tags.
        stream.mark(META_TAG_BUFFER_SIZE);
        char[] buffer = new char[META_TAG_BUFFER_SIZE];
        InputStreamReader isr = new InputStreamReader(stream, "us-ascii");
        int bufferSize = isr.read(buffer);
        stream.reset();

        if (bufferSize != -1) {
            String metaString = new String(buffer, 0, bufferSize);
            Matcher m = HTTP_EQUIV_PATTERN.matcher(metaString);
            if (m.find()) {
                // We have one or more x or x=y attributes, separated by ';'
                String[] attrs = m.group(1).split(";");
                for (String attr : attrs) {
                    String[] keyValue = attr.trim().split("=");
                    if ((keyValue.length == 2) && keyValue[0].equalsIgnoreCase("charset")) {
                        String charset = CharsetUtils.clean(keyValue[1]);
                        if (CharsetUtils.isSupported(charset)) {
                            return charset;
                        }
                    }
                }
            }
        }
        return null;
    }
}
