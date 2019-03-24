import java.util.Calendar;
import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class taskScheduler
{
  //create a priority queue that sort task by scheduled time
  private static final int CAPACITY = 10;
  private final BlockingQueue<TimedTask> queue = new PriorityBlockingQueue<>(CAPACITY, new Comparator<TimedTask>() {
      @Override
      public int compare(TimedTask s, TimedTask t)
      {
        return s.getScheduledTime().compareTo(t.getScheduledTime());
      }
  });

  private final Object lock = new Object();
  private volatile boolean running = true;

  public void start() throws InterruptedException
  {
    while (running)
    {
      TimedTask task = queue.take();
      if (task != null)
      {
        task.run();
      }
      waitForNextTask();
    }
  }

  public void stop()
  {
    this.running = false;
  }

  private void waitForNextTask() throws InterruptedException
  {
    synchronized (lock)
    {
      TimedTask nextTask = queue.peek();
      while (nextTask == null || !nextTask.shouldRunNow())
      {
        if (nextTask == null)
        {
          lock.wait();
        }
        else
        {
          lock.wait(nextTask.runFromNow());
        }
        nextTask = queue.peek();
      }
    }
  }

  public void add(Task task)
  {
    add(task, 0);
  }

  public void add(Task task, long delay)
  {
    synchronized (lock)
    {
      queue.offer(TimedTask.fromTask(task, delay));
      lock.notify();
    }
  }

  public interface Task
  {
    void run();
  }

  private static class TimedTask
  {
    private Task task;
    private Calendar time;
    public TimedTask(Task task, Calendar time)
    {
      this.task = task;
      this.time = time;
    }

    public static TimedTask fromTask(Task task, long delay)
    {
      //set executed time of task
      Calendar now = Calendar.getInstance();
      now.setTimeInMillis(now.getTimeInMillis() + delay);
      return new TimedTask(task, now);
    }

    public Calendar getScheduledTime()
    {
      return time;
    }

    public long runFromNow()
    {
      return time.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
    }

    public boolean shouldRunNow()
    {
      return runFromNow() <= 0;
    }

    public void run()
    {
      task.run();
    }
  }

  public static void main(String[] argv) throws InterruptedException
  {
    class MyTask implements Task
    {
      private String name;
      private static final int MB = 1024 * 1024;
      public MyTask(String name)
      {
        this.name = name;
      }

      @Override
      public void run()
      {
        new Thread(new Runnable() {
          @Override
          public void run()
          {
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            System.out.println(name + " : Cumulative memory usage = " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MB + " MB, Used CPU time = " + threadBean.getCurrentThreadCpuTime() / 1000000.0 + " ms");
          }}).start();
      }
    }

    final taskScheduler scheduler =  new taskScheduler();
    scheduler.add(new MyTask("Task 1"));
    scheduler.add(new MyTask("Task 2"), 300);
    //run scheduler
    new Thread(new Runnable() {
      @Override
      public void run()
      {
        try
        {
          scheduler.start();
        }
        catch (InterruptedException e)
        {
        }
      }}).start();

    //create task by thread
    new Thread(new Runnable() {
      @Override
      public void run()
      {
        scheduler.add(new MyTask("Task 3"), 2000);
        scheduler.add(new MyTask("Task 4"), 500);
        scheduler.add(new MyTask("Task 5"), 100);
        scheduler.add(new MyTask("Task 6"), 1000);
        scheduler.add(new MyTask("Task 7"), 800);
      }}).start();
  }
}
