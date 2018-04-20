package com.checkmarx.clients.rest.login;

import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.DefaultCookieSpec;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;

public class CxRestClient {
	
    protected CookieStore cookieStore = new BasicCookieStore();
    
	protected HttpClient createClient(
			HttpResponseInterceptor requestFilter,
			HttpResponseInterceptor responseFilter,
			List<Header> headers)  {

        // add custom cookie spec to allow cookies from untrusted servers (due to untrusted cert)
        final CookieSpecProvider csf = new CookieSpecProvider() {
            @Override
            public CookieSpec create(HttpContext context) {
                return new DefaultCookieSpec() {
                    @Override
                    public void validate(final Cookie cookie, final CookieOrigin origin)
                            throws MalformedCookieException {
                        // allow all cookies
                    }
                };
            }
        };

        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec("easy").build();

        try {
            final TrustStrategy trustStrategy = TrustAllStrategy.INSTANCE;
            final SSLContextBuilder sslContextBuilder = 
                    SSLContextBuilder.create().loadTrustMaterial(trustStrategy);
            final HttpClientBuilder clientBuilder = HttpClients.custom()
                    .setSSLContext(sslContextBuilder.build())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .setDefaultRequestConfig(requestConfig).useSystemProperties()
                    .setDefaultCookieStore(cookieStore)
                    .setDefaultCookieSpecRegistry(RegistryBuilder.<CookieSpecProvider>create()
                            .register(CookieSpecs.DEFAULT, csf)
                            .register("easy", csf).build());
            
            if (headers != null) {
            	clientBuilder.setDefaultHeaders(headers);
            }

            if (requestFilter != null) {
                clientBuilder.addInterceptorFirst(requestFilter);
            }
            if (requestFilter != null) {
                clientBuilder.addInterceptorLast(responseFilter);
            }

            return clientBuilder.build(); 

        } catch (Throwable t) {
            final String msg = "Unable to create HttpClient";
            throw new RuntimeException(msg, t);
        }

    }

}
