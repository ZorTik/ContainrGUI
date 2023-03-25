package me.zort.containr.tests;

import lombok.extern.log4j.Log4j2;
import me.zort.containr.builder.PatternGUIBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@EnabledOnOs(value = {OS.LINUX, OS.WINDOWS})
@TestMethodOrder(MethodOrderer.MethodName.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestCase1 {

    @Test
    public void testMatcher() {
        System.out.println("Testing matcher...");
        String[] pattern = new String[]{
                "#########",
                "#PPPPPPP#",
                "#PPPPPPP#",
                "#########",
                "####C####",
        };
        List<PatternGUIBuilder.PatternContainerMatcher.SizeMatch> pMatch1 = new PatternGUIBuilder.PatternContainerMatcher(pattern, "P").match();
        assertEquals(1, pMatch1.size());
        assertEquals(10, pMatch1.get(0).getIndex());
        assertArrayEquals(new int[] {7, 2}, pMatch1.get(0).getSize());

        List<PatternGUIBuilder.PatternContainerMatcher.SizeMatch> pMatch2 = new PatternGUIBuilder.PatternContainerMatcher(pattern, "C").match();
        assertEquals(1, pMatch2.size());
        assertEquals(40, pMatch2.get(0).getIndex());
    }

    @Test
    public void testMatcher2() {
        String[] pattern = new String[]{
                "#########",
                "#PP###PP#",
                "#PP###PP#",
                "#########",
                "#########",
        };
        List<PatternGUIBuilder.PatternContainerMatcher.SizeMatch> pMatch = new PatternGUIBuilder.PatternContainerMatcher(pattern, "P").match();
        assertEquals(2, pMatch.size());
        assertEquals(10, pMatch.get(0).getIndex());
        assertArrayEquals(new int[] {2, 2}, pMatch.get(0).getSize());
        assertEquals(15, pMatch.get(1).getIndex());
        assertArrayEquals(new int[] {2, 2}, pMatch.get(1).getSize());
    }

}
