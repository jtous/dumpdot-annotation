package org.ow2.mind.adl.annotations;

import java.util.Comparator;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.ow2.mind.adl.ast.MindInterface;


public class MindInterfaceComparator  implements Comparator<MindInterface> {

		public int compare(MindInterface a, MindInterface b) {
			int result;
			
			//Compare roles
			result = compareRoles(a,b);
			if (result!=0) return result;
			
			//Compare signatures
			result = compareSignatures(a,b);
			if (result!=0) return result;
			
			//Compare names
			return compareNames(a,b);
		}
		
		private int compareRoles(MindInterface a, MindInterface b) {
			String aRole = a.getRole();
			String bRole = b.getRole();
			if (aRole == bRole) return 0; // == or .equals ???
			else if (aRole ==TypeInterface.SERVER_ROLE) return 1;
			else return -1;
		}
		
		private int compareSignatures(MindInterface a, MindInterface b) {
			String aSignature = a.getSignature();
			String bSignature = b.getSignature();
			return aSignature.compareTo(bSignature);
		}
	
		private int compareNames(MindInterface a, MindInterface b) {
			String aName = a.getName();
			String bName = b.getName();
			return aName.compareTo(bName);
		}
	}
	

