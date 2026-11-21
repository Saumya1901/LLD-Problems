import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

class CrawlerWorker implements Runnable {
    private final BlockingQueue<String> queue;
    private final Set<String> visited;
    private final AtomicInteger outstanding;
    private final HtmlParser parser;

    CrawlerWorker(String seedUrl){
        outstanding = new AtomicInteger(1);//starting with 1
        visited = ConcurrentHashMap.newKeySet();
        queue = new LinkedBlockingDeque<>();
        queue.offer(seedUrl);
        visited.add(seedUrl);
    }
    @Override
    public void run() {



        while(true){
            String currUrl = null;
            try {
                currUrl = queue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            try {
                List<String> urls = getURls(currUrl);
                for(String url : urls){
                    if(visited.add(url)){
                        outstanding.incrementAndGet();
                        queue.offer(url);
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }finally {
                outstanding.decrementAndGet();

            }
            if(queue.size() == 0 && outstanding.get() == 0){
                executor.shutdownNow();
            }
        }
    }
}


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.printf("Hello and welcome!");

        for (int i = 1; i <= 5; i++) {
            //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
            // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
            System.out.println("i = " + i);
        }
    }
}