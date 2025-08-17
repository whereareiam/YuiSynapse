package me.whereareiam.yuisynapse.api.tool;

/**
 * SPI for constructing tools by name. Implementations are discovered via ServiceLoader.
 */
public interface ToolFactory {
	String name();

	Tool create();
}


