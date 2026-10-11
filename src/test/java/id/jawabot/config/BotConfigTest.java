package id.jawabot.config;

import id.jawa.client.JaWaClient;
import id.jawa.domain.model.AuthCreds;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BotConfigTest {

    @Test
    public void testOwnerDetection() {
        assertThat(BotConfig.isOwner("62895416602000@s.whatsapp.net")).isTrue();
        assertThat(BotConfig.isOwner("6285607589072@s.whatsapp.net")).isTrue();
        assertThat(BotConfig.isOwner("628123456789@s.whatsapp.net")).isFalse();
    }

    @Test
    public void testBotSelfDetection() {
        BotConfig.setBotNumber("628111222333");
        assertThat(BotConfig.getBotNumber()).isEqualTo("628111222333");

        assertThat(BotConfig.isBotSelf(null, "628111222333@s.whatsapp.net")).isTrue();
        assertThat(BotConfig.isBotSelf(null, "628999999999@s.whatsapp.net")).isFalse();

        JaWaClient client = mock(JaWaClient.class);
        AuthCreds creds = new AuthCreds();
        creds.meJid = "628555444333:0@s.whatsapp.net";
        creds.meLid = "9988776655@lid";
        when(client.creds()).thenReturn(creds);

        assertThat(BotConfig.isBotSelf(client, "628555444333@s.whatsapp.net")).isTrue();
        assertThat(BotConfig.isBotSelf(client, "628555444333:2@s.whatsapp.net")).isTrue();
        assertThat(BotConfig.isBotSelf(client, "9988776655@lid")).isTrue();
        assertThat(BotConfig.isBotSelf(client, "1122334455@lid")).isFalse();
    }

    @Test
    public void testOwnerOrSelfAuthorization() {
        JaWaClient client = mock(JaWaClient.class);
        AuthCreds creds = new AuthCreds();
        creds.meJid = "628111222333@s.whatsapp.net";
        when(client.creds()).thenReturn(creds);

        assertThat(BotConfig.isOwnerOrSelf(client, "62895416602000@s.whatsapp.net")).isTrue();
        assertThat(BotConfig.isOwnerOrSelf(client, "628111222333@s.whatsapp.net")).isTrue();
        assertThat(BotConfig.isOwnerOrSelf(client, "628123456789@s.whatsapp.net")).isFalse();
    }

    @Test
    public void testModeSwitching() {
        BotConfig.setPublicMode(true);
        assertThat(BotConfig.isPublicMode()).isTrue();

        BotConfig.setPublicMode(false);
        assertThat(BotConfig.isPublicMode()).isFalse();
    }
}
