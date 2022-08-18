package io.github.citrussin.modupdater.server;

import com.google.gson.annotations.Expose;

public class ServerConfig {
    @Expose
    public int port = 14238;
    @Expose
    public int maxThreadCount = 10;
    @Expose
    public String commonModsFolder = "./mods";
    @Expose
    public String optionalModsFolder = "./clientMods";
    @Expose
    public String redirectionListPath = "./modupdater_redirection_list.json";
}
