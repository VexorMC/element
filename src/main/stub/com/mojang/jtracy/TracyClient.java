package com.mojang.jtracy;

public class TracyClient {
    public static MemoryPool createMemoryPool(String poolName) { return new MemoryPool(); }
    public static Plot createPlot(String plotName) { return new Plot(); }
}
