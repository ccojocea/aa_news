package com.ccojocea.aanews.data;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TopicsTest {

    @Test
    public void normalize_trimsAndCollapsesWhitespace() {
        assertEquals("machine learning", Topics.normalize("  machine \t  learning "));
    }

    @Test
    public void normalize_removesQuotes() {
        assertEquals("climate change", Topics.normalize("\"climate change\""));
    }

    @Test
    public void normalize_rejectsEmptyAndTooLong() {
        assertNull(Topics.normalize(null));
        assertNull(Topics.normalize("   "));
        assertNull(Topics.normalize("\"\""));
        char[] tooLong = new char[Topics.MAX_TOPIC_LENGTH + 1];
        Arrays.fill(tooLong, 'a');
        assertNull(Topics.normalize(new String(tooLong)));
    }

    @Test
    public void add_appendsNormalizedTopic() {
        List<String> topics = Topics.add(Collections.singletonList("Android"), "  space ");
        assertEquals(Arrays.asList("Android", "space"), topics);
    }

    @Test
    public void add_ignoresDuplicatesIgnoringCase() {
        List<String> topics = Topics.add(Collections.singletonList("Android"), "android");
        assertEquals(Collections.singletonList("Android"), topics);
    }

    @Test
    public void add_ignoresInvalidTopic() {
        List<String> topics = Topics.add(Collections.singletonList("Android"), " ");
        assertEquals(Collections.singletonList("Android"), topics);
    }

    @Test
    public void add_respectsLimit() {
        List<String> topics = new ArrayList<>();
        for (int i = 0; i < Topics.MAX_TOPICS; i++) {
            topics.add("topic" + i);
        }
        assertEquals(Topics.MAX_TOPICS, Topics.add(topics, "one more").size());
    }

    @Test
    public void add_doesNotChangeInput() {
        List<String> input = new ArrayList<>(Collections.singletonList("Android"));
        Topics.add(input, "space");
        assertEquals(Collections.singletonList("Android"), input);
    }

    @Test
    public void contains_ignoresCase() {
        assertTrue(Topics.contains(Arrays.asList("Android", "Space"), "SPACE"));
        assertFalse(Topics.contains(Arrays.asList("Android", "Space"), "spaces"));
    }

    @Test
    public void buildQuery_joinsWithOrAndQuotesPhrases() {
        assertEquals("Android OR \"machine learning\" OR space",
                Topics.buildQuery(Arrays.asList("Android", "machine learning", "space")));
    }

    @Test
    public void buildQuery_singleTopic() {
        assertEquals("Android", Topics.buildQuery(Collections.singletonList("Android")));
    }

    @Test
    public void buildQuery_noTopics() {
        assertEquals("", Topics.buildQuery(Collections.emptyList()));
    }

}
