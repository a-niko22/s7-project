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

@SpringBootTest
class AiServiceApplicationTests {
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void returnsMockSummary() throws Exception {
        mockMvc.perform(post("/api/ai/summary")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("[AI-SERVICE MOCK] Summary"))
                .andExpect(jsonPath("$.bullets[0]").exists())
                .andExpect(jsonPath("$.footer").exists());
    }

    @Test
    void returnsMockDecisions() throws Exception {
        mockMvc.perform(post("/api/ai/decisions")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("[AI-SERVICE MOCK] Decisions"))
                .andExpect(jsonPath("$.bullets[1]").exists());
    }

    private String requestBody() {
        return """
                {
                  "channelId": "ai-design",
                  "channelName": "ai-design",
                  "messages": [
                    {
                      "author": "Lena",
                      "text": "Let's keep the Thursday demo focused on one main channel screen.",
                      "time": "10:03 AM"
                    },
                    {
                      "author": "Marco",
                      "text": "Can we make sure the AI panel turns the discussion into a clear recap?",
                      "time": "10:37 AM"
                    }
                  ]
                }
                """;
    }

}
