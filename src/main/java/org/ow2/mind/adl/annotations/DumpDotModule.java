package org.ow2.mind.adl.annotations;

import org.ow2.mind.inject.AbstractMindModule;

public class DumpDotModule extends AbstractMindModule {
	protected void configureImplementationLocator() {
//		bind(ImplementationLocator.class).annotatedWith(Names.named(DotWriter.DUMP_DOT)).to(BasicImplementationLocator.class);
	}
}
