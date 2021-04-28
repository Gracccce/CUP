# CUPPlugin
- Our tool is built upon the Client/Server architecture.
## Installation
### The Server
- The server deploys a lightweight web application framework Flask
- Also a trained model is deployed on the server, more details can be seen in https://github.com/Tbabm/CUP
### The Client
1. Download `CUPPlugin.zip`;
2. Open Intellij IDEA, choose
      File -> Settings -> Plugins;
3. choose `Install Plugin from Disk`;
4. find the path of the zip and install it;
5. Restart Intellij IDEA to activate the plugin

## Usage
1. Select the name of the java method of which you want to update comment;
2. choose `update` -> `updateComment`;
3. click `fix` to update the comment if you think the generated comment is correct.

