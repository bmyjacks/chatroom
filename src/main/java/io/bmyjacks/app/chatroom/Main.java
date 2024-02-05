package io.bmyjacks.app.chatroom;

import io.bmyjacks.app.chatroom.client.Client;
import io.bmyjacks.app.chatroom.server.Server;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(name = "chatroom-1.0-all.jar", description = "A simple chatroom application", mixinStandardHelpOptions = true, version = {"Chatroom 1.0", "Picocli " + picocli.CommandLine.VERSION, "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})", "OS: ${os.name} ${os.version} ${os.arch}"})
public class Main {
    @CommandLine.Option(names = {"-s", "--server"}, description = "Run as server")
    static boolean isServer;
    @CommandLine.Option(names = "-p", defaultValue = "54736", description = "The port to use. Default:${DEFAULT-VALUE}")
    static int port;
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help")
    boolean help;
    @CommandLine.Option(names = {"-v", "--version"}, versionHelp = true, description = "Print version information")
    boolean versionRequested;

    public static void main(String... args) throws IOException {
        CommandLine commandLine = new CommandLine(new Main());
        commandLine.parseArgs(args);

        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            return;
        }

        if (commandLine.isVersionHelpRequested()) {
            commandLine.printVersionHelp(System.out);
            return;
        }

        switch (isServer ? "server" : "client") {
            case "server":
                System.out.println("Running as server on port " + port + "...");
                Server server = new Server(port);
                server.run();
                break;
            case "client":
                System.out.println("Running as client on port " + port + "...");
                Client client = new Client(port);
                client.run();
                break;
            default:
                System.out.println("Invalid option");
                break;
        }
    }
}