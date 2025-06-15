package dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.aditya.Presentation;
import dev.aditya.PresentationTools;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Main class for the Java MCP (Model Context Protocol) Server
 *
 * This server demonstrates how to build an MCP server in Java that can be used
 * by AI assistants like Claude to access presentation data through standardized tools.
 *
 * Key Features:
 * - Exposes presentation data through MCP tools
 * - Uses stdio transport for communication with clients
 * - Provides synchronous tool execution
 * - Supports logging capabilities
 *
 * Usage:
 * 1. Run this class to start the MCP server
 * 2. Use MCP Inspector to test: npx @modelcontextprotocol/inspector java -cp ... dev.Main
 * 3. Connect to Claude Desktop by adding to claude_desktop_config.json
 *
 * @author Your Name
 * @version 1.0
 */
public class Main {

    // Logger for server operations and debugging
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    // Business logic handler for presentation-related operations
    private static final PresentationTools presentationTools = new PresentationTools();

    /**
     * Main entry point for the MCP server application.
     *
     * This method:
     * 1. Sets up the transport provider (stdio)
     * 2. Configures server capabilities
     * 3. Registers available tools
     * 4. Starts the server
     *
     * @param args Command line arguments (not used in this implementation)
     */
    public static void main(String[] args) {

        // Step 1: Configure Transport Provider
        // ===================================
        // MCP supports different transport mechanisms. Here we use stdio (standard input/output)
        // which allows the server to communicate via command line pipes.
        // ObjectMapper is used for JSON serialization/deserialization.
        var transportProvider = new StdioServerTransportProvider(new ObjectMapper());

        // Step 2: Define Tool Specifications
        // ==================================
        // Get the specification for our custom tools. This defines what tools are available
        // and how they should be executed when called by the client.
        var syncToolSpecification = getSyncToolSpecification();

        // Step 3: Create and Configure the MCP Server
        // ===========================================
        // Build a synchronous MCP server with:
        // - Transport provider for communication
        // - Server metadata (name and version)
        // - Capabilities declaration
        // - Tool registrations
        McpSyncServer syncServer = McpServer.sync(transportProvider)
                // Server identification - shown to clients
                .serverInfo("java-mcp-server", "0.0.1")

                // Declare server capabilities - what features this server supports
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .tools(true)        // This server provides tools
                        .logging()          // This server supports logging
                        .build())

                // Register our tools - makes them available to clients
                .tools(syncToolSpecification)

                // Build the configured server
                .build();

        // Step 4: Start the Server
        // ========================
        // Log startup message and begin listening for client connections
        log.info("Starting Java MCP Server...");
        log.info("Server is ready to accept connections via stdio transport");
        log.info("Available tools: get_presentations");

        // Note: The server runs indefinitely, listening for MCP protocol messages
        // It will respond to:
        // - initialize requests
        // - tool call requests
        // - logging requests
        // - shutdown requests
    }

    /**
     * Creates and configures the tool specification for the "get_presentations" tool.
     *
     * This method demonstrates how to:
     * 1. Define a JSON schema for tool parameters
     * 2. Create tool metadata (name, description, schema)
     * 3. Implement the tool's execution logic
     * 4. Return properly formatted MCP responses
     *
     * @return McpServerFeatures.SyncToolSpecification configured tool specification
     */
    private static McpServerFeatures.SyncToolSpecification getSyncToolSpecification() {

        // Step 1: Define JSON Schema for Tool Parameters
        // ==============================================
        // This schema defines what parameters the tool accepts.
        // Currently accepts an "operation" string parameter (though not used in implementation).
        // You can expand this schema to accept more complex parameters.
        var schema = """
            {
              "type" : "object",
              "id" : "urn:jsonschema:Operation",
              "properties" : {
                "operation" : {
                  "type" : "string",
                  "description" : "Type of operation to perform (currently not used)"
                }
              },
              "additionalProperties" : false
            }
            """;

        // Step 2: Create Tool Specification
        // =================================
        // This combines the tool metadata with its implementation
        var syncToolSpecification = new McpServerFeatures.SyncToolSpecification(

                // Tool metadata - visible to clients
                new McpSchema.Tool(
                        "get_presentations",                           // Tool name (must be unique)
                        "Get a list of all presentations from Java",  // Human-readable description
                        schema                                         // Parameter schema defined above
                ),

                // Tool implementation - executed when tool is called
                (exchange, arguments) -> {

                    // Step 3: Implement Tool Logic
                    // ============================
                    // This is where your tool's actual work happens

                    try {
                        log.info("Executing get_presentations tool");
                        log.debug("Tool arguments received: {}", arguments);

                        // Call business logic to retrieve presentations
                        List<Presentation> presentations = presentationTools.getPresentations();
                        log.info("Retrieved {} presentations", presentations.size());

                        // Step 4: Format Response for MCP Protocol
                        // ========================================
                        // Convert business objects to MCP content format
                        List<McpSchema.Content> contents = new ArrayList<>();

                        // Add each presentation as text content
                        for (Presentation presentation : presentations) {
                            // Convert presentation to string representation
                            String presentationText = String.format(
                                    "Title: %s%nURL: %s%nYear: %d%n%n",
                                    presentation.title(),
                                    presentation.url(),
                                    presentation.year()
                            );
                            contents.add(new McpSchema.TextContent(presentationText));
                        }

                        // Return successful result
                        // - contents: the data to return to the client
                        // - isError: false indicates success
                        return new McpSchema.CallToolResult(contents, false);

                    } catch (Exception e) {
                        // Step 5: Handle Errors Gracefully
                        // ================================
                        log.error("Error executing get_presentations tool", e);

                        // Return error result
                        List<McpSchema.Content> errorContents = List.of(
                                new McpSchema.TextContent("Error retrieving presentations: " + e.getMessage())
                        );
                        return new McpSchema.CallToolResult(errorContents, true);
                    }
                }
        );

        return syncToolSpecification;
    }

    // Additional methods you might want to add:

    /**
     * Example of how to add more tools to your server.
     * Uncomment and modify this method, then add it to the server builder.
     */
    /*
    private static McpServerFeatures.SyncToolSpecification getAdditionalToolSpecification() {
        var schema = """
            {
              "type" : "object",
              "properties" : {
                "year" : {
                  "type" : "integer",
                  "description" : "Filter presentations by year"
                }
              }
            }
            """;

        return new McpServerFeatures.SyncToolSpecification(
            new McpSchema.Tool("get_presentations_by_year", "Get presentations filtered by year", schema),
            (exchange, arguments) -> {
                // Implementation for year-based filtering
                // You can access arguments like: arguments.get("year")
                return new McpSchema.CallToolResult(List.of(), false);
            }
        );
    }
    */
}