package s7project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ChatServiceApplicationTests {
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
    void returnsSeededChannels() throws Exception {
        mockMvc.perform(get("/api/channels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("general"))
                .andExpect(jsonPath("$[2].id").value("ai-design"));
    }

    @Test
    void createsMessageForChannel() throws Exception {
        mockMvc.perform(post("/api/channels/ai-design/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "Demo smoke test message"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author").value("You"))
                .andExpect(jsonPath("$.text").value("Demo smoke test message"));
    }

    @Test
    void returnsAiInsight() throws Exception {
        mockMvc.perform(post("/api/channels/ai-design/ai/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Summary"))
                .andExpect(jsonPath("$.bullets[0]").exists());
    }

}
