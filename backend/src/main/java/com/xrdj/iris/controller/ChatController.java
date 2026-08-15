package com.xrdj.iris.controller;

import com.xrdj.iris.ai.MetricsTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClient chatClient;
    private final MetricsTools metricsTools;
    private final ChatMemory chatMemory;

    public ChatController(ChatClient.Builder chatClientBuilder, MetricsTools metricsTools) {
        this.chatMemory = new InMemoryChatMemory();
        this.chatClient =
                chatClientBuilder
                        .defaultSystem(
                                "You are an expert banking data assistant for XRDJ-IRIS. You MUST"
                                    + " base your answers on the provided Context. Never guess or"
                                    + " invent numbers. A 'CRE' stands for 'Compte Rendu"
                                    + " d'Evénement'. Be concise, professional, and accurate. Write"
                                    + " your response as plain sentences. End every distinct point"
                                    + " with a period (.). Format details like: 'Received: 0 : CRE:"
                                    + " 0, EC: 0.' Always put a space between words and numbers. Do"
                                    + " not use lists, bullet points, asterisks (*), or plus (+)."
                                    + " If you do not have the data in the Context, state clearly"
                                    + " that you cannot access it.")
                        .defaultAdvisors(new MessageChatMemoryAdvisor(this.chatMemory))
                        .build();
        this.metricsTools = metricsTools;
    }

    private String buildPromptWithContext(String userPrompt) {
        String promptLower = userPrompt.toLowerCase();
        StringBuilder contexts = new StringBuilder();

        try {
            if (promptLower.contains("all time")
                    || promptLower.contains("all-time")
                    || promptLower.contains("tous les temps")
                    || promptLower.contains("global")
                    || promptLower.contains("total")) {
                contexts.append(metricsTools.getAllTimeMetricsSummary()).append("\n");
            }
            if (promptLower.contains("yesterday")
                    || promptLower.contains("hier")
                    || promptLower.contains("veille")) {
                contexts.append(
                                metricsTools.getMetricsSummaryForDate(
                                        java.time.LocalDate.now().minusDays(1)))
                        .append("\n");
            }
            if (promptLower.contains("today") || promptLower.contains("aujourd'hui")) {
                contexts.append(metricsTools.getMetricsSummaryForDate(java.time.LocalDate.now()))
                        .append("\n");
            }

            // Default to today if they ask about metrics but didn't specify when (and didn't
            // trigger any above)
            if (contexts.length() == 0
                    && (promptLower.contains("cre")
                            || promptLower.contains("transaction")
                            || promptLower.contains("ec")
                            || promptLower.contains("rejected")
                            || promptLower.contains("treated"))) {
                contexts.append(metricsTools.getMetricsSummaryForDate(java.time.LocalDate.now()))
                        .append("\n");
            }

            if (contexts.length() > 0) {
                return "Context:\n"
                        + contexts.toString()
                        + "\nUser Question: "
                        + userPrompt
                        + "\n\n"
                        + "INSTRUCTION: Write your response as plain sentences. End every single"
                        + " sentence with a period (.). Do not use lists, bullet points, asterisks"
                        + " (*), or plus (+). Format metrics like: 'Treated Correctly: 0 : CRE: 0,"
                        + " EC: 0.'";
            }
        } catch (Exception e) {
            return userPrompt;
        }

        return userPrompt;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        log.info("Received chat prompt: {}", request.prompt());
        String chatId = request.chatId() != null ? request.chatId() : "default";
        String responseContent =
                chatClient
                        .prompt()
                        .user(buildPromptWithContext(request.prompt()))
                        .advisors(
                                a ->
                                        a.param(
                                                AbstractChatMemoryAdvisor
                                                        .CHAT_MEMORY_CONVERSATION_ID_KEY,
                                                chatId))
                        .call()
                        .content();

        return new ChatResponse(responseContent);
    }

    @PostMapping(
            value = "/stream",
            produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public reactor.core.publisher.Flux<String> chatStream(@RequestBody ChatRequest request) {
        log.info("Received streaming chat prompt: {}", request.prompt());
        String chatId = request.chatId() != null ? request.chatId() : "default";
        return chatClient
                .prompt()
                .user(buildPromptWithContext(request.prompt()))
                .advisors(
                        a ->
                                a.param(
                                        AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY,
                                        chatId))
                .stream()
                .content();
    }

    public record ChatRequest(String prompt, String chatId) {}

    public record ChatResponse(String response) {}
}
