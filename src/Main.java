import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        int numberOfThreads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        CompletableFuture<?>[] tasks = new CompletableFuture<?>[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            tasks[i] = createTask(executor);
        }

        CompletableFuture<Void> allTasks = CompletableFuture.allOf(tasks);
        CompletableFuture<Object> anyTask = CompletableFuture.anyOf(tasks);

        allTasks.join();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("All tasks completed.");
        try {
            System.out.println("First completed task result: " + anyTask.get());
        } catch (Exception e) {
            System.err.println("Error retrieving result from tasks: " + e.getMessage());
        }
    }

    private static CompletableFuture<Double> createTask(ExecutorService executor) {
        return CompletableFuture.supplyAsync(() -> {
            double result = computeHeavyTask();
            //System.out.println("Task completed with result: " + result);
            return result;
        }, executor);
    }

    private static double computeHeavyTask() {
        double sum = 0;
        for (int i = 0; i < 1_000_000; i++) {
            sum += Math.sin(ThreadLocalRandom.current().nextDouble()) * Math.cos(ThreadLocalRandom.current().nextDouble());
        }
        return sum;
    }
}