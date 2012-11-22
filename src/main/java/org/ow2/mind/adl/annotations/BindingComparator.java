package org.ow2.mind.adl.annotations;

import java.util.Comparator;
import org.ow2.mind.adl.ast.Binding;

public class BindingComparator implements Comparator<Binding> {

		public int compare(Binding a, Binding b) {
			int result;
			
			//Compare Servers
			result = compareServerComponents(a,b);
			if (result!=0) return result;
			result = compareServerInterfaces(a,b);
			if (result!=0) return result;
			
			//Compare Clients
			result = compareClientComponents(a,b);
			if (result!=0) return result;
			return compareClientInterfaces(a,b);
		}
		
		private int compareServerComponents(Binding a, Binding b) {
			return a.getToComponent().compareTo(b.getToComponent());
		}
		
		private int compareServerInterfaces(Binding a, Binding b) {
			return a.getToInterface().compareTo(b.getToInterface());
		}
		
		private int compareClientComponents(Binding a, Binding b) {
			return a.getFromComponent().compareTo(b.getFromComponent());
		}
		private int compareClientInterfaces(Binding a, Binding b) {
			return a.getFromInterface().compareTo(b. getFromInterface());
		}
	}
	

