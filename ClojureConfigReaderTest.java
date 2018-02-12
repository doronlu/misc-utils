package net.katros.services.clojure;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import net.katros.services.clojure.ClojureCanonizer;
import net.katros.services.clojure.ClojureConfigReader;
import net.katros.services.utils.InvalidConfigurationException;

/**
 * Tests for {@link ClojureConfigReader}
 * 
 * @author doron
 */
@RunWith(JUnit4.class)
public class ClojureConfigReaderTest
{
	String autoRecorderCljStr
= "	{"
+ "	  :version \\\"0.1.0\\\""
+ "	  :data-source \\\"IDC/recorder_v1\\\""
+ "	  :exchange \\\"748\\\""
+ "   :signal-category \\\"FUTURES\\\""
+ "	  :date \\\"20120903\\\""
+ "	  :signal-id \\\"FESXZ2_DE0009652388\\\""
+ "   :state-change-file \\\"/home/datashare/DATA/NORMALIZED/IDC/748/FUTURES/20120903/FESXZ2_DE0009652388/katros_PLUSTICK_FUTURES_748_20120903_FESXZ2_DE0009652388.cs\\\""
+ "   :signals-file \\\"/home/datashare/DATA/NORMALIZED/IDC/748/FUTURES/20120903/instruments.txt\\\""
+ "   :output-root \\\"/home/doron/safe/workspace/data/\\\""
+ "	  :segmentators"
+ "	  ["
+ "	    {"
+ "	      :name \\\"com.algotrade.strategies.recording.segmentators.TradeVolumeSegmentator\\\""
+ "	      :feature-tables"
+ "	      ["
+ "	        {"
+ "	          :type \\\"adapted\\\""
+ "	          :feature-groups"
+ "	          [ ; (list\n" // needs the '\n' so the Clojure comment doesn't include the rest of the String
+ "	            {"
+ "	              :name \\\"com.algotrade.strategies.recording.labellers.MomentLabeller\\\""
+ "	              :window-size-seq \\\"(1000,60000,10000)\\\""
+ "	              :num-of-ticks-threshold 3"
+ "	            }"
+ "	            {"
+ "	              :name \\\"com.algotrade.strategies.recording.labellers.MomentLabeller\\\""
+ "	              :window-type \\\"Volume\\\""
+ "	              :window-size-seq \\\"(10,100,20)\\\""
+ "	            }"
+ "	          ] ; )\n" // needs the '\n' so the Clojure comment doesn't include the rest of the String
+ "	        }"
+ "	      ]"
+ "	    }"
+ "	  ]"
+ "	}";

	String autoRecorderCljCanonicalStr
= "{"
+ 	":data-source \"IDC/recorder_v1\", "
+ 	":date \"20120903\", "
+ 	":exchange \"748\", "
+ 	":output-root \"/home/doron/safe/workspace/data/\", "
+ 	":segmentators "
+ 	"["
+ 		"{"
+ 			":feature-tables "
+ 			"["
+ 				"{"
+ 					":feature-groups "
+ 					"["
+ 						"{"
+ 							":name \"com.algotrade.strategies.recording.labellers.MomentLabeller\", "
+ 							":num-of-ticks-threshold 3, "
+ 							":window-size-seq \"(1000,60000,10000)\""
+ 						"} "
+ 						"{"
+ 							":name \"com.algotrade.strategies.recording.labellers.MomentLabeller\", "
+ 							":window-size-seq \"(10,100,20)\", "
+ 							":window-type \"Volume\""
+ 						"}"
+ 					"], "
+ 					":type \"adapted\""
+ 				"}"
+ 			"], "
+ 			":name \"com.algotrade.strategies.recording.segmentators.TradeVolumeSegmentator\""
+ 		"}"
+ 	"], "
+ 	":signal-category \"FUTURES\", "
+ 	":signal-id \"FESXZ2_DE0009652388\", "
+ 	":signals-file \"/home/datashare/DATA/NORMALIZED/IDC/748/FUTURES/20120903/instruments.txt\", "
+ 	":state-change-file \"/home/datashare/DATA/NORMALIZED/IDC/748/FUTURES/20120903/FESXZ2_DE0009652388/katros_PLUSTICK_FUTURES_748_20120903_FESXZ2_DE0009652388.cs\", "
+ 	":version \"0.1.0\""
+ "}";

