    package org.openaudible.util;

    import org.junit.Test;

    import static org.junit.Assert.*;

    public class TimeToSecondsTest {

        @Test
        public void parseTimeStringToSeconds() {

            assertEquals(TimeToSeconds.parseTimeStringToSeconds("1:00"), 60);
            assertEquals(TimeToSeconds.parseTimeStringToSeconds("00:55"), 55);
            assertEquals(TimeToSeconds.parseTimeStringToSeconds("5:55"), 5 * 60 + 55);
            assertEquals(TimeToSeconds.parseTimeStringToSeconds(""), 0);
            assertEquals(TimeToSeconds.parseTimeStringToSeconds("6:01:05"), 6 * 3600 + 1*60 + 5);
        }

        @Test
        public void parseTime() {
            // make sure all these tests fail.
            String fails[] = {null, "", "abc", ":::", "A:B:C", "1:2:3:4", "1:99", "1:99:05", ":50", "-4:32", "-99:-2:4", "2.2:30"};
            for (String t: fails)
            {
                try {
                    long seconds = TimeToSeconds.parseTime(t);
                    assertFalse("FAIL: Expected failure:"+t+" got "+seconds, true);
                } catch (NumberFormatException nfe)
                {
                    assertNotNull(nfe);
                    assertTrue(nfe instanceof NumberFormatException);
                    // expected this nfe.
                }
            }
        }


    }