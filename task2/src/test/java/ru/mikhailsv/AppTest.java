package ru.mikhailsv;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.assertj.core.api.Assertions;


public class AppTest {
    private final int nodesCount = 10;
    private final File testFile = new File("testLogPath");
    private final TokenRingConfig config = new TokenRingConfig((new Random()).nextInt(nodesCount),
            3, 2, nodesCount, testFile);

    @Test
    void TokenRingTest() throws IOException {
        assertDoesNotThrow(() -> App.runTokenRing(config));

        String content = Files.readString(Path.of(testFile.getAbsolutePath()));
        Pattern p = Pattern.compile("\\[Node-\\d+]: Finishing processing messages");
        Matcher m = p.matcher(content);

        Assertions.assertThat(m.results().count()).isEqualTo(nodesCount);
        assertTrue(testFile.delete());
    }
}
