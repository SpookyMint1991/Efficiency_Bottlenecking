// BankSimulation.java
//
// CSC --- Concurrency Bottleneck Diagnosis, Level 1
// -------------------------------------------------
// This program pretends to simulate multiple ATM withdrawals against a shared bank account.
// It is intentionally buggy for teaching purposes.
//
// Known (intentional) problems for students to find and explain:
// 1. Race condition on shared balance (multiple threads read/modify/write without sync)
// 2. Possible negative balance (business logic failure)
// 3. Inconsistent final balance between runs
// 4. Inefficient CPU usage (spin loop pretending to be "work")
// 5. Manual Thread management instead of a safer executor model
//
// Your job (student) is to:
//  - Run the program several times and observe different outputs.
//  - Explain WHY the balance is inconsistent.
//  - Propose at least two code-level fixes.

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BankSimulationFixed {

    static class BankAccount {
        private AtomicInteger balance;

        // Is no longer unprotected with the use of AtomicInteger
        public BankAccount(int startingBalance) {
            this.balance = new AtomicInteger(startingBalance);
        }

        // Multiple threads can NOT call this at the same time.
        //Iterates and updates balance as threads act on it, then prints the withdrawal and new balance.
        public synchronized void withdraw(int amount, String who) {
           
            // Check balance first
            if (balance.get() >= amount) {
               
                AtomicInteger oldBalance = balance;
                balance.addAndGet(-amount);
                AtomicInteger newBalance = balance;

                System.out.println(
                    who + " withdrew $" + amount +
                    " | old balance = " + oldBalance +
                    " -> new balance = " + newBalance
                );
            } else {
               
                System.out.println(
                    who + " tried to withdraw $" + amount +
                    " but INSUFFICIENT FUNDS. (current balance = " + balance + ")"
                );
            }
        }

        public int getBalance() {
            return balance.get();
        }

        //fakeWork() method was removed.
    }

    // Represents an ATM / user hitting the shared account.
    static class WithdrawTask implements Runnable {
        private final BankAccount account;
        private final String userName;
        private final int amountPerWithdrawal;
        private final int times;

        public WithdrawTask(BankAccount account,
                            String userName,
                            int amountPerWithdrawal,
                            int times) {
            this.account = account;
            this.userName = userName;
            this.amountPerWithdrawal = amountPerWithdrawal;
            this.times = times;
        }

        //As long as the threads are working as intented, the thread.sleep() doesn't really matter.

        @Override
        public void run() {
            for (int i = 0; i < times; i++) {

                account.withdraw(amountPerWithdrawal, userName);

                // "Random" pause logic that is actually NOT random and NOT robust.
                // Also: Thread.sleep() is swallowed without handling.
                try {
                    // Small sleep to reshuffle timing.
                    // This is not enough to FIX the bug. It just makes the output look chaotic.
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    // bad pattern: silently ignore
                }
            }
        }
    }

    public static void main(String[] args) {
        BankAccount shared = new BankAccount(1000); // start $1000

        // We’ll spin up several "people" hitting the same account.
        List<Thread> threads = new ArrayList<>();

        //updated times to prevent wasted CPU cycles and terminal cluttering.
        threads.add(new Thread(new WithdrawTask(shared, "Alice", 50, 5)));
        threads.add(new Thread(new WithdrawTask(shared, "Bob", 50, 5)));
        threads.add(new Thread(new WithdrawTask(shared, "Charlie", 50, 5)));
        threads.add(new Thread(new WithdrawTask(shared, "Diana", 50, 5)));

        // Bonus chaos thread that hammers faster, more times.
        threads.add(new Thread(new WithdrawTask(shared, "ATM-Kiosk", 20, 5)));

        System.out.println("=== Starting transactions with balance = $" + shared.getBalance() + " ===");

        long startTime = System.currentTimeMillis();

        // Start all threads
        for (Thread t : threads) {
            t.start();
        }

        // Wait for all threads to finish (join)
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                // again: swallowing interruption, not great practice
            }
        }

        long endTime = System.currentTimeMillis();

        System.out.println("\n=== All transactions finished. ===");
        System.out.println("Expected balance (theoretically) should never go below $0.");
        System.out.println("Actual FINAL balance reported by program = $" + shared.getBalance());
        System.out.println("Total runtime ms: " + (endTime - startTime));

        // NOTE TO STUDENTS:
        //  - Run this program several times.
        //  - You will often see different final balances.
        //  - Sometimes the balance will even go NEGATIVE or "skip" values.
        //
        // Explain WHY.
        //
        // In your report:
        //  1. What's the core concurrency bug called?
        //  2. Where does it actually happen in code?
        //  3. Under what timing conditions does it show up?
        //  4. Propose at least TWO fixes.
        //
        // Suggested directions for fixes:
        //  - Add 'synchronized' / locks around withdraw()
        //  - Use thread-safe classes / AtomicInteger
        //  - Use an ExecutorService instead of manually new Thread(...)
        //  - Remove / redesign fakeWork() to stop wasting CPU
        //
        // Also include your AI Reflection:
        //  - Did you ask an AI to help debug?
        //  - What did it get right/wrong?
        //  - Did you trust it blindly or verify?
    }
}
