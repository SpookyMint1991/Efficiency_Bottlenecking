BankSimulation was intentionally buggy for educational purposed. The program is to simulate a shared bank account in which many individuals are to withdraw $50 at a time, as well as, the ATM-Kiosk that withdraws $20 at a time.

// Problems

1. The code uses multithreading and failed as the threads were  stomping on each other. The threads were getting the balance before it updated from being altered by different threads, so many, for example, were withdrawing $50 from the same $1000 balance. Its a shared bank account so they cannot all take the same amount from the same balance.

2. The code also was wasting CPU cycles with loops that iterated far too much for a function that wasn't even doing anything, e.g., fakeWork() method. The for-loop interates 100000 times. This is unnecessary. Also because of thread stomping, the code was outputting more than it needed, wasting CPU cycles and data.

// Solutions

1. Solutions for thread stomping is to add the synchronized keyword to the withdraw() method to prevent the threads from using this method at the same time, making it impossible for the threads to access the balance amount before it updated.

2. Solution for CPU cycle waste was to simply prevent thread stomping and do away with the fakeWork() method entirely. It is not needed in this code. And without thread stomping, the output is then reduced. But the code also sets an amount of times the threads must call the withdraw() method. The threads would reach an account balance within 23 method calls and the amount of times the code will output is 80 times. So 57 of the output streams would be exception handling saying that there is INSUFFICIENT FUNDS. This also wastes CPU cycles. So reducing the amount of times the threads called the withdraw() method would solve this problem.

// Levels of the Assignment

I completed the analysis of the code, highlighting the errors in writing and with screenshots, and suggesting solutions.

I implemented the solutions in the BankSimulationFixed.java file. Deleted fakeWork() method, added the synchronized keyword to withdraw() method, and reduced the number of times the threads interacted with the account balance to only 5 time for all individuals and the ATM-Kiosk.

I implemented AtomicInteger for modern multithreading management. By updating the bankAccount() method to create an AtomicInteger object that is set to the $1000 startingBalance and updating the withdraw() method to use the AtomicInteger methods addAndGet(-int) and .get() to allow the threads to interact with the account balance.