	String clojureCanonicalTestStr
= " {"
+ "   :d 1"
+ "   :c"
+ "   ["
+ "     ["
+ "       [7 3 4]"
+ "       6"
+ "       {"
+ "         :aac"
+ "         {"
+ "           :aaac 2"
+ "           :caac 3"
+ "           :baac 4"
+ "         }"
+ "       }"
+ "     ]"
+ "     ["
+ "       [7 3 5 5]"
+ "       6"
+ "       {"
+ "         :aac"
+ "         {"
+ "           :aaac 2"
+ "           :caac 3"
+ "           :baac 8"
+ "         }"
+ "       }"
+ "     ]"
+ "     ["
+ "       [7 3 5 5]"
+ "       6"
+ "       {"
+ "         :aac"
+ "         {"
+ "           :aaac 2"
+ "           :caac 3"
+ "           :baac 6"
+ "         }"
+ "       }"
+ "     ]"
+ "   ]"
+ "   :a 4"
+ "   :b 8"
+ " }";

/* clojureCanonicalTestExpectedStr indented:
{
  :a 4,
  :b 8,
  :c
  [
    [
      6
      [3 4 7]
      {
        :aac
        {
          :aaac 2,
          :baac 4,
          :caac 3
        }
      }
    ]
    [
      6
      [3 5 5 7]
      {
        :aac
        {
          :aaac 2,
          :baac 6,
          :caac 3
        }
      }
    ]
    [
      6
      [3 5 5 7]
      {
        :aac
        {
          :aaac 2,
          :baac 8,
          :caac 3
        }
      }
    ]
  ],
  :d 1
}
 */
	String clojureCanonicalTestExpectedStr
= "{:a 4, :b 8, :c [[6 [3 4 7] {:aac {:aaac 2, :baac 4, :caac 3}}] [6 [3 5 5 7] {:aac {:aaac 2, :baac 6, :caac 3}}] [6 [3 5 5 7] {:aac {:aaac 2, :baac 8, :caac 3}}]], :d 1}";

// Currently not is use
	String expected
= "Date=\"20121130\","
+ "Exchange=\"748\","
+ "Instrument=\"FESXZ2_DE0009652388\","
+ "InstrumentsFile=\"/home/datashare/DATA/NORMALIZED/IDC/748/FUTURES/20120903/instruments.txt\","
+ "OutputRoot=\"/home/doron/safe/workspace/data/\","
+ "Segmentators="
+ "{"
+	"{"
+		"FeatureAndIntervalTables="
+		"{"
+			"{"
+				"StateLabellers="
+				"{"
+					"{"
+						"Name=\"com.algotrade.strategies.recording.labellers.MomentLabeller\","
+						"NumOfTicksThreshold=3,"
+						"WindowSizeSeq=\"(1000,60000,10000)\""
+					"}"
+					"{"
+						"Name=\"com.algotrade.strategies.recording.labellers.MomentLabeller\","
+						"WindowSizeSeq=\"(10,100,20)\","
+						"WindowType=\"Volume\""
+					"}"
+				"},"
+				"Type=\"FeatureTable\""
+			"}"
+		"},"
+		"Name=\"com.algotrade.strategies.recording.segmentators.TradeVolumeSegmentator\""
+	"}"
+ "},"
+ "StateChangeFile=\"/home/datashare/DATA/NORMALIZED/IDC/748/FUTURES/20120903/FESXZ2_DE0009652388/katros_PLUSTICK_FUTURES_748_20120903_FESXZ2_DE0009652388.cs\","
+ "Version=\"0.1.0\"";

// Currently not is use
	String complexClojureStrWithDup // Feature tables A and B are the same
= "	{"
+ "	  \\\"Version\\\" \\\"0.1.0\\\""
+ "	  \\\"Exchange\\\" \\\"748\\\""
+ "	  \\\"Instrument\\\" \\\"FESXZ2_DE0009652388\\\""
+ "	  \\\"Date\\\" \\\"20121130\\\""
+ "	  \\\"Segmentators\\\""
+ "	  ["
+ "	    {"
+ "	      \\\"Name\\\" \\\"com.algotrade.strategies.recording.segmentators.TradeVolumeSegmentator\\\""
+ "	      \\\"FeatureAndIntervalTables\\\""
+ "	      ["
+ "	        {"
+ "	          \\\"Type\\\" \\\"FeatureTable\\\""
+ "	          \\\"StateLabellers\\\""
+ "	          [ ; (list\n" // needs the '\n' so the Clojure comment doesn't include the rest of the String
+ "	            {"
+ "	              \\\"Name\\\" \\\"com.algotrade.strategies.recording.labellers.MomentLabeller\\\""
+ "	              \\\"WindowSizeSeq\\\" \\\"(1000,60000,10000)\\\""
+ "	              \\\"NumOfTicksThreshold\\\" 3"
+ "	            }"
+ "	            {"
+ "	              \\\"Name\\\" \\\"com.algotrade.strategies.recording.labellers.MomentLabeller\\\""
+ "	              \\\"WindowType\\\" \\\"Volume\\\""
+ "	              \\\"WindowSizeSeq\\\" \\\"(10,100,20)\\\""
+ "	            }"
+ "	          ] ; )\n" // needs the '\n' so the Clojure comment doesn't include the rest of the String
+ "	        }"
+ "	        {"
+ "	          \\\"Type\\\" \\\"FeatureTable\\\"" // FeatureTable A
+ "	          \\\"StateLabellers\\\""
+ "	          ["
+ "	            {"
+ "	              \\\"Name\\\" \\\"com.algotrade.strategies.recording.labellers.MomentLabeller\\\""
+ "	              \\\"WindowSizeSeq\\\" \\\"(1000,60000,10000)\\\""
+ "	              \\\"NumOfTicksThreshold\\\" 3"
+ "	            }"
+ "	          ]"
+ "	        }"
+ "	      ]"
+ "	    }"
+ "	    {"
+ "	      \\\"Name\\\" \\\"com.algotrade.strategies.recording.segmentators.TradeVolumeSegmentator\\\""
+ "	      \\\"FeatureAndIntervalTables\\\""
+ "	      ["
+ "	        {"
+ "	          \\\"Type\\\" \\\"FeatureTable\\\"" // FeatureTable B
+ "	          \\\"StateLabellers\\\""
+ "	          ["
+ "	            {"
+ "	              \\\"Name\\\" \\\"com.algotrade.strategies.recording.labellers.MomentLabeller\\\""
+ "	              \\\"WindowSizeSeq\\\" \\\"(1000,60000,10000)\\\""
+ "	              \\\"NumOfTicksThreshold\\\" 3"
+ "	            }"
+ "	          ]"
+ "	        }"
+ "	      ]"
+ "	    }"
+ "	  ]"
+ "	}";

//	@Test
//	public void test() throws InvalidConfigurationException
//	{
//		TradeEngineConf actual = ClojureConfigReader.getTradeEngineConfFromString(autoRecorderCljStr);
//		String rawStr = actual.getRawConfig(); // if and when supporting generic format and not only Clojure use actual.toString
//		System.out.println("RawStr: " + rawStr);
//	}

	@Test
	public void testCanonize() throws InvalidConfigurationException
	{
		testCanonize(clojureCanonicalTestStr, clojureCanonicalTestExpectedStr);
		testCanonize(autoRecorderCljStr, autoRecorderCljCanonicalStr);
	}

	public void testCanonize(String clojureStr, String expected) throws InvalidConfigurationException
	{
		Object clojureObject = ClojureConfigReader.deserializeClojureObjectFromString(clojureStr);
//		String original = clojureObject.toString();
//		System.out.println(" Original: " + original);
		@SuppressWarnings("unchecked")
		Map<Keyword, Object> tradeEngineMap = (Map<Keyword, Object>)clojureObject;
		IPersistentMap canonicalMap = ClojureCanonizer.canonize(tradeEngineMap, true);
		String actual = canonicalMap.toString();
//		System.out.println("Canonical: " + actual);
		assertEquals(expected, actual);
	}

//	Can also test reading from a file:
//	TradeEngineConf tradeEngineConf = readTradeEngineConfFromFile("/home/doron/safe/workspace/testarea/clojure/AutoRecorder.conf");
}