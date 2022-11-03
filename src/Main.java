import java.util.ArrayList;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        final long millis = System.currentTimeMillis();
        final int nWritings = 10;
        final int nWriters = 5;
        final int nReaders = 5;
        final String IS_EMPTY = "__";
        final String IS_WROTE = "[]";
        final String IS_RED = "<>";
        final int arraySize = 5;
        final ArrayList<SynchronousQueue<Integer>> synchronousQueueArrayList = new ArrayList<>();
        final AtomicInteger nReads = new AtomicInteger(nWriters * nWritings);
        final AtomicInteger nWrites = new AtomicInteger(nWriters * nWritings);

        for (int i = 0; i < arraySize; i++)
            synchronousQueueArrayList.add(new SynchronousQueue<>());

        ArrayList<Thread> threadsReaders = new ArrayList<>();
        for (int i = 0; i < nReaders; i++) {
            threadsReaders.add(new Thread(() -> {
                long nanoTime = System.nanoTime();
                int nReadings = 0;
                for (int readPointer = nReads.decrementAndGet(); readPointer >= 0; readPointer = nReads.decrementAndGet()) {
                    try {
                        int current = synchronousQueueArrayList.get(readPointer % arraySize).take();
                        nReadings++;
                        StringBuilder tmp = new StringBuilder();
                        tmp.append("\t\t\t").append(Thread.currentThread().getName())
                                .append("B").append(readPointer % arraySize)
                                .append("R").append(nReadings);
                        for (int k = 0; k < nWritings; k++) {
                            tmp.append(k > current ? IS_EMPTY : IS_RED);
                        }
                        System.out.println(tmp);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println("\n"+ Thread.currentThread().getName() + " read " + nReadings + " in " + ((System.nanoTime() - nanoTime)) + " nanoseconds.");
            }));
            threadsReaders.get(i).setName("Reader(" + i + ")");
            threadsReaders.get(i).start();
        }
        ArrayList<Thread> threadsWriters = new ArrayList<>();
        for (int i = 0; i < nWriters; i++) {
            threadsWriters.add(new Thread(() -> {
                long nanoTime = System.nanoTime();
                for (int j = 0; j < nWritings; j++)
                    try {
                        int writePointer = nWrites.decrementAndGet() % arraySize;
                        synchronousQueueArrayList.get(writePointer).put(j);
                        StringBuilder tmp = new StringBuilder();
                        tmp.append("\t").append(Thread.currentThread().getName())
                                .append("B").append(writePointer % arraySize)
                                .append("W");
                        for (int k = 0; k < nWritings; k++) {
                            tmp.append(k > j ? IS_EMPTY : IS_WROTE);
                        }
                        System.out.println(tmp);
                    } catch (InterruptedException e) {
                        //throw new RuntimeException(e);
                    }
                System.out.println("\n"+ Thread.currentThread().getName() + " write " + nWritings + " in " + ((System.nanoTime() - nanoTime)) + " nanoseconds.");
            }));
            threadsWriters.get(i).setName("Writer(" + i + ")");
            threadsWriters.get(i).start();
        }
        for (Thread thread : threadsWriters)
            thread.join();
        for (Thread thread: threadsReaders)
            thread.join();

        System.out.println("\n\nProgram continued " + ((System.currentTimeMillis() - millis)) + " milliseconds.");
    }
}