/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dhenton9000.dependency;

/**
 *
 * @author dhenton
 */
/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;

/**
 * Base class for all components provides JAXB functionality
 *
 * @author dhenton
 *
 */
public class JaxbService {

    private JAXBContext jaxbContext;

    /**
     * Translate a JAXB annotated object to a String xml representation
     *
     * @param o object to translate
     * @return String with xml
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    public String jaxbToString(Object o) throws JAXBException, UnsupportedEncodingException {

        String info = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        getJaxbContext().createMarshaller().marshal(o, baos);
        info = baos.toString("UTF-8");
        return info;
    }

	public ProjectCollection jaxBStringToProject(String xml) throws JAXBException, UnsupportedEncodingException {

		StringReader sR = new StringReader(xml);
		ProjectCollection o = null;
		o = (ProjectCollection) getJaxbContext().createUnmarshaller().unmarshal(new StreamSource(sR));
		return o;

	}


	public String ProjectToString(ProjectCollection c) throws JAXBException, UnsupportedEncodingException {
		 
		 StringWriter ss = new StringWriter();
		 getJaxbContext().createMarshaller().marshal(c,ss);
		return ss.toString();

	}
    /**
     * @return the jaxbContext
     */
    public JAXBContext getJaxbContext() {
        if (jaxbContext == null) {

            try {
               jaxbContext = JAXBContext.newInstance(IvyDependency.class, 
                       DependencyCollection.class, 
                       ProjectCollection.class);
            } catch (JAXBException e) {
                throw new RuntimeException("Jaxb Instance error on new ", e);
            }
        }
       return jaxbContext;
    }

    /**
     * @param jaxbContext the jaxbContext to set
     */
    public void setJaxbContext(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }
}