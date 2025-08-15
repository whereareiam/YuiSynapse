package me.whereareiam.yuisynapse.provider.openrouter;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yuisynapse.api.model.config.SynapseSettings;
import me.whereareiam.yuisynapse.api.type.ProviderType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class OpenRouterConfiguration {
    @Bean
    public WebClient openRouterWebClient(SynapseSettings settings) {
        var provider = settings.getProviders().get(ProviderType.OPENROUTER);
        log.info("Configuring OpenRouter WebClient: baseUrl={}", provider.getUrl());
        return WebClient.builder()
                .baseUrl(provider.getUrl())
                .defaultHeader("Authorization", "Bearer " + provider.getKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}


