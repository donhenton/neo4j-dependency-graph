/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dhenton9000.dependency;

import java.util.ArrayList;
import javax.xml.bind.annotation.*;

/**
 *
 * @author dhenton
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DependencyCollection", propOrder = {
    "projectName",
    "dependencies" 
})
@XmlRootElement(name = "DependencyCollection", namespace = "http://com.dhenton9000/xsd/ivy")
public class DependencyCollection {
    
    public DependencyCollection()
    {
        
    }
    
    public DependencyCollection(String projectName,ArrayList<IvyDependency> dependencies)
    {
        this.projectName = projectName;
        this.dependencies = dependencies;
    }
    
    @XmlElement(name = "projectname", namespace = "http://com.dhenton9000/xsd/ivy")
    private String projectName = null;
    @XmlElementWrapper(name = "dependencies")
    @XmlElement(name = "dependency", namespace = "http://com.dhenton9000/xsd/ivy")
    private ArrayList<IvyDependency> dependencies = new ArrayList<IvyDependency>();

    /**
     * @return the projectName
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * @param projectName the projectName to set
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * @return the dependencies
     */
    public ArrayList<IvyDependency> getDependencies() {
        return dependencies;
    }

    /**
     * @param dependencies the dependencies to set
     */
    public void setDependencies(ArrayList<IvyDependency> dependencies) {
        this.dependencies = dependencies;
    }
}
