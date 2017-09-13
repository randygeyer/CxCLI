package com.checkmarx.v2.factories;

import com.checkmarx.v2.commands.*;

import java.util.ArrayList;
import java.util.List;

public class CommandsFactory {

    public List<Command> createInitializeCLICommands() {
        List<Command> commands = new ArrayList<>();
        commands.add(new GetConfigurationFileCommand());
        commands.add(new GetArgsCommand());
        commands.add(new LoginCommand());

        return commands;
    }


    public List<Command> createPreScanCommands() {
        List<Command> commands = new ArrayList<>();
        commands.add(new GetPresetsCommand());
        commands.add(new GetTeamsCommand());
        commands.add(new GetEngineConfigurationsCommand());

        return commands;
    }

    public List<Command> createSastScanCommand() {
        List<Command> commands = new ArrayList<>();
        commands.add(new SastScanCommand());

        return commands;
    }

    public List<Command> createOsaScanCommand() {
        List<Command> commands = new ArrayList<>();
        commands.add(new OsaScanCommand());

        return commands;
    }

}
