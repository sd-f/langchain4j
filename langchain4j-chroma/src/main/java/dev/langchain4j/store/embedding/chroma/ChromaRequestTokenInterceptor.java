package dev.langchain4j.store.embedding.chroma;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

class ChromaRequestTokenInterceptor implements Interceptor {

  private final String token;

  public ChromaRequestTokenInterceptor(String token) {
    this.token = token;
  }

  @NotNull @Override
  public Response intercept(Chain chain) throws IOException {
    Request newRequest = chain.request().newBuilder()
      .addHeader("Authorization", "Bearer " + token)
      .build();
    return chain.proceed(newRequest);
  }
}
