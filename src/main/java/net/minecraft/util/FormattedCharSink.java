package net.minecraft.util;


import net.minecraft.text.Style;

@FunctionalInterface
public interface FormattedCharSink {
	boolean accept(int i, Style style, int j);
}
