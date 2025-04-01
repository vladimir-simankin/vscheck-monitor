package ru.vscheck.monitor.config;

import java.net.http.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

  @Bean
  public HttpClient httpClient() {
    return HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
  }
}
