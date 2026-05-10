package s7project.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import s7project.model.AiInsightResponse;
import s7project.model.AiMessageRequest;
import s7project.model.AiRequest;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OllamaClientTests {

    @Test
    void mapsValidOllamaJsonResponseToAiInsightResponse() throws Exception {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        OllamaClient client = ollamaClient(restClientBuilder.build());

        server.expect(requestTo("http://ollama.test/api/generate"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "response": "{\\"title\\":\\"Recap\\",\\"subtitle\\":\\"Demo context\\",\\"content\\":\\"The team chose a local model path.\\",\\"bullets\\":[\\"Use Ollama\\",\\"Keep mock as default\\"],\\"footer\\":\\"Generated locally.\\"}"
                        }
                        """, MediaType.APPLICATION_JSON));

        AiInsightResponse response = client.generate(AiInsightType.SUMMARY, request());

        assertThat(response.title()).isEqualTo("[OLLAMA] Recap");
        assertThat(response.subtitle()).isEqualTo("Demo context");
        assertThat(response.content()).isEqualTo("The team chose a local model path.");
        assertThat(response.bullets()).containsExactly("Use Ollama", "Keep mock as default");
        assertThat(response.footer()).isEqualTo("Generated locally.");
        server.verify();
    }

    @Test
    void invalidOllamaJsonFailsClearly() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        OllamaClient client = ollamaClient(restClientBuilder.build());

        server.expect(requestTo("http://ollama.test/api/generate"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "response": "This is not JSON."
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.generate(AiInsightType.ACTION_POINTS, request()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ollama response did not match the expected AI insight JSON shape");
        server.verify();
    }

    private OllamaClient ollamaClient(RestClient restClient) {
        return new OllamaClient(
                restClient,
                new AiPromptBuilder(),
                new AiInsightResponseParser(new ObjectMapper()),
                "http://ollama.test",
                "llama3.1:8b"
        );
    }

    private AiRequest request() {
        return new AiRequest(
                "ai-design",
                "ai-design",
                List.of(new AiMessageRequest("Marco", "Can we use Ollama for local summaries?", "10:37 AM"))
        );
    }
}
