package s7project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "AI_PROVIDER=gemini")
class AiServiceGeminiFallbackTests {
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void fallsBackToMockWhenGeminiIsNotConfigured() throws Exception {
        mockMvc.perform(post("/api/ai/action-points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "channelId": "backend",
                                  "channelName": "backend",
                                  "messages": [
                                    {
                                      "author": "Chris",
                                      "text": "Let's keep everything in memory so the demo stays reliable.",
                                      "time": "1:05 PM"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("[AI-SERVICE MOCK] Action Points"))
                .andExpect(jsonPath("$.bullets[0]").exists());
    }
}
