package dev.langchain4j.store.embedding.chroma;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.testcontainers.chromadb.ChromaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static dev.langchain4j.internal.Utils.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@Testcontainers
class AuthenticatedChromaEmbeddingStoreIT {

    final static String CHROMA_SERVER_AUTH_CREDENTIALS = "token";

    /* setup chroma with token auth */
    @Container
    private static final ChromaDBContainer chroma = new ChromaDBContainer("chromadb/chroma:0.5.4")
      .withEnv("CHROMA_SERVER_AUTHN_CREDENTIALS", CHROMA_SERVER_AUTH_CREDENTIALS)
      .withEnv("CHROMA_SERVER_AUTHN_PROVIDER", "chromadb.auth.token_authn.TokenAuthenticationServerProvider");

    EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore
        .builder()
        .baseUrl(chroma.getEndpoint())
        .collectionName(randomUUID())
        .logRequests(true)
        .logResponses(true)
        .token(CHROMA_SERVER_AUTH_CREDENTIALS)
        .build();

    EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();

    protected EmbeddingStore<TextSegment> embeddingStore() {
        return embeddingStore;
    }

    protected EmbeddingModel embeddingModel() {
        return embeddingModel;
    }

    @Test
    void should_be_accessible() {
        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest
          .builder()
          .queryEmbedding(embeddingModel().embed("test").content())
          .maxResults(1000)
          .build();

        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore().search(embeddingSearchRequest);
        assertThat(searchResult.matches()).hasSize(0);
    }

    @Test
    void should_not_be_accessible() {
        ChromaEmbeddingStore.Builder unauthenticatedEmbeddingStoreBuilder = ChromaEmbeddingStore
          .builder()
          .baseUrl(chroma.getEndpoint())
          .collectionName(randomUUID())
          .logRequests(true)
          .logResponses(true)
          .token("invalid_token");


        assertThatExceptionOfType(RuntimeException.class)
          .isThrownBy(() -> {
              unauthenticatedEmbeddingStoreBuilder.build();
          }).withMessageContaining("Forbidden");

    }
}
