# How to Build MCP Server in Java

A comprehensive guide to building a Model Context Protocol (MCP) server using Java. This project demonstrates how to create a Java-based MCP server that provides presentation data through tools that can be consumed by AI assistants like Claude.

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Running the Server](#running-the-server)
- [Testing with MCP Inspector](#testing-with-mcp-inspector)
- [Connecting with Claude Desktop](#connecting-with-claude-desktop)
- [Understanding the Code](#understanding-the-code)
- [Available Tools](#available-tools)
- [Customization](#customization)
- [Troubleshooting](#troubleshooting)

## Overview

This project implements a Java-based MCP (Model Context Protocol) server that exposes presentation data through a standardized interface. The server uses the `io.modelcontextprotocol.sdk` library to handle MCP communication and provides tools that AI assistants can call to retrieve information about Java presentations.

### What is MCP?

The Model Context Protocol (MCP) is an open standard that enables AI assistants to securely connect to external data sources and tools. It provides a standardized way for applications to expose their functionality to AI systems.

## Prerequisites

- **Java 21 or higher** - This project uses Java 21 features
- **Maven 3.6+** - For dependency management and building
- **Node.js and npm** - Required for MCP Inspector testing (optional)
- **Claude Desktop** - For connecting the server to Claude (optional)

## Project Structure

```
how-to-build-mcp-server-in-java/
├── src/main/java/
│   └── dev/
│       ├── Main.java                    # Main server entry point
│       └── aditya/
│           ├── Presentation.java        # Data model for presentations
│           └── PresentationTools.java   # Business logic for managing presentations
├── pom.xml                             # Maven configuration
├── README.md                           # This documentation
└── .gitignore                         # Git ignore rules
```

## Getting Started

### 1. Clone the Repository

```bash
git clone <your-repository-url>
cd how-to-build-mcp-server-in-java
```

### 2. Build the Project

```bash
mvn clean compile
```

### 3. Verify Dependencies

The project uses the following key dependencies:
- `io.modelcontextprotocol.sdk:mcp` - MCP SDK for Java
- `org.slf4j:slf4j-api` and `org.slf4j:slf4j-simple` - Logging

## Running the Server

### Method 1: Using Maven

```bash
mvn exec:java -Dexec.mainClass="dev.Main"
```

### Method 2: Using Java directly

```bash
# First compile
mvn compile

# Then run
java -cp target/classes:target/dependency/* dev.Main
```

### Method 3: Create executable JAR

```bash
mvn package
java -jar target/how-to-build-mcp-server-in-java-1.0-SNAPSHOT.jar
```

## Testing with MCP Inspector

The MCP Inspector is a debugging tool that allows you to test your MCP server interactively.

### 1. Install MCP Inspector

```bash
npm install -g @modelcontextprotocol/inspector
```

### 2. Run the Inspector

```bash
npx @modelcontextprotocol/inspector java -cp target/classes:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q) dev.Main
```

Or if you have a compiled JAR:

```bash
npx @modelcontextprotocol/inspector java -jar target/how-to-build-mcp-server-in-java-1.0-SNAPSHOT.jar
```

### 3. Using the Inspector

1. The inspector will open in your browser
2. You'll see the server capabilities and available tools
3. You can test the `get_presentations` tool by clicking on it
4. The inspector will show the JSON response from your server

## Connecting with Claude Desktop

To use this MCP server with Claude Desktop, you need to configure it in Claude's settings.

### 1. Locate Claude Desktop Configuration

**On macOS:**
```bash
~/Library/Application Support/Claude/claude_desktop_config.json
```

**On Windows:**
```bash
%APPDATA%\Claude\claude_desktop_config.json
```

### 2. Add Server Configuration

Add your Java MCP server to the configuration file:

```json
{
  "mcpServers": {
    "java-presentations": {
      "command": "java",
      "args": [
        "-cp",
        "/path/to/your/project/target/classes:/path/to/your/project/target/dependency/*",
        "dev.Main"
      ],
      "cwd": "/path/to/your/project"
    }
  }
}
```

**Alternative with JAR file:**

```json
{
  "mcpServers": {
    "java-presentations": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/your/project/target/how-to-build-mcp-server-in-java-1.0-SNAPSHOT.jar"
      ]
    }
  }
}
```

### 3. Restart Claude Desktop

After saving the configuration, restart Claude Desktop. You should now be able to ask Claude about presentations, and it will use your Java MCP server to fetch the data.

### 4. Test the Connection

Try asking Claude:
- "What presentations are available?"
- "Show me all Java presentations from 2025"
- "List the presentation URLs"

## Understanding the Code

### Main.java

The main class sets up the MCP server with:
- **Transport Layer**: Uses stdio for communication
- **Server Info**: Defines server name and version
- **Capabilities**: Declares what the server can do (tools, logging)
- **Tool Registration**: Registers available tools with their implementations

### Presentation.java

A simple record class representing a presentation with:
- `title`: The presentation title
- `url`: Link to the presentation
- `year`: Year the presentation was given

### PresentationTools.java

Contains the business logic for managing presentations:
- Initializes sample presentation data
- Provides methods to retrieve presentations
- Supports filtering by year
- Can convert presentations to different formats

## Available Tools

### get_presentations

**Description**: Retrieves a list of all available Java presentations

**Parameters**:
- `operation` (string): Operation type (currently not used in implementation)

**Returns**: List of presentations with title, URL, and year information

**Example Response**:
```
Presentation[title=Java 24 Launch - Live from JavaOne 2025, url=https://www.youtube.com/watch?v=mk_2MIWxLI0, year=2025]
Presentation[title=Java Turns 30 - Live from JavaOne 2025, url=https://www.youtube.com/watch?v=GwR7Gvi80Xo, year=2025]
...
```

## Customization

### Adding New Tools

To add a new tool to your MCP server:

1. **Define the tool schema** in JSON format
2. **Create the tool specification** with name, description, and schema
3. **Implement the tool logic** in the handler function
4. **Register the tool** with the server builder

Example:
```java
var newToolSpec = new McpServerFeatures.SyncToolSpecification(
    new McpSchema.Tool("my_new_tool", "Description of what it does", schema),
    (exchange, arguments) -> {
        // Your tool implementation here
        return new McpSchema.CallToolResult(contents, false);
    }
);
```

### Modifying Data

To change the presentation data:
1. Edit the constructor in `PresentationTools.java`
2. Add, remove, or modify the `Presentation` objects
3. Rebuild and restart the server

### Adding Resources or Prompts

The MCP SDK also supports resources and prompts. You can add these using the server builder:

```java
.resources(resourceSpecification)
.prompts(promptSpecification)
```

## Troubleshooting

### Common Issues

1. **"Class not found" errors**: Ensure all dependencies are in the classpath
2. **Port already in use**: The server uses stdio, so port conflicts shouldn't occur
3. **JSON parsing errors**: Check that your tool schemas are valid JSON
4. **Claude Desktop not connecting**: Verify the configuration file path and syntax

### Debug Logging

The project uses SLF4J for logging. To see debug output, you can adjust the logging level or add more log statements.

### Dependency Issues

If you encounter dependency resolution problems:

```bash
mvn dependency:tree
mvn clean install -U
```

## Next Steps

- Add more sophisticated tools with complex parameters
- Implement resource endpoints for serving files or data
- Add prompt templates for common AI interactions
- Create tools that interact with external APIs or databases
- Add authentication and security features

## Contributing

Feel free to contribute to this project by:
- Adding new example tools
- Improving documentation
- Fixing bugs or issues
- Adding tests

## Resources

- [MCP Official Documentation](https://modelcontextprotocol.io/)
- [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk)
- [Claude Desktop](https://claude.ai/desktop)
- [MCP Inspector](https://github.com/modelcontextprotocol/inspector)