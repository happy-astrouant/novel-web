package io.github.xzy.novel;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;


@SpringBootTest
class AuthorAiControllerTest {


    @MockBean
    private ChatClient chatClient; // Mock Spring AI 的 ChatClient

    @Test
    void shouldExpandTextSuccessfully() {
        // Given
        String text = "这是一段简介";
        Double ratio = 200.0; // 扩展为 2 倍
        String expectedPrompt = "请将以下文本扩写为原长度的2.0倍：这是一段简介";
        String mockAiContent = "这是一段简介。我们可以进一步展开，描述它的背景、意义和未来发展，使内容更加丰富和完整。";

        // ✅ Mock ChatClient DSL 行为
        // chatClient.prompt().user(...).call() → 返回 mock 内容


    }


}
