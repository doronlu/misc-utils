package my.katros.trials.benchmarks;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

public class GcStats
{
	public static String getGcStats(List<GarbageCollectorMXBean> gcBeansBefore)
	{
		long gcsCount = 0L;
		long gcsTime = 0L;
		List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();
		for (GarbageCollectorMXBean gc : gcs)
		{
			gcsCount += gc.getCollectionCount();
			gcsTime += gc.getCollectionTime();
		}
		StringBuilder sb = new StringBuilder(400);
		if (gcBeansBefore.size() != gcs.size() || !gcBeansBefore.containsAll(gcs))
		{
			sb.append("GarbageCollectorMXBeans after startup: " + gcBeansBefore + '\n');
			sb.append("GarbageCollectorMXBeans           now: " + gcs + '\n');
		}
		sb.append("Total gc count: " + gcsCount + "  Total gc time: " + gcsTime + "ms");
		return sb.toString();
	}
}