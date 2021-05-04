# coinbase-connector

This project has been made with the intention of providing a console based, ongoing view of any available order book
on the Coinbase Pro exchange.
---
## Usage

- Package the jar using maven. This can be achieved by navigating to the project root directory and running the command:
"mvn clean package".
- Navigate to your target folder, then into classes/scripts, there is a helper script to start the application, you can 
  use the following command: "./application_start.sh BTC-USD" (replacing BTC-USD with whichever) market you would like 
  to view, alternatively, the fat jar is just in the target folder.
---
## Design
- The application has three main goals in mind, minimize garbage creation, be fast, and, be lightweight (not using 1000 
  threads). To achieve this the application has been split into two main threads, one which processes the incoming 
  websocket messages, and one which does the printing to console (this is blocking, and while it probably wouldn't be an 
  issue in terms of performance, we can separate it like this and remove any worries).
- The control plane of the application first sets up the threads and objects that will be used, then it makes a query to 
  the Coinbase Pro REST API for some information on the market that we will be dealing with, which can help us out later
  (such as base/ quote increments, liveness of market etc).
- Once the data plane takes over, the CoinbaseWebSocketMessageHandler performs the processing of each incoming message, 
  deferring to an OrderBookAggregatorService which is responsible for maintaining the data structures backing the order 
  book, and offering utility methods for operations such as updating an order book using an incoming message.
- Every effort has been made not to create any unnecessary garbage throughout the hot path, which has resulted in some 
  unusual looking ways in which we pass data around between method calls and threads (namely using buffers rather than
  returning new objects).
- As a consequence of this, the objects we create (such as ConsolePrinter, SimpleOrderBookAggregatorService and others) 
  are intentionally not thread safe, if any more than one thread accesses them at a time, there will be erroneous
  and unpredictable results.
- After each message (other than error messages) has been processed, the application will start a blockingHandler (
  which runs on a different thread) which is responsible for picking up the updated top 10 bids and asks and printing them 
  to console. If a new message comes before the printer thread has signalled completion, we do not print the updated order 
  book and instead wait until we next receive a message to check if it is ready again.
- Currently there is only the SimpleOrderBookAggregatorService implementation for the order book maintenance, although
  with some more time it was planned to try out different data structures (something more custom and memory efficient,
  like an array which is ordered by price and just shifts the other objects around using system.arraycopy when needed).
---
## Potential Improvements
- We could use a lower level networking library, such as Netty, raw NIO or even something based around JNI or Unsafe to 
  lower latency and reduce garbage. Netty has an experimental event loop group implementation that uses io_uring which
  looks very interesting. Right now most of the garbage is coming from the incoming message buffers, and some from the
  Java std lib TreeMap implementation, we could fix these both given some time.
- We could handle errors gracefully, instead of closing down we could retry the connection a few times with a back-off 
  delay or something similar.
- Optimize snapshot message handling (aka not using jackson to parse the whole json) as we have with updates, depending 
  on how often we get errors from the Coinbase Pro WebSocket API (on error and reconnect we would expect to have to
  rebuild the order book).
- Switch the console implementation to something more efficient and potentially also prettier, like JLine or similar.
- We could add integration tests to test against a dummy rest and WebSocket server.
- Include a DI framework although it's not so bad with this small a project.
- Proper logging setup to a different output than the console, for help debugging and such if things go wrong
- More testing of different data structures to be used to back the order book, there are a number of options we could
  try, such as linear scanning primitive arrays and shifting elements for memory access efficiency, or at the very least
  creating a custom data structure TreeMap like the one we have that can cache Map.Entry objects that aren't being used,
  such that we never allocate new Entries just re-use old objects that we still have references to.