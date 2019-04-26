package mcp.kiuwan.rules.apex;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.als.core.AbstractRule;
import com.als.core.Rule;
import com.als.core.RuleContext;
import com.als.core.RuleViolation;
import com.als.core.ast.BaseNode;
import com.als.core.ast.NodeVisitor;
import com.als.core.ast.TreeNode;
import com.als.core.io.IOUtils;
import com.optimyth.qaking.xml.XmlNode;
import com.optimyth.qaking.xml.ast.XmlDocument;
import com.optimyth.qaking.xml.parser.XmlParser;

/**
 * @author rmepp
 * 
 * This rule loads Apex PMD report and generates Kiuwan violations for each PMD violation.
 * It also looks up the source code text of the beginline of the violation. 
 * For this the source code needs to be there where is says in apex.xml, 
 * tag <file name=...
 * The Kiuwan user needs read access to this location.
 * NOTE: This is not necessarily the same location as the Kiuwan project basedir, but it can be. For example:
 *    A project source code file is at: "C:\projects\clients\telefonica\apex-mdapi-master\apex-mdapi\src\classes\MetadataDataController.cls">
 *    The Kiuwan project basedir is at: "D:\Kiuwan\ApexReports"  --> under this directory is the apex.xml file.
 * If a source code file is not found, a default text is used in the Kiuwan reports.
 * 
 * IMPORTANT:
 * To avoid a lot of errors in the Kiuwan logs, if the apex.xml file to be processed is in the same Kiuwan project basedir as other project files, 
 * these extensions or languages should be disabled in the Kuiwan analisis:
 *    - Abap:           *.component
 *    - Visual Basic 6: *.cls
 */
public class ApexRule extends AbstractRule { 
	private final static Logger logger = Logger.getLogger(ApexRule.class);

	private String MANUALPMD_RULESET = "CUS.JAVA.MANUALPMD.";
	private HashMap <String, Rule> HmRules= new HashMap <String, Rule>();
	Rule rule = this;

	private static final String PMD_REPORT_NAME_DEFAULT = "apex.xml";
	private String PMD_REPORT_NAME;
	
	public void initialize (RuleContext ctx) { 
		super.initialize(ctx);
		PMD_REPORT_NAME = getProperty("PMDreportName", PMD_REPORT_NAME_DEFAULT);
		File baseDir = ctx.getBaseDirs().get(0);
		logger.debug("initialize: " +  this.getName() + " : " + baseDir);
		
		// Read list of Kiuwan rules into memory as not to access them for every violation
		int startPos = MANUALPMD_RULESET.length();
		Iterator<Rule> i = ctx.getRules().getRules();
		while (i.hasNext()) {
			Rule rule = (Rule) i.next();
			String rName = rule.getName().substring(startPos);
			HmRules.put(rName, rule);
			logger.debug("added rule: " + rName);
		}
	}

	protected void visit (BaseNode root, final RuleContext ctx) { 
		// this method is run once for each source file under analysis.
		// this method is left in blank intentionally.
	}

	public void postProcess (RuleContext ctx) { 
		// this method is run once for analysis
		super.postProcess(ctx); 
		logger.error("postProcess: " +  this.getName());

		// basedir.
		File baseDir = ctx.getBaseDirs().get(0);

		// iterates over reports files.
		try {
			Files.walk(Paths.get(baseDir.getAbsolutePath()))
			.filter(Files::isRegularFile)
			.filter(p -> p.getFileName().toString().equals(PMD_REPORT_NAME))
			.forEach(p -> {
				try {
					processReportFileSax(ctx, p);
				} catch (ParserConfigurationException | SAXException | IOException e) {
					logger.error("Error parsing file " + p.getFileName() + ". ", e);
				}
			});
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	private void processReportFileSax(RuleContext ctx, Path p) throws ParserConfigurationException, SAXException, IOException {
		logger.debug("processing: " +  p);

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(false);
		SAXParser parser = factory.newSAXParser();

		ApexReportHandler handler = new ApexReportHandler(ctx, p.toFile());
		parser.parse(p.toFile(), handler);
	}

	/**
	 * The cobertura xml report handler
	 * @author rmepp
	 */
	class ApexReportHandler extends DefaultHandler {
		private RuleContext ctx;
		private File file;
		private Locator locator = null;

		private boolean inFoundFile = false;
		private boolean inMethod = false;
		private String fileName = "";
		private String ruleName = "";
		private int beginLine = 0;

		public ApexReportHandler(RuleContext ctx, File file) {
			super();
			this.ctx = ctx;
			this.file = file;

			this.setDocumentLocator(locator);
		}

		public void setDocumentLocator(Locator locator) {
			this.locator = locator;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (qName.equalsIgnoreCase("file")) {					// Only process <class> where sourcefilename is found
				fileName = attributes.getValue("name");
				logger.debug("CoberturaReportHandler.startElement(file" + ", "+ fileName + ")");
				inFoundFile = true;
			} else if (inFoundFile && qName.equalsIgnoreCase("violation")) {
				String sBeginLine  = attributes.getValue("beginline");
				beginLine = Integer.valueOf(sBeginLine);
				ruleName = attributes.getValue("rule");
				//logger.debug("CoberturaReportHandler.startElement(violation" + ", " + beginLine + ", " + endLine + ")");
				inMethod = true;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (qName.equalsIgnoreCase("file")) {
				inFoundFile = false;
				logger.debug("ApexReportHandler.endElement(file)");
			} 
		}

		public void characters(char[] ch, int start, int length) throws SAXException {
			if (inMethod) {
				String defect = new String(ch, start, length).trim();	 
				inMethod  = false;
				logger.debug("ApexReportHandler.characters, " + ruleName + ", " + fileName + ", " + beginLine + ", " + defect);
				// Does the rule exist in the Kiuwan model? 
				if (HmRules.containsKey(ruleName)) {
					Rule rule = HmRules.get(ruleName);
					File file = new File(fileName);
					ctx.setSourceCodeFilename(file.toPath().toFile());
					logger.debug("ApexReportHandler.characters, Rule used: " + rule.getName());
					// Look up text of beginline of violation. Source code needs to be there where is says in apex.xml, tag <file name=...
					// If source code file is not found, a default text is used in Kiuwan.
					String[] lines = null;
					try {
						 lines = IOUtils.lines(file);
					} catch (IOException e) {
						logger.warn("ApexReportHandler: Cannot open source file: " + fileName);
					}
					
					RuleViolation rv = new RuleViolation(rule, beginLine, file);
					if (lines != null) {
						rv.setCodeViolated(lines[beginLine - 1]);
					}
					ctx.getReport().addRuleViolation(rv);
				}
			}
		}
	}	
}



