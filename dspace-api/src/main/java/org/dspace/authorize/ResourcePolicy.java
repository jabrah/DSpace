/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authorize;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.apache.commons.lang3.StringUtils;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.core.HibernateProxyHelper;
import org.dspace.core.ReloadableEntity;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.hibernate.Length;

/**
 * Database entity representation of the ResourcePolicy table
 *
 * @author kevinvandevelde at atmire.com
 */
@Entity
@Table(name = "resourcepolicy")
public class ResourcePolicy implements ReloadableEntity<Integer> {
    /** This policy was set on submission, to give the submitter access. */
    public static String TYPE_SUBMISSION = "TYPE_SUBMISSION";

    /** This policy was set to allow access by a workflow group. */
    public static String TYPE_WORKFLOW = "TYPE_WORKFLOW";

    /** This policy was explicitly set on this object. */
    public static String TYPE_CUSTOM = "TYPE_CUSTOM";

    /** This policy was copied from the containing object's default policies. */
    public static String TYPE_INHERITED = "TYPE_INHERITED";

    @Id
    @Column(name = "policy_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "resourcepolicy_seq")
    @SequenceGenerator(name = "resourcepolicy_seq", sequenceName = "resourcepolicy_seq", allocationSize = 1)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "dspace_object")
    private DSpaceObject dSpaceObject;

    /*
     * {@see org.dspace.core.Constants#Constants Constants}
     */
    @Column(name = "resource_type_id")
    private int resourceTypeId;

    /*
     * {@see org.dspace.core.Constants#Constants Constants}
     */
    @Column(name = "action_id")
    private int actionId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eperson_id")
    private EPerson eperson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "epersongroup_id")
    private Group epersonGroup;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "rpname", length = 30)
    private String rpname;


    @Column(name = "rptype", length = 30)
    private String rptype;

    @Column(name = "rpdescription", length = Length.LONG32)
    private String rpdescription;

    /**
     * Protected constructor, create object using:
     * {@link org.dspace.authorize.service.ResourcePolicyService#create(Context)}
     */
    protected ResourcePolicy() {

    }

    /**
     * Return true if this object equals obj, false otherwise.
     *
     * @param obj object to compare (eperson, group, start date, end date, ...)
     * @return true if ResourcePolicy objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        Class<?> objClass = HibernateProxyHelper.getClassWithoutInitializingProxy(obj);
        if (getClass() != objClass) {
            return false;
        }
        final ResourcePolicy other = (ResourcePolicy) obj;
        if (!StringUtils.equals(getRpName(), other.getRpName())) {
            return false;
        }
        if (getAction() != other.getAction()) {
            return false;
        }
        if (!Objects.equals(getEPerson(), other.getEPerson())) {
            return false;
        }
        if (!Objects.equals(getGroup(), other.getGroup())) {
            return false;
        }
        if (!Objects.equals(getStartDate(), other.getStartDate())) {
            return false;
        }
        if (!Objects.equals(getEndDate(), other.getEndDate())) {
            return false;
        }
        return true;
    }

    /**
     * Return a hash code for this object.
     *
     * @return int hash of object
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + this.getAction();
        if (this.getGroup() != null) {
            hash = 19 * hash + this.getGroup().hashCode();
        } else {
            hash = 19 * hash + -1;
        }

        if (this.getEPerson() != null) {
            hash = 19 * hash + this.getEPerson().hashCode();
        } else {
            hash = 19 * hash + -1;
        }

        hash = 19 * hash + (this.getStartDate() != null ? this.getStartDate().hashCode() : 0);
        hash = 19 * hash + (this.getEndDate() != null ? this.getEndDate().hashCode() : 0);
        return hash;
    }

    /**
     * Get the ResourcePolicy's internal identifier
     *
     * @return the internal identifier
     */
    @Override
    public Integer getID() {
        return id;
    }

    public DSpaceObject getdSpaceObject() {
        return dSpaceObject;
    }

    public void setdSpaceObject(DSpaceObject dSpaceObject) {
        this.dSpaceObject = dSpaceObject;
        this.resourceTypeId = dSpaceObject.getType();
    }

    /**
     * set the action this policy authorizes
     *
     * @param myid action ID from {@link org.dspace.core.Constants Constants}
     */
    public void setAction(int myid) {
        this.actionId = myid;
    }

    /**
     * @return get the action this policy authorizes
     */
    public int getAction() {
        return actionId;
    }

    /**
     * @return eperson, null if EPerson not set
     */
    public EPerson getEPerson() {
        return eperson;
    }

    /**
     * assign an EPerson to this policy
     *
     * @param eperson Eperson
     */
    public void setEPerson(EPerson eperson) {
        this.eperson = eperson;
    }

    /**
     * gets the Group referred to by this policy
     *
     * @return group, or null if no group set
     */
    public Group getGroup() {
        return epersonGroup;
    }

    /**
     * sets the Group referred to by this policy
     *
     * @param epersonGroup Group
     */
    public void setGroup(Group epersonGroup) {
        this.epersonGroup = epersonGroup;
    }

    /**
     * Get the start date of the policy
     *
     * @return start date, or null if there is no start date set (probably most
     * common case)
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Set the start date for the policy
     *
     * @param d date, or null for no start date
     */
    public void setStartDate(LocalDate d) {
        startDate = d;
    }

    /**
     * Get end date for the policy
     *
     * @return end date or null for no end date
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Set end date for the policy
     *
     * @param d end date, or null
     */
    public void setEndDate(LocalDate d) {
        this.endDate = d;
    }

    public String getRpName() {
        return rpname;
    }

    public void setRpName(String name) {
        this.rpname = name;
    }

    public String getRpType() {
        return rptype;
    }

    public void setRpType(String type) {
        this.rptype = type;
    }

    public String getRpDescription() {
        return rpdescription;
    }

    public void setRpDescription(String description) {
        this.rpdescription = description;
    }

    /**
     * Describe the ResourcePolicy in String form. Useful for debugging ResourcePolicy issues in tests or similar.
     * @return String representation of ResourcePolicy object
     */
    @Override
    public String toString() {
        return "ResourcePolicy{" +
            "id='" + id + '\'' +
            ", action_id='" + actionId + '\'' +
            ", eperson='" + eperson + '\'' +
            ", group='" + epersonGroup + '\'' +
            ", type='" + rptype + '\'' +
            ", name='" + rpname + '\'' +
            ", description='" + rpdescription + '\'' +
            ", start_date='" + startDate + '\'' +
            ", end_date='" + endDate + '\'' +
            '}';
    }
}
