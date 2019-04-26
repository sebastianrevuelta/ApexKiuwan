package mcp.kiuwan.rules.apex;

import org.apache.log4j.Logger;

import com.als.core.AbstractRule;
import com.als.core.RuleContext;
import com.als.core.ast.BaseNode;

/**
 * @author rmepp
 * Regla que no haca nada para tener una class que no da errores en el log para las "reglas" CUS.JAVA.PMD.*
 **/
public class ReglaVacia extends AbstractRule { 
	private final static Logger logger = Logger.getLogger(ReglaVacia.class);

	public void initialize (RuleContext ctx) { 
		// Vacio
	}

	protected void visit (BaseNode root, final RuleContext ctx) { 
		// Vacio
	}

	public void postProcess (RuleContext ctx) { 
		// Vacio
	}
}



