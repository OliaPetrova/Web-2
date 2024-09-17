package org.example;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> queryParams;

    public Request(String method, String path, Map<String, String> queryParams) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public static Map<String, String> parseqQeryParams(String query) {
        List<NameValuePair> pairs = URLEncodedUtils.parse(query, Charset.forName("UTF-8"));
        Map<String, String> queryParams = new HashMap<>();
        for (NameValuePair pair : pairs) {
            queryParams.put(pair.getName(), pair.getValue());
        }
        return queryParams;
    }
}