package io.bmyjacks.app.chatroom;

import io.bmyjacks.app.chatroom.client.Client;
import io.bmyjacks.app.chatroom.server.Server;
import java.io.IOException;
import java.net.InetAddress;
import picocli.CommandLine;

/**
 * This is the main class for the chatroom application. It uses the picocli library to parse command
 * line arguments. The application can be run as a server or a client.
 */
@CommandLine.Command(name = "chatroom-1.0-all.jar", description = "A simple chatroom application", mixinStandardHelpOptions = true, version = {
    "Chatroom 1.0", "Picocli " + picocli.CommandLine.VERSION,
    "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
    "OS: ${os.name} ${os.version} ${os.arch}"})
public class Main {

  // Option to run the application as a server
  @CommandLine.Option(names = {"-s", "--server"}, description = "Run as server")
  static boolean isServer;
  // The port to use for the server or client
  @CommandLine.Option(names = "-p", defaultValue = "54736", description = "The port to use. Default:${DEFAULT-VALUE}")
  static int port;
  // Option to display help information
  @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help")
  boolean help;
  // Option to print version information
  @CommandLine.Option(names = {"-v",
      "--version"}, versionHelp = true, description = "Print version information")
  boolean versionRequested;

  /**
   * The main method for the application. It parses the command line arguments and runs the
   * application as a server or client based on the arguments.
   *
   * @param args the command line arguments
   * @throws IOException if there is an error running the server or client
   */
  public static void main(String... args) throws IOException {
    // Create a new CommandLine object for parsing the arguments
    CommandLine commandLine = new CommandLine(new Main());
    // Parse the arguments
    commandLine.parseArgs(args);

    // If the help option was specified, display the help information and return
    if (commandLine.isUsageHelpRequested()) {
      commandLine.usage(System.out);
      return;
    }

    // If the version option was specified, print the version information and return
    if (commandLine.isVersionHelpRequested()) {
      commandLine.printVersionHelp(System.out);
      return;
    }

    // Run the application as a server or client based on the arguments
    switch (isServer ? "server" : "client") {
      case "server":
        // Running as a server
        System.out.println("Running as a server on port " + port + "...");
        Server server = new Server(port);
        server.run();
        break;
      case "client":
        // Running as a client
        System.out.println("Running as a client on port " + port + "...");
        Client client = new Client(InetAddress.getByName("127.0.0.1"), port);
        client.run();
        break;
      default:
        // Invalid option
        System.out.println("Invalid option");
        break;
    }
  }
}