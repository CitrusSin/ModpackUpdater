package io.github.citrussin.modupdater.cli;

public class ModUpdaterArguments {
    @Argument(value = "server", aliases = {"s"}, isOption = true)
    public boolean isServer = false;

    @Argument(value = "curseforgeConfiguration", aliases = {"cfConfig"}, isOption = true)
    public String curseforgeConfigManifestFilename = null;

    @Argument(value = "modrinthModpack", aliases = {"mrpack"}, isOption = true)
    public String modrinthPackFilename = null;
}
