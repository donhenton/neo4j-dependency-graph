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
@XmlType(name = "ProjectCollection", propOrder = {
    "projectDependencies" 
})
@XmlRootElement(name = "ProjectCollection", namespace = "http://com.dhenton9000/xsd/ivy")
public class ProjectCollection {
    
    @XmlElementWrapper(name = "projects")
    @XmlElement(name = "project", namespace = "http://com.dhenton9000/xsd/ivy")
    private ArrayList<DependencyCollection> projectDependencies =  
            new ArrayList<DependencyCollection>();

    /**
     * @return the projectDependencies
     */
    public ArrayList<DependencyCollection> getProjectDependencies() {
        return projectDependencies;
    }

    /**
     * @param projectDependencies the projectDependencies to set
     */
    public void setProjectDependencies(ArrayList<DependencyCollection> projectDependencies) {
        this.projectDependencies = projectDependencies;
    }
    
    
    
}
