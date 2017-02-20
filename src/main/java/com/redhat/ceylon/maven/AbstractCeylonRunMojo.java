package com.redhat.ceylon.maven;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathFactory;

public abstract class AbstractCeylonRunMojo extends AbstractCeylonMojo {
	  private static final String SAX_DRIVER = "org.xml.sax.driver";

	  private ClassLoader contextClassLoader;
	  private String documentBuilderFactory;
	  private String saxParserFactory;
	  private String transformerFactory;
	  private String xPathFactory;
	  private String xmlEventFactory;
	  private String xmlInputFactory;
	  private String xmlOutputFactory;
	  private String datatypeFactory;
	  private String schemaFactory;
	  private String saxDriver;

	  protected void saveBeforeJBossModules(){
	      documentBuilderFactory = System.getProperty(DocumentBuilderFactory.class.getName());
	      saxParserFactory = System.getProperty(SAXParserFactory.class.getName());
	      transformerFactory = System.getProperty(TransformerFactory.class.getName());
	      xPathFactory = System.getProperty(XPathFactory.class.getName());
	      xmlEventFactory = System.getProperty(XMLEventFactory.class.getName());
	      xmlInputFactory = System.getProperty(XMLInputFactory.class.getName());
	      xmlOutputFactory = System.getProperty(XMLOutputFactory.class.getName());
	      datatypeFactory = System.getProperty(DatatypeFactory.class.getName());
	      schemaFactory = System.getProperty(SchemaFactory.class.getName());
	      saxDriver = System.getProperty(SAX_DRIVER);
	      contextClassLoader = Thread.currentThread().getContextClassLoader();
	  }

	  protected void restoreAfterJBossModules(){
	      restoreProperty(DocumentBuilderFactory.class.getName(),documentBuilderFactory);
	      restoreProperty(SAXParserFactory.class.getName(),saxParserFactory);
	      restoreProperty(TransformerFactory.class.getName(),transformerFactory);
	      restoreProperty(XPathFactory.class.getName(),xPathFactory);
	      restoreProperty(XMLEventFactory.class.getName(),xmlEventFactory);
	      restoreProperty(XMLInputFactory.class.getName(),xmlInputFactory);
	      restoreProperty(XMLOutputFactory.class.getName(),xmlOutputFactory);
	      restoreProperty(DatatypeFactory.class.getName(),datatypeFactory);
	      restoreProperty(SchemaFactory.class.getName(),schemaFactory);
	      restoreProperty(SAX_DRIVER,saxDriver);
	      Thread.currentThread().setContextClassLoader(contextClassLoader);
	  }

	  private void restoreProperty(String key, String value) {
		  if(value == null)
			  System.clearProperty(key);
		  else
			  System.setProperty(key, value);
	  }

}
