package com.glisterbyte.singingmonsters.networking;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.glisterbyte.singingmonsters.networking.exceptions.ClientHttpRequestException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class NetUtil {

    public static String jsonToHttpQueryStr(ObjectNode json, String[] order) {
        List<String> parts = new ArrayList<>();
        if (order != null) {
            for (String key : order) {
                parts.add(key + "=" + json.get(key).asText());
            }
        }
        else {
            json.fields().forEachRemaining(
                    field -> parts.add(
                            field.getKey() + "=" + field.getValue().asText()
                    )
            );
        }
        return String.join("&", parts);
    }

    private static String httpRequestBlocking(
            ClassicHttpRequest request,
            ObjectNode headers,
            String[] order
    ) throws ClientHttpRequestException {

        headers.fields().forEachRemaining(
                entry -> request.addHeader(entry.getKey(), entry.getValue().asText())
        );

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        CloseableHttpClient httpClient;
        if (order == null) httpClient = clientBuilder.build();
        else httpClient = clientBuilder.addRequestInterceptorLast(
                (request1, x1, x2) -> {
                    ArrayList<Header> headers1 = new ArrayList<>(Arrays.asList(request1.getHeaders()));
                    for (Header header : headers1) request1.removeHeader(header);

                    for (String name : order) {
                        int length = headers1.size();
                        for (int i = 0; i < length; i++)
                            if (headers1.get(i).getName().equals(name)) {
                                request1.addHeader(headers1.remove(i));
                                break;
                            }
                    }
                }
        ).build();

        try {

            ClassicHttpResponse response = httpClient.executeOpen(
                    new HttpHost("https", request.getAuthority().getHostName(), 443),
                    request, null
            );

            final HttpEntity entity = response.getEntity();
            String output = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            httpClient.close();

            return output;

        } catch (IOException | ParseException ex) {
            throw new ClientHttpRequestException(ex);
        }

    }

    public static String httpPostBlocking(
            String target,
            String hostname,
            String postData,
            ObjectNode headers,
            String[] order
    ) throws ClientHttpRequestException {
        ClassicHttpRequest httpPost = ClassicRequestBuilder
                .post(target).setHttpHost(new HttpHost("https", hostname, 443)).build();
        httpPost.setPath(target);
        httpPost.setEntity(new StringEntity(postData));
        return httpRequestBlocking(httpPost, headers, order);
    }

    public static String httpGetBlocking(
            String uri,
            String hostname,
            ObjectNode headers,
            String[] order
    ) throws ClientHttpRequestException {
        HttpGet httpGet = new HttpGet(uri);
        return httpRequestBlocking(httpGet, headers, order);
    }

}