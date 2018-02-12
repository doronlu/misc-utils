package my.katros.trials.benchmarks;

/**
 * Based on:
 * http://stackoverflow.com/questions/2086529/what-is-the-relative-performance-difference-of-if-else-versus-switch-statement-i
 */
public class SpeedTestSwitch
{
    private static void doSwitch(int loop)
    {
        long sum = 0;
        for (; loop > 0; --loop)
        {
            int r = generateInt(loop);
            switch (r)
            {
                case 0:
                    sum += 9;
                    break;
                case 1:
                    sum += 8;
                    break;
                case 2:
                    sum += 7;
                    break;
                case 3:
                    sum += 6;
                    break;
                case 4:
                    sum += 5;
                    break;
                case 5:
                    sum += 4;
                    break;
                case 6:
                    sum += 3;
                    break;
                case 7:
                    sum += 2;
                    break;
                case 8:
                    sum += 1;
                    break;
                case 9:
                    sum += 0;
                    break;
            }
        }
        System.out.println("sum=" + sum);
    }

    private static void doIfElse(int loop)
    {
        long sum = 0;
        for (; loop > 0; --loop)
        {
            int r = generateInt(loop);
            if (r == 0)
                sum += 9;
            else
                if (r == 1)
                    sum += 8;
                else
                    if (r == 2)
                        sum += 7;
                    else
                        if (r == 3)
                            sum += 6;
                        else
                            if (r == 4)
                                sum += 5;
                            else
                                if (r == 5)
                                    sum += 4;
                                else
                                    if (r == 6)
                                        sum += 3;
                                    else
                                        if (r == 7)
                                            sum += 2;
                                        else
                                            if (r == 8)
                                                sum += 1;
                                            else
                                                if (r == 9)
                                                    sum += 0;
        }
        System.out.println("sum=" + sum);
    }

//    private static int generateInt()
//    {
//    	return 9 - (int)(Math.cbrt(Math.random() * 10));
//    }

//    private static int generateInt(int loop)
//    {
//    	if ((loop & 0b11111) != 0b0)
//    		return 0;
//    	return 1;
//    }

    private static int generateInt(int loop)
    {
    	return (loop & 0x1F) != 0b0 ? 0 : loop % 10;
    }

    public static void main(String[] args)
    {
        long time;
        int loop = 100_000_000;
        System.out.println("warming up...");
        doSwitch(loop / 100);
        doIfElse(loop / 100);

        System.out.println("start");

        // run 2
        System.out.println("if/else:");
        time = System.currentTimeMillis();
        doIfElse(loop);
        System.out.println(" -> time needed: " + (System.currentTimeMillis() - time));

        // run 1
        System.out.println("switch:");
        time = System.currentTimeMillis();
        doSwitch(loop);
        System.out.println(" -> time needed: " + (System.currentTimeMillis() - time));
    }
}