package com.ccojocea.aanews;

import com.ccojocea.aanews.data.NewsRepository;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test_test() {
        List<Integer> test = NewsRepository.test();
        int x = test.size();
    }

    @Test
    public void test_test2() {
        List<Integer> test = NewsRepository.test2();
        int x = test.size();
    }

    @Test
    public void test_test3() {
        List<List<Integer>> test = NewsRepository.test3();
        int x = test.size();
    }
}